package com.uniminuto.clinica.seguridad;

/**
 * Usuario autenticado en la solicitud actual.
 *
 * @param sesionId  identificador de la fila en la tabla session.
 * @param usuarioId identificador del usuario.
 * @param username  nombre de usuario.
 * @param perfilId  identificador del perfil.
 * @param perfil    código normalizado del perfil (ver {@link Perfiles}).
 * @param medicoId  médico asociado al usuario, si el perfil es MEDICO.
 */
public record SesionUsuario(long sesionId, long usuarioId, String username, long perfilId, String perfil,
                            Long medicoId) {

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
