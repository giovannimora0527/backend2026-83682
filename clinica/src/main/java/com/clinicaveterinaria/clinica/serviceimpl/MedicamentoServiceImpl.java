package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Medicamento;
import com.clinicaveterinaria.clinica.models.MedicamentoDTO;
import com.clinicaveterinaria.clinica.repository.MedicamentoRepository;
import com.clinicaveterinaria.clinica.service.MedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    private Medicamento toEntity(MedicamentoDTO dto) {
        Medicamento entity = new Medicamento();
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPresentacion(dto.getPresentacion());
        entity.setFechaCompra(dto.getFechaCompra());
        entity.setFechaVence(dto.getFechaVence());
        entity.setFechaCreacionRegistro(LocalDateTime.now());
        return entity;
    }

    private MedicamentoDTO toDTO(Medicamento entity) {
        return new MedicamentoDTO(
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPresentacion(),
                entity.getFechaCompra(),
                entity.getFechaVence(),
                entity.getFechaCreacionRegistro(),
                entity.getFechaModificacionRegistro()
        );
    }

    @Override
    public MedicamentoDTO crear(MedicamentoDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isEmpty()) {
            throw new RuntimeException("El nombre del medicamento es obligatorio.");
        }
        if (dto.getFechaCompra() == null) {
            throw new RuntimeException("La fecha de compra es obligatoria.");
        }
        if (dto.getFechaVence() == null) {
            throw new RuntimeException("La fecha de vencimiento es obligatoria.");
        }
        if (medicamentoRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un medicamento con el nombre: " + dto.getNombre());
        }

        Medicamento entity = toEntity(dto);
        Medicamento guardado = medicamentoRepository.save(entity);
        return toDTO(guardado);
    }

    @Override
    public List<MedicamentoDTO> listarTodos() {
        return medicamentoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicamentoDTO> OrdenarListaFecha() {
        return medicamentoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    
    

    @Override
    public MedicamentoDTO buscarPorId(Long id) {
        Medicamento entity = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public MedicamentoDTO buscarPorNombre(String nombre) {
        Medicamento entity = medicamentoRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con nombre: " + nombre));
        return toDTO(entity);
    }

    @Override
    public MedicamentoDTO actualizar(Long id, MedicamentoDTO dto) {
        Medicamento entity = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con ID: " + id));

        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPresentacion(dto.getPresentacion());
        entity.setFechaCompra(dto.getFechaCompra());
        entity.setFechaVence(dto.getFechaVence());
        entity.setFechaModificacionRegistro(LocalDateTime.now());

        Medicamento actualizado = medicamentoRepository.save(entity);
        return toDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (!medicamentoRepository.existsById(id)) {
            throw new RuntimeException("Medicamento no encontrado con ID: " + id);
        }
        medicamentoRepository.deleteById(id);
    }
}