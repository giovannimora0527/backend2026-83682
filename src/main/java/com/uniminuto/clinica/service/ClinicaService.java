package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

public interface ClinicaService {

    String testService2() throws BadRequestException;

    MiRespuestaRS testService3() throws BadRequestException;
}
