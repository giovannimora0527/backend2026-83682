package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.HorarioRq;
import com.uniminuto.clinica.models.HorarioRs;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.HorarioService;
import com.uniminuto.clinica.api.MedicoApi;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class MedicoApiController implements MedicoApi {

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private HorarioService horarioService;

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<Medico>> listarMedicosDisponibles(LocalDateTime inicio, LocalDateTime fin,
                                                                 LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicosDisponiblesPorFecha(
                inicio == null ? fechaInicio : inicio, fin == null ? fechaFin : fin));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<Medico>> listarTodos() throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicos());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<MiRespuestaRS> crearMedico(MedicoRq medicoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.guardarMedico(medicoRq));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<MiRespuestaRS> actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.actualizarMedico(medicoRq));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<HorarioRs>> listarHorarios(Long id) {
        return ResponseEntity.ok(this.horarioService.listar(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<RespuestaRs<List<HorarioRs>>> guardarHorarios(Long id, List<HorarioRq> horarios) {
        return ResponseEntity.ok(RespuestaRs.ok("Horarios guardados correctamente.",
                this.horarioService.reemplazar(id, horarios)));
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<HorarioRs>> misHorarios() {
        return ResponseEntity.ok(this.horarioService.misHorarios());
    }
}
