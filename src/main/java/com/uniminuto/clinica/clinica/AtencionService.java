package com.uniminuto.clinica.clinica;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.PdfDocumento;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.facturacion.Liquidador;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Requerimiento 3.2 y 3.3: atención de la cita médica y generación de fórmulas.
 *
 * <ul>
 *   <li>Solo el veterinario asignado a la cita puede abrir, registrar y finalizar la atención.</li>
 *   <li>La atención usa control de versión optimista: si otra sesión la modificó, se responde 409.</li>
 *   <li>Al finalizar se congela la tarifa vigente de cada servicio, la cita pasa a "atendida" y el resumen
 *   queda en la historia clínica de la mascota.</li>
 *   <li>La fórmula se crea como BORRADOR y el médico la confirma. Confirmarla NO descuenta inventario:
 *   queda PENDIENTE_DESPACHO y solo la dispensación confirmada genera salidas.</li>
 * </ul>
 */
@Service
public class AtencionService {

    private static final NumberFormat COP = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    private final Db db;
    private final TarifarioService tarifario;

    public AtencionService(Db db, TarifarioService tarifario) {
        this.db = db;
        this.tarifario = tarifario;
    }

    // ------------------------------------------------------------------ Atención

    public Map<String, Object> consultar(long citaId) {
        Map<String, Object> cita = cita(citaId);
        verificarLectura(cita);
        Map<String, Object> atencion = atencionDeCita(citaId);
        LocalDate fecha = ((LocalDateTime) cita.get("fecha_hora")).toLocalDate();

        Map<String, Object> r = new LinkedHashMap<>();
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("citaId", citaId);
        resumen.put("medico", cita.get("medico"));
        resumen.put("mascota", cita.get("mascota"));
        resumen.put("mascotaId", cita.get("mascota_id"));
        resumen.put("especie", cita.get("especie"));
        resumen.put("raza", cita.get("raza"));
        resumen.put("edad", cita.get("edad"));
        resumen.put("propietario", cita.get("propietario"));
        resumen.put("telefono", cita.get("telefono"));
        resumen.put("estado", cita.get("estado"));
        resumen.put("inicio", cita.get("fecha_hora"));
        resumen.put("motivoCita", cita.get("motivo"));
        r.put("cita", resumen);

        if (atencion == null) {
            r.put("atencion", null);
            r.put("servicios", List.of());
        } else {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("id", atencion.get("id"));
            a.put("historiaId", atencion.get("historia_id"));
            a.put("especialidad", atencion.get("especialidad"));
            a.put("finalizada", atencion.get("fecha_finalizacion"));
            a.put("version", atencion.get("version"));
            a.put("motivo", atencion.get("motivo"));
            a.put("sintomas", atencion.get("sintomas"));
            a.put("diagnostico", atencion.get("diagnostico"));
            a.put("observaciones", atencion.get("observaciones"));
            a.put("recomendaciones", atencion.get("recomendaciones"));
            r.put("atencion", a);
            r.put("servicios", db.lista("""
                    SELECT s.servicio_id AS servicioId, sc.nombre, s.cantidad, s.valor_unitario AS valor,
                           s.iva_porcentaje AS iva, s.impuesto_porcentaje AS impuesto
                    FROM atencion_servicio s JOIN servicio_clinico sc ON sc.id = s.servicio_id
                    WHERE s.atencion_id = :a ORDER BY s.id
                    """, Db.p("a", atencion.get("id"))));
        }
        r.put("antecedentes", db.lista("""
                SELECT an.fecha, CONCAT(md.nombres, ' ', md.apellidos) AS medico, an.descripcion
                FROM anotacion_historia an
                JOIN historia_medica h ON h.id = an.historia_id
                JOIN medico md ON md.id = an.medico_id
                WHERE h.paciente_id = :m AND (an.cita_id IS NULL OR an.cita_id <> :c)
                ORDER BY an.fecha DESC LIMIT 30
                """, Db.p("m", cita.get("mascota_id"), "c", citaId)));
        long especialidad = atencion == null ? Validar.largo(cita, "especializacion_id")
                : Validar.largo(atencion, "especializacion_id");
        List<Map<String, Object>> catalogo = new ArrayList<>();
        for (Map<String, Object> s : db.lista("""
                SELECT id, especializacion_id AS especialidadId, nombre FROM servicio_clinico
                WHERE especializacion_id = :e AND activo = 1 ORDER BY nombre
                """, Db.p("e", especialidad))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("servicio", s);
            item.put("tarifa", tarifario.vigente(Validar.largo(s, "id"), fecha));
            catalogo.add(item);
        }
        r.put("catalogo", catalogo);
        r.put("formulas", formulasDeCita(citaId));
        return r;
    }

