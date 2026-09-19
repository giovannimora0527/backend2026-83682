package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador encargado de atender las peticiones
 * relacionadas con las fórmulas médicas.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /**
     * Servicio utilizado para gestionar
     * las fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    /**
     * Lista todas las fórmulas médicas ordenadas
     * desde la más reciente hasta la más antigua.
     *
     * @return respuesta HTTP con la lista de fórmulas médicas.
     * @throws BadRequestException si ocurre un error en la solicitud.
     */
    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException {

        return ResponseEntity.ok(
                this.formulaMedicaService.obtenerFormulasMedicas()
        );
    }
}