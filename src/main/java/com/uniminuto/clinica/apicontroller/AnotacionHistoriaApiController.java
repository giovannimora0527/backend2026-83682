package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnotacionHistoriaApiController
        implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;


    // Lista todas las anotaciones
    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones()
            throws BadRequestException {

        return ResponseEntity.ok(
                anotacionHistoriaService.listarAnotaciones()
        );
    }


    // Guarda una nueva anotación
    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException {

        return ResponseEntity.ok(
                anotacionHistoriaService.guardarAnotacion(
                        anotacionHistoriaRq
                )
        );
    }


    // Actualiza una anotación existente
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException {

        return ResponseEntity.ok(
                anotacionHistoriaService.actualizarAnotacion(
                        anotacionHistoriaRq
                )
        );
    }
}