package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Especializacion;
import com.clinicaveterinaria.clinica.models.EspecializacionDTO;
import com.clinicaveterinaria.clinica.repository.EspecializacionRepository;
import com.clinicaveterinaria.clinica.service.EspecializacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspecializacionServiceImpl implements EspecializacionService {

    private final EspecializacionRepository especializacionRepository;

    // DTO → Entity
    private Especializacion toEntity(EspecializacionDTO dto) {
        Especializacion entity = new Especializacion();
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCodigoEspecializacion(dto.getCodigoEspecializacion());
        return entity;
    }

    // Entity → DTO
    private EspecializacionDTO toDTO(Especializacion entity) {
        return new EspecializacionDTO(
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getCodigoEspecializacion()
        );
    }

    @Override
    public EspecializacionDTO crear(EspecializacionDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isEmpty()) {
            throw new RuntimeException("El nombre de la especialización es obligatorio.");
        }
        if (dto.getCodigoEspecializacion() == null || dto.getCodigoEspecializacion().isEmpty()) {
            throw new RuntimeException("El código de la especialización es obligatorio.");
        }
        if (especializacionRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe una especialización con el nombre: " + dto.getNombre());
        }
        if (especializacionRepository.findByCodigoEspecializacion(dto.getCodigoEspecializacion()).isPresent()) {
            throw new RuntimeException("Ya existe una especialización con el código: " + dto.getCodigoEspecializacion());
        }

        Especializacion entity = toEntity(dto);
        Especializacion guardada = especializacionRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<EspecializacionDTO> listarTodos() {
        return especializacionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EspecializacionDTO buscarPorId(Long id) {
        Especializacion entity = especializacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialización no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public EspecializacionDTO buscarPorNombre(String nombre) {
        Especializacion entity = especializacionRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Especialización no encontrada con nombre: " + nombre));
        return toDTO(entity);
    }

    @Override
    public EspecializacionDTO buscarPorCodigo(String codigo) {
        Especializacion entity = especializacionRepository.findByCodigoEspecializacion(codigo)
                .orElseThrow(() -> new RuntimeException("Especialización no encontrada con código: " + codigo));
        return toDTO(entity);
    }

    @Override
    public EspecializacionDTO actualizar(Long id, EspecializacionDTO dto) {
        Especializacion entity = especializacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialización no encontrada con ID: " + id));

        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCodigoEspecializacion(dto.getCodigoEspecializacion());

        Especializacion actualizada = especializacionRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!especializacionRepository.existsById(id)) {
            throw new RuntimeException("Especialización no encontrada con ID: " + id);
        }
        especializacionRepository.deleteById(id);
    }
}