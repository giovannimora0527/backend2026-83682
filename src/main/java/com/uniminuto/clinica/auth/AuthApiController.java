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
    public ResponseEntity<DesafioRs> login(LoginRq datos) {
        return ResponseEntity.ok(servicio.login(datos));
    }

    @Override
    public ResponseEntity<LoginRs> verificar(VerificarRq datos) {
        return ResponseEntity.ok(servicio.verificar(datos));
    }

    @Override
    public ResponseEntity<DesafioRs> reenviar(DesafioRq datos) {
        return ResponseEntity.ok(servicio.reenviar(datos == null ? null : datos.desafio()));
    }

    @Override
    public ResponseEntity<DesafioRs> recuperar(RecuperarRq datos) {
        return ResponseEntity.ok(servicio.recuperar(datos == null ? null : datos.usuario()));
    }

    @Override
    public ResponseEntity<Void> restablecer(RestablecerRq datos) {
        servicio.restablecer(datos);
        return ResponseEntity.noContent().build();
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
