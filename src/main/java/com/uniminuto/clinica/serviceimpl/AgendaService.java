package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HorarioMedico;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HorarioMedicoRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Reglas para ocupar un espacio de la agenda (5.3). Bloquea medico y mascota, en ese orden. */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class AgendaService {

    public static final int DURACION_POR_DEFECTO = 30;

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final MascotaRepository mascotaRepository;
    private final HorarioMedicoRepository horarioRepository;

    public AgendaService(CitaRepository citaRepository, MedicoRepository medicoRepository,
                         MascotaRepository mascotaRepository, HorarioMedicoRepository horarioRepository) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
        this.mascotaRepository = mascotaRepository;
        this.horarioRepository = horarioRepository;
    }

    /** Espacio ya validado. */
    public record Espacio(Medico medico, Mascota mascota, LocalDateTime inicio, LocalDateTime fin) {
    }

    /**
     * @param exigirFuturo  al crear y cuando cambia la fecha
     * @param citaExcluida  id de la cita que se edita, para que no choque consigo misma
     */
    public Espacio reservar(LocalDateTime fecha, Long medicoId, Integer mascotaId, Long clienteId,
                            Servicio servicio, boolean exigirFuturo, Long citaExcluida) {
        LocalDateTime inicio = fecha.truncatedTo(ChronoUnit.MINUTES);
        if (exigirFuturo && !inicio.isAfter(LocalDateTime.now())) {
            throw new ConflictException("fechaInicio", "La cita debe ser en una fecha y hora futuras.");
        }
        Medico medico = medicoRepository.bloquear(medicoId)
                .orElseThrow(() -> new NotFoundException("El médico no existe."));
        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new ConflictException("medicoId", "El médico está inactivo.");
        }
        Mascota mascota = mascotaRepository.bloquear(mascotaId)
                .orElseThrow(() -> new NotFoundException("La mascota no existe."));
        if (!Boolean.TRUE.equals(mascota.getActivo())) {
            throw new ConflictException("mascotaId", "La mascota está inactiva.");
        }
        if (clienteId != null && !clienteId.equals(mascota.getCliente().getId())) {
            throw new ConflictException("clienteId", "El cliente no es el propietario de la mascota.");
        }
        LocalDateTime fin = inicio.plusMinutes(servicio == null ? DURACION_POR_DEFECTO : servicio.getDuracionMinutos());
        if (!fin.toLocalDate().equals(inicio.toLocalDate())) {
            throw new ConflictException("fechaInicio", "La cita no puede cruzar la medianoche.");
        }
        if (horarioRepository.findByMedico_IdAndActivoTrue(medicoId).stream()
                .noneMatch(h -> cabe(h, inicio, fin))) {
            throw new ConflictException("fechaInicio", "La cita está fuera del horario de disponibilidad del médico.");
        }
        if (citaRepository.medicoOcupado(medicoId, inicio, fin, citaExcluida)) {
            throw new ConflictException("fechaInicio", "El médico ya tiene una cita en ese rango de hora.");
        }
        if (citaRepository.mascotaOcupada(mascotaId, inicio, fin, citaExcluida)) {
            throw new ConflictException("fechaInicio", "La mascota ya tiene una cita en ese rango de hora.");
        }
        return new Espacio(medico, mascota, inicio, fin);
    }

    /** El rango, de un mismo dia, cabe completo en el bloque. */
    static boolean cabe(HorarioMedico horario, LocalDateTime inicio, LocalDateTime fin) {
        return horario.getDiaSemana() == inicio.getDayOfWeek().getValue()
                && fin.toLocalDate().equals(inicio.toLocalDate())
                && !inicio.toLocalTime().isBefore(horario.getHoraInicio())
                && !fin.toLocalTime().isAfter(horario.getHoraFin());
    }
}
