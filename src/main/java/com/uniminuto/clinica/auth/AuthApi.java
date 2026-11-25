package com.uniminuto.clinica.auth;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /** Segundo paso: desafío recibido en el login y código enviado al correo. */
    record VerificarRq(String desafio, String codigo) {
    }

    /** Desafío para el que se solicita un nuevo código. */
    record DesafioRq(String desafio) {
    }

    /** Usuario o correo para recuperar la contraseña. */
    record RecuperarRq(String usuario) {
    }

    /** Desafío, código recibido y nueva contraseña. */
    record RestablecerRq(String desafio, String codigo, String nuevaClave) {
    }

    /** Sesión iniciada que el frontend guarda en localStorage. */
    record LoginRs(String token, String tokenType, long usuarioId, String username, long perfilId, String perfil,
                   java.util.Set<String> permisos) {
    }

    /**
     * Paso 1: validar usuario y contraseña y enviar el código de verificación al correo.
     * <p>401 si las credenciales son incorrectas; 403 si el usuario o su perfil están inactivos.</p>
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Paso 1: validar usuario y contraseña y enviar el código de verificación al correo",
            description = "401 si las credenciales son incorrectas; 403 si el usuario o su perfil están inactivos.")
    /**
     * Paso 1 del inicio de sesión: valida usuario y contraseña y envía el código al correo.
     *
     * @param datos usuario y contraseña.
     * @return desafío a verificar con el código recibido.
     */
    @PostMapping("/login")
    ResponseEntity<DesafioRs> login(@RequestBody LoginRq datos);

    /**
     * Paso 2: verificar el código de 6 caracteres y obtener el token JWT.
     * <p>El código vence en 5 minutos y admite 5 intentos.</p>
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Paso 2: verificar el código de 6 caracteres y obtener el token JWT",
            description = "El código vence en 5 minutos y admite 5 intentos.")
    /**
     * Paso 2 del inicio de sesión: valida el código y emite el token JWT.
     *
     * @param datos desafío y código.
     * @return sesión iniciada con su token.
     */
    @PostMapping("/verificar")
    ResponseEntity<LoginRs> verificar(@RequestBody VerificarRq datos);

    /**
     * Reenviar un nuevo código para el mismo desafío (invalida el anterior).
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Reenviar un nuevo código para el mismo desafío (invalida el anterior)")
    @PostMapping("/reenviar")
    ResponseEntity<DesafioRs> reenviar(@RequestBody DesafioRq datos);

    /**
     * ¿Olvidaste tu contraseña?: envía un código al correo del usuario (usuario o correo).
     * <p>Responde igual exista o no el usuario, para no revelar cuentas registradas.</p>
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "¿Olvidaste tu contraseña?: envía un código al correo del usuario (usuario o correo)",
            description = "Responde igual exista o no el usuario, para no revelar cuentas registradas.")
    /**
     * Inicia la recuperación de contraseña enviando un código al correo.
     *
     * @param datos usuario o correo.
     * @return desafío de recuperación.
     */
    @PostMapping("/recuperar")
    ResponseEntity<DesafioRs> recuperar(@RequestBody RecuperarRq datos);

    /**
     * Restablecer la contraseña con el código recibido (cierra las sesiones abiertas).
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta sin contenido (204).
     */
    @Operation(summary = "Restablecer la contraseña con el código recibido (cierra las sesiones abiertas)")
    @PostMapping("/restablecer")
    ResponseEntity<Void> restablecer(@RequestBody RestablecerRq datos);

    /**
     * Validar la sesión actual.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Validar la sesión actual")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/validar")
    ResponseEntity<LoginRs> validar();

    /**
     * Cerrar la sesión actual (revoca el token).
     *
     * @return respuesta sin contenido (204).
     */
    @Operation(summary = "Cerrar la sesión actual (revoca el token)")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    ResponseEntity<Void> logout();
}
