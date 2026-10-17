package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.CitaDTO;
import com.uniminuto.clinica.exception.FechaInvalidaException;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.Cita;
import com.uniminuto.clinica.models.EstadoCita;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import com.uniminuto.clinica.models.Veterinario;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.service.PacienteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link CitaService}.
 *
 * <p>Contiene la logica de negocio para filtrar, crear y actualizar citas
 * (Requerimientos 2, 3 y 4 del taller).</p>
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Repositorio para acceder a los datos de Cita en la base de datos. */
    private final CitaRepository citaRepository;

    /** Servicio de pacientes, usado para validar y obtener el paciente asociado a una cita. */
    private final PacienteService pacienteServiceImpl;
    private final VeterinarioRepository veterinarioRepository;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param citaRepository      repositorio de citas
     * @param pacienteServiceImpl servicio para obtener los pacientes asociados a las citas
     */
    public CitaServiceImpl(CitaRepository citaRepository, PacienteService pacienteServiceImpl, VeterinarioRepository veterinarioRepository) {
        this.citaRepository = citaRepository; this.pacienteServiceImpl = pacienteServiceImpl; this.veterinarioRepository = veterinarioRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<CitaDTO> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Validamos que las fechas hayan llegado y que el rango tenga sentido
        // antes de consultar la base de datos, para controlar el flujo de error.
        validarRangoDeFechas(fechaInicio, fechaFin);

        return citaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public CitaDTO obtenerCitaPorId(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cita solicitada."));
        return convertirADTO(cita);
    }

    /** {@inheritDoc} */
    @Override
    public CitaDTO crearCita(CitaDTO citaDTO) {
        // Buscamos el paciente al que pertenece la cita; si no existe,
        // buscarPacientePorId lanza automaticamente RecursoNoEncontradoException.
        Paciente paciente = pacienteServiceImpl.buscarPacientePorId(citaDTO.getPacienteId());
        validarClienteDeMascota(citaDTO.getClienteId(), paciente);

        validarFechaFutura(citaDTO.getFecha());
        Veterinario veterinario = obtenerVeterinarioObligatorio(citaDTO.getVeterinarioId());
        validarDisponibilidad(citaDTO.getFecha(), paciente.getId(), veterinario.getId(), null);

        Cita cita = new Cita();
        cita.setFecha(citaDTO.getFecha());
        cita.setMotivo(citaDTO.getMotivo());
        cita.setPaciente(paciente);
        cita.setVeterinario(veterinario);

        // Si no se envia un estado, la cita nueva queda como PENDIENTE por defecto.
        cita.setEstado(convertirEstado(citaDTO.getEstado(), EstadoCita.PENDIENTE));

        Cita citaGuardada = citaRepository.save(cita);
        return convertirADTO(citaGuardada);
    }

    /** {@inheritDoc} */
    @Override
    public CitaDTO actualizarCita(Long id, CitaDTO citaDTO) {
        // 1. Buscamos la cita que se quiere actualizar; si no existe, error 404 controlado.
        Cita citaExistente = citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la cita con id " + id));

        // Una cita cancelada o atendida conserva su valor histórico. Para evitar
        // cambios accidentales solo se permite editar citas activas.
        if (citaExistente.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("Una cita cancelada no se puede editar. Registre una nueva cita.");
        }
        if (citaExistente.getEstado() == EstadoCita.COMPLETADA || citaExistente.getEstado() == EstadoCita.ATENDIDA) {
            throw new IllegalArgumentException("Una cita atendida no se puede modificar porque hace parte del historial clínico.");
        }

        // 2. Si se envio un pacienteId distinto, validamos que el nuevo paciente exista.
        if (citaDTO.getPacienteId() != null) {
            Paciente paciente = pacienteServiceImpl.buscarPacientePorId(citaDTO.getPacienteId());
            validarClienteDeMascota(citaDTO.getClienteId(), paciente);
            citaExistente.setPaciente(paciente);
        }
        if (citaDTO.getVeterinarioId() != null) {
            citaExistente.setVeterinario(obtenerVeterinarioObligatorio(citaDTO.getVeterinarioId()));
        }

        // 3. Actualizamos solo los campos que llegaron en la peticion.
        if (citaDTO.getFecha() != null) {
            citaExistente.setFecha(citaDTO.getFecha());
        }
        if (citaDTO.getMotivo() != null) {
            citaExistente.setMotivo(citaDTO.getMotivo());
        }
        if (citaDTO.getEstado() != null) {
            citaExistente.setEstado(convertirEstado(citaDTO.getEstado(), citaExistente.getEstado()));
        }

        // Solo se exige una fecha futura cuando se programa o reprograma una cita activa.
        // Permite marcar como atendida una cita cuya hora ya pasó.
        if (citaDTO.getFecha() != null && citaExistente.getEstado() != EstadoCita.ATENDIDA && citaExistente.getEstado() != EstadoCita.COMPLETADA) {
            validarFechaFutura(citaExistente.getFecha());
        }
        if (citaExistente.getEstado() != EstadoCita.ATENDIDA && citaExistente.getEstado() != EstadoCita.COMPLETADA) {
            validarDisponibilidad(citaExistente.getFecha(), citaExistente.getPaciente().getId(), citaExistente.getVeterinario() == null ? null : citaExistente.getVeterinario().getId(), id);
        }
        Cita citaActualizada = citaRepository.save(citaExistente);
        return convertirADTO(citaActualizada);
    }

    /**
     * Valida que el rango de fechas recibido sea correcto: ambas fechas
     * deben existir y la fecha inicial no puede ser posterior a la final.
     *
     * @param fechaInicio fecha inicial del rango
     * @param fechaFin    fecha final del rango
     * @throws FechaInvalidaException si el rango de fechas no es valido
     */
    /**
     * Evita agendar una cita en el pasado y detectar conflictos de horario
     * para la mascota y el veterinario dentro de una ventana de 15 minutos.
     */
    private void validarFechaFutura(LocalDateTime fecha) {
        if (fecha == null || fecha.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de la cita debe ser igual o posterior a la fecha y hora actual.");
        }
    }

    /** Comprueba que la mascota no tenga otra cita activa a menos de 15 minutos. */
    private void validarDisponibilidad(LocalDateTime fecha, Long pacienteId, Long veterinarioId, Long citaIgnoradaId) {
        for (Cita otra : citaRepository.findAll()) {
            if (citaIgnoradaId != null && citaIgnoradaId.equals(otra.getId())) continue;
            if (otra.getEstado() == EstadoCita.CANCELADA) continue;
            if (otra.getFecha() == null) continue;
            long minutos = Math.abs(ChronoUnit.MINUTES.between(fecha, otra.getFecha()));
            boolean mismaMascota = pacienteId != null && otra.getPaciente() != null && pacienteId.equals(otra.getPaciente().getId());
            boolean mismoVeterinario = veterinarioId != null && otra.getVeterinario() != null && veterinarioId.equals(otra.getVeterinario().getId());
            if (minutos <= 15 && mismaMascota) throw new IllegalArgumentException("La mascota ya tiene una cita dentro de un intervalo de 15 minutos.");
            if (minutos <= 15 && mismoVeterinario) throw new IllegalArgumentException("El veterinario ya tiene una cita dentro de un intervalo de 15 minutos.");
        }
    }

    /** Impide asociar una cita a una mascota de otro cliente seleccionado. */
    private void validarClienteDeMascota(Long clienteId, Paciente paciente) {
        if (clienteId == null) throw new IllegalArgumentException("Debe seleccionar un cliente.");
        if (paciente.getCliente() == null || !clienteId.equals(paciente.getCliente().getId()))
            throw new IllegalArgumentException("La mascota no pertenece al cliente seleccionado.");
    }

    private Veterinario obtenerVeterinarioObligatorio(Long id) {
        if (id == null) throw new IllegalArgumentException("Debe seleccionar un veterinario.");
        Veterinario veterinario = veterinarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe el veterinario indicado."));
        if (!veterinario.isActivo()) throw new IllegalArgumentException("El veterinario seleccionado no está activo.");
        return veterinario;
    }

    private void validarRangoDeFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new FechaInvalidaException("Debe indicar la fecha inicial y la fecha final para filtrar las citas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new FechaInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    /**
     * Convierte el texto del estado que llega en el DTO al valor del enum
     * {@link EstadoCita}, controlando el caso en que el texto no sea
     * valido (por ejemplo, si el usuario escribe "PENDIENT" mal escrito).
     *
     * @param texto        estado recibido como texto (puede ser nulo)
     * @param valorPorDefecto estado a usar si el texto es nulo
     * @return el valor del enum correspondiente
     * @throws FechaInvalidaException reutilizada aqui de forma generica para
     *         reportar datos invalidos; en un caso real se usaria una
     *         excepcion mas especifica, pero se mantiene simple a proposito.
     */
    private EstadoCita convertirEstado(String texto, EstadoCita valorPorDefecto) {
        if (texto == null || texto.isBlank()) {
            return valorPorDefecto;
        }
        try {
            return EstadoCita.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "El estado '" + texto + "' no es valido. Los valores permitidos son: PENDIENTE, CONFIRMADA, PROGRAMADA, ATENDIDA, COMPLETADA, CANCELADA");
        }
    }

    /**
     * Convierte una entidad {@link Cita} en su correspondiente
     * {@link CitaDTO}.
     *
     * @param cita entidad a convertir
     * @return DTO con los datos de la cita
     */
    private CitaDTO convertirADTO(Cita cita) {
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setFecha(cita.getFecha());
        dto.setMotivo(cita.getMotivo());
        dto.setEstado(cita.getEstado() != null ? cita.getEstado().name() : null);
        if (cita.getPaciente() != null) {
            dto.setPacienteId(cita.getPaciente().getId()); dto.setNombrePaciente(cita.getPaciente().getNombre());
            if (cita.getPaciente().getCliente() != null) { dto.setClienteId(cita.getPaciente().getCliente().getId()); dto.setNombreCliente(cita.getPaciente().getCliente().getNombre()); }
        }
        if (cita.getVeterinario() != null) { dto.setVeterinarioId(cita.getVeterinario().getId()); dto.setNombreVeterinario(cita.getVeterinario().getNombre()); }
        return dto;
    }
}
