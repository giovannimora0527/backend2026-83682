package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Rango de dias inclusivo; los extremos ausentes quedan abiertos. */
public record RangoFechas(LocalDateTime inicio, LocalDateTime finExclusivo) {

    private static final LocalDate MINIMA = LocalDate.of(1900, 1, 1);
    private static final LocalDate MAXIMA = LocalDate.of(9999, 1, 1);

    public static RangoFechas de(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la final.");
        }
        return new RangoFechas((desde == null ? MINIMA : desde).atStartOfDay(),
                (hasta == null ? MAXIMA : hasta.plusDays(1)).atStartOfDay());
    }
}
