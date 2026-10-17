
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;

/**
 * Define las operaciones de comprobación del servicio de la clínica.
 */
public interface ClinicaService {

    /**
     * Ejecuta una comprobación básica del servicio.
     *
     * @return mensaje que indica que el servicio está disponible
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    String testService2() throws BadRequestException;

    /**
     * Ejecuta una comprobación del servicio mediante una respuesta personalizada.
     *
     * @return respuesta con el estado y el mensaje de comprobación
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    MiRespuestaRS testService3() throws BadRequestException;
}
