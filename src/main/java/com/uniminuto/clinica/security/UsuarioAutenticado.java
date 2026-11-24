package com.uniminuto.clinica.security;

/** Usuario tomado del JWT ya validado. Rol y medico son los vigentes en la base de datos. */
public record UsuarioAutenticado(Long id, String username, String rol, Long medicoId, String jti) {

    public boolean esMedico() {
        return Rol.MEDICO.equals(rol);
    }
}
