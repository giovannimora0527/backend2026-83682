package com.uniminuto.clinica.reportes;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link ReportesService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class ReportesRepository {

    /** Filtro común de citas por fecha, médico y estado. */
    private static final String FILTRO_CITA = """
            c.fecha_hora >= :desde AND c.fecha_hora < :hasta
              AND (:medico IS NULL OR c.medico_id = :medico)
              AND (:especialidad IS NULL OR md.especializacion_id = :especialidad)
              AND (:mascota IS NULL OR c.mascota_id = :mascota)
              AND (:propietario IS NULL OR c.cliente_id = :propietario)
              AND (:estado IS NULL OR c.estado = :estado)
            """;

    /** Filtro común de facturas por fecha y estado. */
    static final String FILTRO_FACTURA = """
            f.fecha_emision >= :desde AND f.fecha_emision < :hasta
              AND (:medico IS NULL OR f.medico_id = :medico)
              AND (:especialidad IS NULL OR f.especializacion_id = :especialidad)
              AND (:mascota IS NULL OR f.mascota_id = :mascota)
              AND (:propietario IS NULL OR f.cliente_id = :propietario)
            """;

    /** Subconsulta de líneas por factura. */
    static final String LINEAS_POR_FACTURA = """
            (SELECT factura_id,
                    SUM(CASE WHEN servicio_id IS NOT NULL THEN total ELSE 0 END) AS servicios,
                    SUM(CASE WHEN movimiento_id IS NOT NULL THEN total ELSE 0 END) AS medicamentos
             FROM factura_linea GROUP BY factura_id) l
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public ReportesRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link ReportesService} (financiero): SELECT DATE_FORMAT(f.fecha_emision, :formato) AS periodo, SUM(f.estado <> 'ANULADA') AS facturas, SUM(f.estado…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFactura(Map<String, ?> p) {
        return db.lista("""
                SELECT DATE_FORMAT(f.fecha_emision, :formato) AS periodo,
                       SUM(f.estado <> 'ANULADA') AS facturas,
                       SUM(f.estado = 'PAGADA') AS pagadas,
                       SUM(f.estado = 'PENDIENTE_PAGO') AS pendientes,
                       SUM(f.estado = 'PAGO_PARCIAL') AS parciales,
                       SUM(f.estado = 'ANULADA') AS anuladas,
                       SUM(IF(f.estado <> 'ANULADA', l.servicios, 0)) AS ingresos_consultas,
                       SUM(IF(f.estado <> 'ANULADA', l.medicamentos, 0)) AS ingresos_medicamentos,
                       SUM(IF(f.estado <> 'ANULADA', f.descuento, 0)) AS descuentos,
                       SUM(IF(f.estado <> 'ANULADA', f.iva, 0)) AS iva,
                       SUM(IF(f.estado <> 'ANULADA', f.otros_impuestos, 0)) AS otros_impuestos,
                       SUM(IF(f.estado <> 'ANULADA', f.total, 0)) AS total_facturado,
                       SUM(IF(f.estado <> 'ANULADA', f.pagado, 0)) AS recaudado,
                       SUM(IF(f.estado <> 'ANULADA', f.total - f.pagado, 0)) AS saldo
                FROM factura f JOIN
                """ + LINEAS_POR_FACTURA + " ON l.factura_id = f.id WHERE " + FILTRO_FACTURA
                + " GROUP BY periodo ORDER BY periodo", p);
    }

    /**
     * Usada por {@link ReportesService} (facturas): SELECT f.numero, f.fecha_emision AS emitida, f.propietario, f.mascota, f.veterinario, f.especialidad, f.estado…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFacturaEnFacturas(Map<String, ?> p) {
        return db.lista("""
                SELECT f.numero, f.fecha_emision AS emitida, f.propietario, f.mascota, f.veterinario, f.especialidad,
                       f.estado, f.subtotal, f.descuento, f.iva, f.otros_impuestos, f.total, f.pagado,
                       f.total - f.pagado AS saldo
                FROM factura f WHERE
                """ + FILTRO_FACTURA + " AND (:estado IS NULL OR f.estado = :estado) ORDER BY f.fecha_emision LIMIT :maximo", p);
    }

    /**
     * Usada por {@link ReportesService} (pagos): SELECT p.fecha, f.numero AS factura, f.propietario, f.mascota, p.metodo, p.referencia, p.importe, u.username A…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarPago(Map<String, ?> p) {
        return db.lista("""
                SELECT p.fecha, f.numero AS factura, f.propietario, f.mascota, p.metodo, p.referencia, p.importe,
                       u.username AS responsable, f.estado AS estado_factura
                FROM pago p JOIN factura f ON f.id = p.factura_id JOIN usuario u ON u.id = p.usuario_id
                WHERE p.fecha >= :desde AND p.fecha < :hasta
                  AND (:medico IS NULL OR f.medico_id = :medico)
                  AND (:especialidad IS NULL OR f.especializacion_id = :especialidad)
                  AND (:mascota IS NULL OR f.mascota_id = :mascota)
                  AND (:propietario IS NULL OR f.cliente_id = :propietario)
                  AND (:estado IS NULL OR p.metodo = :estado)
                ORDER BY p.fecha LIMIT :maximo
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (conceptos): SELECT x.tipo, x.concepto, SUM(x.cantidad) AS cantidad, SUM(x.subtotal) AS subtotal, SUM(x.descuento) AS descu…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFacturaLinea(Map<String, ?> p) {
        return db.lista("""
                SELECT x.tipo, x.concepto, SUM(x.cantidad) AS cantidad, SUM(x.subtotal) AS subtotal,
                       SUM(x.descuento) AS descuento, SUM(x.iva) AS iva, SUM(x.otros) AS otros_impuestos, SUM(x.total) AS total
                FROM (SELECT IF(fl.servicio_id IS NOT NULL, 'SERVICIO', 'MEDICAMENTO') AS tipo,
                             COALESCE(sc.nombre, md.nombre) AS concepto, fl.cantidad, fl.subtotal, fl.descuento, fl.iva,
                             fl.otros_impuestos AS otros, fl.total
                      FROM factura_linea fl
                      JOIN factura f ON f.id = fl.factura_id
                      LEFT JOIN servicio_clinico sc ON sc.id = fl.servicio_id
                      LEFT JOIN movimiento_inventario mv ON mv.id = fl.movimiento_id
                      LEFT JOIN medicamento md ON md.id = mv.medicamento_id
                      WHERE f.estado <> 'ANULADA' AND
                """ + FILTRO_FACTURA + """
                        AND (:medicamento IS NULL OR mv.medicamento_id = :medicamento)) x
                WHERE (:estado IS NULL OR x.tipo = :estado)
                GROUP BY x.tipo, x.concepto ORDER BY x.tipo DESC, total DESC
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (existencias): SELECT * FROM ( SELECT m.codigo, m.nombre, m.tipo, m.stock_minimo, COALESCE(SUM(l.cantidad), 0) AS existencia,…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamento(Map<String, ?> p) {
        return db.lista("""
                SELECT * FROM (
                  SELECT m.codigo, m.nombre, m.tipo, m.stock_minimo,
                         COALESCE(SUM(l.cantidad), 0) AS existencia,
                         COALESCE(SUM(CASE WHEN l.fecha_vence IS NULL OR l.fecha_vence >= :hoy THEN l.cantidad END), 0) AS disponible,
                         COALESCE(SUM(CASE WHEN l.fecha_vence < :hoy THEN l.cantidad END), 0) AS vencidas,
                         COALESCE(SUM(CASE WHEN l.fecha_vence BETWEEN :hoy AND :limite THEN l.cantidad END), 0) AS proximas,
                         m.precio_compra, m.precio_venta,
                         COALESCE(SUM(l.cantidad * l.costo), 0) AS valor_costo,
                         COALESCE(SUM(CASE WHEN l.fecha_vence IS NULL OR l.fecha_vence >= :hoy THEN l.cantidad END), 0)
                           * m.precio_venta AS valor_venta
                  FROM medicamento m LEFT JOIN medicamento_lote l ON l.medicamento_id = m.id
                  WHERE m.activo = 1 AND (:medicamento IS NULL OR m.id = :medicamento)
                  GROUP BY m.id) x
                ORDER BY x.nombre
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (existenciasHistoricas): SELECT m.codigo, m.nombre, l.numero AS lote, l.fecha_vence, COALESCE(SUM(CASE WHEN mv.fecha < :desde THEN mv.d…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamentoLote(Map<String, ?> p) {
        return db.lista("""
                SELECT m.codigo, m.nombre, l.numero AS lote, l.fecha_vence,
                       COALESCE(SUM(CASE WHEN mv.fecha < :desde THEN mv.delta END), 0) AS saldo_inicial,
                       COALESCE(SUM(CASE WHEN mv.fecha >= :desde AND mv.fecha < :hasta AND mv.delta > 0 THEN mv.delta END), 0) AS entradas,
                       COALESCE(SUM(CASE WHEN mv.fecha >= :desde AND mv.fecha < :hasta AND mv.delta < 0 THEN -mv.delta END), 0) AS salidas,
                       COALESCE(SUM(CASE WHEN mv.fecha < :hasta THEN mv.delta END), 0) AS saldo_final,
                       COALESCE(SUM(CASE WHEN mv.fecha < :hasta THEN mv.delta END), 0) * l.costo AS valor_costo
                FROM medicamento_lote l
                JOIN medicamento m ON m.id = l.medicamento_id
                LEFT JOIN movimiento_inventario mv ON mv.lote_id = l.id
                WHERE (:medicamento IS NULL OR m.id = :medicamento) AND (:lote IS NULL OR l.id = :lote)
                GROUP BY l.id HAVING saldo_inicial <> 0 OR entradas <> 0 OR salidas <> 0 OR saldo_final <> 0
                ORDER BY m.nombre, l.fecha_vence
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (lotes): SELECT m.codigo, m.nombre, l.id AS lote_id, l.numero AS lote, l.fecha_ingreso, l.fecha_vence, l.cantidad_inici…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamentoLoteEnLotes(Map<String, ?> p) {
        return db.lista("""
                SELECT m.codigo, m.nombre, l.id AS lote_id, l.numero AS lote, l.fecha_ingreso, l.fecha_vence, l.cantidad_inicial,
                       l.cantidad, l.costo, l.cantidad * l.costo AS valor_costo,
                       CASE WHEN l.cantidad = 0 THEN 'agotado' WHEN l.fecha_vence < :hoy THEN 'vencido'
                            WHEN l.fecha_vence <= :limite THEN 'proximo a vencer' ELSE 'vigente' END AS estado
                FROM medicamento_lote l JOIN medicamento m ON m.id = l.medicamento_id
                WHERE (:medicamento IS NULL OR m.id = :medicamento) AND (:lote IS NULL OR l.id = :lote)
                  AND (:fdesde IS NULL OR (l.fecha_vence BETWEEN :fdesde AND :fhasta))
                ORDER BY l.fecha_vence IS NULL, l.fecha_vence, m.nombre
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (movimientos): SELECT mv.id, mv.fecha, mv.tipo, m.codigo, m.nombre, l.numero AS lote, IF(mv.delta > 0, mv.delta, 0) AS entrad…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMovimientoInventario(Map<String, ?> p) {
        return db.lista("""
                SELECT mv.id, mv.fecha, mv.tipo, m.codigo, m.nombre, l.numero AS lote,
                       IF(mv.delta > 0, mv.delta, 0) AS entrada, IF(mv.delta < 0, -mv.delta, 0) AS salida,
                       mv.saldo_lote AS saldo, mv.motivo, mv.referencia, mv.formula_id AS formula, mv.despacho_id AS despacho,
                       u.username AS usuario
                FROM movimiento_inventario mv
                JOIN medicamento m ON m.id = mv.medicamento_id
                JOIN medicamento_lote l ON l.id = mv.lote_id
                JOIN usuario u ON u.id = mv.usuario_id
                WHERE mv.fecha >= :desde AND mv.fecha < :hasta
                  AND (:medicamento IS NULL OR m.id = :medicamento) AND (:lote IS NULL OR l.id = :lote)
                  AND (:estado IS NULL OR mv.tipo = :estado)
                ORDER BY mv.fecha, mv.id LIMIT :maximo
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (dispensaciones): SELECT d.fecha, d.id AS despacho, d.formula_id AS formula, ma.nombre_mascota AS mascota, CONCAT(cl.nombres, ' …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarDespachoDetalle(Map<String, ?> p) {
        return db.lista("""
                SELECT d.fecha, d.id AS despacho, d.formula_id AS formula, ma.nombre_mascota AS mascota,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                       m.nombre AS medicamento, l.numero AS lote, dd.cantidad, dd.movimiento_id AS movimiento,
                       dd.movimiento_reversion_id AS reversion, IF(d.fecha_reversion IS NULL, 'VIGENTE', 'REVERTIDA') AS estado,
                       u.username AS responsable
                FROM despacho_detalle dd
                JOIN despacho d ON d.id = dd.despacho_id
                JOIN formula_clinica fc ON fc.id = d.formula_id
                JOIN mascota ma ON ma.mascota_id = fc.mascota_id
                JOIN cita c ON c.id = fc.cita_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = fc.medico_id
                JOIN medicamento m ON m.id = dd.medicamento_id
                JOIN medicamento_lote l ON l.id = dd.lote_id
                JOIN usuario u ON u.id = d.usuario_id
                WHERE d.fecha >= :desde AND d.fecha < :hasta
                  AND (:medicamento IS NULL OR m.id = :medicamento) AND (:lote IS NULL OR l.id = :lote)
                  AND (:medico IS NULL OR fc.medico_id = :medico) AND (:mascota IS NULL OR fc.mascota_id = :mascota)
                  AND (:propietario IS NULL OR c.cliente_id = :propietario)
                  AND (:estado IS NULL OR IF(d.fecha_reversion IS NULL, 'VIGENTE', 'REVERTIDA') = :estado)
                ORDER BY d.fecha, dd.id LIMIT :maximo
                """, p);
    }

    /**
     * Usada por {@link ReportesService} (citas): SELECT c.id, c.fecha_hora AS inicio, c.fecha_fin AS fin, c.estado, ma.nombre_mascota AS mascota, CONCAT(cl.nom…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCita(Map<String, ?> p) {
        return db.lista("""
                SELECT c.id, c.fecha_hora AS inicio, c.fecha_fin AS fin, c.estado, ma.nombre_mascota AS mascota,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                       e.nombre AS especialidad, c.motivo
                FROM cita c
                JOIN medico md ON md.id = c.medico_id
                JOIN especializacion e ON e.id = md.especializacion_id
                JOIN mascota ma ON ma.mascota_id = c.mascota_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                WHERE
                """ + FILTRO_CITA + " ORDER BY c.fecha_hora LIMIT :maximo", p);
    }

    /**
     * Usada por {@link ReportesService} (consultas): SELECT CONCAT(md.nombres, ' ', md.apellidos) AS veterinario, e.nombre AS especialidad, COUNT(*) AS citas, SUM(…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaEnConsultas(Map<String, ?> p) {
        return db.lista("""
                SELECT CONCAT(md.nombres, ' ', md.apellidos) AS veterinario, e.nombre AS especialidad,
                       COUNT(*) AS citas, SUM(c.estado = 'pendiente') AS pendientes, SUM(c.estado = 'confirmada') AS confirmadas,
                       SUM(c.estado = 'cancelada') AS canceladas, SUM(c.estado IN ('atendida', 'finalizada')) AS atendidas,
                       COUNT(a.fecha_finalizacion) AS consultas_realizadas,
                       COUNT(DISTINCT IF(c.estado IN ('atendida', 'finalizada'), c.mascota_id, NULL)) AS mascotas_atendidas
                FROM cita c
                JOIN medico md ON md.id = c.medico_id
                JOIN especializacion e ON e.id = md.especializacion_id
                LEFT JOIN atencion_medica a ON a.cita_id = c.id
                WHERE
                """ + FILTRO_CITA + " GROUP BY md.id, e.nombre ORDER BY consultas_realizadas DESC, veterinario", p);
    }

    /**
     * Usada por {@link ReportesService} (consultas): SELECT COUNT(DISTINCT c.mascota_id) FROM cita c JOIN medico md ON md.id = c.medico_id WHERE c.estado IN ('aten…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarCita(Map<String, ?> p) {
        return db.contar("""
                SELECT COUNT(DISTINCT c.mascota_id) FROM cita c JOIN medico md ON md.id = c.medico_id
                WHERE c.estado IN ('atendida', 'finalizada') AND
                """ + FILTRO_CITA, p);
    }

    /**
     * Usada por {@link ReportesService} (atenciones): SELECT a.fecha_finalizacion AS finalizada, c.id AS cita, ma.nombre_mascota AS mascota, r.especie, CONCAT(cl.no…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAtencionServicio(Map<String, ?> p) {
        return db.lista("""
                SELECT a.fecha_finalizacion AS finalizada, c.id AS cita, ma.nombre_mascota AS mascota, r.especie,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                       e.nombre AS especialidad, a.diagnostico,
                       (SELECT COUNT(*) FROM atencion_servicio s WHERE s.atencion_id = a.id) AS servicios,
                       (SELECT COUNT(*) FROM formula_clinica fc WHERE fc.atencion_id = a.id AND fc.estado = 'CONFIRMADA') AS formulas
                FROM atencion_medica a
                JOIN cita c ON c.id = a.cita_id
                JOIN medico md ON md.id = a.medico_id
                JOIN especializacion e ON e.id = a.especializacion_id
                JOIN mascota ma ON ma.mascota_id = c.mascota_id
                JOIN raza r ON r.raza_id = ma.raza_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                WHERE a.fecha_finalizacion IS NOT NULL AND
                """ + FILTRO_CITA + " ORDER BY a.fecha_finalizacion LIMIT :maximo", p);
    }

    /**
     * Usada por {@link ReportesService} (operativas): SELECT DATE(c.fecha_hora) AS fecha, COUNT(*) AS citas, SUM(c.estado = 'pendiente') AS pendientes, SUM(c.estado…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarCitaEnOperativas(Map<String, ?> p) {
        return db.lista("""
                SELECT DATE(c.fecha_hora) AS fecha, COUNT(*) AS citas, SUM(c.estado = 'pendiente') AS pendientes,
                       SUM(c.estado = 'confirmada') AS confirmadas, SUM(c.estado = 'cancelada') AS canceladas,
                       SUM(c.estado = 'atendida') AS atendidas, SUM(c.estado = 'finalizada') AS finalizadas,
                       COUNT(DISTINCT c.mascota_id) AS mascotas, COUNT(DISTINCT c.medico_id) AS veterinarios
                FROM cita c JOIN medico md ON md.id = c.medico_id
                WHERE
                """ + FILTRO_CITA + " GROUP BY DATE(c.fecha_hora) ORDER BY fecha", p);
    }

    /** Entidades que se describen por nombre en el encabezado del reporte. */
    public enum Entidad {
        /** Médico. */
        MEDICO("SELECT CONCAT(nombres, ' ', apellidos) FROM medico WHERE id = :id"),
        /** Especialidad. */
        ESPECIALIDAD("SELECT nombre FROM especializacion WHERE id = :id"),
        /** Medicamento. */
        MEDICAMENTO("SELECT CONCAT(codigo, ' · ', nombre) FROM medicamento WHERE id = :id"),
        /** Lote. */
        LOTE("SELECT numero FROM medicamento_lote WHERE id = :id"),
        /** Mascota. */
        MASCOTA("SELECT nombre_mascota FROM mascota WHERE mascota_id = :id"),
        /** Propietario. */
        PROPIETARIO("SELECT CONCAT(nombres, ' ', apellidos) FROM cliente WHERE cliente_id = :id");

        /** Consulta fija del nombre. */
        private final String sql;

        /**
         * @param sql consulta del nombre.
         */
        Entidad(String sql) {
            this.sql = sql;
        }
    }

    /**
     * @param entidad entidad.
     * @param id identificador.
     * @return nombre legible o null si no existe.
     */
    public String nombreDe(Entidad entidad, long id) {
        return db.valor(entidad.sql, Db.p("id", id), String.class);
    }

    /**
     * Facturación agrupada por veterinario o especialidad.
     *
     * @param campo "veterinario" o "especialidad" (lista cerrada).
     * @param p filtros del reporte.
     * @return filas agrupadas.
     */
    public List<Map<String, Object>> ventasAgrupadas(String campo, Map<String, ?> p) {
        if (!java.util.Set.of("veterinario", "especialidad").contains(campo)) {
            throw new IllegalArgumentException("Agrupación no permitida: " + campo);
        }
        return db.lista("SELECT f." + campo + " AS grupo, " + """
                       COUNT(*) AS facturas,
                       SUM(l.servicios) AS ingresos_consultas, SUM(l.medicamentos) AS ingresos_medicamentos,
                       SUM(f.descuento) AS descuentos, SUM(f.iva) AS iva, SUM(f.otros_impuestos) AS otros_impuestos,
                       SUM(f.total) AS total_facturado, SUM(f.pagado) AS recaudado, SUM(f.total - f.pagado) AS saldo
                FROM factura f JOIN
                """ + LINEAS_POR_FACTURA + " ON l.factura_id = f.id WHERE f.estado <> 'ANULADA' AND " + FILTRO_FACTURA
                + " GROUP BY f." + campo + " ORDER BY total_facturado DESC", p);
    }
}
