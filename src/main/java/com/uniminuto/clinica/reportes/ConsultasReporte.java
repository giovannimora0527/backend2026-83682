package com.uniminuto.clinica.reportes;

import static com.uniminuto.clinica.reportes.ReporteDatos.*;

import java.util.List;

final class ConsultasReporte {
    private ConsultasReporte() {}

    private static final String FACTURA = """
            WITH base AS (
              SELECT f.*, a.medico_id,a.especializacion_id,
                EXISTS(SELECT 1 FROM factura_estado h WHERE h.factura_id=f.id AND h.nuevo='ANULADA'
                    AND h.fecha<:fin) anulada,
                COALESCE((SELECT SUM(p.importe) FROM factura_pago p WHERE p.factura_id=f.id
                    AND p.estado='REGISTRADO' AND p.fecha<:fin),0) pagado_corte
              FROM factura f JOIN atencion_veterinaria a ON a.id=f.atencion_id
              WHERE f.emitida>=:inicio AND f.emitida<:fin
                AND (:medico IS NULL OR a.medico_id=:medico)
                AND (:especialidad IS NULL OR a.especializacion_id=:especialidad)
            ), corte AS (
              SELECT base.*, CASE WHEN anulada THEN 'ANULADA' WHEN pagado_corte=total THEN 'PAGADA'
                WHEN pagado_corte>0 THEN 'PAGO_PARCIAL' ELSE 'PENDIENTE_PAGO' END estado_corte
              FROM base
            )
            """;
    private static final String CAJA = """
            FROM factura_pago p JOIN factura f ON f.id=p.factura_id
            JOIN atencion_veterinaria a ON a.id=f.atencion_id
            WHERE p.estado='REGISTRADO' AND p.fecha>=:inicio AND p.fecha<:fin
                AND NOT EXISTS(SELECT 1 FROM factura_estado h WHERE h.factura_id=f.id AND h.nuevo='ANULADA' AND h.fecha<:fin)
                AND (:medico IS NULL OR a.medico_id=:medico)
                AND (:especialidad IS NULL OR a.especializacion_id=:especialidad)
            """;
    private static final String CITA_ESTADO = """
            CASE LOWER(TRIM(c.estado)) WHEN 'programada' THEN 'pendiente' ELSE LOWER(TRIM(c.estado)) END
            """;
    private static final String CITA = """
            FROM cita c JOIN medico m ON m.id=c.medico_id
            JOIN mascota p ON p.mascota_id=c.mascota_id
            JOIN cliente cl ON cl.cliente_id=c.cliente_id
            LEFT JOIN atencion_veterinaria a ON a.cita_id=c.id
            LEFT JOIN especializacion e ON e.id=m.especializacion_id
            WHERE %s>=:inicio AND %s<:fin
                AND (:medico IS NULL OR c.medico_id=:medico)
                AND (:especialidad IS NULL OR COALESCE(a.especializacion_id,m.especializacion_id)=:especialidad)
                AND (:estado IS NULL OR %s=:estado)
            """;
    private static final String FILTRO_PACIENTE = """
             AND (:mascota IS NULL OR c.mascota_id=:mascota) AND (:propietario IS NULL OR c.cliente_id=:propietario)
            """;
    private static final String STOCK = """
            WITH saldos AS (
                SELECT m.id,m.nombre,m.codigo,m.activo,m.stock_minimo,
                  COALESCE(SUM(l.cantidad),0) fisico,
                  COALESCE(SUM(CASE WHEN m.activo=1 AND (l.fecha_vence IS NULL OR l.fecha_vence>=:hoy)
                    THEN l.cantidad END),0) disponible,
                  COALESCE(SUM(CASE WHEN l.fecha_vence<:hoy THEN l.cantidad END),0) vencido,
                  COALESCE(SUM(CASE WHEN l.fecha_vence>=:hoy AND l.fecha_vence<=:proximo THEN l.cantidad END),0) proximo,
                  COALESCE(SUM(l.cantidad*l.costo),0) costo,
                  COALESCE(SUM(CASE WHEN m.activo=1 AND (l.fecha_vence IS NULL OR l.fecha_vence>=:hoy)
                    THEN l.cantidad*m.precio_venta END),0) potencial
                FROM medicamento m LEFT JOIN inventario_lote l ON l.medicamento_id=m.id
                WHERE (:medicamento IS NULL OR m.id=:medicamento)
                GROUP BY m.id,m.nombre,m.codigo,m.activo,m.stock_minimo
            )
            """;

