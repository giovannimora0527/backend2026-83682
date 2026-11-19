package com.uniminuto.clinica.calendario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class AgendaDatos {
    private AgendaDatos() {}
    public record Franja(int dia, LocalTime inicio, LocalTime fin) {}
    public record Ausencia(Long id, LocalDateTime inicio, LocalDateTime fin, String motivo) {}
    public record Configuracion(boolean activo, List<Franja> horarios, List<Ausencia> ausencias) {}
    public record Intervalo(LocalDateTime inicio, LocalDateTime fin) {}
    public record Disponibilidad(LocalDate fecha, boolean activo, List<Intervalo> libres) {}
    public record Evento(long id, LocalDateTime inicio, LocalDateTime fin, String estado,
            String medico, String mascota, String propietario, String motivo) {}
    public record Cambio(String estado, LocalDateTime inicio, LocalDateTime fin) {}
    public record Historial(long id, Long usuarioId, LocalDateTime fecha,
            tools.jackson.databind.JsonNode anterior, tools.jackson.databind.JsonNode actual) {}
}
