package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;


    // Lista las citas entre dos fechas
    @Override
    public ResponseEntity<List<Cita>> listarCitas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) throws BadRequestException {

        return ResponseEntity.ok(
                citaService.listarCita(fechaInicial, fechaFinal)
        );
    }


    // Guarda una nueva cita
    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(
            CitaRq citaRq
    ) throws BadRequestException {

        return ResponseEntity.ok(
                citaService.guardarCita(citaRq)
        );
    }


    // Actualiza una cita existente
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(
            CitaRq citaRq
    ) throws BadRequestException {

        return ResponseEntity.ok(
                citaService.actualizarCita(citaRq)
        );
    }
}