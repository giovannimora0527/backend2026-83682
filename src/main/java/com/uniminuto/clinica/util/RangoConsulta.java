package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Rango desde/hasta de las consultas de agenda. Acepta fecha (el dia final se incluye completo), fecha y hora, o
 * fecha y hora con zona, que se lleva a la hora de Bogota.
 */
public record RangoConsulta(LocalDateTime inicio, LocalDateTime fin) {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final int DIAS_MAXIMOS = 366;

    public static RangoConsulta de(String desde, String hasta) {
        LocalDateTime inicio = leer("desde", desde, false);
        LocalDateTime fin = leer("hasta", hasta, true);
        if (!inicio.isBefore(fin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la final.");
        }
        if (Duration.between(inicio, fin).toDays() > DIAS_MAXIMOS) {
            throw new BadRequestException("El rango máximo es de un año.");
        }
        return new RangoConsulta(inicio, fin);
    }

    private static LocalDateTime leer(String nombre, String valor, boolean esFin) {
        try {
            String texto = valor.trim();
            if (texto.length() == 10) {
                LocalDate fecha = LocalDate.parse(texto);
                return (esFin ? fecha.plusDays(1) : fecha).atStartOfDay();
            }
            if (texto.endsWith("Z") || texto.matches(".*T.*[+-]\\d{2}:\\d{2}")) {
                return OffsetDateTime.parse(texto).atZoneSameInstant(ZONA).toLocalDateTime();
            }
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("El parametro " + nombre + " no es una fecha valida.");
        }
    }
}
