package com.uniminuto.clinica.historia;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Historias clínicas, anotaciones y fórmulas médicas (primer y segundo parcial).
 *
 * <p>Relaciones: CLIENTE 1:N MASCOTA 1:N HISTORIA CLÍNICA 1:N ANOTACIÓN y 1:N FÓRMULA MÉDICA. La historia
 * corresponde a una consulta (cita) de la mascota y el cliente se obtiene de la mascota, de modo que no
 * puede quedar información huérfana ni inconsistente.</p>
 *
 * <p>Reglas principales:</p>
 * <ul>
 *   <li>La mascota debe existir y pertenecer al cliente indicado.</li>
 *   <li>La cita debe pertenecer a esa mascota, estar atendida o finalizada y no tener otra historia.</li>
 *   <li>El veterinario debe existir; el perfil MEDICO solo registra a su nombre.</li>
 *   <li>Las fórmulas no se eliminan: se inactivan para conservar la información clínica.</li>
 *   <li>La fecha de emisión de la fórmula no puede ser anterior a la fecha de la consulta.</li>
 * </ul>
 */
@Service
public class HistoriaService {

    /** Estados de cita que permiten generar una historia clínica (consulta realizada). */
    private static final Set<String> CITA_ATENDIDA = Set.of("atendida", "finalizada");

    /** Consulta base de historias con los datos de mascota, cliente, veterinario y cita. */
    private static final String HISTORIA = """
            SELECT h.id, h.paciente_id AS pacienteId, h.paciente_id AS mascotaId, m.nombre_mascota AS mascota,
                   m.cliente_id AS clienteId, CONCAT(cl.nombres, ' ', cl.apellidos) AS cliente,
                   h.cita_id AS citaId, h.medico_id AS medicoId, CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                   h.fecha, h.motivo, h.diagnostico, h.observaciones, c.estado AS estadoCita,
                   IF(h.activo = 1, 'Activa', 'Inactiva') AS estado, h.activo, h.fecha_creacion AS fechaCreacion,
                   h.fecha_modificacion AS fechaModificacion,
                   (SELECT COUNT(*) FROM anotacion_historia a WHERE a.historia_id = h.id) AS anotaciones
            FROM historia_medica h
            JOIN mascota m ON m.mascota_id = h.paciente_id
            JOIN cliente cl ON cl.cliente_id = m.cliente_id
            LEFT JOIN medico md ON md.id = h.medico_id
            LEFT JOIN cita c ON c.id = h.cita_id
            """;

    /** Consulta base de anotaciones con el nombre del veterinario. */
    private static final String ANOTACION = """
            SELECT a.id, a.historia_id AS historiaId, a.medico_id AS medicoId,
                   CONCAT(md.nombres, ' ', md.apellidos) AS veterinario, a.cita_id AS citaId, a.fecha,
                   a.descripcion, a.observacion, a.diagnostico, a.fecha_modificacion AS fechaModificacion
            FROM anotacion_historia a JOIN medico md ON md.id = a.medico_id
            """;

    /** Consulta base de fórmulas con los nombres de mascota, cliente, veterinario y medicamento. */
    private static final String FORMULA = """
            SELECT f.id, f.cita_id AS citaId, f.mascota_id AS mascotaId, m.nombre_mascota AS mascota,
                   m.cliente_id AS clienteId, CONCAT(cl.nombres, ' ', cl.apellidos) AS cliente,
                   f.medico_id AS medicoId, CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                   f.historia_id AS historiaId, f.medicamento_id AS medicamentoId, me.nombre AS medicamento,
                   me.presentacion, f.dosis, f.frecuencia, f.duracion AS duracionTratamiento, f.indicaciones,
                   f.fecha_emision AS fechaEmision, c.fecha_hora AS fechaCita, f.activo,
                   IF(f.activo = 1, 'Activa', 'Inactiva') AS estado,
                   f.fecha_creacion_registro AS fechaCreacionRegistro,
                   f.fecha_actualizacion_registro AS fechaActualizacionRegistro,
                   f.formula_clinica_id AS ordenDespachoId, fc.estado_despacho AS estadoDespacho,
                   (SELECT d.cantidad FROM formula_clinica_detalle d WHERE d.formula_id = fc.id
                      AND d.medicamento_id = f.medicamento_id) AS cantidadDespacho
            FROM formula_medica f
            LEFT JOIN formula_clinica fc ON fc.id = f.formula_clinica_id
            JOIN mascota m ON m.mascota_id = f.mascota_id
            JOIN cliente cl ON cl.cliente_id = m.cliente_id
            JOIN medico md ON md.id = f.medico_id
            JOIN medicamento me ON me.id = f.medicamento_id
            JOIN cita c ON c.id = f.cita_id
            """;

