package com.uniminuto.clinica.calendario;

import static com.uniminuto.clinica.calendario.AgendaDatos.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class AgendaRepositorio {
    static final String FIN_CITA = "COALESCE(c.fecha_fin, DATE_ADD(c.fecha_hora, INTERVAL 30 MINUTE))";
    static final String ESTADO = "CASE LOWER(TRIM(c.estado)) WHEN 'programada' THEN 'pendiente' ELSE LOWER(TRIM(c.estado)) END";
    private static final String RESERVADA = "LOWER(TRIM(c.estado)) IN ('pendiente','programada','confirmada')";
    private static final String OCUPADA = "LOWER(TRIM(c.estado)) <> 'cancelada'";
    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    public AgendaRepositorio(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public boolean medicoActivo(long medico, boolean bloquear) {
        return jdbc.query("SELECT activo FROM medico WHERE id=?" + (bloquear ? " FOR UPDATE" : ""),
                (r,i) -> r.getBoolean(1), medico).stream().findFirst()
                .orElseThrow(() -> AgendaServicio.noExiste("Médico"));
    }

    void activar(int medico, boolean activo) {
        jdbc.update("UPDATE medico SET activo=? WHERE id=?",activo,medico);
    }

    public int propietario(int mascota) {
        return jdbc.query("SELECT cliente_id FROM mascota WHERE mascota_id=? FOR UPDATE",
                (r,i) -> r.getInt(1), mascota).stream().findFirst()
                .orElseThrow(() -> AgendaServicio.invalido("La mascota seleccionada no existe."));
    }

    public boolean clienteExiste(int cliente) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM cliente WHERE cliente_id=?)",
                Boolean.class, cliente));
    }

    boolean conflicto(Long excluir, String recurso, int id, Intervalo periodo) {
        String columna = switch (recurso) {
            case "medico" -> "medico_id";
            case "mascota" -> "mascota_id";
            default -> throw new IllegalArgumentException("Recurso inválido");
        };
        String sql = "SELECT EXISTS(SELECT 1 FROM cita c WHERE c." + columna + "=? AND c.id<>?"
                + " AND " + OCUPADA + " AND c.fecha_hora<? AND " + FIN_CITA + ">?)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, id, excluir == null ? 0L : excluir,
                periodo.fin(), periodo.inicio()));
    }

    List<Franja> horarios(long id) {
        return jdbc.query("SELECT dia,inicio,fin FROM medico_horario WHERE medico_id=? ORDER BY dia,inicio",
                (r,i) -> new Franja(r.getInt(1),r.getObject(2,LocalTime.class),r.getObject(3,LocalTime.class)),id);
    }

    void reemplazarHorarios(long id, List<Franja> horarios) {
        jdbc.update("DELETE FROM medico_horario WHERE medico_id=?",id);
        jdbc.batchUpdate("INSERT INTO medico_horario(medico_id,dia,inicio,fin) VALUES (?,?,?,?)",
                horarios.stream().map(f->new Object[]{id,f.dia(),f.inicio(),f.fin()}).toList());
    }

    List<Ausencia> ausencias(long id, Intervalo periodo) {
        String query = "SELECT id,inicio,fin,motivo FROM medico_ausencia WHERE medico_id=?";
        Object[] params = {id};
        if (periodo != null) {
            query += " AND inicio<? AND fin>?";
            params = new Object[]{id,periodo.fin(),periodo.inicio()};
        }
        return jdbc.query(query+" ORDER BY inicio,id",(r,i)->new Ausencia(r.getLong(1),
                r.getObject(2,LocalDateTime.class),r.getObject(3,LocalDateTime.class),r.getString(4)),params);
    }

    void agregarAusencia(long id, Ausencia a) {
        jdbc.update("INSERT INTO medico_ausencia(medico_id,inicio,fin,motivo) VALUES(?,?,?,?)",
                id,a.inicio(),a.fin(),a.motivo());
    }

    List<Intervalo> reservas(long id, Intervalo rango) { return intervalos(id,rango,RESERVADA); }

    List<Intervalo> ocupados(long id, Intervalo rango) { return intervalos(id,rango,OCUPADA); }

    private List<Intervalo> intervalos(long id, Intervalo rango, String estado) {
        return jdbc.query("SELECT c.fecha_hora,"+FIN_CITA+" FROM cita c WHERE c.medico_id=? AND "+estado
                        +" AND c.fecha_hora<? AND "+FIN_CITA+">? ORDER BY c.fecha_hora,c.id",
                (r,i)->new Intervalo(r.getObject(1,LocalDateTime.class),r.getObject(2,LocalDateTime.class)),
                id,rango.fin(),rango.inicio());
    }

    List<Evento> eventos(int medico, Intervalo rango, String estado) {
        return jdbc.query("""
                SELECT c.id,c.fecha_hora,%s AS fin,%s AS estado,
                       CONCAT_WS(' ', m.nombres, m.apellidos) AS medico,
                       p.nombre_mascota, CONCAT_WS(' ', cl.nombres, cl.apellidos) AS propietario,c.motivo
                FROM cita c JOIN medico m ON m.id=c.medico_id
                LEFT JOIN mascota p ON p.mascota_id=c.mascota_id
                LEFT JOIN cliente cl ON cl.cliente_id=c.cliente_id
                WHERE c.medico_id=? AND c.fecha_hora<? AND %s>? AND (? IS NULL OR %s=?)
                ORDER BY c.fecha_hora,c.id
                """.formatted(FIN_CITA,ESTADO,FIN_CITA,ESTADO),
                (r,i)->new Evento(r.getLong("id"),r.getObject("fecha_hora",LocalDateTime.class),
                        r.getObject("fin",LocalDateTime.class),r.getString("estado"),r.getString("medico"),
                        r.getString("nombre_mascota"),r.getString("propietario"),r.getString("motivo")),
                medico,rango.fin(),rango.inicio(),estado,estado);
    }

    public int estadoAtencion(long cita) {
        return jdbc.query("SELECT IF(finalizada IS NULL,1,2) FROM atencion_veterinaria WHERE cita_id=?",
                (r,i)->r.getInt(1),cita).stream().findFirst().orElse(0);
    }

    public void registrarHistorial(long cita, Long usuario, LocalDateTime fecha, Object anterior, Object actual) {
        jdbc.update("INSERT INTO cita_historial(cita_id,usuario_id,fecha,anterior,actual) VALUES(?,?,?,?,?)",
                cita,usuario,fecha,anterior==null ? null : json.writeValueAsString(anterior),json.writeValueAsString(actual));
    }

    List<Historial> historial(long id) {
        return jdbc.query("SELECT id,usuario_id,fecha,anterior,actual FROM cita_historial WHERE cita_id=? ORDER BY id DESC",
                (r,i)->new Historial(r.getLong(1),r.getObject(2,Long.class),r.getObject(3,LocalDateTime.class),
                        r.getString(4)==null ? null : json.readTree(r.getString(4)),json.readTree(r.getString(5))),id);
    }
}
