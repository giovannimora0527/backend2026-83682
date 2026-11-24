package com.uniminuto.clinica.seguridad;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Códigos de perfil del sistema. El código se obtiene normalizando el nombre del perfil
 * guardado en la tabla {@code perfil} (sin tildes, mayúsculas y espacios como "_").
 */
public final class Perfiles {

    /** Perfil con acceso a todos los módulos administrativos. */
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    /** Perfil de recepción: clientes, mascotas, citas y facturación. */
    public static final String RECEPCION = "RECEPCION";
    /** Perfil del médico veterinario: agenda propia, atención, fórmulas e historias. */
    public static final String MEDICO = "MEDICO";
    /** Perfil de despacho: consulta de inventario y dispensación de fórmulas. */
    public static final String DESPACHO_MEDICO = "DESPACHO_MEDICO";

    /** Clase de constantes: no se instancia. */
    private Perfiles() {
    }

    /**
     * Convierte el nombre de un perfil en su código normalizado (sin tildes, en mayúsculas y con "_").
     *
     * @param nombre nombre del perfil guardado en la tabla perfil.
     * @return código del perfil, por ejemplo "DESPACHO_MEDICO".
     */
    public static String codigo(String nombre) {
        if (nombre == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "_");
    }
}
