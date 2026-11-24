package com.uniminuto.clinica.comun;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Fecha y hora oficial de la clínica (America/Bogota), independiente de la zona del servidor.
 */
public final class Tiempo {

    /** Zona horaria oficial de la clínica. */
    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    /** Días de anticipación para considerar un lote "próximo a vencer". */
    public static final int DIAS_PROXIMO_VENCER = 30;

    /** Clase de utilidades: no se instancia. */
    private Tiempo() {
    }

    /**
     * @return fecha y hora actual en Bogotá, sin fracciones de segundo.
     */
    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * @return fecha actual en Bogotá.
     */
    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    /**
     * @return fecha límite para considerar un lote próximo a vencer (hoy + 30 días).
     */
    public static LocalDate limiteProximoVencer() {
        return hoy().plusDays(DIAS_PROXIMO_VENCER);
    }
}