    @Transactional
    public Map<String, Object> abrir(long citaId) {
        Map<String, Object> cita = bloquearCita(citaId);
        verificarMedico(cita);
        String estado = Validar.cadena(cita, "estado");
        if (!Set.of("pendiente", "confirmada", "atendida").contains(estado)) {
            throw new ConflictException("No se puede atender una cita " + estado + ".");
        }
        LocalDateTime inicio = (LocalDateTime) cita.get("fecha_hora");
        if (inicio.toLocalDate().isAfter(Tiempo.hoy())) {
            throw new ConflictException("La atención solo puede abrirse a partir del día de la cita (" + inicio.toLocalDate() + ").");
        }
        if (atencionDeCita(citaId) == null) {
            // La historia clínica corresponde a la consulta (cita): se reutiliza si ya existe.
            Long historia = db.valor("SELECT id FROM historia_medica WHERE cita_id = :c",
                    Db.p("c", citaId), Long.class);
            if (historia == null) {
                historia = db.insertar("""
                        INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, activo, fecha_creacion)
                        VALUES (:m, :c, :medico, :fecha, :motivo, 1, :ahora)
                        """, Db.p("m", cita.get("mascota_id"), "c", citaId, "medico", cita.get("medico_id"),
                        "fecha", cita.get("fecha_hora"), "motivo", cita.get("motivo"), "ahora", Tiempo.ahora()));
            }
            db.insertar("""
                    INSERT INTO atencion_medica (cita_id, historia_id, medico_id, especializacion_id, motivo, version,
                                                 fecha_apertura)
                    VALUES (:c, :h, :m, :e, :motivo, 0, :ahora)
                    """, Db.p("c", citaId, "h", historia, "m", cita.get("medico_id"), "e", cita.get("especializacion_id"),
                    "motivo", cita.get("motivo"), "ahora", Tiempo.ahora()));
        }
        return consultar(citaId);
    }

    @Transactional
    public Map<String, Object> guardar(long citaId, AtencionApi.AtencionRq d) {
        Map<String, Object> cita = bloquearCita(citaId);
        verificarMedico(cita);
        Map<String, Object> atencion = bloquearAtencion(citaId);
        if (d == null) {
            throw new BadRequestException("Los datos de la atención son obligatorios.");
        }
        verificarVersion(atencion, d.version());
        LocalDate fecha = ((LocalDateTime) cita.get("fecha_hora")).toLocalDate();
        List<Object[]> servicios = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (AtencionApi.ServicioRq s : d.servicios() == null ? List.<AtencionApi.ServicioRq>of() : d.servicios()) {
            Validar.requerido(s.servicioId(), "servicio");
            int cantidad = Validar.entero(s.cantidad(), "cantidad del servicio", 1);
            if (cantidad > 99) {
                throw new BadRequestException("La cantidad máxima por servicio es 99.");
            }
            if (!vistos.add(s.servicioId())) {
                throw new BadRequestException("Un servicio no puede registrarse dos veces en la misma atención.");
            }
            Map<String, Object> sc = db.requerido("SELECT especializacion_id, activo, nombre FROM servicio_clinico WHERE id = :id",
                    Db.p("id", s.servicioId()), "El servicio no existe.");
            if (Validar.largo(sc, "especializacion_id") != Validar.largo(atencion, "especializacion_id")
                    || !Validar.bool(sc, "activo")) {
                throw new BadRequestException("El servicio " + sc.get("nombre") + " no está disponible para la especialidad.");
            }
            Map<String, Object> tarifa = tarifario.vigente(s.servicioId(), fecha);
            if (tarifa == null) {
                throw new ConflictException("El servicio " + sc.get("nombre") + " no tiene tarifa vigente.");
            }
            servicios.add(new Object[]{s.servicioId(), tarifa, cantidad});
        }
        db.ejecutar("""
                UPDATE atencion_medica SET motivo = :motivo, sintomas = :sintomas, diagnostico = :diagnostico,
                       observaciones = :obs, recomendaciones = :rec, version = version + 1
                WHERE id = :id
                """, Db.p("motivo", Validar.opcional(d.motivo(), "motivo", 4000),
                "sintomas", Validar.opcional(d.sintomas(), "síntomas", 4000),
                "diagnostico", Validar.opcional(d.diagnostico(), "diagnóstico", 4000),
                "obs", Validar.opcional(d.observaciones(), "observaciones", 4000),
                "rec", Validar.opcional(d.recomendaciones(), "recomendaciones", 4000), "id", atencion.get("id")));
        db.ejecutar("DELETE FROM atencion_servicio WHERE atencion_id = :a", Db.p("a", atencion.get("id")));
        for (Object[] s : servicios) {
            insertarServicio(Validar.largo(atencion, "id"), (Long) s[0], (Map<String, Object>) s[1], (int) s[2]);
        }
        return consultar(citaId);
    }

