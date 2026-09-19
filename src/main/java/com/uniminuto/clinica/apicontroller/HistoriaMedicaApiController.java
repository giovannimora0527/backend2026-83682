package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaMedicaService;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class HistoriaMedicaApiController
        implements HistoriaMedicaApi {

    @Autowired
    private HistoriaMedicaService historiaMedicaService;


    // Lista las historias médicas por rango de fechas
    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistorias(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) throws BadRequestException {

        return ResponseEntity.ok(
                historiaMedicaService.listarHistorias(
                        fechaInicial,
                        fechaFinal
                )
        );
    }


    // Guarda una nueva historia médica
    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(
            HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException {

        return ResponseEntity.ok(
                historiaMedicaService.guardarHistoriaMedica(
                        historiaMedicaRq
                )
        );
    }


    // Actualiza una historia médica existente
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(
            HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException {

        return ResponseEntity.ok(
                historiaMedicaService.actualizarHistoriaMedica(
                        historiaMedicaRq
                )
        );
    }
}