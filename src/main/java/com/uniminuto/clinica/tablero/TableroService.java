package com.uniminuto.clinica.tablero;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula los pendientes de cada perfil para conectar el flujo de trabajo de la clínica:
 * <ol>
 *   <li>Recepción agenda y confirma citas.</li>
 *   <li>El médico atiende, formula y finaliza la atención.</li>
 *   <li>Despacho entrega los medicamentos de las fórmulas confirmadas.</li>
 *   <li>Recepción factura las atenciones listas y registra los pagos.</li>
 *   <li>El administrador supervisa todo y el inventario.</li>
 * </ol>
 */
@Service
public class TableroService {

    /** Estados de una cita que todavía ocupa agenda. */
    private static final String RESERVADAS = "('pendiente', 'programada', 'confirmada')";

    /** Acceso a datos. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public TableroService(Db db) {
        this.db = db;
    }

    /**
     * Pendientes del usuario autenticado según su perfil.
     *
     * @return tarjetas de pendientes.
     */
    public List<Map<String, Object>> pendientes() {
        SesionUsuario s = SesionActual.obtener();
        List<Map<String, Object>> r = new ArrayList<>();
        if (s.es(Perfiles.RECEPCION, Perfiles.ADMINISTRADOR)) {
            recepcion(r);
        }
        if (s.es(Perfiles.MEDICO)) {
            medico(r, SesionActual.medicoId());
        }
        if (s.es(Perfiles.DESPACHO_MEDICO, Perfiles.ADMINISTRADOR)) {
            despacho(r, s.es(Perfiles.ADMINISTRADOR));
        }
        if (s.es(Perfiles.ADMINISTRADOR, Perfiles.DESPACHO_MEDICO)) {
            inventario(r);
        }
        return r;
    }

    /**
     * Pendientes de recepción: agenda del día, citas por confirmar, atenciones por facturar y cobros.
     *
     * @param r lista donde se agregan las tarjetas.
     */
    private void recepcion(List<Map<String, Object>> r) {
        LocalDate hoy = Tiempo.hoy();
        r.add(tarjeta("citas-hoy", "Citas de hoy", "Citas reservadas para hoy en todos los médicos.",
                db.contar("SELECT COUNT(*) FROM cita WHERE estado IN " + RESERVADAS
                        + " AND fecha_hora >= :d AND fecha_hora < :h", Db.p("d", hoy.atStartOfDay(), "h", hoy.plusDays(1).atStartOfDay())),
                "/inicio/citas", "info"));
        r.add(tarjeta("por-confirmar", "Citas por confirmar", "Citas futuras pendientes o programadas: confirma con el propietario.",
                db.contar("SELECT COUNT(*) FROM cita WHERE estado IN ('pendiente', 'programada') AND fecha_hora >= :a",
                        Db.p("a", Tiempo.ahora())), "/inicio/citas", "aviso"));
        r.add(tarjeta("por-facturar", "Atenciones por facturar",
                "El médico finalizó la atención y despacho ya procesó las fórmulas: lista para facturar.",
                db.contar("""
                        SELECT COUNT(*) FROM atencion_medica a
                        WHERE a.fecha_finalizacion IS NOT NULL
                          AND NOT EXISTS (SELECT 1 FROM factura f WHERE f.atencion_id = a.id AND f.estado <> 'ANULADA')
                          AND NOT EXISTS (SELECT 1 FROM formula_clinica fc WHERE fc.atencion_id = a.id AND fc.estado = 'CONFIRMADA'
                                            AND fc.estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION'))
                        """, Map.of()), "/inicio/facturacion", "accion"));
        r.add(tarjeta("esperando-despacho", "Esperando despacho",
                "Atenciones finalizadas cuyas fórmulas aún no entrega despacho; se podrán facturar después.",
                db.contar("""
                        SELECT COUNT(DISTINCT a.id) FROM atencion_medica a
                        JOIN formula_clinica fc ON fc.atencion_id = a.id AND fc.estado = 'CONFIRMADA'
                         AND fc.estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION')
                        WHERE a.fecha_finalizacion IS NOT NULL
                          AND NOT EXISTS (SELECT 1 FROM factura f WHERE f.atencion_id = a.id AND f.estado <> 'ANULADA')
                        """, Map.of()), "/inicio/facturacion", "info"));
        r.add(tarjeta("por-cobrar", "Facturas por cobrar", "Facturas pendientes de pago o con pago parcial.",
                db.contar("SELECT COUNT(*) FROM factura WHERE estado IN ('PENDIENTE_PAGO', 'PAGO_PARCIAL')", Map.of()),
                "/inicio/facturacion", "aviso"));
    }