    static Consulta consulta(Tipo tipo,String agrupacion) {
        return switch(tipo) {
            case FINANCIERO -> financiero(agrupacion);
            case POR_MEDICO -> grupoFinanciero("medico_id","veterinario","Veterinario");
            case POR_ESPECIALIDAD -> grupoFinanciero("especializacion_id","especialidad","Especialidad");
            case FACTURAS -> new Consulta(FACTURA+"""
                    SELECT numero,emitida fecha,propietario,mascota,estado_corte estado,
                        CASE WHEN anulada THEN total ELSE 0 END importe_anulado,
                        CASE WHEN anulada THEN 0 ELSE total END emitido,
                        CASE WHEN anulada THEN 0 ELSE pagado_corte END pagado,
                        CASE WHEN anulada THEN 0 ELSE total-pagado_corte END saldo,
                        CASE WHEN anulada THEN 0 ELSE iva END iva,
                        CASE WHEN anulada THEN 0 ELSE otros_impuestos END otros,
                        CASE WHEN anulada THEN 0 ELSE descuento END descuento
                    FROM corte WHERE (:estado IS NULL OR estado_corte=:estado) ORDER BY emitida,id
                    """,List.of(texto("numero","Factura"),hora("fecha","Emisión"),texto("propietario","Propietario"),
                    texto("mascota","Mascota"),texto("estado","Estado al cierre"),dinero("importe_anulado","Valor anulado",true),
                    dinero("emitido","Emitido",true),dinero("pagado","Pagado al cierre",true),dinero("saldo","Saldo",true),
                    dinero("iva","IVA registrado",true),dinero("otros","Otros impuestos",true),dinero("descuento","Descuento",true)));
            case PAGOS -> new Consulta("SELECT p.id,f.numero,p.fecha,p.metodo,p.importe "+CAJA+" ORDER BY p.fecha,p.id",
                    List.of(id("id","Pago"),texto("numero","Factura"),hora("fecha","Fecha de pago"),texto("metodo","Método"),
                        dinero("importe","Recaudación",true)));
            case CONCEPTOS -> new Consulta(FACTURA+"""
                    SELECT CASE WHEN d.servicio_id IS NOT NULL THEN 'Servicio' ELSE 'Medicamento dispensado' END clase,
                        d.concepto,SUM(d.cantidad) cantidad,SUM(d.subtotal) subtotal,SUM(d.descuento) descuento,
                        SUM(d.iva) iva,SUM(d.otros_impuestos) otros,SUM(d.total) emitido
                    FROM corte f JOIN factura_detalle d ON d.factura_id=f.id
                    WHERE NOT f.anulada GROUP BY clase,d.concepto ORDER BY clase,d.concepto
                    """,List.of(texto("clase","Tipo"),texto("concepto","Concepto"),cantidad("cantidad","Unidades"),
                    dinero("subtotal","Subtotal",true),dinero("descuento","Descuento",true),dinero("iva","IVA",true),
                    dinero("otros","Otros impuestos",true),dinero("emitido","Valor facturado",true)));
            case EXISTENCIAS -> new Consulta(STOCK+"""
                    SELECT id,codigo,nombre,IF(activo=1,'Activo','Inactivo') estado,stock_minimo,
                           fisico,disponible,vencido,proximo,costo,potencial
                    FROM saldos WHERE :estado IS NULL OR
                      (:estado='agotado' AND activo=1 AND disponible=0) OR
                      (:estado='minimo' AND activo=1 AND disponible<=stock_minimo) OR
                      (:estado='vencido' AND vencido>0) OR (:estado='proximo' AND proximo>0)
                    ORDER BY nombre,id
                    """,List.of(id("id","Producto"),texto("codigo","Código"),texto("nombre","Producto"),
                    texto("estado","Estado"),id("stock_minimo","Stock mínimo"),cantidad("fisico","Físico"),
                    cantidad("disponible","Disponible"),cantidad("vencido","Vencido"),cantidad("proximo","Próximo a vencer"),
                    dinero("costo","Valor al costo",true),dinero("potencial","Potencial de venta",true)));
            case LOTES -> new Consulta("""
                    SELECT l.id,m.nombre,l.numero,l.fecha_ingreso,l.fecha_vence,l.cantidad fisico,
                      IF(m.activo=1 AND (l.fecha_vence IS NULL OR l.fecha_vence>=:hoy),l.cantidad,0) disponible,
                      IF(l.fecha_vence<:hoy,'Vencido',IF(m.activo=1,'Vigente','Inactivo')) estado,
                      l.costo,l.costo*l.cantidad valor,
                      IF(m.activo=1 AND (l.fecha_vence IS NULL OR l.fecha_vence>=:hoy),l.cantidad*m.precio_venta,0) potencial
                    FROM inventario_lote l JOIN medicamento m ON m.id=l.medicamento_id
                    WHERE (:medicamento IS NULL OR m.id=:medicamento) AND (:lote IS NULL OR l.id=:lote)
                      AND (:inicio IS NULL OR l.fecha_vence>=:inicio AND l.fecha_vence<:fin)
                    ORDER BY m.nombre,l.fecha_vence IS NULL,l.fecha_vence,l.id
                    """,List.of(id("id","Lote"),texto("nombre","Producto"),texto("numero","Número"),
                    fecha("fecha_ingreso","Ingreso"),fecha("fecha_vence","Vencimiento"),texto("estado","Estado"),
                    cantidad("fisico","Físico"),cantidad("disponible","Disponible"),dinero("costo","Costo unitario",false),
                    dinero("valor","Valor al costo",true),dinero("potencial","Potencial de venta",true)));
            case MOVIMIENTOS -> new Consulta("""
                    SELECT v.id,v.fecha,m.nombre,l.numero,v.tipo,v.cantidad,v.saldo,v.referencia,
                      IF(v.tipo='ENTRADA',v.cantidad,0) entradas,IF(v.tipo='SALIDA',v.cantidad,0) salidas,
                      IF(v.tipo='AJUSTE',v.cantidad,0) ajustes
                    FROM inventario_movimiento v JOIN inventario_lote l ON l.id=v.lote_id
                    JOIN medicamento m ON m.id=l.medicamento_id
                    WHERE v.fecha>=:inicio AND v.fecha<:fin
                      AND (:medicamento IS NULL OR m.id=:medicamento) AND (:lote IS NULL OR l.id=:lote)
                      AND (:estado IS NULL OR v.tipo=:estado) ORDER BY v.fecha,v.id
                    """,List.of(id("id","Movimiento"),hora("fecha","Fecha"),texto("nombre","Producto"),
                    texto("numero","Lote"),texto("tipo","Tipo"),id("cantidad","Cantidad"),id("saldo","Saldo tras movimiento"),
                    texto("referencia","Referencia"),cantidad("entradas","Entradas"),cantidad("salidas","Salidas"),cantidad("ajustes","Ajustes")));
            case DISPENSACIONES -> new Consulta("""
                    SELECT v.id,v.fecha,d.entrega_id,d.formula_id,m.nombre,l.numero,v.tipo,
                      IF(v.tipo='SALIDA',d.cantidad,0) entregado,IF(v.tipo='ENTRADA',d.cantidad,0) revertido,
                      IF(v.tipo='SALIDA',d.cantidad,-d.cantidad) neto
                    FROM despacho_detalle d JOIN despacho_entrega e ON e.id=d.entrega_id AND e.confirmada IS NOT NULL
                    JOIN inventario_movimiento v ON v.id IN(d.movimiento_id,d.reversion_movimiento_id)
                    JOIN inventario_lote l ON l.id=d.lote_id JOIN medicamento m ON m.id=d.medicamento_id
                    WHERE v.fecha>=:inicio AND v.fecha<:fin
                      AND (:medicamento IS NULL OR m.id=:medicamento) AND (:lote IS NULL OR l.id=:lote)
                    ORDER BY v.fecha,v.id
                    """,List.of(id("id","Movimiento"),hora("fecha","Fecha"),id("entrega_id","Entrega"),id("formula_id","Fórmula"),
                    texto("nombre","Medicamento"),texto("numero","Lote"),texto("tipo","Movimiento"),
                    cantidad("entregado","Entregado"),cantidad("revertido","Revertido"),cantidad("neto","Neto")));
            case CITAS, CONSULTAS, ATENCIONES -> citas(tipo);
            case CITAS_OPERATIVAS -> new Consulta("""
                    SELECT DATE(c.fecha_hora) fecha,c.medico_id,CONCAT_WS(' ',m.nombres,m.apellidos) veterinario,
                        COALESCE(a.especialidad,e.nombre,'Sin especialidad') especialidad,%s estado,COUNT(*) citas
                    """.formatted(CITA_ESTADO)+CITA.formatted("c.fecha_hora","c.fecha_hora",CITA_ESTADO)+
                    " GROUP BY fecha,c.medico_id,veterinario,especialidad,estado ORDER BY fecha,veterinario,especialidad,estado",
                    List.of(fecha("fecha","Fecha"),id("medico_id","Médico"),texto("veterinario","Veterinario"),
                        texto("especialidad","Especialidad"),texto("estado","Estado"),cantidad("citas","Citas")));
        };
    }

