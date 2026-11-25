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

    /** Formato de moneda colombiana para los PDF. */
    private static final NumberFormat COP = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    /** Consulta de tarifas vigentes. */
    private final TarifarioService tarifario;
    /** Enlace entre la fórmula de la atención y la prescripción en la historia clínica. */
    private final com.uniminuto.clinica.historia.FormulaIntegracionService integracion;

    /** Acceso a datos del módulo (SQL). */
    private final AtencionRepository repositorio;

    /**
     * @param repositorio acceso a datos del módulo.
     * @param tarifario servicio de tarifas.
     * @param integracion enlace con la historia clínica.
     */
    public AtencionService(AtencionRepository repositorio, TarifarioService tarifario,
                           com.uniminuto.clinica.historia.FormulaIntegracionService integracion) {
        this.repositorio = repositorio;
        this.integracion = integracion;
        this.tarifario = tarifario;
    }

    // ------------------------------------------------------------------ Atención

    /**
     * Atención de una cita con servicios, fórmulas e historia.
     *
     * @param citaId identificador de la cita.
     * @return atención o datos de la cita si aún no se abre.
     */
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
            r.put("servicios", repositorio.listarAtencionServicio(Db.p("a", atencion.get("id"))));
        }
        r.put("antecedentes", repositorio.listarAnotacionHistoria(Db.p("m", cita.get("mascota_id"), "c", citaId)));
        long especialidad = atencion == null ? Validar.largo(cita, "especializacion_id")
                : Validar.largo(atencion, "especializacion_id");
        List<Map<String, Object>> catalogo = new ArrayList<>();
        for (Map<String, Object> s : repositorio.listarServicioClinico(Db.p("e", especialidad))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("servicio", s);
            item.put("tarifa", tarifario.vigente(Validar.largo(s, "id"), fecha));
            catalogo.add(item);
        }
        r.put("catalogo", catalogo);
        r.put("formulas", formulasDeCita(citaId));
        return r;
    }

    /**
     * Abre la atención de una cita y crea su historia médica.
     *
     * @param citaId identificador de la cita.
     * @return atención abierta.
     */
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
            Long historia = repositorio.valorHistoriaMedica(Db.p("c", citaId), Long.class);
            if (historia == null) {
                historia = repositorio.insertarHistoriaMedica(Db.p("m", cita.get("mascota_id"), "c", citaId, "medico", cita.get("medico_id"),
                        "fecha", cita.get("fecha_hora"), "motivo", cita.get("motivo"), "ahora", Tiempo.ahora()));
            }
            repositorio.insertarAtencionMedica(Db.p("c", citaId, "h", historia, "m", cita.get("medico_id"), "e", cita.get("especializacion_id"),
                    "motivo", cita.get("motivo"), "ahora", Tiempo.ahora()));
        }
        return consultar(citaId);
    }

    /**
     * Guarda el borrador de la atención con control de versión optimista.
     *
     * @param citaId identificador de la cita.
     * @param d datos de la atención.
     * @return atención actualizada.
     */
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
            Map<String, Object> sc = repositorio.obtenerServicioClinico(Db.p("id", s.servicioId()), "El servicio no existe.");
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
        repositorio.actualizarAtencionMedica(Db.p("motivo", Validar.opcional(d.motivo(), "motivo", 4000),
                "sintomas", Validar.opcional(d.sintomas(), "síntomas", 4000),
                "diagnostico", Validar.opcional(d.diagnostico(), "diagnóstico", 4000),
                "obs", Validar.opcional(d.observaciones(), "observaciones", 4000),
                "rec", Validar.opcional(d.recomendaciones(), "recomendaciones", 4000), "id", atencion.get("id")));
        repositorio.eliminarAtencionServicio(Db.p("a", atencion.get("id")));
        for (Object[] s : servicios) {
            insertarServicio(Validar.largo(atencion, "id"), (Long) s[0], (Map<String, Object>) s[1], (int) s[2]);
        }
        return consultar(citaId);
    }

    /**
     * Finaliza la atención, actualiza la historia y agrega la anotación clínica.
     *
     * @param citaId identificador de la cita.
     * @param version versión esperada.
     * @return atención finalizada.
     */
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
        List<Map<String, Object>> servicios = repositorio.listarAtencionServicioEnFinalizar(Db.p("a", atencionId));
        if (servicios.isEmpty()) {
            throw new BadRequestException("Registra al menos un servicio o consulta prestada antes de finalizar.");
        }
        // Congela la tarifa vigente en la fecha de la cita (valores históricos para la factura).
        LocalDate fecha = ((LocalDateTime) cita.get("fecha_hora")).toLocalDate();
        repositorio.eliminarAtencionServicio(Db.p("a", atencionId));
        for (Map<String, Object> s : servicios) {
            Map<String, Object> tarifa = tarifario.vigente(Validar.largo(s, "servicio_id"), fecha);
            if (tarifa == null) {
                throw new ConflictException("Un servicio de la atención ya no tiene tarifa vigente.");
            }
            insertarServicio(atencionId, Validar.largo(s, "servicio_id"), tarifa, Validar.numero(s, "cantidad"));
        }
        LocalDateTime ahora = Tiempo.ahora();
        repositorio.actualizarAtencionMedicaEnFinalizar(Db.p("f", ahora, "id", atencionId));
        if (!"atendida".equals(cita.get("estado"))) {
            repositorio.actualizarCita(Db.p("f", ahora, "id", citaId));
            repositorio.insertarCitaRevision(Db.p("c", citaId, "u", SesionActual.usuarioId(), "f", ahora, "ea", cita.get("estado"),
                    "i", cita.get("fecha_hora"), "fin", cita.get("fecha_fin")));
        }
        String resumen = "Atención de la cita #" + citaId + ". Motivo: " + atencion.get("motivo")
                + ". Síntomas: " + valor(atencion.get("sintomas")) + ". Diagnóstico: " + atencion.get("diagnostico")
                + ". Observaciones: " + valor(atencion.get("observaciones"))
                + ". Recomendaciones: " + valor(atencion.get("recomendaciones")) + ".";
        repositorio.actualizarHistoriaMedica(Db.p("motivo", atencion.get("motivo"), "diagnostico", atencion.get("diagnostico"),
                "obs", atencion.get("observaciones"), "f", ahora, "h", atencion.get("historia_id")));
        repositorio.insertarAnotacionHistoria(Db.p("h", atencion.get("historia_id"), "m", atencion.get("medico_id"), "c", citaId, "f", ahora,
                "d", resumen, "o", atencion.get("observaciones"), "dx", atencion.get("diagnostico")));
        // Prescripciones registradas en la historia antes de atender: ahora generan su orden de despacho.
        integracion.generarPendientesDeCita(citaId);
        return consultar(citaId);
    }

    // ------------------------------------------------------------------ Fórmulas

    /**
     * @return fórmulas emitidas por el médico autenticado.
     */
    public List<Map<String, Object>> formulasPropias() {
        return formulas(AtencionRepository.CriterioFormula.MEDICO, SesionActual.medicoId());
    }

    /**
     * Crea una fórmula en borrador para la cita.
     *
     * @param citaId identificador de la cita.
     * @param d medicamentos e indicaciones.
     * @return fórmula creada.
     */
    @Transactional
    public Map<String, Object> crearFormula(long citaId, AtencionApi.FormulaRq d) {
        Map<String, Object> cita = bloquearCita(citaId);
        verificarMedico(cita);
        Map<String, Object> atencion = atencionDeCita(citaId);
        if (atencion == null) {
            throw new ConflictException("Abre la atención de la cita antes de formular.");
        }
        List<Linea> lineas = validarLineas(d);
        long id = repositorio.insertarFormulaClinica(Db.p("c", citaId, "a", atencion.get("id"), "m", cita.get("medico_id"), "mascota", cita.get("mascota_id"),
                "f", Tiempo.ahora()));
        guardarLineas(id, lineas);
        return formula(id);
    }

    /**
     * Actualiza una fórmula en borrador.
     *
     * @param citaId identificador de la cita.
     * @param formulaId identificador de la fórmula.
     * @param d nuevos datos.
     * @return fórmula actualizada.
     */
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
        repositorio.eliminarFormulaClinicaDetalle(Db.p("f", formulaId));
        guardarLineas(formulaId, lineas);
        repositorio.actualizarFormulaClinica(Db.p("id", formulaId));
        return formula(formulaId);
    }

    /**
     * Confirma la fórmula y la deja pendiente de despacho.
     *
     * @param formulaId identificador de la fórmula.
     * @param revision revisión esperada.
     * @return fórmula confirmada.
     */
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
        for (Map<String, Object> l : repositorio.listarMedicamentoLote(Db.p("f", formulaId, "hoy", Tiempo.hoy()))) {
            if (Validar.numero(l, "cantidad") > Validar.numero(l, "disponible")) {
                faltantes.add(l.get("nombre") + " (prescrito " + l.get("cantidad") + ", disponible " + l.get("disponible") + ")");
            }
        }
        if (!faltantes.isEmpty()) {
            throw new ConflictException("No hay existencias suficientes: " + String.join("; ", faltantes)
                    + ". Ajusta las cantidades o selecciona otro medicamento.");
        }
        LocalDateTime ahora = Tiempo.ahora();
        repositorio.actualizarFormulaClinicaEnConfirmarFormula(Db.p("f", ahora, "id", formulaId));
        repositorio.insertarDespachoEstadoHistorial(Db.p("f", formulaId, "u", SesionActual.usuarioId(), "ahora", ahora));
        // La fórmula confirmada queda también en la historia clínica (Gestión de Fórmulas e historia de la mascota).
        integracion.registrarEnHistoria(formulaId);
        return formula(formulaId);
    }

    /**
     * Anula una fórmula que no ha sido despachada.
     *
     * @param formulaId identificador de la fórmula.
     * @param revision revisión esperada.
     * @return fórmula anulada.
     */
    @Transactional
    public Map<String, Object> anularFormula(long formulaId, Integer revision) {
        Map<String, Object> f = bloquearFormula(formulaId);
        verificarPropietario(f);
        if (!"BORRADOR".equals(f.get("estado"))) {
            throw new ConflictException("Solo se pueden anular borradores; la fórmula confirmada se gestiona en despacho.");
        }
        verificarRevision(f, revision);
        repositorio.actualizarFormulaClinicaEnAnularFormula(Db.p("id", formulaId));
        return formula(formulaId);
    }

    /**
     * @param id identificador de la fórmula.
     * @return fórmula con sus líneas.
     */
    public Map<String, Object> formula(long id) {
        List<Map<String, Object>> f = formulas(AtencionRepository.CriterioFormula.ID, id);
        if (f.isEmpty()) {
            throw new com.uniminuto.clinica.exception.NotFoundException("La fórmula no existe.");
        }
        return f.get(0);
    }

    /**
     * @param cita identificador de la cita.
     * @return fórmulas de la cita.
     */
    private List<Map<String, Object>> formulasDeCita(long cita) {
        return formulas(AtencionRepository.CriterioFormula.CITA, cita);
    }

    /**
     * Consulta fórmulas con sus líneas.
     *
     * @param criterio criterio de búsqueda.
     * @param valor valor buscado.
     * @return fórmulas.
     */
    private List<Map<String, Object>> formulas(AtencionRepository.CriterioFormula criterio, Object valor) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> f : repositorio.formulasPor(criterio, valor)) {
            f.put("medicamentos", repositorio.listarFormulaClinicaDetalle(Db.p("f", f.get("id"))));
            resultado.add(f);
        }
        return resultado;
    }

    /** Línea validada de una fórmula (medicamento, posología y cantidad). */
    private record Linea(long medicamento, String dosis, String frecuencia, String duracion, int cantidad, String via,
                         String indicaciones) {
    }

    /**
     * Valida las líneas de la fórmula (medicamento activo, dosis y duración con número positivo).
     *
     * @param d datos de la fórmula.
     * @return líneas validadas.
     */
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
            Map<String, Object> m = repositorio.obtenerMedicamento(Db.p("id", l.medicamentoId()), "El medicamento no existe.");
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

    /**
     * Reemplaza las líneas de la fórmula.
     *
     * @param formula identificador de la fórmula.
     * @param lineas líneas validadas.
     */
    private void guardarLineas(long formula, List<Linea> lineas) {
        for (Linea l : lineas) {
            repositorio.insertarFormulaClinicaDetalle(Db.p("f", formula, "m", l.medicamento(), "dosis", l.dosis(), "frecuencia", l.frecuencia(),
                    "duracion", l.duracion(), "cantidad", l.cantidad(), "via", l.via(), "ind", l.indicaciones()));
        }
    }

    // ------------------------------------------------------------------ Documentos PDF

    /**
     * Genera el PDF de la fórmula médica.
     *
     * @param formulaId identificador de la fórmula.
     * @return PDF.
     */
    public byte[] formulaPdf(long formulaId) {
        Map<String, Object> f = repositorio.obtenerFormulaClinica(Db.p("id", formulaId), "La fórmula no existe.");
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.VETERINARIO) && Validar.largo(f, "medico_id") != SesionActual.medicoId()) {
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
        for (Map<String, Object> l : repositorio.listarFormulaClinicaDetalleEnFormulaPdf(Db.p("f", formulaId))) {
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

    /**
     * Genera el PDF de una fórmula del módulo de historias (formula_medica).
     *
     * @param id identificador de la fórmula.
     * @return PDF.
     */
    public byte[] formulaLegadaPdf(long id) {
        Map<String, Object> f = repositorio.obtenerFormulaMedica(Db.p("id", id), "La fórmula no existe.");
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
        if (s.es(Perfiles.VETERINARIO) && Validar.largo(cita, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo puedes consultar la prefactura de tus citas.");
        }
        Map<String, Object> atencion = atencionDeCita(citaId);
        if (atencion == null || atencion.get("fecha_finalizacion") == null) {
            throw new ConflictException("La prefactura está disponible al finalizar la atención médica.");
        }
        List<Liquidador.Concepto> conceptos = new ArrayList<>();
        for (Map<String, Object> sv : repositorio.listarAtencionServicioEnPrefactura(Db.p("a", atencion.get("id")))) {
            conceptos.add(new Liquidador.Concepto(Validar.largo(sv, "servicio_id"), Validar.largo(sv, "tarifa_id"), null,
                    "Servicio: " + sv.get("nombre"), Validar.numero(sv, "cantidad"), Validar.decimal(sv, "valor_unitario"),
                    Validar.decimal(sv, "iva_porcentaje"), Validar.decimal(sv, "impuesto_porcentaje")));
        }
        for (Map<String, Object> m : repositorio.listarFormulaClinica(Db.p("c", citaId))) {
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

    /**
     * @param id identificador de la cita.
     * @return cita con cliente, mascota y médico.
     */
    private Map<String, Object> cita(long id) {
        return repositorio.obtenerCita(Db.p("id", id), "La cita no existe.");
    }

    /**
     * @param id identificador de la cita.
     * @return cita bloqueada para actualización.
     */
    private Map<String, Object> bloquearCita(long id) {
        repositorio.obtenerCitaEnBloquearCita(Db.p("id", id), "La cita no existe.");
        return cita(id);
    }

    /**
     * @param cita identificador de la cita.
     * @return atención de la cita o null.
     */
    private Map<String, Object> atencionDeCita(long cita) {
        return repositorio.buscarAtencionMedica(Db.p("c", cita));
    }

    /**
     * @param cita identificador de la cita.
     * @return atención bloqueada para actualización.
     */
    private Map<String, Object> bloquearAtencion(long cita) {
        Map<String, Object> a = repositorio.buscarAtencionMedicaEnBloquearAtencion(Db.p("c", cita));
        if (a == null) {
            throw new ConflictException("La atención aún no ha sido abierta.");
        }
        if (a.get("fecha_finalizacion") != null) {
            throw new ConflictException("La atención ya fue finalizada y no admite cambios.");
        }
        return a;
    }

    /**
     * @param id identificador de la fórmula.
     * @return fórmula bloqueada para actualización.
     */
    private Map<String, Object> bloquearFormula(long id) {
        return repositorio.obtenerFormulaClinicaEnBloquearFormula(Db.p("id", id), "La fórmula no existe.");
    }

    /**
     * Agrega un servicio a la atención con la tarifa vigente.
     *
     * @param atencion identificador de la atención.
     * @param servicio identificador del servicio.
     * @param tarifa tarifa vigente.
     * @param cantidad cantidad.
     */
    private void insertarServicio(long atencion, long servicio, Map<String, Object> tarifa, int cantidad) {
        repositorio.insertarAtencionServicio(Db.p("a", atencion, "s", servicio, "t", tarifa.get("id"), "c", cantidad, "v", tarifa.get("valor"),
                "iva", tarifa.get("iva"), "imp", tarifa.get("impuesto")));
    }

    /**
     * Control de concurrencia optimista de la atención.
     *
     * @param atencion fila actual.
     * @param version versión recibida.
     */
    private void verificarVersion(Map<String, Object> atencion, Integer version) {
        if (version == null || version != Validar.numero(atencion, "version")) {
            throw new ConflictException("La atención fue modificada en otra sesión. Recarga la información.");
        }
    }

    /**
     * Control de concurrencia optimista de la fórmula.
     *
     * @param formula fila actual.
     * @param revision revisión recibida.
     */
    private void verificarRevision(Map<String, Object> formula, Integer revision) {
        if (revision == null || revision != Validar.numero(formula, "revision")) {
            throw new ConflictException("La fórmula fue modificada en otra sesión. Recarga la información.");
        }
    }

    /**
     * Verifica que la cita pertenezca al médico autenticado.
     *
     * @param cita fila de la cita.
     */
    private void verificarMedico(Map<String, Object> cita) {
        if (Validar.largo(cita, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo el veterinario asignado puede atender esta cita.");
        }
    }

    /**
     * Verifica que el usuario pueda consultar la cita.
     *
     * @param cita fila de la cita.
     */
    private void verificarLectura(Map<String, Object> cita) {
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.VETERINARIO)) {
            verificarMedico(cita);
        }
    }

    /**
     * Verifica que la fórmula sea del médico autenticado.
     *
     * @param formula fila de la fórmula.
     */
    private void verificarPropietario(Map<String, Object> formula) {
        if (Validar.largo(formula, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo el médico que formuló puede modificar la fórmula.");
        }
    }

    /**
     * @param v valor.
     * @return texto o "—" si está vacío.
     */
    private static String valor(Object v) {
        return v == null ? "sin registro" : v.toString();
    }

    /**
     * @param v valor.
     * @return texto o cadena vacía.
     */
    private static String texto(Object v) {
        return v == null ? "—" : v.toString().replace('T', ' ');
    }

    /**
     * @param v valor monetario.
     * @return valor en formato de pesos colombianos.
     */
    static String cop(BigDecimal v) {
        synchronized (COP) {
            return COP.format(v);
        }
    }
}
