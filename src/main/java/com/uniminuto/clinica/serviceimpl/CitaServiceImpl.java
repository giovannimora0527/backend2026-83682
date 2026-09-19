package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.CitaDTO;
import com.uniminuto.clinica.exception.FechaInvalidaException;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.Cita;
import com.uniminuto.clinica.models.EstadoCita;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final PacienteServiceImpl pacienteServiceImpl;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param citaRepository      repositorio de citas
     * @param pacienteServiceImpl servicio para obtener los pacientes asociados a las citas
     */
    public CitaServiceImpl(CitaRepository citaRepository, PacienteServiceImpl pacienteServiceImpl) {
        this.citaRepository = citaRepository;
        this.pacienteServiceImpl = pacienteServiceImpl;
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
    public CitaDTO crearCita(CitaDTO citaDTO) {
        // Buscamos el paciente al que pertenece la cita; si no existe,
        // buscarPacientePorId lanza automaticamente RecursoNoEncontradoException.
        Paciente paciente = pacienteServiceImpl.buscarPacientePorId(citaDTO.getPacienteId());

        Cita cita = new Cita();
        cita.setFecha(citaDTO.getFecha());
        cita.setMotivo(citaDTO.getMotivo());
        cita.setPaciente(paciente);

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

        // 2. Si se envio un pacienteId distinto, validamos que el nuevo paciente exista.
        if (citaDTO.getPacienteId() != null) {
            Paciente paciente = pacienteServiceImpl.buscarPacientePorId(citaDTO.getPacienteId());
            citaExistente.setPaciente(paciente);
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
                    "El estado '" + texto + "' no es valido. Los valores permitidos son: PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA");
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
            dto.setPacienteId(cita.getPaciente().getId());
            dto.setNombrePaciente(cita.getPaciente().getNombre());
        }
        return dto;
    }
}
