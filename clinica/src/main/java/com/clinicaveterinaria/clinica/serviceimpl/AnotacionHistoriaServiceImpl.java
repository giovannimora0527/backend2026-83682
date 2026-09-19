package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.AnotacionHistoria;
import com.clinicaveterinaria.clinica.models.AnotacionHistoriaDTO;
import com.clinicaveterinaria.clinica.repository.AnotacionHistoriaRepository;
import com.clinicaveterinaria.clinica.repository.HistoriaMedicaRepository;
import com.clinicaveterinaria.clinica.repository.MedicoRepository;
import com.clinicaveterinaria.clinica.service.AnotacionHistoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    private final AnotacionHistoriaRepository anotacionHistoriaRepository;
    private final HistoriaMedicaRepository historiaMedicaRepository;  // ← Para validar
    private final MedicoRepository medicoRepository;                  // ← Para validar

    private AnotacionHistoria toEntity(AnotacionHistoriaDTO dto) {
        AnotacionHistoria entity = new AnotacionHistoria();
        entity.setHistoriaId(dto.getHistoriaId());
        entity.setMedicoId(dto.getMedicoId());
        entity.setFecha(LocalDateTime.now());
        entity.setDescripcion(dto.getDescripcion());
        return entity;
    }

    private AnotacionHistoriaDTO toDTO(AnotacionHistoria entity) {
        return new AnotacionHistoriaDTO(
                entity.getHistoriaId(),
                entity.getMedicoId(),
                entity.getFecha(),
                entity.getDescripcion()
        );
    }

    @Override
    public AnotacionHistoriaDTO crear(AnotacionHistoriaDTO dto) {
        // Validaciones básicas
        if (dto.getDescripcion() == null || dto.getDescripcion().isEmpty()) {
            throw new RuntimeException("La descripción es obligatoria.");
        }
        if (dto.getHistoriaId() == null) {
            throw new RuntimeException("La historia médica es obligatoria.");
        }
        if (dto.getMedicoId() == null) {
            throw new RuntimeException("El médico es obligatorio.");
        }

        // VALIDAR RELACIONES
        if (!historiaMedicaRepository.existsById(dto.getHistoriaId().longValue())) {
            throw new RuntimeException("Historia médica no encontrada con ID: " + dto.getHistoriaId());
        }
        if (!medicoRepository.existsById(dto.getMedicoId().longValue())) {
            throw new RuntimeException("Médico no encontrado con ID: " + dto.getMedicoId());
        }

        AnotacionHistoria entity = toEntity(dto);
        AnotacionHistoria guardada = anotacionHistoriaRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<AnotacionHistoriaDTO> listarTodos() {
        return anotacionHistoriaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AnotacionHistoriaDTO buscarPorId(Long id) {
        AnotacionHistoria entity = anotacionHistoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anotación no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public List<AnotacionHistoriaDTO> buscarPorHistoria(Integer historiaId) {
        return anotacionHistoriaRepository.findByHistoriaId(historiaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnotacionHistoriaDTO> buscarPorMedico(Integer medicoId) {
        return anotacionHistoriaRepository.findByMedicoId(medicoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AnotacionHistoriaDTO actualizar(Long id, AnotacionHistoriaDTO dto) {
        AnotacionHistoria entity = anotacionHistoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anotación no encontrada con ID: " + id));

        if (dto.getHistoriaId() != null) {
            if (!historiaMedicaRepository.existsById(dto.getHistoriaId().longValue())) {
                throw new RuntimeException("Historia médica no encontrada con ID: " + dto.getHistoriaId());
            }
            entity.setHistoriaId(dto.getHistoriaId());
        }
        if (dto.getMedicoId() != null) {
            if (!medicoRepository.existsById(dto.getMedicoId().longValue())) {
                throw new RuntimeException("Médico no encontrado con ID: " + dto.getMedicoId());
            }
            entity.setMedicoId(dto.getMedicoId());
        }

        entity.setDescripcion(dto.getDescripcion());

        AnotacionHistoria actualizada = anotacionHistoriaRepository.save(entity);
        return toDTO(actualizada);
    }

   /* @Override
    public void eliminar(Long id) {
        if (!anotacionHistoriaRepository.existsById(id)) {
            throw new RuntimeException("Anotación no encontrada con ID: " + id);
        }
        anotacionHistoriaRepository.deleteById(id);
    }*/
}