package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String ESTADO_PROGRAMADA = "PROGRAMADA";

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;


    @Override
    public List<Cita> listarCitas() throws BadRequestException {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        this.validarFechas(fechaInicio, fechaFin);
        List<Cita> citas = citaRepository.findByFechaHoraBetween(fechaInicio, fechaFin);
        citas.sort((cita1, cita2)
                -> cita1.getFechaHora().compareTo(cita2.getFechaHora()));
        return citas;
    }

    private void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser mayor a la fecha de fin");
        }
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        this.validarCita(citaRq);
        Optional<Mascota> mascotaOptional = mascotaRepository.findById(citaRq.getMascotaId());
        if (mascotaOptional.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }

        Optional<Medico> medicoOptional = medicoRepository.findById(citaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico no existe");
        }

        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita no existe");
        }

        Cita citaActualizar = optCita.get();
        if (citaActualizar.getFechaHora().equals(citaRq.getFechaHora()) ) {
           if (!sePuedeAgendarCitaXFecha(citaActualizar)) {
               throw new BadRequestException("No se puede actualizar la cita  ya que hay una cita agendada en ese rango de fechas.");
           }
        }

        List<Cita> citas = citaRepository.findByMedicoAndFechaHoraBetween(medicoOptional.get(),
                citaRq.getFechaHora().minusMinutes(30), citaRq.getFechaHora().plusMinutes(30));
        if (!citas.isEmpty()) {
            throw new BadRequestException("No se puede actualizar la cita " +
                    "ya que hay el médico tiene una cita agendada en ese rango de fechas.");
        }

        citaActualizar.setMedico(medicoOptional.get());
        citaActualizar.setMascota(mascotaOptional.get());
        citaActualizar.setFechaHora(citaRq.getFechaHora());
        citaActualizar.setMotivo(citaRq.getMotivo());
        citaActualizar.setEstado(citaRq.getEstado());
        citaRepository.save(citaActualizar);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendo una nueva cita para la mascota "
                + mascotaOptional.get().getNombreMascota() + " con el médico " + medicoOptional.get().getNombres() + " "
                + medicoOptional.get().getApellidos() + " para el día " + citaRq.getFechaHora().toString());

        return rta;
    }

    @Override
    public MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException {
        this.validarCita(citaRq);
        Optional<Mascota> mascotaOptional = mascotaRepository.findById(citaRq.getMascotaId());
        if (mascotaOptional.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }

        Optional<Medico> medicoOptional = medicoRepository.findById(citaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico no existe");
        }

        Optional<Cita> citaOptional = citaRepository.findByMascotaAndMedico(mascotaOptional.get(), medicoOptional.get());

        if (citaOptional.isPresent()) {
            // Valido las fechas de la cita para darle un tiempo de 30 minutos para que no se solapen las citas
            Cita cita = citaOptional.get();
            LocalDateTime fechaHoraCita = cita.getFechaHora();
            LocalDateTime fechaHoraCitaMas30Min = fechaHoraCita.plusMinutes(15);
            LocalDateTime fechaHoraCitaMenos30Min = fechaHoraCita.minusMinutes(15);
            if (citaRq.getFechaHora().isAfter(fechaHoraCitaMenos30Min)
                    && citaRq.getFechaHora().isBefore(fechaHoraCitaMas30Min)
                    && cita.getEstado().equals(ESTADO_PROGRAMADA)) {
                throw new BadRequestException("No se puede asignar la cita ya que tiene una cita programada en ese rango de fechas.");
            }
        }

        List<Cita> citas = citaRepository
                .findByMedicoAndFechaHoraBetween(medicoOptional.get(),
                        citaRq.getFechaHora().minusMinutes(15),
                        citaRq.getFechaHora().plusMinutes(15));
        if (!citas.isEmpty()) {
            throw new BadRequestException("El médico ya tiene una cita programada en ese rango de hora.");
        }

        Mascota mascota = mascotaOptional.get();
        Medico medico = medicoOptional.get();
        Cita nuevaCita = new Cita();
        nuevaCita.setFechaHora(citaRq.getFechaHora());
        nuevaCita.setCliente(mascota.getCliente());
        nuevaCita.setMascota(mascota);
        nuevaCita.setMedico(medico);
        nuevaCita.setMotivo(citaRq.getMotivo());
        nuevaCita.setEstado(ESTADO_PROGRAMADA);
        citaRepository.save(nuevaCita);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendo una nueva cita para la mascota "
                + mascota.getNombreMascota() + " con el médico " + medico.getNombres() + " "
                + medico.getApellidos() + " para el día " + citaRq.getFechaHora().toString());

        return rta;
    }

    private boolean sePuedeAgendarCitaXFecha(Cita cita) {
        LocalDateTime fechaHoraCita = cita.getFechaHora();
        LocalDateTime fechaHoraCitaMas30Min = fechaHoraCita.plusMinutes(15);
        LocalDateTime fechaHoraCitaMenos30Min = fechaHoraCita.minusMinutes(15);
        return !cita.getFechaHora().isAfter(fechaHoraCitaMenos30Min)
                || !cita.getFechaHora().isBefore(fechaHoraCitaMas30Min)
                || !cita.getEstado().equals(ESTADO_PROGRAMADA);
    }

    private void validarCita(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("La cita no puede ser nula");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El id de la mascota no puede ser nulo");
        }
        if (citaRq.getMedicoId() == null) {
            throw new BadRequestException("El id del medico no puede ser nulo");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().isEmpty()) {
            throw new BadRequestException("El motivo de la cita no puede ser nulo o vacio");
        }
    }
}
