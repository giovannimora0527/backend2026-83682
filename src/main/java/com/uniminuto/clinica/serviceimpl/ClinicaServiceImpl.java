package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClinicaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de pruebas de la clínica.
 */
@Service
public class ClinicaServiceImpl implements ClinicaService {

    /**
     * Método de prueba que retorna un texto de confirmación.
     * @return String con el mensaje de que el servicio funciona.
     * @throws BadRequestException si ocurre un error.
     */
    @Override
    public String testService2() throws BadRequestException {
        return "Servicio ok";
    }

    /**
     * Método de prueba que retorna una respuesta estandarizada.
     * @return Objeto MiRespuestaRS indicando que el servicio funciona.
     * @throws BadRequestException si ocurre un error.
     */
    @Override
    public MiRespuestaRS testService3() throws BadRequestException {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Servicio ok desde clase y objeto");
        return respuesta;
    }
}