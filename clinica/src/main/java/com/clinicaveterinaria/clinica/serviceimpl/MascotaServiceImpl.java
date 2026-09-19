package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Mascota;
import com.clinicaveterinaria.clinica.models.MascotaDTO;
import com.clinicaveterinaria.clinica.repository.ClienteRepository;
import com.clinicaveterinaria.clinica.repository.MascotaRepository;
import com.clinicaveterinaria.clinica.repository.RazaRepository;
import com.clinicaveterinaria.clinica.service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;   // ← Para validar
    private final RazaRepository razaRepository;         // ← Para validar

    private Mascota toEntity(MascotaDTO dto) {
        Mascota entity = new Mascota();
        entity.setNombreMascota(dto.getNombreMascota());
        entity.setEdad(dto.getEdad());
        entity.setRazaId(dto.getRazaId());
        entity.setClienteId(dto.getClienteId());
        entity.setFechaRegistro(LocalDateTime.now());
        return entity;
    }

    private MascotaDTO toDTO(Mascota entity) {
        return new MascotaDTO(
                entity.getNombreMascota(),
                entity.getEdad(),
                entity.getRazaId(),
                entity.getFechaRegistro(),
                entity.getFechaModificacion(),
                entity.getClienteId()
        );
    }

    @Override
    public MascotaDTO crear(MascotaDTO dto) {
        // Validaciones básicas
        if (dto.getNombreMascota() == null || dto.getNombreMascota().isEmpty()) {
            throw new RuntimeException("El nombre de la mascota es obligatorio.");
        }
        if (dto.getEdad() == null || dto.getEdad() < 0) {
            throw new RuntimeException("La edad debe ser un número positivo.");
        }
        if (dto.getClienteId() == null) {
            throw new RuntimeException("El cliente es obligatorio.");
        }
        if (dto.getRazaId() == null) {
            throw new RuntimeException("La raza es obligatoria.");
        }

        // ✅ VALIDAR QUE EL CLIENTE EXISTA
        if (!clienteRepository.existsById(dto.getClienteId().longValue())) {
            throw new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId());
        }

        // ✅ VALIDAR QUE LA RAZA EXISTA
        if (!razaRepository.existsById(dto.getRazaId().longValue())) {
            throw new RuntimeException("Raza no encontrada con ID: " + dto.getRazaId());
        }

        Mascota entity = toEntity(dto);
        Mascota guardada = mascotaRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<MascotaDTO> listarTodos() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MascotaDTO buscarPorId(Long id) {
        Mascota entity = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public List<MascotaDTO> buscarPorCliente(Integer clienteId) {
        return mascotaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MascotaDTO> buscarPorRaza(Integer razaId) {
        return mascotaRepository.findByRazaId(razaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MascotaDTO actualizar(Long id, MascotaDTO dto) {
        Mascota entity = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con ID: " + id));

        // Validar relaciones si se cambian
        if (dto.getClienteId() != null) {
            if (!clienteRepository.existsById(dto.getClienteId().longValue())) {
                throw new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId());
            }
            entity.setClienteId(dto.getClienteId());
        }
        if (dto.getRazaId() != null) {
            if (!razaRepository.existsById(dto.getRazaId().longValue())) {
                throw new RuntimeException("Raza no encontrada con ID: " + dto.getRazaId());
            }
            entity.setRazaId(dto.getRazaId());
        }

        entity.setNombreMascota(dto.getNombreMascota());
        entity.setEdad(dto.getEdad());
        entity.setFechaModificacion(LocalDateTime.now());

        Mascota actualizada = mascotaRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!mascotaRepository.existsById(id)) {
            throw new RuntimeException("Mascota no encontrada con ID: " + id);
        }
        mascotaRepository.deleteById(id);
    }
}