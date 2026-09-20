package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

/** Implementa {@link AnotacionHistoriaApi}. Sin try/catch: los errores los resuelve GlobalExceptionHandler. */
@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    /** Servicio de anotaciones de historia medica. */
    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    @Override
    public ResponseEntity<AnotacionHistoriaRs> guardarAnotacion(AnotacionHistoriaRq anotacionRq) {
        return ResponseEntity.ok(anotacionHistoriaService.crearAnotacion(anotacionRq));
    }

    @Override
    public ResponseEntity<AnotacionHistoriaRs> actualizarAnotacion(AnotacionHistoriaRq anotacionRq) {
        return ResponseEntity.ok(anotacionHistoriaService.actualizarAnotacion(anotacionRq));
    }

    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones() {
        return ResponseEntity.ok(anotacionHistoriaService.listarAnotaciones());
    }

    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> filtrarAnotaciones(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        return ResponseEntity.ok(anotacionHistoriaService.filtrarAnotaciones(fechaInicial, fechaFinal));
    }
}
