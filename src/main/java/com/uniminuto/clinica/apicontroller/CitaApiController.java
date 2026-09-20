package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

/** Implementa {@link CitaApi}. Sin try/catch: los errores los resuelve GlobalExceptionHandler. */
@RestController
public class CitaApiController implements CitaApi {

    /** Servicio de citas. */
    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<CitaRs> guardarCita(CitaRq citaRq) {
        return ResponseEntity.ok(citaService.crearCita(citaRq));
    }

    @Override
    public ResponseEntity<CitaRs> actualizarCita(CitaRq citaRq) {
        return ResponseEntity.ok(citaService.actualizarCita(citaRq));
    }

    @Override
    public ResponseEntity<List<CitaRs>> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        return ResponseEntity.ok(citaService.filtrarCitas(fechaInicial, fechaFinal));
    }
}
