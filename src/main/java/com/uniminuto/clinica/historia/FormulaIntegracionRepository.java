package com.uniminuto.clinica.historia;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link FormulaIntegracionService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class FormulaIntegracionRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public FormulaIntegracionRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link FormulaIntegracionService} (generarOrden): SELECT * FROM formula_medica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaMedica(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM formula_medica WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (generarOrden): INSERT INTO formula_clinica (cita_id, atencion_id, medico_id, mascota_id, estado, estado_despacho, revision, f…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFormulaClinica(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO formula_clinica (cita_id, atencion_id, medico_id, mascota_id, estado, estado_despacho, revision,
                                             fecha_creacion, fecha_confirmacion)
                VALUES (:c, :a, :m, :pet, 'CONFIRMADA', 'PENDIENTE_DESPACHO', 1, :ahora, :ahora)
                """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (generarOrden): INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo) V…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoEstadoHistorial(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, 'BORRADOR', 'PENDIENTE_DESPACHO', :u, :ahora, 'Prescripción registrada en la historia clínica')
                """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (generarOrden): UPDATE formula_medica SET formula_clinica_id = :o WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedica(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_medica SET formula_clinica_id = :o WHERE id = :id", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (generarPendientesDeCita): SELECT id FROM formula_medica WHERE cita_id = :c AND activo = 1 AND formula_clinica_id IS NULL
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaMedica(Map<String, ?> p) {
        return db.lista("""
                SELECT id FROM formula_medica WHERE cita_id = :c AND activo = 1 AND formula_clinica_id IS NULL
                """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (registrarEnHistoria): SELECT f.*, a.historia_id FROM formula_clinica f JOIN atencion_medica a ON a.id = f.atencion_id WHERE f.id = :…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinica(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT f.*, a.historia_id FROM formula_clinica f JOIN atencion_medica a ON a.id = f.atencion_id WHERE f.id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (registrarEnHistoria): SELECT * FROM formula_clinica_detalle WHERE formula_id = :f
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.lista("SELECT * FROM formula_clinica_detalle WHERE formula_id = :f", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (registrarEnHistoria): INSERT INTO formula_medica (cita_id, mascota_id, medico_id, historia_id, medicamento_id, dosis, frecuencia, du…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFormulaMedica(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO formula_medica (cita_id, mascota_id, medico_id, historia_id, medicamento_id, dosis, frecuencia,
                           duracion, indicaciones, fecha_emision, fecha_creacion_registro, activo, formula_clinica_id)
                    VALUES (:c, :m, :medico, :h, :med, :dosis, :frec, :dur, :ind, :emision, :ahora, 1, :o)
                    """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEdicion): SELECT * FROM formula_medica WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaMedicaEnSincronizarEdicion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM formula_medica WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEdicion): DELETE FROM formula_clinica_detalle WHERE formula_id = :o AND medicamento_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int eliminarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.ejecutar("DELETE FROM formula_clinica_detalle WHERE formula_id = :o AND medicamento_id = :m", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEdicion): UPDATE formula_clinica SET revision = revision + 1 WHERE id = :o
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaClinica(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_clinica SET revision = revision + 1 WHERE id = :o", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEstado): SELECT * FROM formula_medica WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaMedicaEnSincronizarEstado(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM formula_medica WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEstado): UPDATE formula_medica SET formula_clinica_id = NULL WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedicaEnSincronizarEstado(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_medica SET formula_clinica_id = NULL WHERE id = :id", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEstado): SELECT COUNT(*) FROM formula_clinica_detalle WHERE formula_id = :o
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM formula_clinica_detalle WHERE formula_id = :o", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEstado): UPDATE formula_clinica SET estado = 'ANULADA', estado_despacho = 'CANCELADO', revision = revision + 1 WHERE id…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaClinicaEnSincronizarEstado(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_clinica SET estado = 'ANULADA', estado_despacho = 'CANCELADO', revision = revision + 1 WHERE id = :o", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (sincronizarEstado): INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo) V…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoEstadoHistorialEnSincronizarEstado(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, 'PENDIENTE_DESPACHO', 'CANCELADO', :u, :ahora, 'Prescripción inactivada en la historia clínica')
                """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (inactivarPorOrden): UPDATE formula_medica SET activo = 0, fecha_actualizacion_registro = :ahora WHERE formula_clinica_id = :o
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedicaEnInactivarPorOrden(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_medica SET activo = 0, fecha_actualizacion_registro = :ahora WHERE formula_clinica_id = :o", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (atencionDisponible): SELECT id FROM atencion_medica WHERE cita_id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @param tipo clase del valor esperado.
     * @param <T> tipo del valor.
     * @return resultado.
     */
    public <T> T valorAtencionMedica(Map<String, ?> p, Class<T> tipo) {
        return db.valor("SELECT id FROM atencion_medica WHERE cita_id = :c", p, tipo);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (atencionDisponible): SELECT c.*, md.especializacion_id FROM cita c JOIN medico md ON md.id = c.medico_id WHERE c.id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCita(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT c.*, md.especializacion_id FROM cita c JOIN medico md ON md.id = c.medico_id WHERE c.id = :c
                """, p, mensaje);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (atencionDisponible): SELECT id FROM historia_medica WHERE cita_id = :c
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
     * Usada por {@link FormulaIntegracionService} (atencionDisponible): INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, activo, fecha_creacion) VALUES (:…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarHistoriaMedica(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, activo, fecha_creacion)
                    VALUES (:m, :c, :medico, :fecha, :motivo, 1, :ahora)
                    """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (atencionDisponible): UPDATE formula_medica SET historia_id = :h WHERE cita_id = :c AND historia_id IS NULL
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaMedicaEnAtencionDisponible(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_medica SET historia_id = :h WHERE cita_id = :c AND historia_id IS NULL", p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (atencionDisponible): INSERT INTO atencion_medica (cita_id, historia_id, medico_id, especializacion_id, motivo, version, fecha_apert…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarAtencionMedica(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO atencion_medica (cita_id, historia_id, medico_id, especializacion_id, motivo, version,
                                             fecha_apertura, fecha_finalizacion)
                VALUES (:c, :h, :m, :e, :motivo, 0, :ahora, :ahora)
                """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (verificarExistencias): SELECT nombre, COALESCE((SELECT SUM(l.cantidad) FROM medicamento_lote l WHERE l.medicamento_id = m.id AND (l.f…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamentoLote(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT nombre, COALESCE((SELECT SUM(l.cantidad) FROM medicamento_lote l WHERE l.medicamento_id = m.id
                       AND (l.fecha_vence IS NULL OR l.fecha_vence >= :hoy)), 0) AS disponible
                FROM medicamento m WHERE m.id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (insertarLinea): INSERT INTO formula_clinica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion, cantidad, via, i…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO formula_clinica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion, cantidad, via, indicaciones)
                VALUES (:o, :m, :dosis, :frec, :dur, :cant, :via, :ind)
                """, p);
    }

    /**
     * Usada por {@link FormulaIntegracionService} (orden): SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarFormulaClinica(Map<String, ?> p) {
        return db.uno("SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE", p);
    }
}
