package com.uniminuto.clinica.security;

/** Usuario tomado del JWT ya validado. El rol es el perfil vigente en la base de datos. */
public record UsuarioAutenticado(Long id, String username, String rol, String jti) {
}