    @Transactional
    public Map<String, Object> finalizar(long citaId, Integer version) {
        Map<String, Object> cita = bloquearCita(citaId);
        verificarMedico(cita);
        Map<String, Object> atencion = bloquearAtencion(citaId);
        verificarVersion(atencion, version);
        if (Validar.cadena(atencion, "motivo") == null || Validar.cadena(atencion, "diagnostico") == null) {
            throw new BadRequestException("Registra el motivo de consulta y el diagnóstico antes de finalizar.");
        }
        long atencionId = Validar.largo(atencion, "id");
        List<Map<String, Object>> servicios = db.lista("SELECT servicio_id, cantidad FROM atencion_servicio WHERE atencion_id = :a",
                Db.p("a", atencionId));
        if (servicios.isEmpty()) {
            throw new BadRequestException("Registra al menos un servicio o consulta prestada antes de finalizar.");
        }
        // Congela la tarifa vigente en la fecha de la cita (valores históricos para la factura).
        LocalDate fecha = ((LocalDateTime) cita.get("fecha_hora")).toLocalDate();
        db.ejecutar("DELETE FROM atencion_servicio WHERE atencion_id = :a", Db.p("a", atencionId));
        for (Map<String, Object> s : servicios) {
            Map<String, Object> tarifa = tarifario.vigente(Validar.largo(s, "servicio_id"), fecha);
            if (tarifa == null) {
                throw new ConflictException("Un servicio de la atención ya no tiene tarifa vigente.");
            }
            insertarServicio(atencionId, Validar.largo(s, "servicio_id"), tarifa, Validar.numero(s, "cantidad"));
        }
        LocalDateTime ahora = Tiempo.ahora();
        db.ejecutar("UPDATE atencion_medica SET fecha_finalizacion = :f, version = version + 1 WHERE id = :id",
                Db.p("f", ahora, "id", atencionId));
        if (!"atendida".equals(cita.get("estado"))) {
            db.ejecutar("UPDATE cita SET estado = 'atendida', fecha_modificacion = :f WHERE id = :id",
                    Db.p("f", ahora, "id", citaId));
            db.insertar("""
                    INSERT INTO cita_revision (cita_id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior,
                                               estado_nuevo, inicio_nuevo, fin_nuevo)
                    VALUES (:c, :u, :f, :ea, :i, :fin, 'atendida', :i, :fin)
                    """, Db.p("c", citaId, "u", SesionActual.usuarioId(), "f", ahora, "ea", cita.get("estado"),
                    "i", cita.get("fecha_hora"), "fin", cita.get("fecha_fin")));
        }
        String resumen = "Atención de la cita #" + citaId + ". Motivo: " + atencion.get("motivo")
                + ". Síntomas: " + valor(atencion.get("sintomas")) + ". Diagnóstico: " + atencion.get("diagnostico")
                + ". Observaciones: " + valor(atencion.get("observaciones"))
                + ". Recomendaciones: " + valor(atencion.get("recomendaciones")) + ".";
        db.ejecutar("""
                UPDATE historia_medica SET motivo = :motivo, diagnostico = :diagnostico, observaciones = :obs,
                       fecha_modificacion = :f WHERE id = :h
                """, Db.p("motivo", atencion.get("motivo"), "diagnostico", atencion.get("diagnostico"),
                "obs", atencion.get("observaciones"), "f", ahora, "h", atencion.get("historia_id")));
        db.insertar("""
                INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, fecha, descripcion, observacion, diagnostico)
                VALUES (:h, :m, :c, :f, :d, :o, :dx)
                """, Db.p("h", atencion.get("historia_id"), "m", atencion.get("medico_id"), "c", citaId, "f", ahora,
                "d", resumen, "o", atencion.get("observaciones"), "dx", atencion.get("diagnostico")));
        return consultar(citaId);
    }

