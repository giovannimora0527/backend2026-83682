package com.uniminuto.clinica.reportes;

import static com.uniminuto.clinica.reportes.ReporteDatos.*;

import java.util.List;

final class ExistenciasHistoricas {
    private ExistenciasHistoricas() {}

    private static final String REGISTRO = """
            WITH movimientos AS (
              SELECT v.lote_id, v.id, v.fecha, v.saldo,
                CASE WHEN v.tipo = 'SALIDA' THEN -v.cantidad ELSE v.cantidad END variacion,
                LAG(v.saldo, 1, 0) OVER (PARTITION BY v.lote_id ORDER BY v.fecha, v.id) anterior,
                ROW_NUMBER() OVER (PARTITION BY v.lote_id ORDER BY v.fecha DESC, v.id DESC) ultimo
              FROM inventario_movimiento v JOIN inventario_lote l ON l.id = v.lote_id
              WHERE :medicamento IS NULL OR l.medicamento_id = :medicamento
            ), saldos AS (
              SELECT lote_id,
                MAX(CASE WHEN ultimo = 1 THEN saldo END) saldo_registrado,
                MIN(fecha) primer_registro,
                SUM(CASE WHEN saldo <> anterior + variacion THEN 1 ELSE 0 END) inconsistencias,
                SUM(CASE WHEN fecha < :inicio THEN variacion ELSE 0 END) apertura,
                SUM(CASE WHEN fecha >= :inicio AND fecha < :fin AND variacion > 0 THEN variacion ELSE 0 END) aumentos,
                SUM(CASE WHEN fecha >= :inicio AND fecha < :fin AND variacion < 0 THEN -variacion ELSE 0 END) disminuciones,
                SUM(CASE WHEN fecha < :fin THEN variacion ELSE 0 END) cierre
              FROM movimientos GROUP BY lote_id
            )
            """;

    static final String LOTE_SIN_CONCILIAR = REGISTRO + """
            SELECT l.id
            FROM inventario_lote l LEFT JOIN saldos s ON s.lote_id = l.id
            WHERE (:medicamento IS NULL OR l.medicamento_id = :medicamento)
              AND (l.fecha_ingreso < :fin OR s.primer_registro < :fin)
              AND (COALESCE(s.saldo_registrado, 0) <> l.cantidad
                OR s.inconsistencias > 0 OR s.primer_registro < l.fecha_ingreso)
            ORDER BY l.id LIMIT 1
            """;

    static Consulta consulta() {
        return new Consulta(REGISTRO + """
                SELECT l.id lote_id, m.id medicamento_id, m.codigo, m.nombre,
                       l.numero, l.fecha_vence, s.apertura, s.aumentos, s.disminuciones,
                       s.cierre, s.cierre * l.costo valor_costo
                FROM saldos s JOIN inventario_lote l ON l.id = s.lote_id
                JOIN medicamento m ON m.id = l.medicamento_id
                WHERE s.primer_registro < :fin
                ORDER BY m.nombre, m.id, l.id
                """, List.of(id("lote_id","ID lote"), id("medicamento_id","ID producto"),
                texto("codigo","Código actual"), texto("nombre","Producto actual"), texto("numero","Lote"),
                fecha("fecha_vence","Vencimiento"), cantidad("apertura","Apertura"),
                cantidad("aumentos","Aumentos"), cantidad("disminuciones","Disminuciones"),
                cantidad("cierre","Cierre físico"), dinero("valor_costo","Valor al costo del lote",true)));
    }
}
