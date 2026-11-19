package com.uniminuto.clinica.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;



class FinanzasReporteRepositorio {
    record Resumen(long facturasValidas, long pagadas, long parciales, long pendientes,
                   long anuladas, BigDecimal importeAnulado, BigDecimal emitido,
                   BigDecimal recaudado, BigDecimal saldoAlCierre, BigDecimal ivaRegistrado,
                   BigDecimal otrosImpuestosRegistrados, BigDecimal descuentos) {}

    private static final String RESUMEN = """
            WITH cohorte AS (
                SELECT f.id,f.total,f.iva,f.otros_impuestos,f.descuento,
                    EXISTS (SELECT 1 FROM factura_estado h
                      WHERE h.factura_id=f.id AND h.nuevo='ANULADA' AND h.fecha<:fin) anulada,
                    COALESCE((SELECT SUM(p.importe) FROM factura_pago p
                      WHERE p.factura_id=f.id AND p.estado='REGISTRADO' AND p.fecha<:fin),0) pagado
                FROM factura f WHERE f.emitida>=:inicio AND f.emitida<:fin
            ),
            caja AS (
                SELECT COALESCE(SUM(p.importe),0) importe
                FROM factura_pago p JOIN factura f ON f.id=p.factura_id
                WHERE p.estado='REGISTRADO' AND p.fecha>=:inicio AND p.fecha<:fin
                  AND NOT EXISTS (SELECT 1 FROM factura_estado h
                    WHERE h.factura_id=f.id AND h.nuevo='ANULADA' AND h.fecha<:fin)
            )
            SELECT COUNT(CASE WHEN NOT anulada THEN 1 END) facturas_validas,
                   COUNT(CASE WHEN NOT anulada AND pagado=total THEN 1 END) pagadas,
                   COUNT(CASE WHEN NOT anulada AND pagado>0 AND pagado<total THEN 1 END) parciales,
                   COUNT(CASE WHEN NOT anulada AND pagado=0 AND total>0 THEN 1 END) pendientes,
                   COUNT(CASE WHEN anulada THEN 1 END) anuladas,
                   COALESCE(SUM(CASE WHEN anulada THEN total END),0) importe_anulado,
                   COALESCE(SUM(CASE WHEN NOT anulada THEN total END),0) emitido,
                   (SELECT importe FROM caja) recaudado,
                   COALESCE(SUM(CASE WHEN NOT anulada THEN total-pagado END),0) saldo,
                   COALESCE(SUM(CASE WHEN NOT anulada THEN iva END),0) iva,
                   COALESCE(SUM(CASE WHEN NOT anulada THEN otros_impuestos END),0) otros,
                   COALESCE(SUM(CASE WHEN NOT anulada THEN descuento END),0) descuentos
            FROM cohorte
            """;

    private final NamedParameterJdbcTemplate jdbc;

    FinanzasReporteRepositorio(NamedParameterJdbcTemplate jdbc) {
        this.jdbc=jdbc;
    }

    Resumen resumen(LocalDate desde, LocalDate hasta) {
        if(desde==null || hasta==null || hasta.isBefore(desde) || hasta.getYear()>9998) {
            throw new IllegalArgumentException("Selecciona un rango de fechas válido.");
        }
        return jdbc.queryForObject(RESUMEN,Map.of("inicio",desde.atStartOfDay(),"fin",hasta.plusDays(1).atStartOfDay()),
                (r,i)->new Resumen(r.getLong("facturas_validas"),r.getLong("pagadas"),r.getLong("parciales"),
                    r.getLong("pendientes"),r.getLong("anuladas"),r.getBigDecimal("importe_anulado"),
                    r.getBigDecimal("emitido"),r.getBigDecimal("recaudado"),r.getBigDecimal("saldo"),
                    r.getBigDecimal("iva"),r.getBigDecimal("otros"),r.getBigDecimal("descuentos")));
    }
}
