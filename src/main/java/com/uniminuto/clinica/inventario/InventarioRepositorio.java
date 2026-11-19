package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
public class InventarioRepositorio {
    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert medicamentos;
    private final SimpleJdbcInsert lotes;
    private final SimpleJdbcInsert cargas;

    public InventarioRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        medicamentos = new SimpleJdbcInsert(jdbc).withTableName("medicamento").usingGeneratedKeyColumns("id")
                .usingColumns("codigo","nombre","descripcion","presentacion","tipo","invima","precio_compra",
                        "precio_venta","stock_minimo","fecha_compra","fecha_ingreso","fecha_proximo_pedido",
                        "fecha_vence","activo","fecha_creacion_registro").withoutTableColumnMetaDataAccess();
        lotes = new SimpleJdbcInsert(jdbc).withTableName("inventario_lote").usingGeneratedKeyColumns("id")
                .usingColumns("medicamento_id","numero","costo","fecha_ingreso","fecha_vence","cantidad")
                .withoutTableColumnMetaDataAccess();
        cargas = new SimpleJdbcInsert(jdbc).withTableName("inventario_importacion").usingGeneratedKeyColumns("id")
                .usingColumns("usuario_id","archivo","fecha","filas","rechazados").withoutTableColumnMetaDataAccess();
    }

    List<Ficha> catalogo(LocalDate hoy, Long id) {
        String consulta = """
                SELECT m.*, COALESCE(l.existencia,0) AS existencia,
                    CASE WHEN m.activo THEN COALESCE(l.disponible,0) ELSE 0 END AS disponible,
                    COALESCE(l.vencido,0) AS vencido, COALESCE(l.proximo,0) AS proximo,
                    l.vencimiento_lote
                FROM medicamento m LEFT JOIN (
                    SELECT medicamento_id, SUM(cantidad) AS existencia,
                        SUM(CASE WHEN fecha_vence IS NULL OR fecha_vence >= ? THEN cantidad ELSE 0 END) AS disponible,
                        MAX(cantidad > 0 AND fecha_vence < ?) AS vencido,
                        MAX(cantidad > 0 AND fecha_vence BETWEEN ? AND ?) AS proximo,
                        MIN(CASE WHEN cantidad > 0 THEN fecha_vence END) AS vencimiento_lote
                    FROM inventario_lote GROUP BY medicamento_id
                ) l ON l.medicamento_id=m.id
                """;
        return id == null
                ? jdbc.query(consulta + " ORDER BY m.nombre,m.id", this::ficha, hoy,hoy,hoy,hoy.plusDays(30))
                : jdbc.query(consulta + " WHERE m.id=?", this::ficha, hoy,hoy,hoy,hoy.plusDays(30),id);
    }

    private Ficha ficha(ResultSet r, int fila) throws SQLException {
        var producto = new Producto(r.getString("codigo"),r.getString("nombre"),r.getString("descripcion"),
                r.getString("presentacion"),TipoProducto.valueOf(r.getString("tipo")),r.getString("invima"),
                r.getBigDecimal("precio_compra"),r.getBigDecimal("precio_venta"),r.getInt("stock_minimo"),
                r.getObject("fecha_compra",LocalDate.class),r.getObject("fecha_ingreso",LocalDate.class),
                r.getObject("fecha_proximo_pedido",LocalDate.class),r.getObject("fecha_vence",LocalDate.class),
                r.getBoolean("activo"));
        return new Ficha(r.getLong("id"),producto,r.getLong("existencia"),r.getLong("disponible"),
                r.getBoolean("vencido"),r.getBoolean("proximo"),r.getObject("vencimiento_lote",LocalDate.class),
                r.getObject("fecha_creacion_registro",LocalDateTime.class),
                r.getObject("fecha_modificacion_registro",LocalDateTime.class));
    }

    long crear(Producto p, LocalDateTime ahora) {
        var campos = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("codigo",p.codigo()).addValue("nombre",p.nombre()).addValue("descripcion",p.descripcion())
                .addValue("presentacion",p.presentacion()).addValue("tipo",p.tipo().name()).addValue("invima",p.invima())
                .addValue("precio_compra",p.precioCompra()).addValue("precio_venta",p.precioVenta())
                .addValue("stock_minimo",p.stockMinimo()).addValue("fecha_compra",p.fechaCompra())
                .addValue("fecha_ingreso",p.fechaIngreso()).addValue("fecha_proximo_pedido",p.fechaProximoPedido())
                .addValue("fecha_vence",p.fechaVence()).addValue("activo",p.activo())
                .addValue("fecha_creacion_registro",ahora);
        return medicamentos.executeAndReturnKey(campos).longValue();
    }

    void editar(long id, Producto p, LocalDateTime ahora) {
        jdbc.update("""
                UPDATE medicamento SET codigo=?, nombre=?, descripcion=?, presentacion=?, tipo=?, invima=?,
                    precio_compra=?, precio_venta=?, stock_minimo=?, fecha_compra=?, fecha_ingreso=?,
                    fecha_proximo_pedido=?, fecha_vence=?, activo=?, fecha_modificacion_registro=? WHERE id=?
                """,p.codigo(),p.nombre(),p.descripcion(),p.presentacion(),p.tipo().name(),p.invima(),
                p.precioCompra(),p.precioVenta(),p.stockMinimo(),p.fechaCompra(),p.fechaIngreso(),
                p.fechaProximoPedido(),p.fechaVence(),p.activo(),ahora,id);
    }

    boolean duplicado(Producto p) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM medicamento WHERE codigo=? OR nombre=?)",
                Boolean.class,p.codigo(),p.nombre()));
    }

    boolean bloquearProducto(long id) {
        return jdbc.query("SELECT activo FROM medicamento WHERE id=? FOR UPDATE",
                (r,i)->r.getBoolean(1),id).stream().findFirst()
                .orElseThrow(() -> noExiste("Medicamento"));
    }

    List<Lote> lotes(long id, LocalDate hoy) {
        return jdbc.query("""
                SELECT l.*,m.activo FROM inventario_lote l JOIN medicamento m ON m.id=l.medicamento_id
                WHERE l.medicamento_id=? ORDER BY l.fecha_vence IS NULL,l.fecha_vence,l.id
                """,(r,i)->lote(r,hoy),id);
    }

    Lote bloquearLote(long medicamento, long id, LocalDate hoy, boolean activo) {
        return jdbc.query("SELECT l.*, ? AS activo FROM inventario_lote l WHERE medicamento_id=? AND id=? FOR UPDATE",
                (r,i)->lote(r,hoy),activo,medicamento,id).stream().findFirst().orElseThrow(() -> noExiste("Lote"));
    }

    private Lote lote(ResultSet r, LocalDate hoy) throws SQLException {
        LocalDate vence=r.getObject("fecha_vence",LocalDate.class);
        boolean vencido=vence!=null && vence.isBefore(hoy);
        int cantidad=r.getInt("cantidad");
        return new Lote(r.getLong("id"),r.getLong("medicamento_id"),r.getString("numero"),
                r.getBigDecimal("costo"),r.getObject("fecha_ingreso",LocalDate.class),vence,cantidad,vencido,
                vence!=null && !vencido && !vence.isAfter(hoy.plusDays(30)),
                r.getBoolean("activo") && !vencido && cantidad>0);
    }

    long crearLote(long id, RegistrarLote lote) {
        return lotes.executeAndReturnKey(new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("medicamento_id",id).addValue("numero",lote.numero()).addValue("costo",lote.costo())
                .addValue("fecha_ingreso",lote.fechaIngreso()).addValue("fecha_vence",lote.fechaVence())
                .addValue("cantidad",0)).longValue();
    }

    long movimiento(long lote, long usuario, RegistrarMovimiento solicitud, int saldo, LocalDateTime ahora) {
        jdbc.update("UPDATE inventario_lote SET cantidad=? WHERE id=?",saldo,lote);
        return new SimpleJdbcInsert(jdbc).withTableName("inventario_movimiento").usingGeneratedKeyColumns("id")
                .usingColumns("lote_id","usuario_id","tipo","cantidad","saldo","fecha","motivo","referencia")
                .withoutTableColumnMetaDataAccess()
                .executeAndReturnKey(new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                    .addValue("lote_id",lote).addValue("usuario_id",usuario).addValue("tipo",solicitud.tipo().name())
                    .addValue("cantidad",solicitud.cantidad()).addValue("saldo",saldo).addValue("fecha",ahora)
                    .addValue("motivo",solicitud.motivo()).addValue("referencia",solicitud.referencia())).longValue();
    }

    List<Movimiento> movimientos(long id) {
        return jdbc.query("""
                SELECT v.*, l.medicamento_id,l.numero,u.username FROM inventario_movimiento v
                JOIN inventario_lote l ON l.id=v.lote_id JOIN usuario u ON u.id=v.usuario_id
                WHERE l.medicamento_id=? ORDER BY v.id DESC
                """,(r,i)->new Movimiento(r.getLong("id"),r.getLong("medicamento_id"),r.getLong("lote_id"),
                r.getString("numero"),r.getLong("usuario_id"),r.getString("username"),
                TipoMovimiento.valueOf(r.getString("tipo")),r.getInt("cantidad"),r.getInt("saldo"),
                r.getObject("fecha",LocalDateTime.class),r.getString("motivo"),r.getString("referencia")),id);
    }

    record Carga(Resumen resumen, String filas) {}

    long guardarCarga(long usuario, String archivo, String filas, int rechazados, LocalDateTime ahora) {
        return cargas.executeAndReturnKey(Map.of("usuario_id",usuario,"archivo",archivo,"filas",filas,
                "rechazados",rechazados,"fecha",ahora)).longValue();
    }

    Carga carga(long id, boolean bloquear) {
        return jdbc.query("SELECT * FROM inventario_importacion WHERE id=?" + (bloquear ? " FOR UPDATE" : ""),
                (r,i)->new Carga(resumen(r),r.getString("filas")),id).stream().findFirst()
                .orElseThrow(()->noExiste("Importación"));
    }

    List<Resumen> cargas() {
        return jdbc.query("SELECT id,usuario_id,archivo,fecha,fecha_confirmacion,importados,rechazados "
                + "FROM inventario_importacion ORDER BY id DESC LIMIT 100",(r,i)->resumen(r));
    }

    private Resumen resumen(ResultSet r) throws SQLException {
        return new Resumen(r.getLong("id"),r.getLong("usuario_id"),r.getString("archivo"),
                r.getObject("fecha",LocalDateTime.class),r.getObject("fecha_confirmacion",LocalDateTime.class),
                r.getInt("importados"),r.getInt("rechazados"));
    }

    void confirmarCarga(long id, String filas, int importados, int rechazados, LocalDateTime ahora) {
        jdbc.update("UPDATE inventario_importacion SET filas=?,importados=?,rechazados=?,fecha_confirmacion=? WHERE id=?",
                filas,importados,rechazados,ahora,id);
    }

    static ResponseStatusException noExiste(String nombre) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,nombre+" no encontrado.");
    }
}
