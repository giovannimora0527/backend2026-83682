package com.uniminuto.clinica.seguridad;

/**
 * Usuario autenticado en la solicitud actual.
 *
 * @param sesionId  identificador de la fila en la tabla session.
 * @param usuarioId identificador del usuario.
 * @param username  nombre de usuario.
 * @param perfilId  identificador del perfil.
 * @param perfil    código normalizado del perfil (ver {@link Perfiles}).
 * @param medicoId  médico asociado al usuario, si el perfil es VETERINARIO.
 * @param permisos  permisos del rol (tabla perfil_permiso); son las <i>authorities</i> de Spring Security.
 */
public record SesionUsuario(long sesionId, long usuarioId, String username, long perfilId, String perfil,
                            Long medicoId, java.util.Set<String> permisos) {

    /**
     * Indica si el rol del usuario tiene un permiso.
     *
     * @param permiso código del permiso.
     * @return true si lo tiene.
     */
    public boolean tiene(String permiso) {
        return permisos != null && permisos.contains(permiso);
    }

    /**
     * Indica si el rol tiene alguno de los permisos.
     *
     * @param codigos códigos de permiso.
     * @return true si tiene al menos uno.
     */
    public boolean tieneAlguno(String... codigos) {
        for (String c : codigos) {
            if (tiene(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indica si el perfil del usuario es alguno de los indicados.
     *
     * @param perfiles códigos de perfil.
     * @return true si coincide con alguno.
     */
    public boolean es(String... perfiles) {
        for (String p : perfiles) {
            if (p.equals(perfil)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true si el usuario tiene perfil ADMINISTRADOR.
     */
    public boolean esAdmin() {
        return Perfiles.ADMINISTRADOR.equals(perfil);
    }
}
