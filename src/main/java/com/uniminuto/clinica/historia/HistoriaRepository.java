package com.uniminuto.clinica.historia;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link HistoriaService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class HistoriaRepository {

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

    /** Consulta base de historias con los datos de mascota, cliente, veterinario y cita. */
    static final String HISTORIA = """
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

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public HistoriaRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link HistoriaService} (listarHistorias): WHERE (:desde IS NULL OR h.fecha >= :desde) AND (:hasta IS NULL OR h.fecha < :hasta) AND (:mascota IS NULL OR …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAnotacionHistoria(Map<String, ?> p) {
        return db.lista(HISTORIA + """
                WHERE (:desde IS NULL OR h.fecha >= :desde) AND (:hasta IS NULL OR h.fecha < :hasta)
                  AND (:mascota IS NULL OR h.paciente_id = :mascota) AND (:cliente IS NULL OR m.cliente_id = :cliente)
                  AND (:medico IS NULL OR h.medico_id = :medico)
                ORDER BY h.fecha DESC, h.id DESC
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (detalleHistoria): WHERE h.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerAnotacionHistoria(Map<String, ?> p, String mensaje) {
        return db.requerido(HISTORIA + " WHERE h.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (detalleHistoria): WHERE f.historia_id = :id ORDER BY f.fecha_creacion_registro DESC
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.lista(FORMULA + " WHERE f.historia_id = :id ORDER BY f.fecha_creacion_registro DESC", p);
    }

    /**
     * Usada por {@link HistoriaService} (crearHistoria): SELECT * FROM cita WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCita(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM cita WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (crearHistoria): SELECT 1 FROM historia_medica WHERE cita_id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeHistoriaMedica(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM historia_medica WHERE cita_id = :c", p);
    }

    /**
     * Usada por {@link HistoriaService} (crearHistoria): INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, diagnostico, observaciones, activ…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarHistoriaMedica(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, diagnostico, observaciones,
                                             activo, fecha_creacion)
                VALUES (:m, :c, :medico, :fecha, :motivo, :dx, :obs, 1, :ahora)
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (crearHistoria): UPDATE formula_medica SET historia_id = :h WHERE cita_id = :c AND historia_id IS NULL
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedica(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_medica SET historia_id = :h WHERE cita_id = :c AND historia_id IS NULL", p);
    }

    /**
     * Usada por {@link HistoriaService} (actualizarHistoria): SELECT * FROM historia_medica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerHistoriaMedica(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM historia_medica WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (actualizarHistoria): UPDATE historia_medica SET medico_id = :medico, fecha = :fecha, motivo = :motivo, diagnostico = :dx, observaci…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarHistoriaMedica(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE historia_medica SET medico_id = :medico, fecha = :fecha, motivo = :motivo, diagnostico = :dx,
                       observaciones = :obs, activo = :activo, fecha_modificacion = :ahora
                WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (listarAnotaciones): WHERE (:desde IS NULL OR a.fecha >= :desde) AND (:hasta IS NULL OR a.fecha < :hasta) AND (:historia IS NULL OR…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAnotacionHistoriaEnListarAnotaciones(Map<String, ?> p) {
        return db.lista(ANOTACION + """
                WHERE (:desde IS NULL OR a.fecha >= :desde) AND (:hasta IS NULL OR a.fecha < :hasta)
                  AND (:historia IS NULL OR a.historia_id = :historia)
                ORDER BY a.fecha DESC, a.id DESC
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (anotacionesDeHistoria): SELECT id FROM historia_medica WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerHistoriaMedicaEnAnotacionesDeHistoria(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM historia_medica WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (anotacionesDeHistoria): WHERE a.historia_id = :h ORDER BY a.fecha DESC, a.id DESC
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAnotacionHistoriaEnAnotacionesDeHistoria(Map<String, ?> p) {
        return db.lista(ANOTACION + " WHERE a.historia_id = :h ORDER BY a.fecha DESC, a.id DESC", p);
    }

    /**
     * Usada por {@link HistoriaService} (anotacion): WHERE a.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerAnotacionHistoriaEnAnotacion(Map<String, ?> p, String mensaje) {
        return db.requerido(ANOTACION + " WHERE a.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (crearAnotacion): SELECT * FROM historia_medica WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerHistoriaMedicaEnCrearAnotacion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM historia_medica WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (crearAnotacion): INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, fecha, descripcion, observacion, diagnostico)…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarAnotacionHistoria(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, fecha, descripcion, observacion, diagnostico)
                VALUES (:h, :m, :c, :f, :d, :o, :dx)
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (actualizarAnotacion): SELECT * FROM anotacion_historia WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerAnotacionHistoriaEnActualizarAnotacion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM anotacion_historia WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (actualizarAnotacion): UPDATE anotacion_historia SET medico_id = :m, descripcion = :d, observacion = :o, diagnostico = :dx, fecha_mod…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarAnotacionHistoria(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE anotacion_historia SET medico_id = :m, descripcion = :d, observacion = :o, diagnostico = :dx,
                       fecha_modificacion = :ahora WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (listarFormulas): WHERE (:medico IS NULL OR f.medico_id = :medico)
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinicaDetalleEnListarFormulas(Map<String, ?> p) {
        return db.lista(FORMULA + " WHERE (:medico IS NULL OR f.medico_id = :medico) "
                + "ORDER BY f.fecha_creacion_registro DESC, f.id DESC", p);
    }

    /**
     * Usada por {@link HistoriaService} (formula): WHERE f.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinicaDetalle(Map<String, ?> p, String mensaje) {
        return db.requerido(FORMULA + " WHERE f.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (crearFormula): INSERT INTO formula_medica (cita_id, mascota_id, medico_id, historia_id, medicamento_id, dosis, frecuencia, du…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFormulaMedica(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO formula_medica (cita_id, mascota_id, medico_id, historia_id, medicamento_id, dosis, frecuencia,
                       duracion, indicaciones, fecha_emision, fecha_creacion_registro, activo)
                VALUES (:cita, :mascota, :medico, :historia, :med, :dosis, :frecuencia, :duracion, :ind, :emision, :ahora, 1)
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (actualizarFormula): SELECT * FROM formula_medica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaMedica(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM formula_medica WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (actualizarFormula): UPDATE formula_medica SET medico_id = :medico, historia_id = :historia, medicamento_id = :med, dosis = :dosis,…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedicaEnActualizarFormula(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE formula_medica SET medico_id = :medico, historia_id = :historia, medicamento_id = :med, dosis = :dosis,
                       frecuencia = :frecuencia, duracion = :duracion, indicaciones = :ind, fecha_emision = :emision,
                       fecha_actualizacion_registro = :ahora
                WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link HistoriaService} (cambiarEstadoFormula): UPDATE formula_medica SET activo = :a, fecha_actualizacion_registro = :ahora WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedicaEnCambiarEstadoFormula(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_medica SET activo = :a, fecha_actualizacion_registro = :ahora WHERE id = :id", p);
    }

    /**
     * Usada por {@link HistoriaService} (validarFormula): SELECT * FROM cita WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCitaEnValidarFormula(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM cita WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (validarFormula): SELECT mascota_id, cliente_id, activo FROM mascota WHERE mascota_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMascota(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT mascota_id, cliente_id, activo FROM mascota WHERE mascota_id = :m", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (validarFormula): SELECT cliente_id FROM cliente WHERE cliente_id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCliente(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT cliente_id FROM cliente WHERE cliente_id = :c", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (validarFormula): SELECT activo FROM medicamento WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamento(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT activo FROM medicamento WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (validarFormula): SELECT id FROM historia_medica WHERE cita_id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @param tipo clase del valor esperado.
     * @param <T> tipo del valor.
     * @return resultado.
     */
    public <T> T valorHistoriaMedica(Map<String, ?> p, Class<T> tipo) {
        return db.valor("SELECT id FROM historia_medica WHERE cita_id = :c", p, tipo);
    }

    /**
     * Usada por {@link HistoriaService} (validarClienteMascota): SELECT mascota_id, cliente_id FROM mascota WHERE mascota_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMascotaEnValidarClienteMascota(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT mascota_id, cliente_id FROM mascota WHERE mascota_id = :m", p, mensaje);
    }

    /**
     * Usada por {@link HistoriaService} (medicoValido): SELECT id FROM medico WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedico(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM medico WHERE id = :id", p, mensaje);
    }
}
