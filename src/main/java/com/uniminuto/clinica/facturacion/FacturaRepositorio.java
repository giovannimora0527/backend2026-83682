package com.uniminuto.clinica.facturacion;

import static com.uniminuto.clinica.calendario.AgendaServicio.noExiste;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class FacturaRepositorio {
    record Origen(long id, long cita, String propietario, String mascota, String veterinario,
                  String especialidad, boolean finalizada) {}
    record Concepto(Long servicio, Long movimiento, String nombre, int cantidad, @com.fasterxml.jackson.annotation.JsonFormat(shape=com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) BigDecimal precio,
                    BigDecimal iva, BigDecimal otros) {}
    record Previa(Origen origen, List<Linea> lineas, ImportesFactura.Importes importes, String huella) {}
    record Linea(Concepto origen, ImportesFactura.Importes importes) {}
    record Bloqueada(long id, BigDecimal total, BigDecimal pagado, String estado) {}

    private static final String ORIGEN = """
            SELECT a.id,c.id cita,CONCAT_WS(' ',cl.nombres,cl.apellidos) propietario,p.nombre_mascota mascota,
                   CONCAT_WS(' ',m.nombres,m.apellidos) veterinario,a.especialidad,
                   a.finalizada IS NOT NULL AND LOWER(c.estado)='finalizada' finalizada
            FROM atencion_veterinaria a JOIN cita c ON c.id=a.cita_id
            JOIN cliente cl ON cl.cliente_id=c.cliente_id JOIN mascota p ON p.mascota_id=c.mascota_id
            JOIN medico m ON m.id=a.medico_id
            """;
    private static final String SERVICIOS = """
            FROM atencion_servicio s LEFT JOIN factura_detalle fd
            ON fd.atencion_vigente=s.atencion_id AND fd.servicio_vigente=s.servicio_id
            WHERE s.atencion_id=? AND s.activo=1 AND fd.id IS NULL
            """;
    private static final String ENTREGAS = """
            FROM despacho_detalle d JOIN despacho_entrega e ON e.id=d.entrega_id
            JOIN formula_clinica f ON f.id=d.formula_id AND f.revision=d.revision
            JOIN formula_clinica_detalle p ON p.formula_id=d.formula_id AND p.revision=d.revision
                AND p.medicamento_id=d.medicamento_id
            LEFT JOIN factura_detalle fd ON fd.despacho_vigente=d.movimiento_id
            WHERE f.atencion_id=? AND f.estado='CONFIRMADA' AND e.confirmada IS NOT NULL
                AND e.revertida IS NULL AND fd.id IS NULL
            """;
    private static final String FACTURAS = """
            SELECT f.id,f.numero,f.atencion_id AS atencion,c.id AS cita,f.emitida,f.clinica,f.propietario,f.mascota,f.veterinario,
                   f.especialidad,f.descuento_pct,f.motivo_descuento,f.subtotal,f.descuento,f.iva,f.otros_impuestos,
                   f.total,f.pagado,IF(f.estado='ANULADA',0,f.total-f.pagado) saldo,f.estado,u.username responsable
            FROM factura f JOIN atencion_veterinaria a ON a.id=f.atencion_id JOIN cita c ON c.id=a.cita_id
            JOIN usuario u ON u.id=f.usuario_id
            """;

    private final JdbcTemplate jdbc;

    FacturaRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    Origen origen(long id, boolean bloqueo) {
        if (bloqueo) jdbc.query("SELECT id FROM atencion_veterinaria WHERE id=? FOR UPDATE",
                (r,i) -> r.getLong(1), id).stream().findFirst().orElseThrow(() -> noExiste("Atención"));
        return jdbc.query(ORIGEN + " WHERE a.id=?", this::mapearOrigen, id).stream().findFirst()
                .orElseThrow(() -> noExiste("Atención"));
    }

    private Origen mapearOrigen(java.sql.ResultSet r, int fila) throws java.sql.SQLException {
        return new Origen(r.getLong("id"),r.getLong("cita"),r.getString("propietario"),r.getString("mascota"),
                r.getString("veterinario"),r.getString("especialidad"),r.getBoolean("finalizada"));
    }

    List<Origen> elegibles(int offset) {
        String sql=ORIGEN+"""
                WHERE a.finalizada IS NOT NULL AND LOWER(c.estado)='finalizada'
                AND (EXISTS (SELECT 1
                """+SERVICIOS.replace("s.atencion_id=?","s.atencion_id=a.id")+") OR EXISTS (SELECT 1 "
                +ENTREGAS.replace("f.atencion_id=?","f.atencion_id=a.id") + ")) ORDER BY a.id DESC LIMIT 50 OFFSET ?";
        return jdbc.query(sql,this::mapearOrigen,offset);
    }

    List<Concepto> conceptos(long id, boolean bloquearEntregas) {
        // El mismo bloqueo se toma al revertir la entrega. Se vuelve a leer tras adquirirlo.
        if (bloquearEntregas) {
            jdbc.query("SELECT id FROM formula_clinica WHERE atencion_id=? ORDER BY id FOR UPDATE", (r,i)->r.getLong(1), id);
            var entregas = jdbc.query("SELECT DISTINCT e.id "+ENTREGAS+" ORDER BY e.id",(r,i)->r.getLong(1),id);
            for (long entrega : entregas) {
                jdbc.queryForObject("SELECT id FROM despacho_entrega WHERE id=? FOR UPDATE",Long.class,entrega);
            }
            var vigentes = jdbc.query("SELECT DISTINCT e.id "+ENTREGAS+" ORDER BY e.id",(r,i)->r.getLong(1),id);
            if (!entregas.equals(vigentes)) {
                throw com.uniminuto.clinica.calendario.AgendaServicio.conflicto("Cambió el despacho. Actualiza la vista previa.");
            }
        }
        var conceptos = jdbc.query("SELECT s.servicio_id,s.nombre,s.cantidad,s.valor,s.iva,s.impuesto "+SERVICIOS+
                " ORDER BY s.servicio_id",(r,i)->new Concepto(r.getLong(1),null,r.getString(2),r.getInt(3),
                r.getBigDecimal(4),r.getBigDecimal(5),r.getBigDecimal(6)),id);
        conceptos.addAll(jdbc.query("SELECT d.movimiento_id,p.nombre,d.cantidad,p.precio_estimado "+ENTREGAS+
                " ORDER BY e.id,d.movimiento_id",(r,i)->new Concepto(null,r.getLong(1),r.getString(2),r.getInt(3),
                r.getBigDecimal(4),BigDecimal.ZERO,BigDecimal.ZERO),id));
        return conceptos;
    }

    Map<String,Object> porClave(String clave) {
        return jdbc.queryForList("SELECT id,huella FROM factura WHERE clave=?",clave).stream().findFirst().orElse(null);
    }

    long emitir(Previa p,String clave,byte[] huella,long usuario,String numero,BigDecimal descuento,String motivo,String estado) {
        var o=p.origen();
        var i=p.importes();
        var llave=new GeneratedKeyHolder();
        jdbc.update(conexion->{
            var sql=conexion.prepareStatement("""
                    INSERT INTO factura(numero,atencion_id,usuario_id,clave,huella,emitida,clinica,propietario,mascota,
                       veterinario,especialidad,descuento_pct,motivo_descuento,subtotal,descuento,iva,otros_impuestos,total,estado)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """,Statement.RETURN_GENERATED_KEYS);
            Object[] datos={numero,o.id(),usuario,clave,huella,FacturaServicio.ahoraFactura(),"Clínica Veterinaria UNIMINUTO",
                    o.propietario(),o.mascota(),o.veterinario(),o.especialidad(),descuento,motivo,
                    i.subtotal(),i.descuento(),i.iva(),i.otros(),i.total(),estado};
            for(int idx=0;idx<datos.length;idx++) sql.setObject(idx+1,datos[idx]);
            return sql;
        },llave);
        long id=java.util.Objects.requireNonNull(llave.getKey()).longValue();
        for (var linea:p.lineas()) {
            var c=linea.origen();
            var l=linea.importes();
            jdbc.update("""
                    INSERT INTO factura_detalle(factura_id,atencion_servicio_id,servicio_id,despacho_movimiento_id,
                        concepto,cantidad,precio_unitario,subtotal,descuento,iva_pct,otros_pct,iva,otros_impuestos,total)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """,id,c.servicio()==null?null:o.id(),c.servicio(),c.movimiento(),c.nombre(),c.cantidad(),
                    c.precio(),l.subtotal(),l.descuento(),c.iva(),c.otros(),l.iva(),l.otros(),l.total());
        }
        cambiar(id,usuario,null,estado,null);
        return id;
    }

    Bloqueada bloquear(long id) {
        return jdbc.query("SELECT id,total,pagado,estado FROM factura WHERE id=? FOR UPDATE",
                (r,i)->new Bloqueada(r.getLong(1),r.getBigDecimal(2),r.getBigDecimal(3),r.getString(4)),id)
                .stream().findFirst().orElseThrow(()->noExiste("Factura"));
    }

    Map<String,Object> factura(long id) {
        return jdbc.queryForList(FACTURAS+" WHERE f.id=?",id).stream().findFirst()
                .orElseThrow(()->noExiste("Factura"));
    }

    List<Map<String,Object>> listar(LocalDate desde,LocalDate hasta,String propietario,String mascota,
                                   Long propietarioId,Long mascotaId,String estado,int offset) {
        return jdbc.queryForList(FACTURAS+"""
                WHERE (? IS NULL OR f.emitida>=?) AND (? IS NULL OR f.emitida<?)
                  AND (? IS NULL OR LOCATE(?,f.propietario)>0) AND (? IS NULL OR LOCATE(?,f.mascota)>0)
                  AND (? IS NULL OR c.cliente_id=?) AND (? IS NULL OR c.mascota_id=?)
                  AND (? IS NULL OR f.estado=?)
                ORDER BY f.emitida DESC,f.id DESC LIMIT 50 OFFSET ?
                """,desde,desde,hasta,hasta,propietario,propietario,mascota,mascota,propietarioId,propietarioId,
                mascotaId,mascotaId,estado,estado,offset);
    }

    List<Map<String,Object>> lineas(long id) {
        return jdbc.queryForList("""
                SELECT id,servicio_id,despacho_movimiento_id,concepto,cantidad,precio_unitario,
                       subtotal,descuento,iva_pct,otros_pct,iva,otros_impuestos,total
                FROM factura_detalle WHERE factura_id=? ORDER BY id
                """,id);
    }

    List<Map<String,Object>> historial(long id) {
        return jdbc.queryForList("""
                SELECT e.id,e.anterior,e.nuevo,e.motivo,e.fecha,u.username responsable
                FROM factura_estado e JOIN usuario u ON e.usuario_id=u.id WHERE factura_id=? ORDER BY e.id
                """,id);
    }

    List<Map<String,Object>> pagos(long id) {
        return jdbc.queryForList("""
                SELECT p.id,p.fecha,p.importe,p.metodo,p.referencia,p.estado,u.username responsable
                FROM factura_pago p JOIN usuario u ON p.usuario_id=u.id WHERE factura_id=? ORDER BY p.id
                """,id);
    }

    Map<String,Object> pagoPorClave(long id,String clave) {
        return jdbc.queryForList("SELECT id,importe,metodo,referencia FROM factura_pago WHERE factura_id=? AND clave=?",id,clave)
                .stream().findFirst().orElse(null);
    }

    void pagar(Bloqueada f,long usuario,String clave,BigDecimal importe,String metodo,String referencia) {
        jdbc.update("""
                INSERT INTO factura_pago(factura_id,usuario_id,clave,fecha,importe,metodo,referencia)
                VALUES (?,?,?,?,?,?,?)
                """,f.id(),usuario,clave,FacturaServicio.ahoraFactura(),importe,metodo,referencia);
        var pagado=f.pagado().add(importe);
        String estado=pagado.compareTo(f.total())==0?"PAGADA":"PAGO_PARCIAL";
        jdbc.update("UPDATE factura SET pagado=?,estado=? WHERE id=?",pagado,estado,f.id());
        if (!estado.equals(f.estado())) cambiar(f.id(),usuario,f.estado(),estado,null);
    }

    void anular(Bloqueada f,long usuario,String motivo) {
        jdbc.update("UPDATE factura SET estado='ANULADA' WHERE id=?",f.id());
        jdbc.update("UPDATE factura_detalle SET vigente=0 WHERE factura_id=?",f.id());
        cambiar(f.id(),usuario,f.estado(),"ANULADA",motivo);
    }

    private void cambiar(long id,long usuario,String anterior,String nuevo,String motivo) {
        jdbc.update("""
                INSERT INTO factura_estado(factura_id,usuario_id,anterior,nuevo,motivo,fecha) VALUES (?,?,?,?,?,?)
                """,id,usuario,anterior,nuevo,motivo,FacturaServicio.ahoraFactura());
    }
}
