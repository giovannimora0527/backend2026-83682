package com.uniminuto.clinica.util;

import java.util.Locale;

public final class Texto {

    private Texto() {
    }

    /** Minusculas y sin tildes, para buscar. */
    public static String normalizar(String valor) {
        return java.text.Normalizer.normalize(valor.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
