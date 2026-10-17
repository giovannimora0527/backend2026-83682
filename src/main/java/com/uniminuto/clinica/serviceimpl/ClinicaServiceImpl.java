package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClinicaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.stereotype.Service;

/** Implementa las operaciones simples de comprobación del servicio de clínica. */
/** Implementa las operaciones simples de comprobación del servicio de clínica. */
@Service
public class ClinicaServiceImpl implements ClinicaService {

    /** {@inheritDoc} */
    @Override
    public String testService2() throws BadRequestException {
        return "Servicio ok";
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS testService3() throws BadRequestException {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Servicio con respuesta personalizada :)");
        return rta;
    }
}
