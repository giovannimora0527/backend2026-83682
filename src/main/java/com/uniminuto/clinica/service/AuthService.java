package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {

    AuthRs login(AuthRq authRq) throws BadRequestException;

    LoginRs validateToken(TokenRq tokenRq) throws BadRequestException;

    CloseSessionRs cerrarSesion(CloseSession closeSession)
            throws BadRequestException;
}
