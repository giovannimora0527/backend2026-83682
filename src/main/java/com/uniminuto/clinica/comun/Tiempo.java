package com.uniminuto.clinica.comun;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Fecha y hora oficial de la clínica (America/Bogota), independiente de la zona del servidor.
 */
public final class Tiempo {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    /** Días de anticipación para considerar un lote "próximo a vencer". */
    public static final int DIAS_PROXIMO_VENCER = 30;

    private Tiempo() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA).truncatedTo(ChronoUnit.SECONDS);
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    public static LocalDate limiteProximoVencer() {
        return hoy().plusDays(DIAS_PROXIMO_VENCER);
    }
}
