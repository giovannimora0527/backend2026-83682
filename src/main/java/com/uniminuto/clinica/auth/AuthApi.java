package com.uniminuto.clinica.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Autenticación con usuario y contraseña. Devuelve un token JWT que debe enviarse en el
 * encabezado {@code Authorization: Bearer <token>} en las demás solicitudes.
 */
@Tag(name = "Autenticación", description = "Inicio, validación y cierre de sesión")
@RequestMapping("/auth")
public interface AuthApi {

    /** Datos de inicio de sesión. */
    record LoginRq(String username, String password) {
    }

    /** Respuesta de inicio de sesión que el frontend guarda en localStorage. */
    record LoginRs(String token, String tokenType, long usuarioId, String username, long perfilId, String perfil) {
    }

    @Operation(summary = "Iniciar sesión", description = "401 si las credenciales son incorrectas; "
            + "403 si el usuario o su perfil están inactivos.")
    @PostMapping("/login")
    ResponseEntity<LoginRs> login(@RequestBody LoginRq datos);

    @Operation(summary = "Validar la sesión actual")
    @GetMapping("/validar")
    ResponseEntity<LoginRs> validar();

    @Operation(summary = "Cerrar la sesión actual (revoca el token)")
    @PostMapping("/logout")
    ResponseEntity<Void> logout();
}