    /**
     * Pendientes del médico: citas de hoy por atender, atenciones sin finalizar y fórmulas en borrador.
     *
     * @param r lista donde se agregan las tarjetas.
     * @param medico identificador del médico autenticado.
     */
    private void medico(List<Map<String, Object>> r, long medico) {
        LocalDate hoy = Tiempo.hoy();
        r.add(tarjeta("mis-citas-hoy", "Mis citas de hoy", "Citas reservadas hoy en tu agenda: ábrelas desde el calendario para atender.",
                db.contar("SELECT COUNT(*) FROM cita WHERE medico_id = :m AND estado IN " + RESERVADAS
                        + " AND fecha_hora >= :d AND fecha_hora < :h",
                        Db.p("m", medico, "d", hoy.atStartOfDay(), "h", hoy.plusDays(1).atStartOfDay())), "/inicio/citas", "accion"));
        r.add(tarjeta("atenciones-abiertas", "Atenciones sin finalizar",
                "Atenciones abiertas: al finalizarlas pasan a facturación y sus fórmulas a despacho.",
                db.contar("SELECT COUNT(*) FROM atencion_medica WHERE medico_id = :m AND fecha_finalizacion IS NULL",
                        Db.p("m", medico)), "/inicio/citas", "aviso"));
        r.add(tarjeta("formulas-borrador", "Fórmulas en borrador", "Confírmalas para que despacho pueda entregar los medicamentos.",
                db.contar("SELECT COUNT(*) FROM formula_clinica WHERE medico_id = :m AND estado = 'BORRADOR'",
                        Db.p("m", medico)), "/inicio/citas", "aviso"));
        r.add(tarjeta("formulas-en-despacho", "Fórmulas en despacho", "Fórmulas confirmadas que despacho aún no entrega por completo.",
                db.contar("""
                        SELECT COUNT(*) FROM formula_clinica WHERE medico_id = :m AND estado = 'CONFIRMADA'
                          AND estado_despacho IN ('PENDIENTE_DESPACHO', 'EN_PREPARACION', 'DESPACHO_PARCIAL', 'PENDIENTE_DISPONIBILIDAD')
                        """, Db.p("m", medico)), "/inicio/formulas", "info"));
    }

    /**
     * Pendientes de despacho: fórmulas confirmadas por los médicos que esperan entrega.
     *
     * @param r lista donde se agregan las tarjetas.
     * @param soloLectura true para el administrador (consulta, no opera el despacho).
     */
    private void despacho(List<Map<String, Object>> r, boolean soloLectura) {
        String url = soloLectura ? null : "/inicio/despacho";
        r.add(tarjeta("por-despachar", "Fórmulas por despachar", "Fórmulas confirmadas por el médico que aún no se procesan.",
                contarDespacho("'PENDIENTE_DESPACHO'"), url, "accion"));
        r.add(tarjeta("en-preparacion", "En preparación", "Despachos preparados pendientes de confirmar la entrega.",
                contarDespacho("'EN_PREPARACION'"), url, "aviso"));
        r.add(tarjeta("sin-existencias", "Parciales o sin existencias",
                "Fórmulas con entrega parcial o pendientes por disponibilidad de inventario.",
                contarDespacho("'DESPACHO_PARCIAL', 'PENDIENTE_DISPONIBILIDAD'"), url, "info"));
    }

    /**
     * Alertas de inventario: productos agotados y con lotes vencidos o próximos a vencer.
     *
     * @param r lista donde se agregan las tarjetas.
     */
    private void inventario(List<Map<String, Object>> r) {
        Map<String, Object> p = Db.p("hoy", Tiempo.hoy(), "limite", Tiempo.limiteProximoVencer());
        r.add(tarjeta("agotados", "Productos agotados o bajo mínimo", "Productos activos sin existencias vigentes o por debajo del stock mínimo.",
                db.contar("""
                        SELECT COUNT(*) FROM (
                          SELECT m.id, m.stock_minimo,
                                 COALESCE(SUM(CASE WHEN l.fecha_vence IS NULL OR l.fecha_vence >= :hoy THEN l.cantidad END), 0) AS disponible
                          FROM medicamento m LEFT JOIN medicamento_lote l ON l.medicamento_id = m.id
                          WHERE m.activo = 1 GROUP BY m.id, m.stock_minimo) x
                        WHERE x.disponible <= x.stock_minimo
                        """, p), "/inicio/medicamentos", "aviso"));
        r.add(tarjeta("por-vencer", "Lotes vencidos o por vencer", "Lotes con existencias vencidos o que vencen en los próximos 30 días.",
                db.contar("""
                        SELECT COUNT(*) FROM medicamento_lote
                        WHERE cantidad > 0 AND fecha_vence IS NOT NULL AND fecha_vence <= :limite
                        """, p), "/inicio/medicamentos", "info"));
    }

    /**
     * @param estados estados de despacho entre comillas simples, separados por coma.
     * @return número de fórmulas confirmadas en esos estados.
     */
    private long contarDespacho(String estados) {
        return db.contar("SELECT COUNT(*) FROM formula_clinica WHERE estado = 'CONFIRMADA' AND estado_despacho IN ("
                + estados + ")", Map.of());
    }

    /**
     * Construye una tarjeta del tablero.
     *
     * @param clave identificador de la tarjeta.
     * @param titulo título visible.
     * @param descripcion explicación del pendiente y del siguiente paso.
     * @param cantidad número de registros pendientes.
     * @param url pantalla donde se resuelve (null si el perfil solo consulta).
     * @param nivel accion, aviso o info (define el color).
     * @return tarjeta.
     */
    private static Map<String, Object> tarjeta(String clave, String titulo, String descripcion, long cantidad,
                                               String url, String nivel) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("clave", clave);
        t.put("titulo", titulo);
        t.put("descripcion", descripcion);
        t.put("cantidad", cantidad);
        t.put("url", url);
        t.put("nivel", nivel);
        return t;
    }
}
