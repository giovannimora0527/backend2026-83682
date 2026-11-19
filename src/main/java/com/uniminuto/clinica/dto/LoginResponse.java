
package com.uniminuto.clinica.dto;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Respuesta del Backend después de
 * autenticar correctamente a un usuario.
 *
 * No incluye la contraseña ni su hash.
 */
public record LoginResponse(

        // Token de autenticación.
        String token,

        // Tipo de autenticación.
        String tokenType,

        // Identificador del usuario.
        Long usuarioId,

        // Nombre de usuario.
        String username,

        // Identificador del perfil.
        Integer perfilId,

        // Nombre del perfil.
        String perfil

) {
}
