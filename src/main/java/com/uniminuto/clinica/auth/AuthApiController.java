package com.uniminuto.clinica.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController implements AuthApi {

    private final AuthService servicio;

    public AuthApiController(AuthService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<LoginRs> login(LoginRq datos) {
        return ResponseEntity.ok(servicio.login(datos));
    }

    @Override
    public ResponseEntity<LoginRs> validar() {
        return ResponseEntity.ok(servicio.validar());
    }

    @Override
    public ResponseEntity<Void> logout() {
        servicio.logout();
        return ResponseEntity.noContent().build();
    }
}
