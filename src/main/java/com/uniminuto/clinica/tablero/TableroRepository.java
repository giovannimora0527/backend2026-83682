package com.uniminuto.clinica.tablero;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link TableroService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class TableroRepository {

    /** Estados de una cita que todavía ocupa agenda. */
    private static final String RESERVADAS = "('pendiente', 'programada', 'confirmada')";

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public TableroRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link TableroService} (citas): SELECT COUNT(*) FROM cita WHERE estado IN
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarCita(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM cita WHERE estado IN " + RESERVADAS
                        + " AND fecha_hora >= :d AND fecha_hora < :h", p);
    }

    /**
     * Usada por {@link TableroService} (citas): SELECT COUNT(*) FROM cita WHERE estado IN ('pendiente', 'programada') AND fecha_hora >= :a
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarCitaEnCitas(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM cita WHERE estado IN ('pendiente', 'programada') AND fecha_hora >= :a", p);
    }

    /**
     * Usada por {@link TableroService} (facturacion): SELECT COUNT(*) FROM atencion_medica a WHERE a.fecha_finalizacion IS NOT NULL AND NOT EXISTS (SELECT 1 FROM fa…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarAtencionMedica(Map<String, ?> p) {
        return db.contar("""
                        SELECT COUNT(*) FROM atencion_medica a
                        WHERE a.fecha_finalizacion IS NOT NULL
                          AND NOT EXISTS (SELECT 1 FROM factura f WHERE f.atencion_id = a.id AND f.estado <> 'ANULADA')
                          AND NOT EXISTS (SELECT 1 FROM formula_clinica fc WHERE fc.atencion_id = a.id AND fc.estado = 'CONFIRMADA'
                                            AND fc.estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION'))
                        """, p);
    }

    /**
     * Usada por {@link TableroService} (facturacion): SELECT COUNT(DISTINCT a.id) FROM atencion_medica a JOIN formula_clinica fc ON fc.atencion_id = a.id AND fc.est…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarAtencionMedicaEnFacturacion(Map<String, ?> p) {
        return db.contar("""
                        SELECT COUNT(DISTINCT a.id) FROM atencion_medica a
                        JOIN formula_clinica fc ON fc.atencion_id = a.id AND fc.estado = 'CONFIRMADA'
                         AND fc.estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION')
                        WHERE a.fecha_finalizacion IS NOT NULL
                          AND NOT EXISTS (SELECT 1 FROM factura f WHERE f.atencion_id = a.id AND f.estado <> 'ANULADA')
                        """, p);
    }

    /**
     * Usada por {@link TableroService} (facturacion): SELECT COUNT(*) FROM factura WHERE estado IN ('PENDIENTE_PAGO', 'PAGO_PARCIAL')
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarFactura(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM factura WHERE estado IN ('PENDIENTE_PAGO', 'PAGO_PARCIAL')", p);
    }

    /**
     * Usada por {@link TableroService} (medico): SELECT COUNT(*) FROM cita WHERE medico_id = :m AND estado IN
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarCitaEnMedico(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM cita WHERE medico_id = :m AND estado IN " + RESERVADAS
                        + " AND fecha_hora >= :d AND fecha_hora < :h", p);
    }

    /**
     * Usada por {@link TableroService} (medico): SELECT COUNT(*) FROM atencion_medica WHERE medico_id = :m AND fecha_finalizacion IS NULL
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarAtencionMedicaEnMedico(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM atencion_medica WHERE medico_id = :m AND fecha_finalizacion IS NULL", p);
    }

    /**
     * Usada por {@link TableroService} (medico): SELECT COUNT(*) FROM formula_clinica WHERE medico_id = :m AND estado = 'BORRADOR'
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarFormulaClinica(Map<String, ?> p) {
        return db.contar("SELECT COUNT(*) FROM formula_clinica WHERE medico_id = :m AND estado = 'BORRADOR'", p);
    }

    /**
     * Usada por {@link TableroService} (medico): SELECT COUNT(*) FROM formula_clinica WHERE medico_id = :m AND estado = 'CONFIRMADA' AND estado_despacho IN ('P…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarFormulaClinicaEnMedico(Map<String, ?> p) {
        return db.contar("""
                        SELECT COUNT(*) FROM formula_clinica WHERE medico_id = :m AND estado = 'CONFIRMADA'
                          AND estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION', 'DESPACHO_PARCIAL', 'PENDIENTE_DISPONIBILIDAD')
                        """, p);
    }

    /**
     * Usada por {@link TableroService} (inventario): SELECT COUNT(*) FROM ( SELECT m.id, m.stock_minimo, COALESCE(SUM(CASE WHEN l.fecha_vence IS NULL OR l.fecha_ve…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarMedicamento(Map<String, ?> p) {
        return db.contar("""
                        SELECT COUNT(*) FROM (
                          SELECT m.id, m.stock_minimo,
                                 COALESCE(SUM(CASE WHEN l.fecha_vence IS NULL OR l.fecha_vence >= :hoy THEN l.cantidad END), 0) AS disponible
                          FROM medicamento m LEFT JOIN medicamento_lote l ON l.medicamento_id = m.id
                          WHERE m.activo = 1 GROUP BY m.id, m.stock_minimo) x
                        WHERE x.disponible <= x.stock_minimo
                        """, p);
    }

    /**
     * Usada por {@link TableroService} (inventario): SELECT COUNT(*) FROM medicamento_lote WHERE cantidad > 0 AND fecha_vence IS NOT NULL AND fecha_vence <= :limit…
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public long contarMedicamentoLote(Map<String, ?> p) {
        return db.contar("""
                        SELECT COUNT(*) FROM medicamento_lote
                        WHERE cantidad > 0 AND fecha_vence IS NOT NULL AND fecha_vence <= :limite
                        """, p);
    }

    /**
     * @param estados estados de despacho.
     * @return fórmulas confirmadas en esos estados.
     */
    public long contarFormulasConfirmadas(List<String> estados) {
        return db.contar("SELECT COUNT(*) FROM formula_clinica WHERE estado = 'CONFIRMADA' AND estado_despacho IN (:estados)",
                Db.p("estados", estados));
    }
}
