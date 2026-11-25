package com.uniminuto.clinica.clinica;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link TarifarioService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class TarifarioRepository {

    /** Consulta base de especialidades con su número de servicios. */
    private static final String ESPECIALIDAD = """
            SELECT id, codigo_especializacion AS codigo, nombre, descripcion, activo FROM especializacion
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public TarifarioRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link TarifarioService} (especialidades): ORDER BY nombre
     *
     * @return filas.
     */
    public List<Map<String, Object>> listarEspecializacion() {
        return db.lista(ESPECIALIDAD + " ORDER BY nombre");
    }

    /**
     * Usada por {@link TarifarioService} (guardarEspecialidad): SELECT 1 FROM especializacion WHERE (codigo_especializacion = :c OR nombre = :n)
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeEspecializacion(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM especializacion WHERE (codigo_especializacion = :c OR nombre = :n) "
                + "AND (:id IS NULL OR id <> :id)", p);
    }

    /**
     * Usada por {@link TarifarioService} (guardarEspecialidad): INSERT INTO especializacion (codigo_especializacion, nombre, descripcion, activo)
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarEspecializacion(Map<String, ?> p) {
        return db.insertar("INSERT INTO especializacion (codigo_especializacion, nombre, descripcion, activo) "
                    + "VALUES (:c, :n, :d, :a)", p);
    }

    /**
     * Usada por {@link TarifarioService} (guardarEspecialidad): SELECT id FROM especializacion WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerEspecializacion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM especializacion WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link TarifarioService} (guardarEspecialidad): SELECT 1 FROM medico WHERE especializacion_id = :id AND activo = 1
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeMedico(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM medico WHERE especializacion_id = :id AND activo = 1", p);
    }

    /**
     * Usada por {@link TarifarioService} (guardarEspecialidad): UPDATE especializacion SET codigo_especializacion = :c, nombre = :n, descripcion = :d,
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarEspecializacion(Map<String, ?> p) {
        return db.ejecutar("UPDATE especializacion SET codigo_especializacion = :c, nombre = :n, descripcion = :d, "
                    + "activo = :a WHERE id = :id", p);
    }

    /**
     * Usada por {@link TarifarioService} (guardarEspecialidad): WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarEspecializacion(Map<String, ?> p) {
        return db.uno(ESPECIALIDAD + " WHERE id = :id", p);
    }

    /**
     * Usada por {@link TarifarioService} (servicios): SELECT id, especializacion_id AS especialidadId, nombre, activo FROM servicio_clinico WHERE especializacion_id…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarServicioClinico(Map<String, ?> p) {
        return db.lista("""
                SELECT id, especializacion_id AS especialidadId, nombre, activo
                FROM servicio_clinico WHERE especializacion_id = :e ORDER BY nombre
                """, p);
    }

    /**
     * Usada por {@link TarifarioService} (agregarServicio): SELECT 1 FROM servicio_clinico WHERE especializacion_id = :e AND nombre = :n
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeServicioClinico(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM servicio_clinico WHERE especializacion_id = :e AND nombre = :n", p);
    }

    /**
     * Usada por {@link TarifarioService} (agregarServicio): INSERT INTO servicio_clinico (especializacion_id, nombre, activo) VALUES (:e, :n, 1)
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarServicioClinico(Map<String, ?> p) {
        return db.insertar("INSERT INTO servicio_clinico (especializacion_id, nombre, activo) VALUES (:e, :n, 1)", p);
    }

    /**
     * Usada por {@link TarifarioService} (agregarServicio): SELECT id, especializacion_id AS especialidadId, nombre, activo FROM servicio_clinico WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarServicioClinico(Map<String, ?> p) {
        return db.uno("SELECT id, especializacion_id AS especialidadId, nombre, activo FROM servicio_clinico WHERE id = :id", p);
    }

    /**
     * Usada por {@link TarifarioService} (tarifas): SELECT id FROM servicio_clinico WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerServicioClinico(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM servicio_clinico WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link TarifarioService} (tarifas): SELECT t.id, t.valor, t.iva_porcentaje AS iva, t.impuesto_porcentaje AS impuesto, t.vigente_desde AS desde, t.…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarTarifaServicio(Map<String, ?> p) {
        return db.lista("""
                SELECT t.id, t.valor, t.iva_porcentaje AS iva, t.impuesto_porcentaje AS impuesto, t.vigente_desde AS desde,
                       t.vigente_hasta AS hasta, t.usuario_id AS usuarioId, u.username AS usuario, t.fecha_registro AS fecha
                FROM tarifa_servicio t JOIN usuario u ON u.id = t.usuario_id
                WHERE t.servicio_id = :s ORDER BY t.vigente_desde DESC, t.id DESC
                """, p);
    }

    /**
     * Usada por {@link TarifarioService} (nuevaTarifa): SELECT id FROM servicio_clinico WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerServicioClinicoEnNuevaTarifa(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM servicio_clinico WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link TarifarioService} (nuevaTarifa): SELECT 1 FROM tarifa_servicio WHERE servicio_id = :s AND vigente_desde >= :d
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeTarifaServicio(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM tarifa_servicio WHERE servicio_id = :s AND vigente_desde >= :d", p);
    }

    /**
     * Usada por {@link TarifarioService} (nuevaTarifa): UPDATE tarifa_servicio SET vigente_hasta = :cierre WHERE servicio_id = :s AND (vigente_hasta IS NULL OR vigent…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarTarifaServicio(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE tarifa_servicio SET vigente_hasta = :cierre
                WHERE servicio_id = :s AND (vigente_hasta IS NULL OR vigente_hasta >= :d)
                """, p);
    }

    /**
     * Usada por {@link TarifarioService} (nuevaTarifa): INSERT INTO tarifa_servicio (servicio_id, valor, iva_porcentaje, impuesto_porcentaje, vigente_desde, vigente_h…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarTarifaServicio(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO tarifa_servicio (servicio_id, valor, iva_porcentaje, impuesto_porcentaje, vigente_desde,
                       vigente_hasta, usuario_id, fecha_registro)
                VALUES (:s, :v, :iva, :imp, :d, :h, :u, :ahora)
                """, p);
    }

    /**
     * Usada por {@link TarifarioService} (vigente): SELECT id, valor, iva_porcentaje AS iva, impuesto_porcentaje AS impuesto, vigente_desde AS desde, vigente_hast…
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarTarifaServicio(Map<String, ?> p) {
        return db.uno("""
                SELECT id, valor, iva_porcentaje AS iva, impuesto_porcentaje AS impuesto, vigente_desde AS desde,
                       vigente_hasta AS hasta
                FROM tarifa_servicio
                WHERE servicio_id = :s AND vigente_desde <= :f AND (vigente_hasta IS NULL OR vigente_hasta >= :f)
                ORDER BY vigente_desde DESC LIMIT 1
                """, p);
    }
}
