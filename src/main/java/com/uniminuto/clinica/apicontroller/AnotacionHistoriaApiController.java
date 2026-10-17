package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;


    @Override
    public ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoria) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.crearAnotacionHistoria(anotacionHistoria));
    }

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones() throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.listarAnotaciones());
    }

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarPorHistoria(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.listarPorHistoria(historiaId));
    }
}
