package com.uniminuto.clinica.cita;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link CitaService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class CitaRepository {

    /** Columnas de una cita con los nombres de cliente, mascota y veterinario para el DataTable. */
    private static final String CITA_DETALLE = """
            SELECT c.id, c.cliente_id AS clienteId, c.mascota_id AS mascotaId, c.medico_id AS medicoId,
                   c.fecha_hora AS fechaHora, c.fecha_fin AS fechaFin, c.estado, c.motivo,
                   CONCAT(cl.nombres, ' ', cl.apellidos) AS cliente, m.nombre_mascota AS mascota,
                   CONCAT(md.nombres, ' ', md.apellidos) AS veterinario, h.id AS historiaId
            FROM cita c
            JOIN cliente cl ON cl.cliente_id = c.cliente_id
            JOIN mascota m ON m.mascota_id = c.mascota_id
            JOIN medico md ON md.id = c.medico_id
            LEFT JOIN historia_medica h ON h.cita_id = c.id
            """;

    /** Consulta base de las citas para la agenda (calendario) del médico. */
    private static final String EVENTO = """
            SELECT c.id, c.estado, c.fecha_hora AS inicio, c.fecha_fin AS fin, c.motivo,
                   CONCAT(md.nombres, ' ', md.apellidos) AS medico, m.nombre_mascota AS mascota,
                   c.mascota_id AS mascotaId, c.cliente_id AS clienteId, c.medico_id AS medicoId,
                   CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario,
                   cl.numero_documento AS documento, cl.telefono, r.especie, r.nombre AS raza, m.edad
            FROM cita c
            JOIN medico md ON md.id = c.medico_id
            JOIN mascota m ON m.mascota_id = c.mascota_id
            JOIN raza r ON r.raza_id = m.raza_id
            JOIN cliente cl ON cl.cliente_id = c.cliente_id
            """;

    /** Estados que ocupan la agenda (bloquean el horario). */
    static final String OCUPAN = "'pendiente', 'programada', 'confirmada', 'atendida'";

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public CitaRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link CitaService} (listarRango): WHERE (:medico IS NULL OR c.medico_id = :medico) AND (:desde IS NULL OR c.fecha_hora >= :desde) AND (:hasta IS…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCita(Map<String, ?> p) {
        return db.lista(CITA_DETALLE + """
                WHERE (:medico IS NULL OR c.medico_id = :medico)
                  AND (:desde IS NULL OR c.fecha_hora >= :desde)
                  AND (:hasta IS NULL OR c.fecha_hora < :hasta)
                ORDER BY c.fecha_hora DESC, c.id DESC
                """, p);
    }

    /**
     * Usada por {@link CitaService} (porMascota): SELECT mascota_id FROM mascota WHERE mascota_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMascota(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT mascota_id FROM mascota WHERE mascota_id = :m", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (porMascota): WHERE c.mascota_id = :m AND (:medico IS NULL OR c.medico_id = :medico) ORDER BY c.fecha_hora DESC
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaEnPorMascota(Map<String, ?> p) {
        return db.lista(CITA_DETALLE + """
                WHERE c.mascota_id = :m AND (:medico IS NULL OR c.medico_id = :medico)
                ORDER BY c.fecha_hora DESC
                """, p);
    }

    /**
     * Usada por {@link CitaService} (detalle): WHERE c.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCita(Map<String, ?> p, String mensaje) {
        return db.requerido(CITA_DETALLE + " WHERE c.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (reactivar): UPDATE cita SET estado = 'pendiente', fecha_hora = :i, fecha_fin = :f, fecha_modificacion = :ahora WHERE id = …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarCita(Map<String, ?> p) {
        return db.ejecutar("UPDATE cita SET estado = 'pendiente', fecha_hora = :i, fecha_fin = :f, fecha_modificacion = :ahora WHERE id = :id", p);
    }

    /**
     * Usada por {@link CitaService} (consultar): SELECT id, cliente_id AS clienteId, mascota_id AS mascotaId, medico_id AS medicoId, fecha_hora AS fechaHora, f…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCitaEnConsultar(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT id, cliente_id AS clienteId, mascota_id AS mascotaId, medico_id AS medicoId,
                       fecha_hora AS fechaHora, fecha_fin AS fechaFin, estado, motivo
                FROM cita WHERE id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (crear): INSERT INTO cita (cliente_id, mascota_id, medico_id, fecha_hora, fecha_fin, estado, motivo, usuario_creacion, …
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarCita(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO cita (cliente_id, mascota_id, medico_id, fecha_hora, fecha_fin, estado, motivo,
                                  usuario_creacion, fecha_creacion)
                VALUES (:cliente, :mascota, :medico, :inicio, :fin, :estado, :motivo, :usuario, :ahora)
                """, p);
    }

    /**
     * Usada por {@link CitaService} (actualizar): UPDATE cita SET cliente_id = :cliente, mascota_id = :mascota, medico_id = :medico, fecha_hora = :inicio, fecha…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarCitaEnActualizar(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE cita SET cliente_id = :cliente, mascota_id = :mascota, medico_id = :medico, fecha_hora = :inicio,
                       fecha_fin = :fin, estado = :estado, motivo = :motivo, fecha_modificacion = :ahora
                WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link CitaService} (eventos): WHERE c.medico_id = :medico AND c.fecha_hora >= :desde AND c.fecha_hora < :hasta AND (:estado IS NULL OR c.est…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaEnEventos(Map<String, ?> p) {
        return db.lista(EVENTO + """
                WHERE c.medico_id = :medico AND c.fecha_hora >= :desde AND c.fecha_hora < :hasta
                  AND (:estado IS NULL OR c.estado = :estado)
                ORDER BY c.fecha_hora
                """, p);
    }

    /**
     * Usada por {@link CitaService} (excel): SELECT CONCAT(nombres, ' ', apellidos) AS nombre FROM medico WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarMedico(Map<String, ?> p) {
        return db.uno("SELECT CONCAT(nombres, ' ', apellidos) AS nombre FROM medico WHERE id = :id", p);
    }

    /**
     * Usada por {@link CitaService} (disponibilidad): SELECT activo FROM medico WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedico(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT activo FROM medico WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (disponibilidad): SELECT fecha_hora - INTERVAL 15 MINUTE AS i, fecha_fin + INTERVAL 15 MINUTE AS f FROM cita
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaEnDisponibilidad(Map<String, ?> p) {
        return db.lista("SELECT fecha_hora - INTERVAL 15 MINUTE AS i, fecha_fin + INTERVAL 15 MINUTE AS f FROM cita "
                    + "WHERE medico_id = :m AND estado IN (" + OCUPAN + ") AND fecha_fin > :d AND fecha_hora < :h "
                    + "UNION ALL SELECT inicio, fin FROM medico_ausencia WHERE medico_id = :m AND fin > :d AND inicio < :h", p);
    }

    /**
     * Usada por {@link CitaService} (configuracion): SELECT dia_semana AS dia, hora_inicio AS inicio, hora_fin AS fin FROM medico_horario WHERE medico_id = :m ORDE…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicoHorario(Map<String, ?> p) {
        return db.lista("""
                SELECT dia_semana AS dia, hora_inicio AS inicio, hora_fin AS fin
                FROM medico_horario WHERE medico_id = :m ORDER BY dia_semana, hora_inicio
                """, p);
    }

    /**
     * Usada por {@link CitaService} (configuracion): SELECT id, inicio, fin, motivo FROM medico_ausencia WHERE medico_id = :m AND fin >= :ahora ORDER BY inicio
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicoAusencia(Map<String, ?> p) {
        return db.lista("""
                SELECT id, inicio, fin, motivo FROM medico_ausencia
                WHERE medico_id = :m AND fin >= :ahora ORDER BY inicio
                """, p);
    }

    /**
     * Usada por {@link CitaService} (guardarHorarios): SELECT id FROM medico WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicoEnGuardarHorarios(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM medico WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (guardarHorarios): DELETE FROM medico_horario WHERE medico_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int eliminarMedicoHorario(Map<String, ?> p) {
        return db.ejecutar("DELETE FROM medico_horario WHERE medico_id = :m", p);
    }

    /**
     * Usada por {@link CitaService} (guardarHorarios): INSERT INTO medico_horario (medico_id, dia_semana, hora_inicio, hora_fin) VALUES (:m, :d, :i, :f)
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarMedicoHorario(Map<String, ?> p) {
        return db.insertar("INSERT INTO medico_horario (medico_id, dia_semana, hora_inicio, hora_fin) VALUES (:m, :d, :i, :f)", p);
    }

    /**
     * Usada por {@link CitaService} (registrarAusencia): SELECT id FROM medico WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicoEnRegistrarAusencia(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM medico WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (registrarAusencia): SELECT COUNT(*) FROM cita WHERE medico_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarCita(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM cita WHERE medico_id = :m "
                + "AND estado IN ('pendiente', 'programada', 'confirmada') "
                + "AND fecha_hora < :f AND fecha_fin > :i", p);
    }

    /**
     * Usada por {@link CitaService} (registrarAusencia): SELECT 1 FROM medico_ausencia WHERE medico_id = :m AND inicio < :f AND fin > :i
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeMedicoAusencia(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM medico_ausencia WHERE medico_id = :m AND inicio < :f AND fin > :i", p);
    }

    /**
     * Usada por {@link CitaService} (registrarAusencia): INSERT INTO medico_ausencia (medico_id, inicio, fin, motivo, usuario_id, fecha_registro) VALUES (:m, :i, :f, :…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarMedicoAusencia(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO medico_ausencia (medico_id, inicio, fin, motivo, usuario_id, fecha_registro)
                VALUES (:m, :i, :f, :motivo, :u, :ahora)
                """, p);
    }

    /**
     * Usada por {@link CitaService} (eliminarAusencia): SELECT fin FROM medico_ausencia WHERE id = :id AND medico_id = :m FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicoAusencia(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT fin FROM medico_ausencia WHERE id = :id AND medico_id = :m FOR UPDATE
                """, p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (eliminarAusencia): DELETE FROM medico_ausencia WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int eliminarMedicoAusencia(Map<String, ?> p) {
        return db.ejecutar("DELETE FROM medico_ausencia WHERE id = :id", p);
    }

    /**
     * Usada por {@link CitaService} (ausenciasEnRango): SELECT id, inicio, fin, motivo FROM medico_ausencia WHERE medico_id = :m AND inicio < :h AND fin > :d ORDER BY…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicoAusenciaEnAusenciasEnRango(Map<String, ?> p) {
        return db.lista("""
                SELECT id, inicio, fin, motivo FROM medico_ausencia
                WHERE medico_id = :m AND inicio < :h AND fin > :d ORDER BY inicio
                """, p);
    }

    /**
     * Usada por {@link CitaService} (actividad): SELECT id FROM medico WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicoEnActividad(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM medico WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (actividad): UPDATE medico SET activo = :a WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarMedico(Map<String, ?> p) {
        return db.ejecutar("UPDATE medico SET activo = :a WHERE id = :id", p);
    }

    /**
     * Usada por {@link CitaService} (cambiar): UPDATE cita SET estado = :e, fecha_hora = :i, fecha_fin = :f, fecha_modificacion = :ahora WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarCitaEnCambiar(Map<String, ?> p) {
        return db.ejecutar("UPDATE cita SET estado = :e, fecha_hora = :i, fecha_fin = :f, fecha_modificacion = :ahora WHERE id = :id", p);
    }

    /**
     * Usada por {@link CitaService} (cambiar): WHERE c.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarCita(Map<String, ?> p) {
        return db.uno(EVENTO + " WHERE c.id = :id", p);
    }

    /**
     * Usada por {@link CitaService} (historial): SELECT id, medico_id FROM cita WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCitaEnHistorial(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id, medico_id FROM cita WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (historial): SELECT id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior, estado_nuevo, inicio_nuevo, fin_…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaRevision(Map<String, ?> p) {
        return db.lista("""
                SELECT id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior,
                       estado_nuevo, inicio_nuevo, fin_nuevo
                FROM cita_revision WHERE cita_id = :id ORDER BY fecha DESC, id DESC
                """, p);
    }

    /**
     * Usada por {@link CitaService} (historialMascota): SELECT c.id, c.fecha_hora AS inicio, c.fecha_fin AS fin, CONCAT(md.nombres, ' ', md.apellidos) AS medico, c.es…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaEnHistorialMascota(Map<String, ?> p) {
        return db.lista("""
                SELECT c.id, c.fecha_hora AS inicio, c.fecha_fin AS fin, CONCAT(md.nombres, ' ', md.apellidos) AS medico,
                       c.estado, a.id AS atencionId
                FROM cita c JOIN medico md ON md.id = c.medico_id
                LEFT JOIN atencion_medica a ON a.cita_id = c.id
                WHERE c.mascota_id = :m
                ORDER BY c.fecha_hora DESC LIMIT :limite OFFSET :desde
                """, p);
    }

    /**
     * Usada por {@link CitaService} (validarProgramacion): SELECT activo FROM medico WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicoEnValidarProgramacion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT activo FROM medico WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (validarProgramacion): SELECT mascota_id FROM mascota WHERE mascota_id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMascotaEnValidarProgramacion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT mascota_id FROM mascota WHERE mascota_id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (validarProgramacion): SELECT 1 FROM cita WHERE medico_id = :m AND estado IN (
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeCita(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM cita WHERE medico_id = :m AND estado IN (" + OCUPAN + ") "
                + "AND fecha_hora < :f AND fecha_fin > :i AND (:excluir IS NULL OR id <> :excluir)", p);
    }

    /**
     * Usada por {@link CitaService} (validarProgramacion): SELECT 1 FROM cita WHERE mascota_id = :m AND estado IN (
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeCitaEnValidarProgramacion(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM cita WHERE mascota_id = :m AND estado IN (" + OCUPAN + ") "
                + "AND fecha_hora < :f AND fecha_fin > :i AND (:excluir IS NULL OR id <> :excluir)", p);
    }

    /**
     * Usada por {@link CitaService} (bloquear): SELECT * FROM cita WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCitaEnBloquear(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM cita WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (franjas): SELECT hora_inicio AS inicio, hora_fin AS fin FROM medico_horario WHERE medico_id = :m AND dia_semana = :d ORD…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicoHorarioEnFranjas(Map<String, ?> p) {
        return db.lista("""
                SELECT hora_inicio AS inicio, hora_fin AS fin FROM medico_horario
                WHERE medico_id = :m AND dia_semana = :d ORDER BY hora_inicio
                """, p);
    }

    /**
     * Usada por {@link CitaService} (registrarRevision): INSERT INTO cita_revision (cita_id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior, estado_…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarCitaRevision(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO cita_revision (cita_id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior,
                                           estado_nuevo, inicio_nuevo, fin_nuevo)
                VALUES (:cita, :usuario, :ahora, :ea, :ia, :fa, :en, :in, :fn)
                """, p);
    }

    /**
     * Usada por {@link CitaService} (validarDatos): SELECT activo FROM cliente WHERE cliente_id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCliente(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT activo FROM cliente WHERE cliente_id = :c", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (validarDatos): SELECT cliente_id, activo FROM mascota WHERE mascota_id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMascotaEnValidarDatos(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT cliente_id, activo FROM mascota WHERE mascota_id = :m", p, mensaje);
    }

    /**
     * Usada por {@link CitaService} (validarDatos): SELECT id FROM medico WHERE id = :m
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicoEnValidarDatos(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM medico WHERE id = :m", p, mensaje);
    }
}
