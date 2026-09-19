package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Raza;
import com.clinicaveterinaria.clinica.models.RazaDTO;
import com.clinicaveterinaria.clinica.repository.RazaRepository;
import com.clinicaveterinaria.clinica.service.RazaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RazaServiceImpl implements RazaService {

    private final RazaRepository razaRepository;

    // Conversión DTO → Entity
    private Raza toEntity(RazaDTO dto) {
        Raza entity = new Raza();
        entity.setNombre(dto.getNombre());
        entity.setEspecie(dto.getEspecie());
        entity.setFechaCreacion(LocalDateTime.now());
        return entity;
    }

    // Conversión Entity → DTO
    private RazaDTO toDTO(Raza entity) {
        return new RazaDTO(
                entity.getNombre(),
                entity.getEspecie(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }

    @Override
    public RazaDTO crear(RazaDTO dto) {
        // Validaciones
        if (dto.getNombre() == null || dto.getNombre().isEmpty()) {
            throw new RuntimeException("El nombre de la raza es obligatorio.");
        }
        if (dto.getEspecie() == null || dto.getEspecie().isEmpty()) {
            throw new RuntimeException("La especie es obligatoria.");
        }
        if (razaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe una raza con el nombre: " + dto.getNombre());
        }

        Raza entity = toEntity(dto);
        Raza guardada = razaRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<RazaDTO> listarTodos() {
        return razaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RazaDTO buscarPorId(Long id) {
        Raza entity = razaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raza no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public RazaDTO buscarPorNombre(String nombre) {
        Raza entity = razaRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Raza no encontrada con nombre: " + nombre));
        return toDTO(entity);
    }

    @Override
    public RazaDTO actualizar(Long id, RazaDTO dto) {
        Raza entity = razaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raza no encontrada con ID: " + id));

        entity.setNombre(dto.getNombre());
        entity.setEspecie(dto.getEspecie());
        entity.setFechaModificacion(LocalDateTime.now());

        Raza actualizada = razaRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!razaRepository.existsById(id)) {
            throw new RuntimeException("Raza no encontrada con ID: " + id);
        }
        razaRepository.deleteById(id);
    }
}