package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.FormulaMedicaDTO;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.FormulaMedica;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import com.uniminuto.clinica.models.Cita;
import com.uniminuto.clinica.models.HistoriaMedica;
import com.uniminuto.clinica.models.Veterinario;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.service.PacienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Implementa la lógica de negocio de las fórmulas médicas. */
@Service
@Transactional
public class FormulaMedicaServiceImpl implements FormulaMedicaService {
    private final FormulaMedicaRepository formulaMedicaRepository;
    private final PacienteService pacienteService;
    private final CitaRepository citaRepository;
    private final HistoriaMedicaRepository historiaRepository;
    private final VeterinarioRepository veterinarioRepository;

    /** Inyecta los repositorios/servicios requeridos para validar la mascota. */
    public FormulaMedicaServiceImpl(FormulaMedicaRepository repository, PacienteService pacienteService, CitaRepository citaRepository, HistoriaMedicaRepository historiaRepository, VeterinarioRepository veterinarioRepository) {
        this.formulaMedicaRepository = repository; this.pacienteService = pacienteService; this.citaRepository = citaRepository; this.historiaRepository = historiaRepository; this.veterinarioRepository = veterinarioRepository;
    }

    /** Lista fórmulas en orden descendente por fecha. */
    @Override
    public List<FormulaMedicaDTO> listarFormulasMedicas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionDesc().stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    /** Devuelve el detalle completo o un 404 si no existe. */
    @Override
    public FormulaMedicaDTO obtenerPorId(Long id) {
        return convertirADTO(buscar(id));
    }

    /** Crea una fórmula validando que la mascota exista. */
    @Override
    public FormulaMedicaDTO crear(FormulaMedicaDTO dto) {
        Paciente paciente = pacienteService.buscarPacientePorId(dto.getPacienteId());
        validarTratamiento(dto);
        FormulaMedica formula = new FormulaMedica();
        aplicarDatos(formula, dto, paciente);
        formula.setFechaCreacion(dto.getFechaEmision() != null ? dto.getFechaEmision() : (dto.getFechaCreacion() == null ? LocalDateTime.now() : dto.getFechaCreacion()));
        asociarDatosClinicos(formula, dto, paciente);
        return convertirADTO(formulaMedicaRepository.save(formula));
    }

    /** Actualiza una fórmula existente sin permitir referencias a mascotas inexistentes. */
    @Override
    public FormulaMedicaDTO actualizar(Long id, FormulaMedicaDTO dto) {
        FormulaMedica formula = buscar(id);
        validarTratamiento(dto);
        Paciente paciente = dto.getPacienteId() == null ? formula.getPaciente() : pacienteService.buscarPacientePorId(dto.getPacienteId());
        aplicarDatos(formula, dto, paciente);
        if (dto.getFechaEmision() != null) formula.setFechaCreacion(dto.getFechaEmision());
        asociarDatosClinicos(formula, dto, paciente);
        return convertirADTO(formulaMedicaRepository.save(formula));
    }