    // ------------------------------------------------------------------ Fórmulas

    public List<Map<String, Object>> formulasPropias() {
        return formulas("WHERE f.medico_id = :m", Db.p("m", SesionActual.medicoId()));
    }

    @Transactional
    public Map<String, Object> crearFormula(long citaId, AtencionApi.FormulaRq d) {
        Map<String, Object> cita = bloquearCita(citaId);
        verificarMedico(cita);
        Map<String, Object> atencion = atencionDeCita(citaId);
        if (atencion == null) {
            throw new ConflictException("Abre la atención de la cita antes de formular.");
        }
        List<Linea> lineas = validarLineas(d);
        long id = db.insertar("""
                INSERT INTO formula_clinica (cita_id, atencion_id, medico_id, mascota_id, estado, revision, fecha_creacion)
                VALUES (:c, :a, :m, :mascota, 'BORRADOR', 0, :f)
                """, Db.p("c", citaId, "a", atencion.get("id"), "m", cita.get("medico_id"), "mascota", cita.get("mascota_id"),
                "f", Tiempo.ahora()));
        guardarLineas(id, lineas);
        return formula(id);
    }

    @Transactional
    public Map<String, Object> actualizarFormula(long citaId, long formulaId, AtencionApi.FormulaRq d) {
        Map<String, Object> f = bloquearFormula(formulaId);
        if (Validar.largo(f, "cita_id") != citaId) {
            throw new BadRequestException("La fórmula no pertenece a la cita indicada.");
        }
        verificarPropietario(f);
        if (!"BORRADOR".equals(f.get("estado"))) {
            throw new ConflictException("Solo se pueden editar fórmulas en borrador.");
        }
        verificarRevision(f, d == null ? null : d.revision());
        List<Linea> lineas = validarLineas(d);
        db.ejecutar("DELETE FROM formula_clinica_detalle WHERE formula_id = :f", Db.p("f", formulaId));
        guardarLineas(formulaId, lineas);
        db.ejecutar("UPDATE formula_clinica SET revision = revision + 1 WHERE id = :id", Db.p("id", formulaId));
        return formula(formulaId);
    }

