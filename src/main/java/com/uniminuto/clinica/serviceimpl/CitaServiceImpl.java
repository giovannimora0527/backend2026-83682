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
    private static final String ESTADO_CANCELADA = "CANCELADA";
    /** Duracion por defecto; pasa a salir del servicio cuando exista ese modelo. */
    private static final int DURACION_MINUTOS = 30;

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

    @Override
    public List<Cita> listarCitasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio y debe ser mayor a cero.");
        }
        Optional<Mascota> mascotaOptional = mascotaRepository.findById(mascotaId);
        if (mascotaOptional.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }
        return citaRepository.findByMascotaOrderByFechaHoraDesc(mascotaOptional.get());
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

        if (citaRq.getId() == null) {
            throw new BadRequestException("El id de la cita no puede ser nulo");
        }
        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita no existe");
        }

        Cita citaActualizar = optCita.get();
        String estado = citaRq.getEstado() == null || citaRq.getEstado().isBlank()
                ? citaActualizar.getEstado() : citaRq.getEstado();
        if (!ESTADO_CANCELADA.equalsIgnoreCase(estado)) {
            this.validarSolapamiento(citaRq, medicoOptional.get(), mascotaOptional.get());
        }
        citaActualizar.setMedico(medicoOptional.get());
        citaActualizar.setMascota(mascotaOptional.get());
        citaActualizar.setCliente(mascotaOptional.get().getCliente());
        citaActualizar.setFechaHora(citaRq.getFechaHora());
        citaActualizar.setMotivo(citaRq.getMotivo());
        citaActualizar.setEstado(estado);
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

        citaRq.setId(null);
        this.validarSolapamiento(citaRq, medicoOptional.get(), mascotaOptional.get());

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

    private void validarSolapamiento(CitaRq citaRq, Medico medico, Mascota mascota) throws BadRequestException {
        LocalDateTime inicio = citaRq.getFechaHora();
        if (citaRepository.existeSolapamiento(medico, mascota, inicio.minusMinutes(DURACION_MINUTOS),
                inicio.plusMinutes(DURACION_MINUTOS), citaRq.getId())) {
            throw new BadRequestException("El médico o la mascota ya tienen una cita en ese rango de hora.");
        }
    }

    private void validarCita(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("La cita no puede ser nula");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
        if (citaRq.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser anterior a la fecha y hora actual");
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