    private static Consulta financiero(String agrupacion) {
        String unidad=switch(agrupacion) {case "MES"->"%Y-%m";case "ANIO"->"%Y";default->"%Y-%m-%d";};
        String periodo="DATE_FORMAT(emitida,'"+unidad+"')";
        String pagos="DATE_FORMAT(p.fecha,'"+unidad+"')";
        String sql=FACTURA+"""
                , indicadores AS (
                  SELECT %s periodo,COUNT(IF(NOT anulada,1,NULL)) facturas,COUNT(IF(anulada,1,NULL)) anuladas,
                    COUNT(IF(estado_corte='PAGADA',1,NULL)) pagadas,COUNT(IF(estado_corte='PAGO_PARCIAL',1,NULL)) parciales,
                    COUNT(IF(estado_corte='PENDIENTE_PAGO',1,NULL)) pendientes,
                    SUM(IF(anulada,total,0)) importe_anulado,SUM(IF(anulada,0,total)) emitido,0 recaudado,
                    SUM(IF(anulada,0,total-pagado_corte)) saldo,SUM(IF(anulada,0,iva)) iva,
                    SUM(IF(anulada,0,otros_impuestos)) otros,SUM(IF(anulada,0,descuento)) descuento
                  FROM corte GROUP BY periodo
                  UNION ALL SELECT %s periodo,0,0,0,0,0,0,0,SUM(p.importe),0,0,0,0
                """.formatted(periodo,pagos)+CAJA+"""
                 GROUP BY periodo)
                SELECT periodo,SUM(facturas) facturas,SUM(anuladas) anuladas,SUM(pagadas) pagadas,
                  SUM(parciales) parciales,SUM(pendientes) pendientes,SUM(importe_anulado) importe_anulado,
                  SUM(emitido) emitido,SUM(recaudado) recaudado,SUM(saldo) saldo,SUM(iva) iva,
                  SUM(otros) otros,SUM(descuento) descuento FROM indicadores GROUP BY periodo ORDER BY periodo
                """;
        return new Consulta(sql,List.of(texto("periodo","Período"),cantidad("facturas","Facturas válidas"),
                cantidad("anuladas","Anuladas"),cantidad("pagadas","Pagadas"),cantidad("parciales","Parciales"),
                cantidad("pendientes","Pendientes"),dinero("importe_anulado","Valor anulado",true),dinero("emitido","Emitido",true),
                dinero("recaudado","Recaudado",true),dinero("saldo","Saldo al cierre",true),dinero("iva","IVA registrado",true),
                dinero("otros","Otros impuestos",true),dinero("descuento","Descuento",true)));
    }

