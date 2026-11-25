package com.uniminuto.clinica.facturacion;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link FacturacionService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class FacturacionRepository {

    /** Consulta base de facturas. */
    private static final String FACTURA = """
            SELECT f.id, f.numero, f.atencion_id AS atencion, f.cita_id AS cita, f.fecha_emision AS emitida,
                   f.propietario, f.documento, f.mascota, f.veterinario, f.especialidad, f.subtotal, f.descuento,
                   f.motivo_descuento AS motivoDescuento, f.iva, f.otros_impuestos, f.total, f.pagado,
                   f.total - f.pagado AS saldo, f.estado, u.username AS responsable
            FROM factura f JOIN usuario u ON u.id = f.usuario_id
            """;

    /** Consulta de la atención que origina la factura. */
    private static final String ORIGEN = """
            SELECT a.id, a.cita_id AS cita, CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario,
                   (SELECT COUNT(*) FROM formula_clinica fc WHERE fc.atencion_id = a.id AND fc.estado = 'CONFIRMADA'
                      AND fc.estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION')) AS formulasSinDespachar,
                   m.nombre_mascota AS mascota, CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                   e.nombre AS especialidad, a.fecha_finalizacion AS finalizada
            FROM atencion_medica a
            JOIN cita c ON c.id = a.cita_id
            JOIN cliente cl ON cl.cliente_id = c.cliente_id
            JOIN mascota m ON m.mascota_id = c.mascota_id
            JOIN medico md ON md.id = a.medico_id
            JOIN especializacion e ON e.id = a.especializacion_id
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public FacturacionRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link FacturacionService} (elegibles): WHERE a.fecha_finalizacion IS NOT NULL AND NOT EXISTS (SELECT 1 FROM factura f WHERE f.atencion_id = a.id AND …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinica(Map<String, ?> p) {
        return db.lista(ORIGEN + """
                WHERE a.fecha_finalizacion IS NOT NULL
                  AND NOT EXISTS (SELECT 1 FROM factura f WHERE f.atencion_id = a.id AND f.estado <> 'ANULADA')
                ORDER BY a.fecha_finalizacion DESC LIMIT :limite OFFSET :desde
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): SELECT id, atencion_id FROM factura WHERE clave = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarFactura(Map<String, ?> p) {
        return db.uno("SELECT id, atencion_id FROM factura WHERE clave = :c", p);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): SELECT id FROM atencion_medica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerAtencionMedica(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM atencion_medica WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): SELECT 1 FROM factura WHERE atencion_id = :a AND estado <> 'ANULADA'
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeFactura(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM factura WHERE atencion_id = :a AND estado <> 'ANULADA'", p);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): SELECT c.id AS cita, c.cliente_id, c.mascota_id, a.medico_id, a.especializacion_id, CONCAT(cl.tipo_documento, …
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarAtencionMedica(Map<String, ?> p) {
        return db.uno("""
                SELECT c.id AS cita, c.cliente_id, c.mascota_id, a.medico_id, a.especializacion_id,
                       CONCAT(cl.tipo_documento, ' ', cl.numero_documento) AS documento
                FROM atencion_medica a JOIN cita c ON c.id = a.cita_id JOIN cliente cl ON cl.cliente_id = c.cliente_id
                WHERE a.id = :a
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): INSERT INTO factura (clave, atencion_id, cita_id, cliente_id, mascota_id, medico_id, especializacion_id, propi…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFactura(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO factura (clave, atencion_id, cita_id, cliente_id, mascota_id, medico_id, especializacion_id,
                       propietario, documento, mascota, veterinario, especialidad, subtotal, descuento, motivo_descuento,
                       iva, otros_impuestos, total, pagado, estado, usuario_id, fecha_emision)
                VALUES (:clave, :a, :cita, :cliente, :mascota, :medico, :esp, :propietario, :documento, :nombreMascota,
                       :veterinario, :especialidad, :subtotal, :descuento, :motivo, :iva, :otros, :total, 0,
                       'PENDIENTE_PAGO', :u, :ahora)
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): UPDATE factura SET numero = :n WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFactura(Map<String, ?> p) {
        return db.ejecutar("UPDATE factura SET numero = :n WHERE id = :id", p);
    }

    /**
     * Usada por {@link FacturacionService} (emitir): INSERT INTO factura_linea (factura_id, servicio_id, tarifa_id, movimiento_id, concepto, cantidad, precio_unita…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFacturaLinea(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO factura_linea (factura_id, servicio_id, tarifa_id, movimiento_id, concepto, cantidad,
                           precio_unitario, subtotal, descuento, iva_porcentaje, iva, impuesto_porcentaje, otros_impuestos, total)
                    VALUES (:f, :s, :t, :m, :concepto, :cantidad, :precio, :subtotal, :descuento, :ivap, :iva, :impp, :otros, :total)
                    """, p);
    }

    /**
     * Usada por {@link FacturacionService} (listar): WHERE (:desde IS NULL OR f.fecha_emision >= :desde) AND (:hasta IS NULL OR f.fecha_emision < :hasta) AND (:pro…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFactura(Map<String, ?> p) {
        return db.lista(FACTURA + """
                WHERE (:desde IS NULL OR f.fecha_emision >= :desde) AND (:hasta IS NULL OR f.fecha_emision < :hasta)
                  AND (:propietario IS NULL OR f.propietario LIKE :propietario OR f.documento LIKE :propietario
                       OR f.cliente_id = :clienteId)
                  AND (:mascota IS NULL OR f.mascota LIKE :mascota OR f.mascota_id = :mascotaId)
                  AND (:estado IS NULL OR f.estado = :estado)
                ORDER BY f.fecha_emision DESC, f.id DESC LIMIT :limite OFFSET :pag
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (detalle): WHERE f.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFactura(Map<String, ?> p, String mensaje) {
        return db.requerido(FACTURA + " WHERE f.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link FacturacionService} (detalle): SELECT id, servicio_id, movimiento_id, concepto, cantidad, precio_unitario, subtotal, descuento, iva_porcentaj…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFacturaLinea(Map<String, ?> p) {
        return db.lista("""
                SELECT id, servicio_id, movimiento_id, concepto, cantidad, precio_unitario, subtotal, descuento,
                       iva_porcentaje, iva, impuesto_porcentaje, otros_impuestos, total
                FROM factura_linea WHERE factura_id = :f ORDER BY id
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (detalle): SELECT p.id, p.fecha, p.importe, p.metodo, p.referencia, u.username AS responsable, p.estado FROM pago p JOIN …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarPago(Map<String, ?> p) {
        return db.lista("""
                SELECT p.id, p.fecha, p.importe, p.metodo, p.referencia, u.username AS responsable, p.estado
                FROM pago p JOIN usuario u ON u.id = p.usuario_id WHERE p.factura_id = :f ORDER BY p.fecha, p.id
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (detalle): SELECT h.id, h.estado_anterior AS anterior, h.estado_nuevo AS nuevo, h.fecha, h.motivo, u.username AS responsa…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFacturaEstadoHistorial(Map<String, ?> p) {
        return db.lista("""
                SELECT h.id, h.estado_anterior AS anterior, h.estado_nuevo AS nuevo, h.fecha, h.motivo, u.username AS responsable
                FROM factura_estado_historial h JOIN usuario u ON u.id = h.usuario_id
                WHERE h.factura_id = :f ORDER BY h.fecha, h.id
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (pagar): SELECT * FROM factura WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFacturaEnPagar(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM factura WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link FacturacionService} (pagar): SELECT factura_id FROM pago WHERE clave = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarPago(Map<String, ?> p) {
        return db.uno("SELECT factura_id FROM pago WHERE clave = :c", p);
    }

    /**
     * Usada por {@link FacturacionService} (pagar): INSERT INTO pago (factura_id, clave, fecha, importe, metodo, referencia, usuario_id, estado) VALUES (:f, :c, :…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarPago(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO pago (factura_id, clave, fecha, importe, metodo, referencia, usuario_id, estado)
                VALUES (:f, :c, :ahora, :i, :m, :r, :u, 'REGISTRADO')
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (pagar): UPDATE factura SET pagado = :p, estado = :e WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFacturaEnPagar(Map<String, ?> p) {
        return db.ejecutar("UPDATE factura SET pagado = :p, estado = :e WHERE id = :id", p);
    }

    /**
     * Usada por {@link FacturacionService} (anular): SELECT estado FROM factura WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFacturaEnAnular(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT estado FROM factura WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link FacturacionService} (anular): UPDATE factura SET estado = 'ANULADA' WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFacturaEnAnular(Map<String, ?> p) {
        return db.ejecutar("UPDATE factura SET estado = 'ANULADA' WHERE id = :id", p);
    }

    /**
     * Usada por {@link FacturacionService} (pdf): SELECT s.servicio_id, s.tarifa_id, sc.nombre, s.cantidad, s.valor_unitario, s.iva_porcentaje, s.impuesto_porce…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAtencionServicio(Map<String, ?> p) {
        return db.lista("""
                SELECT s.servicio_id, s.tarifa_id, sc.nombre, s.cantidad, s.valor_unitario, s.iva_porcentaje, s.impuesto_porcentaje
                FROM atencion_servicio s JOIN servicio_clinico sc ON sc.id = s.servicio_id
                WHERE s.atencion_id = :a ORDER BY s.id
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (pdf): SELECT dd.movimiento_id, md.nombre, l.numero, dd.cantidad, md.precio_venta, md.iva_porcentaje FROM despacho_de…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoDetalle(Map<String, ?> p) {
        return db.lista("""
                SELECT dd.movimiento_id, md.nombre, l.numero, dd.cantidad, md.precio_venta, md.iva_porcentaje
                FROM despacho_detalle dd
                JOIN despacho d ON d.id = dd.despacho_id
                JOIN formula_clinica f ON f.id = d.formula_id
                JOIN medicamento md ON md.id = dd.medicamento_id
                JOIN medicamento_lote l ON l.id = dd.lote_id
                WHERE f.atencion_id = :a AND dd.movimiento_reversion_id IS NULL
                  AND NOT EXISTS (SELECT 1 FROM factura_linea fl JOIN factura x ON x.id = fl.factura_id
                                  WHERE fl.movimiento_id = dd.movimiento_id AND x.estado <> 'ANULADA')
                ORDER BY dd.id
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (formulasSinDespachar): SELECT COUNT(*) FROM formula_clinica WHERE atencion_id = :a AND estado = 'CONFIRMADA' AND estado_despacho IN (…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarFormulaClinica(Map<String, ?> p) {
        return db.contar("""
                SELECT COUNT(*) FROM formula_clinica WHERE atencion_id = :a AND estado = 'CONFIRMADA'
                  AND estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION')
                """, p);
    }

    /**
     * Usada por {@link FacturacionService} (origen): WHERE a.id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinica(Map<String, ?> p, String mensaje) {
        return db.requerido(ORIGEN + " WHERE a.id = :id", p, mensaje);
    }

    /**
     * Usada por {@link FacturacionService} (historial): INSERT INTO factura_estado_historial (factura_id, estado_anterior, estado_nuevo, fecha, motivo, usuario_id) VA…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFacturaEstadoHistorial(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO factura_estado_historial (factura_id, estado_anterior, estado_nuevo, fecha, motivo, usuario_id)
                VALUES (:f, :a, :n, :ahora, :m, :u)
                """, p);
    }
}
