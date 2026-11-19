package com.uniminuto.clinica.atencion;

import static com.uniminuto.clinica.atencion.ClinicaDatos.*;
import static com.uniminuto.clinica.calendario.AgendaServicio.noExiste;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class ClinicaRepositorio {
    private final JdbcTemplate jdbc;

    ClinicaRepositorio(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    void ejecutar(String sql,Object... valores) {
        jdbc.update(sql,valores);
    }

    long agregar(String sql,Object... valores) {
        var clave=new GeneratedKeyHolder();
        jdbc.update(conexion->{
            var stmt=conexion.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            for (int i=0;i<valores.length;i++) stmt.setObject(i+1,valores[i]);
            return stmt;
        },clave);
        return java.util.Objects.requireNonNull(clave.getKey()).longValue();
    }

    <T> T uno(String sql,RowMapper<T> map,String nombre,Object... argumentos) {
        return jdbc.query(sql,map,argumentos).stream().findFirst().orElseThrow(()->noExiste(nombre));
    }

    List<Especialidad> especialidades() {
        return jdbc.query("SELECT * FROM especializacion ORDER BY nombre,id",this::especialidad);
    }

    Especialidad especialidad(long id,boolean lock) {
        return uno("SELECT * FROM especializacion WHERE id=?"+(lock?" FOR UPDATE":""),this::especialidad,"Especialidad",id);
    }

    private Especialidad especialidad(ResultSet r,int i) throws SQLException {
        return new Especialidad(r.getLong("id"),r.getString("codigo_especializacion"),r.getString("nombre"),
                r.getString("descripcion"),r.getBoolean("activo"));
    }

    List<Servicio> servicios(long especialidad) {
        return jdbc.query("SELECT * FROM servicio_veterinario WHERE especializacion_id=? ORDER BY nombre,id",
                this::servicio,especialidad);
    }

    Servicio servicio(long id,boolean lock) {
        return uno("SELECT * FROM servicio_veterinario WHERE id=?"+(lock?" FOR UPDATE":""),this::servicio,"Servicio",id);
    }

    private Servicio servicio(ResultSet r,int i) throws SQLException {
        return new Servicio(r.getLong("id"),r.getLong("especializacion_id"),r.getString("nombre"));
    }

    List<Tarifa> tarifas(long servicio) {
        return jdbc.query("SELECT * FROM tarifa_veterinaria WHERE servicio_id=? ORDER BY desde DESC,id DESC",this::tarifa,servicio);
    }

    Optional<Tarifa> tarifaVigente(long servicio,LocalDate fecha) {
        return jdbc.query("""
                SELECT * FROM tarifa_veterinaria WHERE servicio_id=? AND desde<=?
                AND (hasta IS NULL OR hasta>=?) ORDER BY desde DESC LIMIT 1
                """,this::tarifa,servicio,fecha,fecha).stream().findFirst();
    }

    private Tarifa tarifa(ResultSet r,int i) throws SQLException {
        return new Tarifa(r.getLong("id"),r.getBigDecimal("valor"),r.getBigDecimal("iva"),r.getBigDecimal("impuesto"),
                r.getObject("desde",LocalDate.class),r.getObject("hasta",LocalDate.class),
                r.getLong("usuario_id"),r.getObject("fecha",LocalDateTime.class));
    }

    Paciente paciente(long id) {
        return uno("""
                SELECT c.*, CONCAT_WS(' ',m.nombres,m.apellidos) AS medico,m.registro_profesional,
                  p.nombre_mascota, CONCAT_WS(' ',cl.nombres,cl.apellidos) AS propietario
                FROM cita c JOIN medico m ON m.id=c.medico_id JOIN mascota p ON p.mascota_id=c.mascota_id
                JOIN cliente cl ON cl.cliente_id=c.cliente_id AND p.cliente_id=cl.cliente_id WHERE c.id=?
                """,(r,i)->new Paciente(r.getLong("id"),r.getInt("medico_id"),r.getInt("mascota_id"),
                r.getInt("cliente_id"),com.uniminuto.clinica.calendario.AgendaServicio.estado(r.getString("estado")),
                r.getObject("fecha_hora",LocalDateTime.class),r.getString("medico"),r.getString("registro_profesional"),
                r.getString("nombre_mascota"),r.getString("propietario")),"Cita con paciente válido",id);
    }

    long historia(int mascota,LocalDateTime ahora) {
        uno("SELECT mascota_id FROM mascota WHERE mascota_id=? FOR UPDATE",(r,i)->r.getInt(1),"Mascota",mascota);
        return jdbc.query("SELECT id FROM historia_medica WHERE paciente_id=? ORDER BY id LIMIT 1",
                (r,i)->r.getLong(1),mascota).stream().findFirst()
                .orElseGet(()->agregar("INSERT INTO historia_medica(paciente_id,fecha_creacion) VALUES(?,?)",mascota,ahora));
    }

    Optional<Cabecera> atencion(long cita) {
        return jdbc.query("SELECT * FROM atencion_veterinaria WHERE cita_id=?",this::cabecera,cita).stream().findFirst();
    }

    private Cabecera cabecera(ResultSet r,int i) throws SQLException {
        return new Cabecera(r.getLong("id"),r.getLong("cita_id"),r.getLong("historia_id"),
                r.getLong("especializacion_id"),r.getString("especialidad"),r.getString("motivo"),r.getString("sintomas"),
                r.getString("diagnostico"),r.getString("observaciones"),r.getString("recomendaciones"),
                r.getObject("creada",LocalDateTime.class),r.getObject("actualizada",LocalDateTime.class),
                r.getObject("finalizada",LocalDateTime.class),r.getInt("version"));
    }

    List<Antecedente> antecedentes(int mascota,long cita) {
        return jdbc.query("""
                SELECT n.fecha, CONCAT_WS(' ',m.nombres,m.apellidos) AS medico,n.descripcion
                FROM anotacion_historia n JOIN historia_medica h ON h.id=n.historia_id
                LEFT JOIN medico m ON m.id=n.medico_id WHERE h.paciente_id=?
                UNION ALL
                SELECT a.finalizada, CONCAT_WS(' ',m.nombres,m.apellidos),
                  CONCAT_WS(' · ',a.motivo,a.diagnostico,a.observaciones,a.recomendaciones)
                FROM atencion_veterinaria a JOIN historia_medica h ON h.id=a.historia_id
                JOIN medico m ON m.id=a.medico_id WHERE h.paciente_id=? AND a.cita_id<>? AND a.finalizada IS NOT NULL
                ORDER BY fecha DESC LIMIT 100
                """,(r,i)->new Antecedente(r.getObject(1,LocalDateTime.class),r.getString(2),r.getString(3)),
                mascota,mascota,cita);
    }

    List<LineaServicio> lineas(long id) {
        return jdbc.query("SELECT * FROM atencion_servicio WHERE atencion_id=? AND activo=1 ORDER BY servicio_id",
                (r,i)->new LineaServicio(r.getLong("servicio_id"),r.getString("nombre"),r.getInt("cantidad"),
                        r.getBigDecimal("valor"),r.getBigDecimal("iva"),r.getBigDecimal("impuesto")),id);
    }

    boolean restaurarLinea(long atencion,long servicio,int cantidad) {
        jdbc.update("UPDATE atencion_servicio SET activo=1,cantidad=? WHERE atencion_id=? AND servicio_id=?",
                cantidad,atencion,servicio);
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM atencion_servicio WHERE atencion_id=? AND servicio_id=?)",
                Boolean.class,atencion,servicio));
    }

    record FormulaBase(long id,long cita,String paciente,String estado,int revision,LocalDateTime creada,LocalDateTime confirmada) {}

    List<Formula> formulas(Long cita,int medico) {
        String sql="""
                SELECT f.*,a.cita_id,p.nombre_mascota FROM formula_clinica f
                JOIN atencion_veterinaria a ON a.id=f.atencion_id
                JOIN cita c ON c.id=a.cita_id JOIN mascota p ON p.mascota_id=c.mascota_id
                WHERE a.medico_id=?
                """;
        var bases=cita==null ? jdbc.query(sql+" ORDER BY f.id DESC",this::formulaBase,medico)
                : jdbc.query(sql+" AND a.cita_id=? ORDER BY f.id DESC",this::formulaBase,medico,cita);
        return bases.stream().map(this::formula).toList();
    }

    long citaDeFormula(long id) {
        return uno("""
                SELECT a.cita_id FROM formula_clinica f JOIN atencion_veterinaria a ON a.id=f.atencion_id
                WHERE f.id=?
                """,(r,i)->r.getLong(1),"Fórmula",id);
    }

    private FormulaBase formulaBase(ResultSet r,int i) throws SQLException {
        return new FormulaBase(r.getLong("id"),r.getLong("cita_id"),r.getString("nombre_mascota"),
                r.getString("estado"),r.getInt("revision"),r.getObject("creada",LocalDateTime.class),
                r.getObject("confirmada",LocalDateTime.class));
    }

    Formula formula(long id) {
        return formula(uno("""
                SELECT f.*,a.cita_id,p.nombre_mascota FROM formula_clinica f
                JOIN atencion_veterinaria a ON a.id=f.atencion_id
                JOIN cita c ON c.id=a.cita_id JOIN mascota p ON p.mascota_id=c.mascota_id WHERE f.id=?
                """,this::formulaBase,"Fórmula",id));
    }

    private Formula formula(FormulaBase f) {
        var detalle=jdbc.query("""
                SELECT * FROM formula_clinica_detalle WHERE formula_id=? AND revision=? ORDER BY nombre,medicamento_id
                """,(r,i)->new Detalle(r.getLong("medicamento_id"),r.getString("nombre"),r.getString("dosis"),
                r.getString("frecuencia"),r.getString("duracion"),r.getInt("cantidad"),r.getString("via"),
                r.getString("indicaciones"),r.getBigDecimal("precio_estimado")),f.id(),f.revision());
        return new Formula(f.id(),f.cita(),f.paciente(),f.estado(),f.revision(),f.creada(),f.confirmada(),detalle);
    }
}
