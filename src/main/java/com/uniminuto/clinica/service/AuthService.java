package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;

public interface AuthService {

    AuthRs login(AuthRq authRq) throws BadRequestException;

    LoginRs validateToken(TokenRq tokenRq) throws BadRequestException;
}
