package com.uniminuto.clinica.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Autenticación en dos pasos: usuario y contraseña, y luego un código de 6 caracteres enviado al correo.
 * El token JWT devuelto en {@code /auth/verificar} debe enviarse como {@code Authorization: Bearer <token>}.
 */
@Tag(name = "Autenticación", description = "Inicio de sesión con código de verificación, recuperación de contraseña y cierre de sesión")
@RequestMapping("/auth")
public interface AuthApi {

    /** Datos de inicio de sesión. */
    record LoginRq(String username, String password) {
    }

    /**
     * Respuesta del primer paso. Si {@code verificacion} es true, se envió un código al correo y se debe
     * llamar a {@code /auth/verificar} con el {@code desafio}. Si la verificación está deshabilitada
     * (OTP_HABILITADO=false), {@code sesion} trae directamente el token.
     */
    record DesafioRs(boolean verificacion, String desafio, String correo, int minutos, LoginRs sesion) {
    }

    record VerificarRq(String desafio, String codigo) {
    }

    record DesafioRq(String desafio) {
    }

    record RecuperarRq(String usuario) {
    }

    record RestablecerRq(String desafio, String codigo, String nuevaClave) {
    }

    /** Sesión iniciada que el frontend guarda en localStorage. */
    record LoginRs(String token, String tokenType, long usuarioId, String username, long perfilId, String perfil) {
    }

    @Operation(summary = "Paso 1: validar usuario y contraseña y enviar el código de verificación al correo",
            description = "401 si las credenciales son incorrectas; 403 si el usuario o su perfil están inactivos.")
    @PostMapping("/login")
    ResponseEntity<DesafioRs> login(@RequestBody LoginRq datos);

    @Operation(summary = "Paso 2: verificar el código de 6 caracteres y obtener el token JWT",
            description = "El código vence en 5 minutos y admite 5 intentos.")
    @PostMapping("/verificar")
    ResponseEntity<LoginRs> verificar(@RequestBody VerificarRq datos);

    @Operation(summary = "Reenviar un nuevo código para el mismo desafío (invalida el anterior)")
    @PostMapping("/reenviar")
    ResponseEntity<DesafioRs> reenviar(@RequestBody DesafioRq datos);

    @Operation(summary = "¿Olvidaste tu contraseña?: envía un código al correo del usuario (usuario o correo)",
            description = "Responde igual exista o no el usuario, para no revelar cuentas registradas.")
    @PostMapping("/recuperar")
    ResponseEntity<DesafioRs> recuperar(@RequestBody RecuperarRq datos);

    @Operation(summary = "Restablecer la contraseña con el código recibido (cierra las sesiones abiertas)")
    @PostMapping("/restablecer")
    ResponseEntity<Void> restablecer(@RequestBody RestablecerRq datos);

    @Operation(summary = "Validar la sesión actual")
    @GetMapping("/validar")
    ResponseEntity<LoginRs> validar();

    @Operation(summary = "Cerrar la sesión actual (revoca el token)")
    @PostMapping("/logout")
    ResponseEntity<Void> logout();
}
