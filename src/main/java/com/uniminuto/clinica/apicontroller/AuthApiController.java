package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AuthApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import com.uniminuto.clinica.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController implements AuthApi {

    @Autowired
    private AuthService authService;

    @Override
    public ResponseEntity<AuthRs> login(AuthRq authRq) throws BadRequestException {
        // Implement the login logic here
        return ResponseEntity.ok(this.authService.login(authRq));
    }

    @Override
    public ResponseEntity<LoginRs> validateToken(TokenRq tokenRq) throws BadRequestException {
        return ResponseEntity.ok(this.authService.validateToken(tokenRq));
    }

    @Override
    public ResponseEntity<CloseSessionRs> cerrarSesion(CloseSession closeSession) throws BadRequestException {
        return ResponseEntity.ok(this.authService.cerrarSesion(closeSession));
    }
}
