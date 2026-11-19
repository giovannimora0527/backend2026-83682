package com.uniminuto.clinica.despacho;

import static com.uniminuto.clinica.calendario.AgendaServicio.noExiste;
import static com.uniminuto.clinica.despacho.DespachoDatos.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class DespachoRepositorio {
    private static final String FORMULAS = """
            SELECT f.id,f.revision,f.creada,CONCAT_WS(' ',m.nombres,m.apellidos) veterinario,
                   p.nombre_mascota mascota,CONCAT_WS(' ',cl.nombres,cl.apellidos) propietario,
                   COALESCE(df.estado,'PENDIENTE_DESPACHO') estado
            FROM formula_clinica f JOIN atencion_veterinaria a ON a.id=f.atencion_id
            JOIN cita c ON c.id=a.cita_id JOIN medico m ON m.id=a.medico_id
            JOIN mascota p ON p.mascota_id=c.mascota_id
            JOIN cliente cl ON cl.cliente_id=c.cliente_id AND cl.cliente_id=p.cliente_id
            LEFT JOIN despacho_formula df ON df.formula_id=f.id WHERE f.estado='CONFIRMADA'
            """;
    private final JdbcTemplate jdbc;
    DespachoRepositorio(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    List<Formula> buscar(Long id,String propietario,String mascota,LocalDate fecha,Estado estado,int pagina) {
        return jdbc.query("SELECT * FROM ("+FORMULAS+"""
                ) AS f WHERE (? IS NULL OR f.id=?)
                AND (? IS NULL OR LOCATE(?,f.propietario)>0)
                AND (? IS NULL OR LOCATE(?,f.mascota)>0)
                AND (? IS NULL OR DATE(f.creada)=?) AND (? IS NULL OR f.estado=?)
                ORDER BY f.creada DESC,f.id DESC LIMIT 50 OFFSET ?
                """,this::formula,id,id,propietario,propietario,mascota,mascota,fecha,fecha,
                estado==null?null:estado.name(),estado==null?null:estado.name(),pagina*50);
    }

    Formula formula(long id,boolean bloqueo) {
        if (bloqueo) jdbc.query("SELECT id FROM formula_clinica WHERE id=? FOR UPDATE",(r,i)->r.getLong(1),id)
                .stream().findFirst().orElseThrow(()->noExiste("Fórmula"));
        return jdbc.query(FORMULAS+" AND f.id=?",this::formula,id).stream().findFirst()
                .orElseThrow(()->noExiste("Fórmula confirmada"));
    }

    private Formula formula(ResultSet r,int fila) throws SQLException {
        return new Formula(r.getLong("id"),r.getInt("revision"),r.getObject("creada",LocalDateTime.class),
                r.getString("veterinario"),r.getString("mascota"),r.getString("propietario"),Estado.valueOf(r.getString("estado")));
    }

    List<Prescripcion> prescripciones(Formula f) {
        return jdbc.query("""
                SELECT p.*,COALESCE((
                    SELECT SUM(d.cantidad) FROM despacho_detalle d JOIN despacho_entrega e ON e.id=d.entrega_id
                    WHERE d.formula_id=p.formula_id AND d.revision=p.revision AND d.medicamento_id=p.medicamento_id
                      AND e.confirmada IS NOT NULL AND e.revertida IS NULL),0) entregada
                FROM formula_clinica_detalle p WHERE p.formula_id=? AND p.revision=? ORDER BY p.medicamento_id
                """,(r,i)->new Prescripcion(r.getLong("medicamento_id"),r.getString("nombre"),r.getString("dosis"),
                r.getString("frecuencia"),r.getString("duracion"),r.getString("via"),r.getString("indicaciones"),
                r.getInt("cantidad"),r.getInt("entregada"),r.getInt("cantidad")-r.getInt("entregada")),f.id(),f.revision());
    }

    record Preparacion(long id,long formula,long usuario,int revision,String plan,LocalDateTime confirmada,LocalDateTime revertida) {}

    Optional<Preparacion> porClave(String clave) { return preparacion("clave=?",clave); }
    Optional<Preparacion> ultima(long formula) { return preparacion("formula_id=? ORDER BY id DESC LIMIT 1",formula); }
    Preparacion entrega(long id) {
        return preparacion("id=?",id).orElseThrow(()->noExiste("Entrega"));
    }
    private Optional<Preparacion> preparacion(String filtro,Object valor) {
        return jdbc.query("SELECT * FROM despacho_entrega WHERE "+filtro,(r,i)->new Preparacion(r.getLong("id"),
                r.getLong("formula_id"),r.getLong("usuario_id"),r.getInt("revision"),r.getString("plan"),
                r.getObject("confirmada",LocalDateTime.class),r.getObject("revertida",LocalDateTime.class)),valor)
                .stream().findFirst();
    }

    long preparar(Formula f,long usuario,String clave,String plan,LocalDateTime fecha) {
        var llave=new GeneratedKeyHolder();
        jdbc.update(conexion->{
            var sentencia=conexion.prepareStatement("""
                    INSERT INTO despacho_entrega(formula_id,revision,usuario_id,clave,plan,fecha)
                    VALUES(?,?,?,?,?,?)
                    """,Statement.RETURN_GENERATED_KEYS);
            sentencia.setLong(1,f.id()); sentencia.setInt(2,f.revision()); sentencia.setLong(3,usuario);
            sentencia.setString(4,clave); sentencia.setString(5,plan); sentencia.setObject(6,fecha);
            return sentencia;
        },llave);
        return java.util.Objects.requireNonNull(llave.getKey()).longValue();
    }

    void confirmar(long entrega,long usuario,LocalDateTime fecha,Estado estado,String pendientes) {
        jdbc.update("""
                UPDATE despacho_entrega SET confirmada=?,usuario_id=?,estado_resultante=?,pendiente=? WHERE id=?
                """,fecha,usuario,estado.name(),pendientes,entrega);
    }

    void detalle(Preparacion e,Seleccion l,long movimiento) {
        jdbc.update("""
                INSERT INTO despacho_detalle(entrega_id,formula_id,revision,medicamento_id,lote_id,cantidad,movimiento_id)
                VALUES(?,?,?,?,?,?,?)
                """,e.id(),e.formula(),e.revision(),l.medicamento(),l.lote(),l.cantidad(),movimiento);
    }

    List<Linea> lineas(long id) {
        return jdbc.query("""
                SELECT d.*,p.nombre,l.numero FROM despacho_detalle d JOIN inventario_lote l ON l.id=d.lote_id
                JOIN formula_clinica_detalle p ON p.formula_id=d.formula_id AND p.revision=d.revision
                AND p.medicamento_id=d.medicamento_id WHERE d.entrega_id=? ORDER BY d.medicamento_id,l.fecha_vence IS NULL,l.fecha_vence,l.id
                """,(r,i)->new Linea(r.getLong("medicamento_id"),r.getString("nombre"),r.getLong("lote_id"),
                r.getString("numero"),r.getInt("cantidad"),r.getLong("movimiento_id"),
                r.getObject("reversion_movimiento_id",Long.class)),id);
    }

    void verificarReversionFinanciera(long entrega) {
        jdbc.queryForObject("SELECT id FROM despacho_entrega WHERE id=? FOR UPDATE",Long.class,entrega);
        boolean facturada=Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM despacho_detalle d JOIN factura_detalle f
                    ON f.despacho_vigente=d.movimiento_id WHERE d.entrega_id=?)
                """,Boolean.class,entrega));
        if(facturada) throw com.uniminuto.clinica.calendario.AgendaServicio.conflicto(
                "La entrega está facturada. Resuelve la factura vigente antes de revertirla.");
    }

    void revertirLinea(long entrega,long lote,long movimiento) {
        jdbc.update("UPDATE despacho_detalle SET reversion_movimiento_id=? WHERE entrega_id=? AND lote_id=?",
                movimiento,entrega,lote);
    }

    void revertir(long entrega,long usuario,LocalDateTime fecha,String motivo) {
        jdbc.update("UPDATE despacho_entrega SET revertida=?,reversor_id=?,motivo_reversion=? WHERE id=?",
                fecha,usuario,motivo,entrega);
    }

    void cambiar(Formula f,Estado estado,long usuario,LocalDateTime fecha,String motivo) {
        jdbc.update("""
                INSERT INTO despacho_formula(formula_id,estado) VALUES(?,?)
                ON DUPLICATE KEY UPDATE estado=?
                """,f.id(),estado.name(),estado.name());
        if (f.estado()!=estado) jdbc.update("""
                INSERT INTO despacho_estado(formula_id,usuario_id,anterior,nuevo,fecha,motivo) VALUES(?,?,?,?,?,?)
                """,f.id(),usuario,f.estado().name(),estado.name(),fecha,motivo);
    }

    record Cabecera(long id,long formula,String responsable,LocalDateTime fecha,Estado estado,
            LocalDateTime revertida,String reversor,String motivo,String pendiente) {}

    List<Cabecera> entregas(Long formula,Long id,LocalDate desde,LocalDate hasta,int pagina) {
        return jdbc.query("""
                SELECT e.*,u.username responsable,r.username reversor FROM despacho_entrega e
                JOIN usuario u ON u.id=e.usuario_id LEFT JOIN usuario r ON r.id=e.reversor_id
                WHERE e.confirmada IS NOT NULL AND (? IS NULL OR formula_id=?) AND (? IS NULL OR e.id=?)
                AND (? IS NULL OR e.confirmada>=?) AND (? IS NULL OR e.confirmada<?)
                ORDER BY e.confirmada DESC,e.id DESC LIMIT 50 OFFSET ?
                """,(r,i)->new Cabecera(r.getLong("id"),r.getLong("formula_id"),r.getString("responsable"),
                r.getObject("confirmada",LocalDateTime.class),Estado.valueOf(r.getString("estado_resultante")),
                r.getObject("revertida",LocalDateTime.class),r.getString("reversor"),r.getString("motivo_reversion"),
                r.getString("pendiente")),formula,formula,id,id,desde,desde,hasta,hasta,pagina*50);
    }

    List<Cambio> historial(long id) {
        return jdbc.query("""
                SELECT d.*,u.username FROM despacho_estado d JOIN usuario u ON u.id=d.usuario_id
                WHERE formula_id=? ORDER BY d.id DESC
                """,(r,i)->new Cambio(Estado.valueOf(r.getString("anterior")),Estado.valueOf(r.getString("nuevo")),
                r.getString("username"),r.getObject("fecha",LocalDateTime.class),r.getString("motivo")),id);
    }
}
