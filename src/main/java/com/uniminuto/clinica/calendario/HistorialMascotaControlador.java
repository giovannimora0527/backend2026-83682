package com.uniminuto.clinica.calendario;

import com.uniminuto.clinica.service.AlcanceMedico;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import static com.uniminuto.clinica.calendario.AgendaServicio.*;

@RestController
@RequestMapping("/cita/mascotas")
class HistorialMascotaControlador {
    private static final int TAMANO = 50;
    private final JdbcTemplate jdbc;
    private final AlcanceMedico alcance;

    HistorialMascotaControlador(JdbcTemplate jdbc, AlcanceMedico alcance) {
        this.jdbc = jdbc;
        this.alcance = alcance;
    }

    record CitaAnterior(long id, LocalDateTime inicio, LocalDateTime fin,
                        String medico, String estado, Long atencionId) {}
    record Historial(List<CitaAnterior> citas, boolean hayMas) {}

    @GetMapping("/{id}/historial")
    Historial consultar(@PathVariable int id, @RequestParam(defaultValue = "0") int pagina) {
        if (id < 1 || pagina < 0) throw invalido("Revisa la mascota y la página.");
        Integer medico = alcance.medicoId();
        if (medico != null && !alcance.mascotas(medico).contains(id)) {
            throw prohibido("La mascota no está asignada al médico.");
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM mascota WHERE mascota_id=?", Integer.class, id) == 0) {
            throw noExiste("Mascota");
        }
        var citas = jdbc.query("""
                SELECT c.id, c.fecha_hora, COALESCE(c.fecha_fin, c.fecha_hora + INTERVAL 30 MINUTE) fin,
                       CONCAT_WS(' ',m.nombres,m.apellidos) medico,
                       CASE LOWER(TRIM(c.estado)) WHEN 'programada' THEN 'pendiente'
                           ELSE LOWER(TRIM(c.estado)) END estado, a.id atencion_id
                FROM cita c LEFT JOIN medico m ON m.id=c.medico_id
                LEFT JOIN atencion_veterinaria a ON a.cita_id=c.id
                WHERE c.mascota_id=? AND c.fecha_hora<=? AND (? IS NULL OR c.medico_id=?)
                ORDER BY c.fecha_hora DESC,c.id DESC LIMIT ? OFFSET ?
                """, (r,i) -> new CitaAnterior(r.getLong("id"),
                r.getObject("fecha_hora", LocalDateTime.class), r.getObject("fin", LocalDateTime.class),
                r.getString("medico"), r.getString("estado"), r.getObject("atencion_id", Long.class)),
                id, ahora(), medico, medico, TAMANO + 1, (long) pagina * TAMANO);
        return new Historial(citas.subList(0, Math.min(citas.size(), TAMANO)), citas.size() > TAMANO);
    }
}
