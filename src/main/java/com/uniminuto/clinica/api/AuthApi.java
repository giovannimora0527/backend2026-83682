package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/auth")
public interface AuthApi {

   @PostMapping(value = "/login",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<AuthRs> login(
           @RequestBody AuthRq authRq)
           throws BadRequestException;

    @PostMapping(value = "/validate-token",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<LoginRs> validateToken(
            @RequestBody TokenRq tokenRq)
            throws BadRequestException;


    @PostMapping(value = "/cerrar-sesion",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<CloseSessionRs> cerrarSesion(
            @RequestBody CloseSession closeSession)
            throws BadRequestException;
}