    private static Consulta grupoFinanciero(String id,String etiqueta,String titulo) {
        String sql=FACTURA+"""
                , indicadores AS (
                   SELECT %s agrupador,%s nombre,COUNT(*) facturas,SUM(total) emitido,0 recaudado,
                       SUM(total-pagado_corte) saldo,SUM(iva) iva,SUM(otros_impuestos) otros,SUM(descuento) descuento
                   FROM corte WHERE NOT anulada GROUP BY agrupador,nombre
                   UNION ALL SELECT a.%s agrupador,f.%s nombre,0,0,SUM(p.importe),0,0,0,0
                """.formatted(id,etiqueta,id,etiqueta)+CAJA+"""
                   GROUP BY agrupador,nombre
                )
                SELECT agrupador,nombre,SUM(facturas) facturas,SUM(emitido) emitido,SUM(recaudado) recaudado,
                  SUM(saldo) saldo,SUM(iva) iva,SUM(otros) otros,SUM(descuento) descuento
                FROM indicadores GROUP BY agrupador,nombre ORDER BY nombre,agrupador
                """;
        return new Consulta(sql,List.of(id("agrupador","ID"),texto("nombre",titulo),cantidad("facturas","Facturas"),
                dinero("emitido","Emitido",true),dinero("recaudado","Recaudado",true),dinero("saldo","Saldo",true),dinero("iva","IVA",true),
                dinero("otros","Otros impuestos",true),dinero("descuento","Descuento",true)));
    }

