package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.FormulaMedica;
import com.clinicaveterinaria.clinica.models.FormulaMedicaDTO;
import com.clinicaveterinaria.clinica.repository.CitaRepository;
import com.clinicaveterinaria.clinica.repository.FormulaMedicaRepository;
import com.clinicaveterinaria.clinica.repository.MedicamentoRepository;
import com.clinicaveterinaria.clinica.service.FormulaMedicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    private final FormulaMedicaRepository formulaMedicaRepository;
    private final CitaRepository citaRepository;                 // ← Para validar
    private final MedicamentoRepository medicamentoRepository;   // ← Para validar

    private FormulaMedica toEntity(FormulaMedicaDTO dto) {
        FormulaMedica entity = new FormulaMedica();
        entity.setCitaId(dto.getCitaId());
        entity.setMedicamentoId(dto.getMedicamentoId());
        entity.setDosis(dto.getDosis());
        entity.setIndicaciones(dto.getIndicaciones());
        entity.setFechaCreacionRegistro(LocalDateTime.now());
        return entity;
    }

    private FormulaMedicaDTO toDTO(FormulaMedica entity) {
        return new FormulaMedicaDTO(
                entity.getCitaId(),
                entity.getMedicamentoId(),
                entity.getDosis(),
                entity.getIndicaciones(),
                entity.getFechaCreacionRegistro(),
                entity.getFechaActualizacionRegistro()
        );
    }

    @Override
    public FormulaMedicaDTO crear(FormulaMedicaDTO dto) {
        // Validaciones básicas
        if (dto.getDosis() == null || dto.getDosis().isEmpty()) {
            throw new RuntimeException("La dosis es obligatoria.");
        }
        if (dto.getCitaId() == null) {
            throw new RuntimeException("La cita es obligatoria.");
        }
        if (dto.getMedicamentoId() == null) {
            throw new RuntimeException("El medicamento es obligatorio.");
        }

        // VALIDAR RELACIONES
        if (!citaRepository.existsById(dto.getCitaId().longValue())) {
            throw new RuntimeException("Cita no encontrada con ID: " + dto.getCitaId());
        }
        if (!medicamentoRepository.existsById(dto.getMedicamentoId().longValue())) {
            throw new RuntimeException("Medicamento no encontrado con ID: " + dto.getMedicamentoId());
        }

        FormulaMedica entity = toEntity(dto);
        FormulaMedica guardada = formulaMedicaRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<FormulaMedicaDTO> listarTodos() {
        return formulaMedicaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FormulaMedicaDTO> OrdenarListaFecha(){
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
    } 
    
    
    
    

    @Override
    public FormulaMedicaDTO buscarPorId(Long id) {
        FormulaMedica entity = formulaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fórmula médica no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public List<FormulaMedicaDTO> buscarPorCita(Integer citaId) {
        return formulaMedicaRepository.findByCitaId(citaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FormulaMedicaDTO> buscarPorMedicamento(Integer medicamentoId) {
        return formulaMedicaRepository.findByMedicamentoId(medicamentoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FormulaMedicaDTO actualizar(Long id, FormulaMedicaDTO dto) {
        FormulaMedica entity = formulaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fórmula médica no encontrada con ID: " + id));

        if (dto.getCitaId() != null) {
            if (!citaRepository.existsById(dto.getCitaId().longValue())) {
                throw new RuntimeException("Cita no encontrada con ID: " + dto.getCitaId());
            }
            entity.setCitaId(dto.getCitaId());
        }
        if (dto.getMedicamentoId() != null) {
            if (!medicamentoRepository.existsById(dto.getMedicamentoId().longValue())) {
                throw new RuntimeException("Medicamento no encontrado con ID: " + dto.getMedicamentoId());
            }
            entity.setMedicamentoId(dto.getMedicamentoId());
        }

        entity.setDosis(dto.getDosis());
        entity.setIndicaciones(dto.getIndicaciones());
        entity.setFechaActualizacionRegistro(LocalDateTime.now());

        FormulaMedica actualizada = formulaMedicaRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!formulaMedicaRepository.existsById(id)) {
            throw new RuntimeException("Fórmula médica no encontrada con ID: " + id);
        }
        formulaMedicaRepository.deleteById(id);
    }
}