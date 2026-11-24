package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Autenticación")
@RequestMapping("/auth")
public interface AuthApi {

    @Operation(summary = "Iniciar sesión: verifica credenciales y envía el OTP por correo")
   @PostMapping(value = "/login",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<AuthRs> login(
           @RequestBody AuthRq authRq)
           throws BadRequestException;

    @Operation(summary = "Validar el OTP y obtener el JWT")
    @PostMapping(value = "/validate-token",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<LoginRs> validateToken(
            @RequestBody TokenRq tokenRq)
            throws BadRequestException;


    @Operation(summary = "Cerrar la sesión del token")
    @PostMapping(value = "/cerrar-sesion",
            produces = {"application/json"})
    ResponseEntity<CloseSessionRs> cerrarSesion(
            @RequestBody(required = false) CloseSession closeSession)
            throws BadRequestException;
}
