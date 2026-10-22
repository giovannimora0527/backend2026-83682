package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoriaClinicaApiController implements HistoriaApi {

    @Autowired
    private HistoriaService historiaService;

    @Override
    public ResponseEntity<List<HistoriaMedicaRs>> listarAllHIstorias() throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.getAllHistorias());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoria(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.guardarHistoria(historiaMedicaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoria(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.actualizarHistoria(historiaMedicaRq));
    }
}
