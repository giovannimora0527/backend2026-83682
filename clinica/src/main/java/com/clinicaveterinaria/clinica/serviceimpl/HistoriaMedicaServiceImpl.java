package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.HistoriaMedica;
import com.clinicaveterinaria.clinica.models.HistoriaMedicaDTO;
import com.clinicaveterinaria.clinica.repository.ClienteRepository;
import com.clinicaveterinaria.clinica.repository.HistoriaMedicaRepository;
import com.clinicaveterinaria.clinica.service.HistoriaMedicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    private final HistoriaMedicaRepository historiaMedicaRepository;
    private final ClienteRepository clienteRepository;  // Para validar que el paciente existe

    private HistoriaMedica toEntity(HistoriaMedicaDTO dto) {
        HistoriaMedica entity = new HistoriaMedica();
        entity.setPacienteId(dto.getPacienteId());
        entity.setFechaCreacion(dto.getFechaCreacion() != null ? dto.getFechaCreacion() : LocalDateTime.now());
        return entity;
    }

    private HistoriaMedicaDTO toDTO(HistoriaMedica entity) {
        return new HistoriaMedicaDTO(
                entity.getPacienteId(),
                entity.getFechaCreacion()
        );
    }

    @Override
    public HistoriaMedicaDTO crear(HistoriaMedicaDTO dto) {
        if (dto.getPacienteId() == null) {
            throw new RuntimeException("El paciente es obligatorio.");
        }

        // VALIDAR RELACIÓN: El paciente (cliente) debe existir
        if (!clienteRepository.existsById(dto.getPacienteId().longValue())) {
            throw new RuntimeException("Paciente no encontrado con ID: " + dto.getPacienteId());
        }

        HistoriaMedica entity = toEntity(dto);
        HistoriaMedica guardada = historiaMedicaRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<HistoriaMedicaDTO> listarTodos() {
        return historiaMedicaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HistoriaMedicaDTO buscarPorId(Long id) {
        HistoriaMedica entity = historiaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historia médica no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public List<HistoriaMedicaDTO> buscarPorPaciente(Integer pacienteId) {
        return historiaMedicaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistoriaMedicaDTO> buscarPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        return historiaMedicaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicial, fechaFinal)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HistoriaMedicaDTO actualizar(Long id, HistoriaMedicaDTO dto) {
        HistoriaMedica entity = historiaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historia médica no encontrada con ID: " + id));

        if (dto.getPacienteId() != null) {
            if (!clienteRepository.existsById(dto.getPacienteId().longValue())) {
                throw new RuntimeException("Paciente no encontrado con ID: " + dto.getPacienteId());
            }
            entity.setPacienteId(dto.getPacienteId());
        }

        entity.setFechaCreacion(dto.getFechaCreacion());

        HistoriaMedica actualizada = historiaMedicaRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!historiaMedicaRepository.existsById(id)) {
            throw new RuntimeException("Historia médica no encontrada con ID: " + id);
        }
        historiaMedicaRepository.deleteById(id);
    }
}