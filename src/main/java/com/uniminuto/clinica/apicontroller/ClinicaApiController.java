
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClinicaApi;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los servicios de comprobación
 * y funcionamiento de la clínica veterinaria.
 */
@RestController
public class ClinicaApiController implements ClinicaApi {

    /**
     * Servicio que contiene las operaciones relacionadas con la clínica.
     */
    @Autowired
    private ClinicaService clinicaService;

    /**
     * Comprueba que el servicio de la clínica responde correctamente.
     *
     * @return respuesta HTTP con un mensaje de confirmación
     * @throws BadRequestException si ocurre un error de solicitud
     */
    @Override
    public ResponseEntity<String> testService() throws BadRequestException {
        return ResponseEntity.ok("Servicio funcionando correctamente");
    }

    /**
     * Ejecuta la segunda comprobación del servicio de la clínica.
     *
     * @return respuesta HTTP con el resultado obtenido del servicio
     * @throws BadRequestException si ocurre un error de solicitud
     */
    @Override
    public ResponseEntity<String> testService2() throws BadRequestException {
        return ResponseEntity.ok(clinicaService.testService2());
    }

    /**
     * Ejecuta la tercera comprobación del servicio de la clínica.
     *
     * @return respuesta HTTP con el objeto de respuesta del servicio
     * @throws BadRequestException si ocurre un error de solicitud
     */
    @Override
    public ResponseEntity<MiRespuestaRS> testService3() throws BadRequestException {
        return ResponseEntity.ok(clinicaService.testService3());
    }

}
