package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;
import java.time.LocalDateTime;

/** Valida rangos de fecha; la usan el filtro de citas y el de anotaciones. */
public final class ValidadorFechas {

    /** Clase de solo metodos estaticos: no se instancia. */
    private ValidadorFechas() {
    }

    /** Verifica que ambas fechas existan y que inicio no sea posterior a fin. */
    public static void validarRango(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("La fecha inicial y la fecha final son obligatorias");
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la final");
        }
    }
}
