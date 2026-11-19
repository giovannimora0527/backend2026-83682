
package com.uniminuto.clinica.dto;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Datos que Angular enviará al Backend
 * cuando el usuario intente iniciar sesión.
 */
public record LoginRequest(

        // Nombre de usuario.
        String username,

        // Contraseña ingresada.
        String password

) {
}
