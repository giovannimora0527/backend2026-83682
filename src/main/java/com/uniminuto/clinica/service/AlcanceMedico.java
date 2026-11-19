package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlcanceMedico {
    private final JdbcTemplate jdbc;

    public AlcanceMedico(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer medicoId() {
        var solicitud = RequestContextHolder.getRequestAttributes();
        if (solicitud == null || !(solicitud.getAttribute(IdentidadService.ATRIBUTO,
                RequestAttributes.SCOPE_REQUEST) instanceof Identidad identidad) || identidad.rol() != Rol.MEDICO) {
            return null;
        }
        try {
            return jdbc.query("SELECT medico_id FROM usuario_medico WHERE usuario_id = ?",
                    (row, index) -> row.getInt(1), identidad.usuarioId())
                    .stream().findFirst().orElseThrow(AlcanceMedico::sinVinculo);
        } catch (BadSqlGrammarException error) {
            // MySQL 1146 permite mantener cerrado el acceso hasta aplicar la migración.
            if (error.getSQLException() != null && error.getSQLException().getErrorCode() == 1146) {
                throw sinVinculo();
            }
            throw error;
        }
    }

    public void exigirMedico(Integer asignado) {
        Integer propio = medicoId();
        if (propio != null) {
            exigir(propio.equals(asignado));
        }
    }

    public void exigirEdicionCita(Cita existente, Cita cambios) {
        Integer propio = medicoId();
        if (propio != null) {
            exigir(cambios != null && propio.equals(existente.getMedicoId())
                    && propio.equals(cambios.getMedicoId())
                    && Objects.equals(existente.getMascotaId(), cambios.getMascotaId())
                    && Objects.equals(existente.getClienteId(), cambios.getClienteId()));
        }
    }

    public void exigirCita(Integer citaId) {
        Integer propio = medicoId();
        if (propio != null) {
            exigir(citaId != null && citas(propio).contains(citaId));
        }
    }

    public void exigirHistoria(Integer historiaId) {
        Integer propio = medicoId();
        if (propio != null) {
            exigir(historiaId != null && historias(propio).contains(historiaId.longValue()));
        }
    }

    public Set<Long> clientes(int medicoId) {
        return new HashSet<>(jdbc.queryForList(
                "SELECT DISTINCT cliente_id FROM cita WHERE medico_id = ?", Long.class, medicoId));
    }

    public Set<Integer> mascotas(int medicoId) {
        return new HashSet<>(jdbc.queryForList(
                "SELECT DISTINCT mascota_id FROM cita WHERE medico_id = ?", Integer.class, medicoId));
    }

    public Set<Integer> citas(int medicoId) {
        return new HashSet<>(jdbc.queryForList(
                "SELECT id FROM cita WHERE medico_id = ?", Integer.class, medicoId));
    }

    public Set<Long> historias(int medicoId) {
        return new HashSet<>(jdbc.queryForList("""
                SELECT DISTINCT h.id FROM historia_medica h
                JOIN cita c ON c.mascota_id = h.paciente_id WHERE c.medico_id = ?
                """, Long.class, medicoId));
    }

    private static void exigir(boolean autorizado) {
        if (!autorizado) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El recurso no está asignado a este médico.");
        }
    }

    private static ResponseStatusException sinVinculo() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está vinculado a un médico.");
    }
}
