package com.uniminuto.clinica.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del módulo: implementa {@link AuthApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class AuthApiController implements AuthApi {

    /** Dependencia inyectada por constructor: servicio. */
    private final AuthService servicio;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param servicio dependencia servicio.
     */
    public AuthApiController(AuthService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DesafioRs> login(LoginRq datos) {
        return ResponseEntity.ok(servicio.login(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<LoginRs> verificar(VerificarRq datos) {
        return ResponseEntity.ok(servicio.verificar(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DesafioRs> reenviar(DesafioRq datos) {
        return ResponseEntity.ok(servicio.reenviar(datos == null ? null : datos.desafio()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<DesafioRs> recuperar(RecuperarRq datos) {
        return ResponseEntity.ok(servicio.recuperar(datos == null ? null : datos.usuario()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> restablecer(RestablecerRq datos) {
        servicio.restablecer(datos);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<LoginRs> validar() {
        return ResponseEntity.ok(servicio.validar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> logout() {
        servicio.logout();
        return ResponseEntity.noContent().build();
    }
}
