package com.uniminuto.clinica.usuario;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de un usuario (nunca incluye la contraseña).
 *
 * @param id identificador.
 * @param username nombre de usuario.
 * @param rol identificador del rol.
 * @param perfil nombre del rol.
 * @param email correo.
 * @param activo estado.
 * @param fechaCreacion fecha de creación.
 */
public record UsuarioDto(long id, String username, long rol, String perfil, String email, boolean activo,
                         LocalDateTime fechaCreacion) {
}