    @Transactional
    public Map<String, Object> confirmarFormula(long formulaId, Integer revision) {
        Map<String, Object> f = bloquearFormula(formulaId);
        verificarPropietario(f);
        if (!"BORRADOR".equals(f.get("estado"))) {
            throw new ConflictException("La fórmula ya fue " + Validar.cadena(f, "estado").toLowerCase() + ".");
        }
        verificarRevision(f, revision);
        // Validación de disponibilidad en inventario (lotes vigentes) al confirmar la prescripción.
        List<String> faltantes = new ArrayList<>();
        for (Map<String, Object> l : db.lista("""
                SELECT d.cantidad, m.nombre, COALESCE((SELECT SUM(l.cantidad) FROM medicamento_lote l
                       WHERE l.medicamento_id = m.id AND (l.fecha_vence IS NULL OR l.fecha_vence >= :hoy)), 0) AS disponible
                FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                WHERE d.formula_id = :f
                """, Db.p("f", formulaId, "hoy", Tiempo.hoy()))) {
            if (Validar.numero(l, "cantidad") > Validar.numero(l, "disponible")) {
                faltantes.add(l.get("nombre") + " (prescrito " + l.get("cantidad") + ", disponible " + l.get("disponible") + ")");
            }
        }
        if (!faltantes.isEmpty()) {
            throw new ConflictException("No hay existencias suficientes: " + String.join("; ", faltantes)
                    + ". Ajusta las cantidades o selecciona otro medicamento.");
        }
        LocalDateTime ahora = Tiempo.ahora();
        db.ejecutar("""
                UPDATE formula_clinica SET estado = 'CONFIRMADA', estado_despacho = 'PENDIENTE_DESPACHO',
                       revision = revision + 1, fecha_confirmacion = :f WHERE id = :id
                """, Db.p("f", ahora, "id", formulaId));
        db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, 'BORRADOR', 'PENDIENTE_DESPACHO', :u, :ahora, 'Fórmula confirmada por el médico')
                """, Db.p("f", formulaId, "u", SesionActual.usuarioId(), "ahora", ahora));
        return formula(formulaId);
    }

    @Transactional
    public Map<String, Object> anularFormula(long formulaId, Integer revision) {
        Map<String, Object> f = bloquearFormula(formulaId);
        verificarPropietario(f);
        if (!"BORRADOR".equals(f.get("estado"))) {
            throw new ConflictException("Solo se pueden anular borradores; la fórmula confirmada se gestiona en despacho.");
        }
        verificarRevision(f, revision);
        db.ejecutar("UPDATE formula_clinica SET estado = 'ANULADA', revision = revision + 1 WHERE id = :id",
                Db.p("id", formulaId));
        return formula(formulaId);
    }

    public Map<String, Object> formula(long id) {
        List<Map<String, Object>> f = formulas("WHERE f.id = :id", Db.p("id", id));
        if (f.isEmpty()) {
            throw new com.uniminuto.clinica.exception.NotFoundException("La fórmula no existe.");
        }
        return f.get(0);
    }

    private List<Map<String, Object>> formulasDeCita(long cita) {
        return formulas("WHERE f.cita_id = :c", Db.p("c", cita));
    }

    private List<Map<String, Object>> formulas(String filtro, Map<String, Object> params) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> f : db.lista("""
                SELECT f.id, f.cita_id AS citaId, m.nombre_mascota AS paciente, f.estado, f.estado_despacho AS estadoDespacho,
                       f.revision, f.fecha_creacion AS creada, f.fecha_confirmacion AS confirmada
                FROM formula_clinica f JOIN mascota m ON m.mascota_id = f.mascota_id
                """ + filtro + " ORDER BY f.fecha_creacion DESC, f.id DESC", params)) {
            f.put("medicamentos", db.lista("""
                    SELECT d.medicamento_id AS medicamentoId, m.nombre, d.dosis, d.frecuencia, d.duracion, d.cantidad,
                           d.via, d.indicaciones, m.precio_venta * d.cantidad AS precioEstimado
                    FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                    WHERE d.formula_id = :f ORDER BY d.id
                    """, Db.p("f", f.get("id"))));
            resultado.add(f);
        }
        return resultado;
    }

    private record Linea(long medicamento, String dosis, String frecuencia, String duracion, int cantidad, String via,
                         String indicaciones) {
    }

    private List<Linea> validarLineas(AtencionApi.FormulaRq d) {
        if (d == null || d.medicamentos() == null || d.medicamentos().isEmpty()) {
            throw new BadRequestException("La fórmula debe tener al menos un medicamento.");
        }
        List<Linea> lineas = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (AtencionApi.LineaRq l : d.medicamentos()) {
            if (l == null || l.medicamentoId() == null) {
                throw new BadRequestException("Selecciona el medicamento de cada línea.");
            }
            if (!vistos.add(l.medicamentoId())) {
                throw new BadRequestException("Un medicamento no puede repetirse en la misma fórmula.");
            }
            Map<String, Object> m = db.requerido("SELECT nombre, tipo, activo FROM medicamento WHERE id = :id",
                    Db.p("id", l.medicamentoId()), "El medicamento no existe.");
            if (!"MEDICAMENTO".equals(m.get("tipo")) || !Validar.bool(m, "activo")) {
                throw new BadRequestException(m.get("nombre") + " no es un medicamento activo formulable.");
            }
            int cantidad = Validar.entero(l.cantidad(), "cantidad prescrita", 1);
            if (cantidad > 1000) {
                throw new BadRequestException("La cantidad prescrita no puede superar 1000 unidades.");
            }
            lineas.add(new Linea(l.medicamentoId(), Validar.texto(l.dosis(), "dosis", 255),
                    Validar.texto(l.frecuencia(), "frecuencia", 255), Validar.texto(l.duracion(), "duración", 255),
                    cantidad, Validar.texto(l.via(), "vía de administración", 100),
                    Validar.opcional(l.indicaciones(), "indicaciones", 2000)));
        }
        return lineas;
    }

    private void guardarLineas(long formula, List<Linea> lineas) {
        for (Linea l : lineas) {
            db.insertar("""
                    INSERT INTO formula_clinica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion, cantidad,
                                                         via, indicaciones)
                    VALUES (:f, :m, :dosis, :frecuencia, :duracion, :cantidad, :via, :ind)
                    """, Db.p("f", formula, "m", l.medicamento(), "dosis", l.dosis(), "frecuencia", l.frecuencia(),
                    "duracion", l.duracion(), "cantidad", l.cantidad(), "via", l.via(), "ind", l.indicaciones()));
        }
    }

    // ------------------------------------------------------------------ Documentos PDF

    public byte[] formulaPdf(long formulaId) {
        Map<String, Object> f = db.requerido("""
                SELECT f.*, c.fecha_hora, m.nombre_mascota, r.especie, r.nombre AS raza, m.edad,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, cl.tipo_documento, cl.numero_documento,
                       CONCAT(md.nombres, ' ', md.apellidos) AS medico, md.registro_profesional, e.nombre AS especialidad
                FROM formula_clinica f
                JOIN cita c ON c.id = f.cita_id
                JOIN mascota m ON m.mascota_id = f.mascota_id
                JOIN raza r ON r.raza_id = m.raza_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = f.medico_id
                JOIN especializacion e ON e.id = md.especializacion_id
                WHERE f.id = :id
                """, Db.p("id", formulaId), "La fórmula no existe.");
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.MEDICO) && Validar.largo(f, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo puedes descargar tus propias fórmulas.");
        }
        if (!"CONFIRMADA".equals(f.get("estado"))) {
            throw new ConflictException("Solo se imprimen fórmulas confirmadas.");
        }
        PdfDocumento pdf = new PdfDocumento("Fórmula médica N.º " + formulaId);
        pdf.campos(new String[][]{
                {"Paciente", f.get("nombre_mascota") + " (" + f.get("especie") + " · " + f.get("raza") + ", " + f.get("edad") + " años)"},
                {"Propietario", f.get("propietario") + " · " + f.get("tipo_documento") + " " + f.get("numero_documento")},
                {"Veterinario", f.get("medico") + " · Reg. " + f.get("registro_profesional")},
                {"Especialidad", Validar.cadena(f, "especialidad")},
                {"Cita", "#" + f.get("cita_id") + " · " + texto(f.get("fecha_hora"))},
                {"Confirmada", texto(f.get("fecha_confirmacion"))}});
        pdf.seccion("Medicamentos prescritos");
        List<String[]> filas = new ArrayList<>();
        for (Map<String, Object> l : db.lista("""
                SELECT m.nombre, m.presentacion, d.dosis, d.frecuencia, d.duracion, d.via, d.cantidad, d.indicaciones
                FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id WHERE d.formula_id = :f ORDER BY d.id
                """, Db.p("f", formulaId))) {
            filas.add(new String[]{l.get("nombre") + (l.get("presentacion") == null ? "" : "\n" + l.get("presentacion")),
                    texto(l.get("dosis")), texto(l.get("frecuencia")), texto(l.get("duracion")), texto(l.get("via")),
                    texto(l.get("cantidad")), texto(l.get("indicaciones"))});
        }
        pdf.tabla(new String[]{"Medicamento", "Dosis", "Frecuencia", "Duración", "Vía", "Cantidad", "Indicaciones"}, filas,
                new float[]{2.4f, 1.3f, 1.3f, 1.1f, 1, 0.8f, 2.4f}, new boolean[]{false, false, false, false, false, true, false});
        pdf.parrafo("\n\n\n_______________________________\n" + f.get("medico") + "\nRegistro profesional "
                + f.get("registro_profesional"));
        pdf.nota("La entrega de los medicamentos se registra en el módulo de despacho. Esta fórmula no descuenta inventario.");
        return pdf.cerrar();
    }

    public byte[] formulaLegadaPdf(long id) {
        Map<String, Object> f = db.requerido("""
                SELECT fm.*, md.nombre AS medicamento, md.presentacion, c.fecha_hora, m.nombre_mascota,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, CONCAT(me.nombres, ' ', me.apellidos) AS medico,
                       me.registro_profesional
                FROM formula_medica fm
                JOIN medicamento md ON md.id = fm.medicamento_id
                JOIN cita c ON c.id = fm.cita_id
                JOIN mascota m ON m.mascota_id = c.mascota_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico me ON me.id = c.medico_id
                WHERE fm.id = :id
                """, Db.p("id", id), "La fórmula no existe.");
        PdfDocumento pdf = new PdfDocumento("Fórmula histórica N.º " + id);
        pdf.campos(new String[][]{{"Paciente", texto(f.get("nombre_mascota"))}, {"Propietario", texto(f.get("propietario"))},
                {"Veterinario", f.get("medico") + " · Reg. " + f.get("registro_profesional")},
                {"Cita", "#" + f.get("cita_id") + " · " + texto(f.get("fecha_hora"))},
                {"Emisión", texto(f.get("fecha_emision"))}});
        pdf.seccion("Prescripción");
        pdf.tabla(new String[]{"Medicamento", "Dosis", "Frecuencia", "Duración", "Indicaciones"},
                List.<String[]>of(new String[]{f.get("medicamento") + (f.get("presentacion") == null ? "" : "\n" + f.get("presentacion")),
                        texto(f.get("dosis")), texto(f.get("frecuencia")), texto(f.get("duracion")), texto(f.get("indicaciones"))}),
                new float[]{2.4f, 1.4f, 1.4f, 1.2f, 3}, null);
        pdf.nota("Fórmula registrada en la versión anterior del sistema (entrega parcial).");
        return pdf.cerrar();
    }

    /** Prefactura: servicios prestados (tarifa congelada) y medicamentos formulados a precio de venta. */
    public byte[] prefactura(long citaId) {
        Map<String, Object> cita = cita(citaId);
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.MEDICO) && Validar.largo(cita, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo puedes consultar la prefactura de tus citas.");
        }
        Map<String, Object> atencion = atencionDeCita(citaId);
        if (atencion == null || atencion.get("fecha_finalizacion") == null) {
            throw new ConflictException("La prefactura está disponible al finalizar la atención médica.");
        }
        List<Liquidador.Concepto> conceptos = new ArrayList<>();
        for (Map<String, Object> sv : db.lista("""
                SELECT s.servicio_id, s.tarifa_id, sc.nombre, s.cantidad, s.valor_unitario, s.iva_porcentaje, s.impuesto_porcentaje
                FROM atencion_servicio s JOIN servicio_clinico sc ON sc.id = s.servicio_id WHERE s.atencion_id = :a ORDER BY s.id
                """, Db.p("a", atencion.get("id")))) {
            conceptos.add(new Liquidador.Concepto(Validar.largo(sv, "servicio_id"), Validar.largo(sv, "tarifa_id"), null,
                    "Servicio: " + sv.get("nombre"), Validar.numero(sv, "cantidad"), Validar.decimal(sv, "valor_unitario"),
                    Validar.decimal(sv, "iva_porcentaje"), Validar.decimal(sv, "impuesto_porcentaje")));
        }
        for (Map<String, Object> m : db.lista("""
                SELECT md.id, md.nombre, SUM(d.cantidad) AS cantidad, md.precio_venta, md.iva_porcentaje
                FROM formula_clinica f JOIN formula_clinica_detalle d ON d.formula_id = f.id
                JOIN medicamento md ON md.id = d.medicamento_id
                WHERE f.cita_id = :c AND f.estado = 'CONFIRMADA'
                GROUP BY md.id, md.nombre, md.precio_venta, md.iva_porcentaje ORDER BY md.nombre
                """, Db.p("c", citaId))) {
            conceptos.add(new Liquidador.Concepto(null, null, Validar.largo(m, "id"), "Medicamento formulado: " + m.get("nombre"),
                    Validar.numero(m, "cantidad"), Validar.decimal(m, "precio_venta"), Validar.decimal(m, "iva_porcentaje"),
                    BigDecimal.ZERO));
        }
        Liquidador.Resultado r = Liquidador.liquidar(conceptos, BigDecimal.ZERO);
        PdfDocumento pdf = new PdfDocumento("Prefactura · Cita #" + citaId);
        pdf.campos(new String[][]{{"Propietario", texto(cita.get("propietario"))}, {"Documento", texto(cita.get("documento"))},
                {"Mascota", texto(cita.get("mascota"))}, {"Veterinario", texto(cita.get("medico"))},
                {"Especialidad", texto(atencion.get("especialidad"))}, {"Finalizada", texto(atencion.get("fecha_finalizacion"))}});
        pdf.seccion("Servicios prestados y medicamentos formulados");
        List<String[]> filas = new ArrayList<>();
        for (Liquidador.Linea l : r.lineas()) {
            filas.add(new String[]{l.concepto().nombre(), String.valueOf(l.concepto().cantidad()), cop(l.concepto().precio()),
                    cop(l.subtotal()), cop(l.iva().add(l.otros())), cop(l.total())});
        }
        pdf.tabla(new String[]{"Concepto", "Cant.", "Valor unitario", "Subtotal", "Impuestos", "Total"}, filas,
                new float[]{4, 0.8f, 1.6f, 1.6f, 1.4f, 1.6f}, new boolean[]{false, true, true, true, true, true});
        pdf.totales(new String[][]{{"Subtotal", cop(r.subtotal())}, {"IVA", cop(r.iva())}, {"Otros impuestos", cop(r.otros())},
                {"Total estimado", cop(r.total())}});
        pdf.nota("Documento informativo, no válido como factura. La factura solo incluye los medicamentos efectivamente "
                + "dispensados y puede aplicar descuentos autorizados.");
        return pdf.cerrar();
    }

    // ------------------------------------------------------------------ Utilidades

    private Map<String, Object> cita(long id) {
        return db.requerido("""
                SELECT c.*, m.nombre_mascota AS mascota, r.especie, r.nombre AS raza, m.edad,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario,
                       CONCAT(cl.tipo_documento, ' ', cl.numero_documento) AS documento, cl.telefono,
                       CONCAT(md.nombres, ' ', md.apellidos) AS medico, md.especializacion_id
                FROM cita c
                JOIN mascota m ON m.mascota_id = c.mascota_id
                JOIN raza r ON r.raza_id = m.raza_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = c.medico_id
                WHERE c.id = :id
                """, Db.p("id", id), "La cita no existe.");
    }

    private Map<String, Object> bloquearCita(long id) {
        db.requerido("SELECT id FROM cita WHERE id = :id FOR UPDATE", Db.p("id", id), "La cita no existe.");
        return cita(id);
    }

    private Map<String, Object> atencionDeCita(long cita) {
        return db.uno("""
                SELECT a.*, e.nombre AS especialidad FROM atencion_medica a
                JOIN especializacion e ON e.id = a.especializacion_id WHERE a.cita_id = :c
                """, Db.p("c", cita));
    }

    private Map<String, Object> bloquearAtencion(long cita) {
        Map<String, Object> a = db.uno("SELECT * FROM atencion_medica WHERE cita_id = :c FOR UPDATE", Db.p("c", cita));
        if (a == null) {
            throw new ConflictException("La atención aún no ha sido abierta.");
        }
        if (a.get("fecha_finalizacion") != null) {
            throw new ConflictException("La atención ya fue finalizada y no admite cambios.");
        }
        return a;
    }

    private Map<String, Object> bloquearFormula(long id) {
        return db.requerido("SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE", Db.p("id", id),
                "La fórmula no existe.");
    }

    private void insertarServicio(long atencion, long servicio, Map<String, Object> tarifa, int cantidad) {
        db.insertar("""
                INSERT INTO atencion_servicio (atencion_id, servicio_id, tarifa_id, cantidad, valor_unitario, iva_porcentaje,
                                               impuesto_porcentaje)
                VALUES (:a, :s, :t, :c, :v, :iva, :imp)
                """, Db.p("a", atencion, "s", servicio, "t", tarifa.get("id"), "c", cantidad, "v", tarifa.get("valor"),
                "iva", tarifa.get("iva"), "imp", tarifa.get("impuesto")));
    }

    private void verificarVersion(Map<String, Object> atencion, Integer version) {
        if (version == null || version != Validar.numero(atencion, "version")) {
            throw new ConflictException("La atención fue modificada en otra sesión. Recarga la información.");
        }
    }

    private void verificarRevision(Map<String, Object> formula, Integer revision) {
        if (revision == null || revision != Validar.numero(formula, "revision")) {
            throw new ConflictException("La fórmula fue modificada en otra sesión. Recarga la información.");
        }
    }

    private void verificarMedico(Map<String, Object> cita) {
        if (Validar.largo(cita, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo el veterinario asignado puede atender esta cita.");
        }
    }

    private void verificarLectura(Map<String, Object> cita) {
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.MEDICO)) {
            verificarMedico(cita);
        }
    }

    private void verificarPropietario(Map<String, Object> formula) {
        if (Validar.largo(formula, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo el médico que formuló puede modificar la fórmula.");
        }
    }

    private static String valor(Object v) {
        return v == null ? "sin registro" : v.toString();
    }

    private static String texto(Object v) {
        return v == null ? "—" : v.toString().replace('T', ' ');
    }

    static String cop(BigDecimal v) {
        synchronized (COP) {
            return COP.format(v);
        }
    }
}