    private static Consulta citas(Tipo tipo) {
        boolean atenciones=tipo==Tipo.ATENCIONES;
        String marca=atenciones?"a.finalizada":"c.fecha_hora";
        String origen=CITA.formatted(marca,marca,CITA_ESTADO)+FILTRO_PACIENTE;
        if(atenciones || tipo==Tipo.CONSULTAS) {
            if(!atenciones) origen+=" AND "+CITA_ESTADO+" IN ('atendida','finalizada')";
            return new Consulta("""
                    SELECT c.medico_id,CONCAT_WS(' ',m.nombres,m.apellidos) veterinario,
                        COALESCE(a.especializacion_id,m.especializacion_id) especialidad_id,
                        COALESCE(a.especialidad,e.nombre,'Sin especialidad') especialidad,
                        COUNT(*) atenciones,COUNT(DISTINCT c.mascota_id) mascotas
                    """+origen+" GROUP BY c.medico_id,veterinario,especialidad_id,especialidad ORDER BY veterinario,especialidad",
                    List.of(id("medico_id","Médico"),texto("veterinario","Veterinario"),id("especialidad_id","ID especialidad"),
                        texto("especialidad","Especialidad"),cantidad("atenciones",atenciones?"Atenciones finalizadas":"Consultas atendidas"),id("mascotas","Mascotas distintas")));
        }
        return new Consulta("""
                SELECT c.id,c.fecha_hora fecha,c.mascota_id,p.nombre_mascota mascota,
                    CONCAT_WS(' ',cl.nombres,cl.apellidos) propietario,CONCAT_WS(' ',m.nombres,m.apellidos) veterinario,
                    COALESCE(a.especialidad,e.nombre,'Sin especialidad') especialidad,%s estado,
                    CASE WHEN %s IN ('atendida','finalizada') THEN 1 ELSE 0 END atendidas
                """.formatted(CITA_ESTADO,CITA_ESTADO)+origen+" ORDER BY c.fecha_hora,c.id",
                List.of(id("id","Cita"),hora("fecha","Fecha"),texto("mascota","Mascota"),texto("propietario","Propietario"),
                    texto("veterinario","Veterinario"),texto("especialidad","Especialidad"),texto("estado","Estado"),
                    cantidad("atendidas","Consultas atendidas")));
    }

    static String mascotasFinalizadas() {
        return "SELECT COUNT(DISTINCT c.mascota_id) "+CITA.formatted("a.finalizada","a.finalizada",CITA_ESTADO)+FILTRO_PACIENTE;
    }
}
