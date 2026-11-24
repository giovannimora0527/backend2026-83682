package com.uniminuto.clinica.seguridad;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Códigos de perfil del sistema. El código se obtiene normalizando el nombre del perfil
 * guardado en la tabla {@code perfil} (sin tildes, mayúsculas y espacios como "_").
 */
public final class Perfiles {

    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String RECEPCION = "RECEPCION";
    public static final String MEDICO = "MEDICO";
    public static final String DESPACHO_MEDICO = "DESPACHO_MEDICO";

    private Perfiles() {
    }

    public static String codigo(String nombre) {
        if (nombre == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }
}
