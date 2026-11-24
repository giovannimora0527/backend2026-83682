package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HorarioMedico;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.HorarioRq;
import com.uniminuto.clinica.models.HorarioRs;
import com.uniminuto.clinica.repository.HorarioMedicoRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.HorarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class HorarioServiceImpl implements HorarioService {

    private final HorarioMedicoRepository horarioRepository;
    private final MedicoRepository medicoRepository;
    private final UsuarioActual usuarioActual;

    public HorarioServiceImpl(HorarioMedicoRepository horarioRepository, MedicoRepository medicoRepository,
                              UsuarioActual usuarioActual) {
        this.horarioRepository = horarioRepository;
        this.medicoRepository = medicoRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public List<HorarioRs> listar(Long medicoId) {
        buscarMedico(medicoId);
        return consultar(medicoId);
    }

    @Override
    public List<HorarioRs> misHorarios() {
        Long medicoId = usuarioActual.obtener().medicoId();
        if (medicoId == null) {
            throw new ConflictException("Su usuario no está vinculado a un médico.");
        }
        return consultar(medicoId);
    }

    @Override
    @Transactional
    public List<HorarioRs> reemplazar(Long medicoId, List<HorarioRq> horarios) {
        buscarMedico(medicoId);
        validar(horarios);
        horarioRepository.eliminarPorMedico(medicoId);
        Medico medico = buscarMedico(medicoId);
        for (HorarioRq rq : horarios) {
            HorarioMedico horario = new HorarioMedico();
            horario.setMedico(medico);
            horario.setDiaSemana(rq.diaSemana());
            horario.setHoraInicio(rq.horaInicio());
            horario.setHoraFin(rq.horaFin());
            horario.setActivo(!Boolean.FALSE.equals(rq.activo()));
            horarioRepository.save(horario);
        }
        horarioRepository.flush();
        return consultar(medicoId);
    }

    private void validar(List<HorarioRq> horarios) {
        for (HorarioRq h : horarios) {
            if (h == null || h.diaSemana() == null || h.diaSemana() < 1 || h.diaSemana() > 7) {
                throw new BadRequestException("El día de la semana debe estar entre 1 (lunes) y 7 (domingo).");
            }
            if (h.horaInicio() == null || h.horaFin() == null || !h.horaFin().isAfter(h.horaInicio())) {
                throw new BadRequestException("La hora final debe ser posterior a la hora inicial.");
            }
        }
        List<HorarioRq> orden = horarios.stream()
                .sorted(Comparator.comparing(HorarioRq::diaSemana).thenComparing(HorarioRq::horaInicio))
                .toList();
        for (int i = 1; i < orden.size(); i++) {
            HorarioRq anterior = orden.get(i - 1);
            HorarioRq actual = orden.get(i);
            if (anterior.diaSemana().equals(actual.diaSemana())
                    && actual.horaInicio().isBefore(anterior.horaFin())) {
                throw new ConflictException("horaInicio", "Hay bloques superpuestos el mismo día.");
            }
        }
    }

    private Medico buscarMedico(Long medicoId) {
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new NotFoundException("El médico no existe."));
    }

    private List<HorarioRs> consultar(Long medicoId) {
        return horarioRepository.findByMedico_IdOrderByDiaSemanaAscHoraInicioAsc(medicoId).stream()
                .map(HorarioRs::de).toList();
    }
}