    /** Acceso a datos. */
    private final Db db;
    /** Enlace entre la prescripción de la historia clínica y la orden de despacho. */
    private final FormulaIntegracionService integracion;

    /**
     * @param db acceso a datos con SQL parametrizado.
     * @param integracion enlace prescripción ↔ orden de despacho.
     */
    public HistoriaService(Db db, FormulaIntegracionService integracion) {
        this.db = db;
        this.integracion = integracion;
    }

    // ================================================================== HISTORIAS CLÍNICAS

    /**
     * Lista las historias clínicas de la más reciente a la más antigua, con filtros opcionales.
     *
     * @param fechaInicio fecha inicial de la consulta (AAAA-MM-DD), opcional.
     * @param fechaFin    fecha final de la consulta (AAAA-MM-DD), opcional.
     * @param mascotaId   mascota, opcional.
     * @param clienteId   cliente, opcional.
     * @return historias con mascota, cliente, veterinario, motivo y estado.
     */
    public List<Map<String, Object>> listarHistorias(String fechaInicio, String fechaFin, Integer mascotaId, Long clienteId) {
        LocalDate desde = Validar.fecha(fechaInicio, "fecha inicial", false);
        LocalDate hasta = Validar.fecha(fechaFin, "fecha final", false);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la fecha inicial.");
        }
        return db.lista(HISTORIA + """
                WHERE (:desde IS NULL OR h.fecha >= :desde) AND (:hasta IS NULL OR h.fecha < :hasta)
                  AND (:mascota IS NULL OR h.paciente_id = :mascota) AND (:cliente IS NULL OR m.cliente_id = :cliente)
                  AND (:medico IS NULL OR h.medico_id = :medico)
                ORDER BY h.fecha DESC, h.id DESC
                """, Db.p("desde", desde == null ? null : desde.atStartOfDay(),
                "hasta", hasta == null ? null : hasta.plusDays(1).atStartOfDay(), "mascota", mascotaId,
                "cliente", clienteId, "medico", medicoRestringido()));
    }

    /**
     * Detalle de una historia clínica con sus anotaciones (de la más reciente a la más antigua) y sus fórmulas.
     *
     * @param id identificador de la historia.
     * @return historia; 404 si no existe.
     */
    public Map<String, Object> detalleHistoria(long id) {
        Map<String, Object> historia = new LinkedHashMap<>(db.requerido(HISTORIA + " WHERE h.id = :id",
                Db.p("id", id), "La historia clínica no existe."));
        historia.put("listaAnotaciones", anotacionesDeHistoria(id));
        historia.put("formulas", db.lista(FORMULA + " WHERE f.historia_id = :id ORDER BY f.fecha_creacion_registro DESC",
                Db.p("id", id)));
        return historia;
    }

    /**
     * Registra una historia clínica para una consulta (cita) atendida.
     *
     * @param d datos de la historia.
     * @return historia creada.
     */
    @Transactional
    public Map<String, Object> crearHistoria(HistoriaApi.HistoriaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la historia clínica son obligatorios.");
        }
        Validar.requerido(d.clienteId(), "cliente");
        Validar.requerido(d.mascotaId(), "mascota");
        Validar.requerido(d.citaId(), "cita");
        Map<String, Object> mascota = validarClienteMascota(d.clienteId(), d.mascotaId());
        Map<String, Object> cita = db.requerido("SELECT * FROM cita WHERE id = :id FOR UPDATE", Db.p("id", d.citaId()),
                "La cita no existe.");
        if (Validar.numero(cita, "mascota_id") != d.mascotaId()) {
            throw new BadRequestException("La cita seleccionada pertenece a otra mascota.");
        }
        if (!CITA_ATENDIDA.contains(Validar.cadena(cita, "estado"))) {
            throw new ConflictException("Solo una cita atendida o finalizada puede generar una historia clínica (estado actual: "
                    + cita.get("estado") + ").");
        }
        if (db.existe("SELECT 1 FROM historia_medica WHERE cita_id = :c", Db.p("c", d.citaId()))) {
            throw new ConflictException("La cita ya tiene una historia clínica registrada.");
        }
        long medico = medicoValido(d.medicoId() == null ? Validar.largo(cita, "medico_id") : d.medicoId());
        LocalDateTime fecha = d.fecha() == null || d.fecha().isBlank()
                ? (LocalDateTime) cita.get("fecha_hora") : Validar.fechaHora(d.fecha(), "fecha y hora");
        if (fecha.isAfter(Tiempo.ahora())) {
            throw new BadRequestException("La fecha de la consulta no puede ser futura.");
        }
        long id = db.insertar("""
                INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, diagnostico, observaciones,
                                             activo, fecha_creacion)
                VALUES (:m, :c, :medico, :fecha, :motivo, :dx, :obs, 1, :ahora)
                """, Db.p("m", mascota.get("mascota_id"), "c", d.citaId(), "medico", medico, "fecha", fecha,
                "motivo", Validar.texto(d.motivo(), "motivo de consulta", 4000),
                "dx", Validar.opcional(d.diagnostico(), "diagnóstico", 4000),
                "obs", Validar.opcional(d.observaciones(), "observaciones", 4000), "ahora", Tiempo.ahora()));
        db.ejecutar("UPDATE formula_medica SET historia_id = :h WHERE cita_id = :c AND historia_id IS NULL",
                Db.p("h", id, "c", d.citaId()));
        return detalleHistoria(id);
    }

    /**
     * Actualiza una historia clínica. La mascota y la cita no cambian (integridad del registro clínico);
     * se pueden modificar veterinario, fecha, motivo, diagnóstico, observaciones y el estado activo.
     *
     * @param id identificador de la historia.
     * @param d  nuevos datos.
     * @return historia actualizada.
     */
    @Transactional
    public Map<String, Object> actualizarHistoria(long id, HistoriaApi.HistoriaRq d) {
        Map<String, Object> actual = db.requerido("SELECT * FROM historia_medica WHERE id = :id FOR UPDATE",
                Db.p("id", id), "La historia clínica no existe.");
        if (d == null) {
            throw new BadRequestException("Los datos de la historia clínica son obligatorios.");
        }
        if ((d.mascotaId() != null && d.mascotaId() != Validar.numero(actual, "paciente_id"))
                || (d.citaId() != null && actual.get("cita_id") != null && d.citaId() != Validar.largo(actual, "cita_id"))) {
            throw new ConflictException("No se puede cambiar la mascota ni la cita de una historia clínica registrada.");
        }
        long medico = medicoValido(d.medicoId() == null ? Validar.largo(actual, "medico_id") : d.medicoId());
        LocalDateTime fecha = d.fecha() == null || d.fecha().isBlank()
                ? (LocalDateTime) actual.get("fecha") : Validar.fechaHora(d.fecha(), "fecha y hora");
        if (fecha.isAfter(Tiempo.ahora())) {
            throw new BadRequestException("La fecha de la consulta no puede ser futura.");
        }
        db.ejecutar("""
                UPDATE historia_medica SET medico_id = :medico, fecha = :fecha, motivo = :motivo, diagnostico = :dx,
                       observaciones = :obs, activo = :activo, fecha_modificacion = :ahora
                WHERE id = :id
                """, Db.p("medico", medico, "fecha", fecha, "motivo", Validar.texto(d.motivo(), "motivo de consulta", 4000),
                "dx", Validar.opcional(d.diagnostico(), "diagnóstico", 4000),
                "obs", Validar.opcional(d.observaciones(), "observaciones", 4000),
                "activo", d.activo() == null ? Validar.bool(actual, "activo") : d.activo(), "ahora", Tiempo.ahora(),
                "id", id));
        return detalleHistoria(id);
    }

    // ================================================================== ANOTACIONES

    /**
     * Lista anotaciones de la más reciente a la más antigua, con filtros opcionales.
     *
     * @param fechaInicio fecha inicial, opcional.
     * @param fechaFin    fecha final, opcional.
     * @param historiaId  historia clínica, opcional.
     * @return anotaciones con el nombre del veterinario.
     */
    public List<Map<String, Object>> listarAnotaciones(String fechaInicio, String fechaFin, Long historiaId) {
        LocalDate desde = Validar.fecha(fechaInicio, "fecha inicial", false);
        LocalDate hasta = Validar.fecha(fechaFin, "fecha final", false);
        return db.lista(ANOTACION + """
                WHERE (:desde IS NULL OR a.fecha >= :desde) AND (:hasta IS NULL OR a.fecha < :hasta)
                  AND (:historia IS NULL OR a.historia_id = :historia)
                ORDER BY a.fecha DESC, a.id DESC
                """, Db.p("desde", desde == null ? null : desde.atStartOfDay(),
                "hasta", hasta == null ? null : hasta.plusDays(1).atStartOfDay(), "historia", historiaId));
    }

    /**
     * Anotaciones de una historia clínica, de la más reciente a la más antigua.
     *
     * @param historiaId historia clínica; 404 si no existe.
     * @return anotaciones.
     */
    public List<Map<String, Object>> anotacionesDeHistoria(long historiaId) {
        db.requerido("SELECT id FROM historia_medica WHERE id = :id", Db.p("id", historiaId), "La historia clínica no existe.");
        return db.lista(ANOTACION + " WHERE a.historia_id = :h ORDER BY a.fecha DESC, a.id DESC", Db.p("h", historiaId));
    }

    /**
     * Consulta una anotación.
     *
     * @param id identificador.
     * @return anotación; 404 si no existe.
     */
    public Map<String, Object> anotacion(long id) {
        return db.requerido(ANOTACION + " WHERE a.id = :id", Db.p("id", id), "La anotación no existe.");
    }

    /**
     * Agrega una anotación a una historia clínica activa.
     *
     * @param d datos de la anotación.
     * @return anotación creada.
     */
    @Transactional
    public Map<String, Object> crearAnotacion(HistoriaApi.AnotacionRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la anotación son obligatorios.");
        }
        Validar.requerido(d.historiaId(), "historia clínica");
        Map<String, Object> historia = db.requerido("SELECT * FROM historia_medica WHERE id = :id", Db.p("id", d.historiaId()),
                "La historia clínica no existe.");
        if (!Validar.bool(historia, "activo")) {
            throw new ConflictException("La historia clínica está inactiva y no admite nuevas anotaciones.");
        }
        long medico = medicoValido(d.medicoId() == null ? Validar.largoNulo(historia, "medico_id") : d.medicoId());
        LocalDateTime fecha = d.fecha() == null || d.fecha().isBlank() ? Tiempo.ahora() : Validar.fechaHora(d.fecha(), "fecha y hora");
        if (fecha.isAfter(Tiempo.ahora())) {
            throw new BadRequestException("La fecha de la anotación no puede ser futura.");
        }
        long id = db.insertar("""
                INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, fecha, descripcion, observacion, diagnostico)
                VALUES (:h, :m, :c, :f, :d, :o, :dx)
                """, Db.p("h", d.historiaId(), "m", medico, "c", historia.get("cita_id"), "f", fecha,
                "d", Validar.texto(d.descripcion(), "descripción", 4000),
                "o", Validar.opcional(d.observacion(), "observación", 4000),
                "dx", Validar.opcional(d.diagnostico(), "diagnóstico o evolución", 4000)));
        return anotacion(id);
    }

    /**
     * Actualiza una anotación existente (descripción, observación, diagnóstico y veterinario).
     *
     * @param id identificador de la anotación.
     * @param d  nuevos datos.
     * @return anotación actualizada.
     */
    @Transactional
    public Map<String, Object> actualizarAnotacion(long id, HistoriaApi.AnotacionRq d) {
        Map<String, Object> actual = db.requerido("SELECT * FROM anotacion_historia WHERE id = :id FOR UPDATE",
                Db.p("id", id), "La anotación no existe.");
        if (d == null) {
            throw new BadRequestException("Los datos de la anotación son obligatorios.");
        }
        if (d.historiaId() != null && d.historiaId() != Validar.largo(actual, "historia_id")) {
            throw new ConflictException("No se puede mover una anotación a otra historia clínica.");
        }
        long medico = medicoValido(d.medicoId() == null ? Validar.largo(actual, "medico_id") : d.medicoId());
        db.ejecutar("""
                UPDATE anotacion_historia SET medico_id = :m, descripcion = :d, observacion = :o, diagnostico = :dx,
                       fecha_modificacion = :ahora WHERE id = :id
                """, Db.p("m", medico, "d", Validar.texto(d.descripcion(), "descripción", 4000),
                "o", Validar.opcional(d.observacion(), "observación", 4000),
                "dx", Validar.opcional(d.diagnostico(), "diagnóstico o evolución", 4000), "ahora", Tiempo.ahora(), "id", id));
        return anotacion(id);
    }

    // ================================================================== FÓRMULAS MÉDICAS

    /**
     * Lista las fórmulas médicas ordenadas por fecha de creación, de la más reciente a la más antigua.
     *
     * @return fórmulas con datos resumidos de mascota, cliente, veterinario y medicamento.
     */
    public List<Map<String, Object>> listarFormulas() {
        return db.lista(FORMULA + " WHERE (:medico IS NULL OR f.medico_id = :medico) "
                + "ORDER BY f.fecha_creacion_registro DESC, f.id DESC", Db.p("medico", medicoRestringido()));
    }

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param id identificador.
     * @return fórmula; 404 si no existe.
     */
    public Map<String, Object> formula(long id) {
        return db.requerido(FORMULA + " WHERE f.id = :id", Db.p("id", id), "La fórmula no existe.");
    }

    /**
     * Registra una fórmula médica validando las reglas de negocio del segundo parcial.
     *
     * @param d datos de la fórmula.
     * @return fórmula creada.
     */
    @Transactional
    public Map<String, Object> crearFormula(HistoriaApi.FormulaRq d) {
        Datos v = validarFormula(d, null);
        long id = db.insertar("""
                INSERT INTO formula_medica (cita_id, mascota_id, medico_id, historia_id, medicamento_id, dosis, frecuencia,
                       duracion, indicaciones, fecha_emision, fecha_creacion_registro, activo)
                VALUES (:cita, :mascota, :medico, :historia, :med, :dosis, :frecuencia, :duracion, :ind, :emision, :ahora, 1)
                """, v.parametros());
        // La prescripción genera su orden de dispensación: llega a despacho, descuenta inventario al entregar y se factura.
        integracion.generarOrden(id);
        return formula(id);
    }

    /**
     * Actualiza una fórmula activa. La cita y la mascota no cambian; el resto se valida de nuevo.
     *
     * @param id identificador.
     * @param d  nuevos datos.
     * @return fórmula actualizada.
     */
    @Transactional
    public Map<String, Object> actualizarFormula(long id, HistoriaApi.FormulaRq d) {
        Map<String, Object> actual = db.requerido("SELECT * FROM formula_medica WHERE id = :id FOR UPDATE",
                Db.p("id", id), "La fórmula no existe.");
        if (!Validar.bool(actual, "activo")) {
            throw new ConflictException("La fórmula está inactiva y ya no puede modificarse.");
        }
        verificarPropiedad(Validar.largo(actual, "medico_id"));
        integracion.verificarModificable(actual);
        Datos v = validarFormula(d, actual);
        Map<String, Object> p = v.parametros();
        p.put("id", id);
        db.ejecutar("""
                UPDATE formula_medica SET medico_id = :medico, historia_id = :historia, medicamento_id = :med, dosis = :dosis,
                       frecuencia = :frecuencia, duracion = :duracion, indicaciones = :ind, fecha_emision = :emision,
                       fecha_actualizacion_registro = :ahora
                WHERE id = :id
                """, p);
        integracion.sincronizarEdicion(id, Validar.largo(actual, "medicamento_id"));
        return formula(id);
    }

    /**
     * Activa o inactiva una fórmula. No existe eliminación física porque la fórmula forma parte de la
     * información clínica de la mascota.
     *
     * @param id     identificador.
     * @param activo nuevo estado.
     * @return fórmula actualizada.
     */
    @Transactional
    public Map<String, Object> cambiarEstadoFormula(long id, Boolean activo) {
        Validar.requerido(activo, "activo");
        Map<String, Object> actual = db.requerido("SELECT * FROM formula_medica WHERE id = :id FOR UPDATE",
                Db.p("id", id), "La fórmula no existe.");
        verificarPropiedad(Validar.largo(actual, "medico_id"));
        integracion.verificarModificable(actual);
        db.ejecutar("UPDATE formula_medica SET activo = :a, fecha_actualizacion_registro = :ahora WHERE id = :id",
                Db.p("a", activo, "ahora", Tiempo.ahora(), "id", id));
        integracion.sincronizarEstado(id, activo);
        return formula(id);
    }

    // ================================================================== VALIDACIONES

    /** Datos de una fórmula ya validados. */
    private record Datos(long cita, int mascota, long medico, Long historia, long medicamento, String dosis,
                         String frecuencia, String duracion, String indicaciones, LocalDate emision) {

        /** @return parámetros para las sentencias SQL. */
        Map<String, Object> parametros() {
            return Db.p("cita", cita, "mascota", mascota, "medico", medico, "historia", historia, "med", medicamento,
                    "dosis", dosis, "frecuencia", frecuencia, "duracion", duracion, "ind", indicaciones,
                    "emision", emision, "ahora", Tiempo.ahora());
        }
    }

    /**
     * Valida una fórmula: mascota existente de un cliente registrado, cita de esa misma mascota, veterinario
     * existente, medicamento activo, fecha de emisión no anterior a la consulta y dosis/duración válidas.
     *
     * @param d      datos recibidos.
     * @param actual fórmula existente (edición) o null (creación).
     * @return datos normalizados.
     */
    private Datos validarFormula(HistoriaApi.FormulaRq d, Map<String, Object> actual) {
        if (d == null) {
            throw new BadRequestException("Los datos de la fórmula son obligatorios.");
        }
        long citaId = actual != null ? Validar.largo(actual, "cita_id") : requeridoLargo(d.citaId(), "cita");
        if (actual != null && d.citaId() != null && d.citaId() != citaId) {
            throw new ConflictException("No se puede cambiar la cita de una fórmula registrada.");
        }
        Map<String, Object> cita = db.requerido("SELECT * FROM cita WHERE id = :id", Db.p("id", citaId), "La cita no existe.");
        int mascotaCita = Validar.numero(cita, "mascota_id");
        Integer mascotaId = d.mascotaId() == null ? Integer.valueOf(mascotaCita) : d.mascotaId();
        Map<String, Object> mascota = db.requerido("SELECT mascota_id, cliente_id, activo FROM mascota WHERE mascota_id = :m",
                Db.p("m", mascotaId), "La mascota no existe.");
        if (d.clienteId() != null && d.clienteId() != Validar.largo(mascota, "cliente_id")) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado.");
        }
        db.requerido("SELECT cliente_id FROM cliente WHERE cliente_id = :c", Db.p("c", mascota.get("cliente_id")),
                "La mascota no pertenece a un cliente registrado.");
        if (mascotaCita != mascotaId) {
            throw new BadRequestException("La fórmula no puede asociarse a una consulta de otra mascota.");
        }
        if ("cancelada".equals(cita.get("estado"))) {
            throw new ConflictException("No se puede formular sobre una cita cancelada.");
        }
        long medico = medicoValido(d.medicoId() == null ? Validar.largo(cita, "medico_id") : d.medicoId());
        Validar.requerido(d.medicamentoId(), "medicamento");
        Map<String, Object> med = db.requerido("SELECT activo FROM medicamento WHERE id = :id", Db.p("id", d.medicamentoId()),
                "El medicamento no existe.");
        if (!Validar.bool(med, "activo")) {
            throw new BadRequestException("El medicamento está inactivo.");
        }
        LocalDate emision = Validar.fecha(d.fechaEmision(), "fecha de emisión", true);
        LocalDate fechaConsulta = ((LocalDateTime) cita.get("fecha_hora")).toLocalDate();
        if (emision.isBefore(fechaConsulta)) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la consulta (" + fechaConsulta + ").");
        }
        if (emision.isAfter(Tiempo.hoy())) {
            throw new BadRequestException("La fecha de emisión no puede ser futura.");
        }
        String dosis = Validar.texto(d.dosis(), "dosis", 255);
        String duracion = Validar.texto(d.duracionTratamiento(), "duración del tratamiento", 100);
        if (!contieneCantidadPositiva(dosis)) {
            throw new BadRequestException("La dosis debe indicar una cantidad válida mayor que cero (por ejemplo: 1 tableta, 5 ml).");
        }
        if (!contieneCantidadPositiva(duracion)) {
            throw new BadRequestException("La duración debe indicar un número válido mayor que cero (por ejemplo: 7 días).");
        }
        Long historia = db.valor("SELECT id FROM historia_medica WHERE cita_id = :c", Db.p("c", citaId), Long.class);
        return new Datos(citaId, mascotaId, medico, historia, d.medicamentoId(), dosis,
                Validar.texto(d.frecuencia(), "frecuencia", 100), duracion,
                Validar.opcional(d.indicaciones(), "indicaciones", 2000), emision);
    }

    /**
     * Verifica que la mascota exista, esté activa y pertenezca al cliente indicado.
     *
     * @param clienteId cliente.
     * @param mascotaId mascota.
     * @return fila de la mascota.
     */
    private Map<String, Object> validarClienteMascota(Long clienteId, Integer mascotaId) {
        db.requerido("SELECT cliente_id FROM cliente WHERE cliente_id = :c", Db.p("c", clienteId), "El cliente no existe.");
        Map<String, Object> mascota = db.requerido("SELECT mascota_id, cliente_id FROM mascota WHERE mascota_id = :m",
                Db.p("m", mascotaId), "La mascota no existe.");
        if (Validar.largo(mascota, "cliente_id") != clienteId) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado.");
        }
        return mascota;
    }

    /**
     * Verifica que el veterinario exista. Si el usuario es MEDICO, solo puede registrar a su propio nombre.
     *
     * @param medicoId veterinario solicitado.
     * @return veterinario válido.
     */
    private long medicoValido(Long medicoId) {
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.VETERINARIO)) {
            long propio = SesionActual.medicoId();
            if (medicoId != null && medicoId != propio) {
                throw new ForbiddenException("Solo puedes registrar información clínica a tu nombre.");
            }
            return propio;
        }
        if (medicoId == null) {
            throw new BadRequestException("El campo veterinario es obligatorio.");
        }
        db.requerido("SELECT id FROM medico WHERE id = :id", Db.p("id", medicoId), "El veterinario no existe.");
        return medicoId;
    }

    /**
     * El médico solo puede cambiar el estado de fórmulas propias.
     *
     * @param medicoFormula veterinario de la fórmula.
     */
    private void verificarPropiedad(long medicoFormula) {
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.VETERINARIO) && medicoFormula != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo puedes modificar tus propias fórmulas.");
        }
    }

    /** @return el médico del usuario si su perfil es MEDICO (para filtrar sus registros), o null. */
    private Long medicoRestringido() {
        SesionUsuario s = SesionActual.obtener();
        return s.es(Perfiles.VETERINARIO) ? SesionActual.medicoId() : null;
    }

    /**
     * @param valor texto que debe contener un número mayor que cero.
     * @return true si contiene una cantidad positiva (admite decimales con punto o coma).
     */
    static boolean contieneCantidadPositiva(String valor) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(valor);
        while (m.find()) {
            if (Double.parseDouble(m.group(1).replace(',', '.')) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param valor identificador recibido.
     * @param campo nombre del campo para el mensaje de error.
     * @return el valor si no es nulo.
     */
    private static long requeridoLargo(Long valor, String campo) {
        Validar.requerido(valor, campo);
        return valor;
    }
}