    /** Valida y asocia cita, historia y veterinario evitando referencias cruzadas. */
    private void asociarDatosClinicos(FormulaMedica formula, FormulaMedicaDTO dto, Paciente paciente) {
        if (dto.getCitaId() == null || dto.getHistoriaMedicaId() == null || dto.getVeterinarioId() == null)
            throw new IllegalArgumentException("La fórmula debe asociarse a una cita, una historia clínica y un veterinario.");
        Cita cita = citaRepository.findById(dto.getCitaId()).orElseThrow(() -> new RecursoNoEncontradoException("No existe la cita indicada."));
        HistoriaMedica historia = historiaRepository.findById(dto.getHistoriaMedicaId()).orElseThrow(() -> new RecursoNoEncontradoException("No existe la historia clínica indicada."));
        Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioId()).orElseThrow(() -> new RecursoNoEncontradoException("No existe el veterinario indicado."));
        if (paciente.getCliente() == null) throw new IllegalArgumentException("La mascota debe pertenecer a un cliente registrado.");
        if (!veterinario.isActivo()) throw new IllegalArgumentException("El veterinario seleccionado no está activo.");
        if (cita.getPaciente() == null || !cita.getPaciente().getId().equals(paciente.getId())) throw new IllegalArgumentException("La cita pertenece a otra mascota.");
        if (historia.getPaciente() == null || !historia.getPaciente().getId().equals(paciente.getId())) throw new IllegalArgumentException("La historia clínica pertenece a otra mascota.");
        if (historia.getCita() != null && !historia.getCita().getId().equals(cita.getId())) throw new IllegalArgumentException("La historia clínica está asociada a otra cita.");
        if (cita.getVeterinario() != null && !cita.getVeterinario().getId().equals(veterinario.getId())) throw new IllegalArgumentException("El veterinario no coincide con el asignado a la cita.");
        if (formula.getFechaCreacion() != null && cita.getFecha() != null && formula.getFechaCreacion().isBefore(cita.getFecha())) throw new IllegalArgumentException("La fecha de emisión no puede ser anterior a la cita.");
        formula.setCita(cita); formula.setHistoriaMedica(historia); formula.setVeterinario(veterinario);
    }

    /** Rechaza datos vacíos o cantidades/duración no positivas también desde el backend. */
    private void validarTratamiento(FormulaMedicaDTO dto) {
        if (dto.getMedicamento() == null || dto.getMedicamento().isBlank()) throw new IllegalArgumentException("El medicamento es obligatorio");
        if (dto.getDosis() == null || dto.getDosis().isBlank()) throw new IllegalArgumentException("La dosis es obligatoria");
        Integer duracion = dto.getDuracionDias() != null ? dto.getDuracionDias() : dto.getCantidad();
        if (duracion == null || duracion <= 0) throw new IllegalArgumentException("La duración del tratamiento debe ser mayor que cero");
        if (dto.getFrecuencia() == null || dto.getFrecuencia().isBlank()) throw new IllegalArgumentException("La frecuencia del tratamiento es obligatoria");
    }

    /** Copia los campos editables del DTO a la entidad. */
    private void aplicarDatos(FormulaMedica formula, FormulaMedicaDTO dto, Paciente paciente) {
        formula.setPaciente(paciente);
        formula.setMedicamento(dto.getMedicamento().trim());
        formula.setDosis(dto.getDosis().trim());
        formula.setIndicaciones(dto.getIndicaciones());
        formula.setCantidad(dto.getDuracionDias() != null ? dto.getDuracionDias() : dto.getCantidad());
        formula.setFrecuencia(dto.getFrecuencia());
    }

    /** Busca la entidad o genera un error controlado. */
    private FormulaMedica buscar(Long id) {
        return formulaMedicaRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la fórmula médica con id " + id));
    }

    /** Convierte la entidad en DTO para no exponer la entidad JPA directamente. */
    private FormulaMedicaDTO convertirADTO(FormulaMedica formula) {
        FormulaMedicaDTO dto = new FormulaMedicaDTO();
        dto.setId(formula.getId()); dto.setFechaCreacion(formula.getFechaCreacion());
        dto.setMedicamento(formula.getMedicamento()); dto.setDosis(formula.getDosis());
        dto.setIndicaciones(formula.getIndicaciones()); dto.setCantidad(formula.getCantidad());
        if (formula.getPaciente() != null) { dto.setPacienteId(formula.getPaciente().getId()); dto.setNombrePaciente(formula.getPaciente().getNombre()); }
        dto.setCitaId(formula.getCita() == null ? null : formula.getCita().getId());
        dto.setHistoriaMedicaId(formula.getHistoriaMedica() == null ? null : formula.getHistoriaMedica().getId());
        dto.setVeterinarioId(formula.getVeterinario() == null ? null : formula.getVeterinario().getId());
        dto.setNombreVeterinario(formula.getVeterinario() == null ? null : formula.getVeterinario().getNombre());
        dto.setFechaEmision(formula.getFechaCreacion()); dto.setFrecuencia(formula.getFrecuencia()); dto.setDuracionDias(formula.getCantidad());
        return dto;
    }
}
