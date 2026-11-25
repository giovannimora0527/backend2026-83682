package com.uniminuto.clinica.despacho;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link DespachoService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class DespachoRepository {

    /** Consulta base de fórmulas para despacho. */
    private static final String FORMULA = """
            SELECT f.id, COALESCE(f.fecha_confirmacion, f.fecha_creacion) AS fecha,
                   CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                   CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, m.nombre_mascota AS mascota,
                   f.estado_despacho AS estado, f.cita_id AS cita
            FROM formula_clinica f
            JOIN medico md ON md.id = f.medico_id
            JOIN mascota m ON m.mascota_id = f.mascota_id
            JOIN cita c ON c.id = f.cita_id
            JOIN cliente cl ON cl.cliente_id = c.cliente_id
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public DespachoRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link DespachoService} (formulas): WHERE f.estado = 'CONFIRMADA' AND (:id IS NULL OR f.id = :id) AND (:propietario IS NULL OR CONCAT(cl.nombres, …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinica(Map<String, ?> p) {
        return db.lista(FORMULA + """
                WHERE f.estado = 'CONFIRMADA'
                  AND (:id IS NULL OR f.id = :id)
                  AND (:propietario IS NULL OR CONCAT(cl.nombres, ' ', cl.apellidos) LIKE :propietario
                       OR cl.numero_documento = :documento)
                  AND (:mascota IS NULL OR m.nombre_mascota LIKE :mascota)
                  AND (:desde IS NULL OR (COALESCE(f.fecha_confirmacion, f.fecha_creacion) >= :desde
                       AND COALESCE(f.fecha_confirmacion, f.fecha_creacion) < :hasta))
                  AND (:estado IS NULL OR f.estado_despacho = :estado)
                ORDER BY FIELD(f.estado_despacho, 'EN_PREPARACION', 'PENDIENTE_DESPACHO', 'DESPACHO_PARCIAL',
                               'PENDIENTE_DISPONIBILIDAD', 'DESPACHADO', 'CANCELADO'), fecha DESC, f.id DESC
                LIMIT :limite OFFSET :desplazamiento
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (detalle): WHERE f.id = :id AND f.estado = 'CONFIRMADA'
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinica(Map<String, ?> p, String mensaje) {
        return db.requerido(FORMULA + " WHERE f.id = :id AND f.estado = 'CONFIRMADA'", p, mensaje);
    }

    /**
     * Usada por {@link DespachoService} (detalle): SELECT COALESCE(SUM(cantidad), 0) FROM medicamento_lote WHERE medicamento_id = :m AND cantidad > 0 AND fecha_v…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarMedicamentoLote(Map<String, ?> p) {
        return db.contar("""
                    SELECT COALESCE(SUM(cantidad), 0) FROM medicamento_lote
                    WHERE medicamento_id = :m AND cantidad > 0 AND fecha_vence < :hoy
                    """, p);
    }

    /**
     * Usada por {@link DespachoService} (detalle): SELECT h.estado_anterior AS anterior, h.estado_nuevo AS nuevo, u.username AS responsable, h.fecha, h.motivo FR…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoEstadoHistorial(Map<String, ?> p) {
        return db.lista("""
                SELECT h.estado_anterior AS anterior, h.estado_nuevo AS nuevo, u.username AS responsable, h.fecha, h.motivo
                FROM despacho_estado_historial h JOIN usuario u ON u.id = h.usuario_id
                WHERE h.formula_id = :f ORDER BY h.fecha DESC, h.id DESC
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (preparar): SELECT id, formula_id FROM despacho_preparacion WHERE clave = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarDespachoPreparacion(Map<String, ?> p) {
        return db.uno("SELECT id, formula_id FROM despacho_preparacion WHERE clave = :c", p);
    }

    /**
     * Usada por {@link DespachoService} (preparar): UPDATE despacho_preparacion SET estado = 'DESCARTADA' WHERE formula_id = :f AND estado = 'VIGENTE'
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarDespachoPreparacion(Map<String, ?> p) {
        return db.ejecutar("UPDATE despacho_preparacion SET estado = 'DESCARTADA' WHERE formula_id = :f AND estado = 'VIGENTE'", p);
    }

    /**
     * Usada por {@link DespachoService} (preparar): INSERT INTO despacho_preparacion (formula_id, clave, usuario_id, fecha, estado) VALUES (:f, :c, :u, :ahora, 'V…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoPreparacion(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO despacho_preparacion (formula_id, clave, usuario_id, fecha, estado)
                VALUES (:f, :c, :u, :ahora, 'VIGENTE')
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (preparar): INSERT INTO despacho_preparacion_lote (preparacion_id, detalle_id, lote_id, cantidad) VALUES (:p, :d, :l, :c)
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoPreparacionLote(Map<String, ?> p) {
        return db.insertar("""
                        INSERT INTO despacho_preparacion_lote (preparacion_id, detalle_id, lote_id, cantidad)
                        VALUES (:p, :d, :l, :c)
                        """, p);
    }

    /**
     * Usada por {@link DespachoService} (confirmar): SELECT * FROM despacho_preparacion WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerDespachoPreparacion(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM despacho_preparacion WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link DespachoService} (confirmar): INSERT INTO despacho (formula_id, preparacion_id, usuario_id, fecha, estado) VALUES (:f, :p, :u, :ahora, 'EN_P…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespacho(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO despacho (formula_id, preparacion_id, usuario_id, fecha, estado)
                VALUES (:f, :p, :u, :ahora, 'EN_PREPARACION')
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (confirmar): SELECT pl.detalle_id, pl.lote_id, pl.cantidad, l.numero, l.fecha_vence, l.medicamento_id, m.nombre FROM despac…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoPreparacionLote(Map<String, ?> p) {
        return db.lista("""
                SELECT pl.detalle_id, pl.lote_id, pl.cantidad, l.numero, l.fecha_vence, l.medicamento_id, m.nombre
                FROM despacho_preparacion_lote pl
                JOIN medicamento_lote l ON l.id = pl.lote_id
                JOIN medicamento m ON m.id = l.medicamento_id
                WHERE pl.preparacion_id = :p ORDER BY l.fecha_vence IS NULL, l.fecha_vence, pl.id
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (confirmar): INSERT INTO despacho_detalle (despacho_id, detalle_id, medicamento_id, lote_id, cantidad, movimiento_id) VALUE…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoDetalle(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO despacho_detalle (despacho_id, detalle_id, medicamento_id, lote_id, cantidad, movimiento_id)
                    VALUES (:d, :det, :m, :l, :c, :mov)
                    """, p);
    }

    /**
     * Usada por {@link DespachoService} (confirmar): UPDATE despacho SET estado = :e, pendiente = :p WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarDespacho(Map<String, ?> p) {
        return db.ejecutar("UPDATE despacho SET estado = :e, pendiente = :p WHERE id = :id", p);
    }

    /**
     * Usada por {@link DespachoService} (confirmar): UPDATE despacho_preparacion SET estado = 'CONFIRMADA' WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarDespachoPreparacionEnConfirmar(Map<String, ?> p) {
        return db.ejecutar("UPDATE despacho_preparacion SET estado = 'CONFIRMADA' WHERE id = :id", p);
    }

    /**
     * Usada por {@link DespachoService} (revertir): SELECT * FROM despacho WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerDespacho(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM despacho WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link DespachoService} (revertir): SELECT 1 FROM factura_linea fl JOIN factura f ON f.id = fl.factura_id JOIN despacho_detalle dd ON dd.movimient…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeFacturaLinea(Map<String, ?> p) {
        return db.existe("""
                SELECT 1 FROM factura_linea fl JOIN factura f ON f.id = fl.factura_id
                JOIN despacho_detalle dd ON dd.movimiento_id = fl.movimiento_id
                WHERE dd.despacho_id = :d AND f.estado <> 'ANULADA'
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (revertir): SELECT * FROM despacho_detalle WHERE despacho_id = :d
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoDetalle(Map<String, ?> p) {
        return db.lista("SELECT * FROM despacho_detalle WHERE despacho_id = :d", p);
    }

    /**
     * Usada por {@link DespachoService} (revertir): UPDATE despacho_detalle SET movimiento_reversion_id = :m WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarDespachoDetalle(Map<String, ?> p) {
        return db.ejecutar("UPDATE despacho_detalle SET movimiento_reversion_id = :m WHERE id = :id", p);
    }

    /**
     * Usada por {@link DespachoService} (revertir): UPDATE despacho SET fecha_reversion = :f, usuario_reversion_id = :u, motivo_reversion = :m WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarDespachoEnRevertir(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE despacho SET fecha_reversion = :f, usuario_reversion_id = :u, motivo_reversion = :m WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (revertir): SELECT COALESCE(SUM(dd.cantidad), 0) FROM despacho_detalle dd JOIN despacho x ON x.id = dd.despacho_id WHERE x…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarDespachoDetalle(Map<String, ?> p) {
        return db.contar("""
                    SELECT COALESCE(SUM(dd.cantidad), 0) FROM despacho_detalle dd JOIN despacho x ON x.id = dd.despacho_id
                    WHERE x.formula_id = :f AND dd.movimiento_reversion_id IS NULL
                    """, p);
    }

    /**
     * Usada por {@link DespachoService} (pdf): WHERE f.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinicaEnPdf(Map<String, ?> p, String mensaje) {
        return db.requerido(FORMULA + " WHERE f.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link DespachoService} (lineas): SELECT d.id AS detalle, d.medicamento_id AS medicamento, m.nombre, m.presentacion, d.dosis, d.frecuencia, d.du…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoDetalleEnLineas(Map<String, ?> p) {
        return db.lista("""
                SELECT d.id AS detalle, d.medicamento_id AS medicamento, m.nombre, m.presentacion, d.dosis, d.frecuencia,
                       d.duracion, d.via, d.indicaciones, d.cantidad AS prescrita,
                       COALESCE((SELECT SUM(dd.cantidad) FROM despacho_detalle dd
                                 WHERE dd.detalle_id = d.id AND dd.movimiento_reversion_id IS NULL), 0) AS entregada
                FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                WHERE d.formula_id = :f ORDER BY d.id
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (lotesVigentes): SELECT id, numero, fecha_vence AS fechaVence, cantidad FROM medicamento_lote WHERE medicamento_id = :m AND can…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamentoLote(Map<String, ?> p) {
        return db.lista("""
                SELECT id, numero, fecha_vence AS fechaVence, cantidad FROM medicamento_lote
                WHERE medicamento_id = :m AND cantidad > 0 AND (fecha_vence IS NULL OR fecha_vence >= :hoy)
                ORDER BY fecha_vence IS NULL, fecha_vence, id
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (preparacionVigente): SELECT id FROM despacho_preparacion WHERE formula_id = :f AND estado = 'VIGENTE'
     *
     * @param p parámetros con nombre de la consulta.
     * @param tipo clase del valor esperado.
     * @param <T> tipo del valor.
     * @return resultado.
     */
    public <T> T valorDespachoPreparacion(Map<String, ?> p, Class<T> tipo) {
        return db.valor("SELECT id FROM despacho_preparacion WHERE formula_id = :f AND estado = 'VIGENTE'", p, tipo);
    }

    /**
     * Usada por {@link DespachoService} (plan): SELECT id, formula_id AS formula, estado, fecha FROM despacho_preparacion WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerDespachoPreparacionEnPlan(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id, formula_id AS formula, estado, fecha FROM despacho_preparacion WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link DespachoService} (plan): SELECT d.medicamento_id AS medicamento, pl.lote_id AS lote, pl.cantidad FROM despacho_preparacion_lote pl JOIN…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoPreparacionLoteEnPlan(Map<String, ?> p) {
        return db.lista("""
                SELECT d.medicamento_id AS medicamento, pl.lote_id AS lote, pl.cantidad
                FROM despacho_preparacion_lote pl JOIN formula_clinica_detalle d ON d.id = pl.detalle_id
                WHERE pl.preparacion_id = :p ORDER BY pl.id
                """, p);
    }

    /**
     * Usada por {@link DespachoService} (CONCAT): SELECT m.nombre, l.numero, l.fecha_vence AS fechaVence, dd.cantidad, dd.movimiento_id AS movimiento, dd.movimi…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoDetalleEnCONCAT(Map<String, ?> p) {
        return db.lista("""
                    SELECT m.nombre, l.numero, l.fecha_vence AS fechaVence, dd.cantidad, dd.movimiento_id AS movimiento,
                           dd.movimiento_reversion_id AS movimientoReversion
                    FROM despacho_detalle dd
                    JOIN medicamento m ON m.id = dd.medicamento_id
                    JOIN medicamento_lote l ON l.id = dd.lote_id
                    WHERE dd.despacho_id = :d ORDER BY dd.id
                    """, p);
    }

    /**
     * Usada por {@link DespachoService} (bloquearFormula): SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinicaEnBloquearFormula(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link DespachoService} (cambiarEstado): UPDATE formula_clinica SET estado_despacho = :e WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaClinica(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_clinica SET estado_despacho = :e WHERE id = :id", p);
    }

    /**
     * Usada por {@link DespachoService} (cambiarEstado): INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo) V…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoEstadoHistorial(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, :a, :n, :u, :ahora, :m)
                """, p);
    }

    /** Criterios cerrados para consultar entregas (despachos). */
    public enum CriterioEntrega {
        /** Entregas de una fórmula (:f). */
        FORMULA("WHERE d.formula_id = :f", ""),
        /** Una entrega (:id). */
        ID("WHERE d.id = :id", ""),
        /** Historial con filtros opcionales (:desde, :hasta, :med) y paginación (:limite, :desplazamiento). */
        HISTORIAL("""
                WHERE (:desde IS NULL OR d.fecha >= :desde) AND (:hasta IS NULL OR d.fecha < :hasta)
                  AND (:med IS NULL OR EXISTS (SELECT 1 FROM despacho_detalle x WHERE x.despacho_id = d.id AND x.medicamento_id = :med))
                """, "LIMIT :limite OFFSET :desplazamiento");

        /** Condición SQL fija. */
        private final String condicion;
        /** Paginación SQL fija. */
        private final String limite;

        /**
         * @param condicion condición SQL.
         * @param limite paginación SQL.
         */
        CriterioEntrega(String condicion, String limite) {
            this.condicion = condicion;
            this.limite = limite;
        }
    }

    /**
     * Entregas con responsable, reversión, mascota, propietario y veterinario.
     *
     * @param criterio criterio.
     * @param p parámetros del criterio.
     * @return entregas, de la más reciente a la más antigua.
     */
    public List<Map<String, Object>> entregasPor(CriterioEntrega criterio, Map<String, ?> p) {
        return db.lista("""
                SELECT d.id, d.formula_id AS formula, u.username AS responsable, d.fecha, d.estado,
                       d.fecha_reversion AS revertida, r.username AS reversor, d.motivo_reversion AS motivo, d.pendiente,
                       m.nombre_mascota AS mascota, CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario,
                       CONCAT(md.nombres, ' ', md.apellidos) AS veterinario
                FROM despacho d
                JOIN usuario u ON u.id = d.usuario_id
                LEFT JOIN usuario r ON r.id = d.usuario_reversion_id
                JOIN formula_clinica f ON f.id = d.formula_id
                JOIN mascota m ON m.mascota_id = f.mascota_id
                JOIN cita c ON c.id = f.cita_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = f.medico_id
                """ + criterio.condicion + " ORDER BY d.fecha DESC, d.id DESC " + criterio.limite, p);
    }
}
