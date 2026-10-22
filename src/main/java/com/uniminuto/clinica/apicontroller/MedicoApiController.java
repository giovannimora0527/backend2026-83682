package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicoApi;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class MedicoApiController implements MedicoApi {

    @Autowired
    private MedicoService medicoService;

    @Override
    public ResponseEntity<List<Medico>> listarMedicosDisponibles(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicosDisponiblesPorFecha(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<List<Medico>> listarTodos() throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicos());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearMedico(MedicoRq medicoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.guardarMedico(medicoRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.actualizarMedico(medicoRq));
    }
}
