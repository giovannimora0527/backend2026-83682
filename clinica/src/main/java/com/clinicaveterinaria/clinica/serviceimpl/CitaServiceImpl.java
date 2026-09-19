package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Cita;
import com.clinicaveterinaria.clinica.models.CitaDTO;
import com.clinicaveterinaria.clinica.repository.ClienteRepository;
import com.clinicaveterinaria.clinica.repository.CitaRepository;
import com.clinicaveterinaria.clinica.repository.MascotaRepository;
import com.clinicaveterinaria.clinica.repository.MedicoRepository;
import com.clinicaveterinaria.clinica.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final MedicoRepository medicoRepository;

    private Cita toEntity(CitaDTO dto) {
        Cita entity = new Cita();
        entity.setClienteId(dto.getClienteId());
        entity.setMascotaId(dto.getMascotaId());
        entity.setMedicoId(dto.getMedicoId());
        entity.setFechaHora(dto.getFechaHora());
        entity.setEstado(dto.getEstado());
        entity.setMotivo(dto.getMotivo());
        return entity;
    }

    private CitaDTO toDTO(Cita entity) {
        return new CitaDTO(
                entity.getClienteId(),
                entity.getMascotaId(),
                entity.getMedicoId(),
                entity.getFechaHora(),
                entity.getEstado(),
                entity.getMotivo()
        );
    }

    @Override
    public CitaDTO crear(CitaDTO dto) {
        // Validaciones básicas
        if (dto.getFechaHora() == null) {
            throw new RuntimeException("La fecha y hora son obligatorias.");
        }
        if (dto.getEstado() == null || dto.getEstado().isEmpty()) {
            throw new RuntimeException("El estado es obligatorio.");
        }
        if (dto.getClienteId() == null) {
            throw new RuntimeException("El cliente es obligatorio.");
        }
        if (dto.getMascotaId() == null) {
            throw new RuntimeException("La mascota es obligatoria.");
        }
        if (dto.getMedicoId() == null) {
            throw new RuntimeException("El médico es obligatorio.");
        }

        // VALIDAR RELACIONES
        if (!clienteRepository.existsById(dto.getClienteId().longValue())) {
            throw new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId());
        }
        if (!mascotaRepository.existsById(dto.getMascotaId().longValue())) {
            throw new RuntimeException("Mascota no encontrada con ID: " + dto.getMascotaId());
        }
        if (!medicoRepository.existsById(dto.getMedicoId().longValue())) {
            throw new RuntimeException("Médico no encontrado con ID: " + dto.getMedicoId());
        }

        Cita entity = toEntity(dto);
        Cita guardada = citaRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<CitaDTO> listarTodos() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CitaDTO buscarPorId(Long id) {
        Cita entity = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public List<CitaDTO> buscarPorCliente(Integer clienteId) {
        return citaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> buscarPorMascota(Integer mascotaId) {
        return citaRepository.findByMascotaId(mascotaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> buscarPorMedico(Integer medicoId) {
        return citaRepository.findByMedicoId(medicoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> buscarPorEstado(String estado) {
        return citaRepository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaDTO> buscarPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        return citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial,fechaFinal)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CitaDTO actualizar(Long id, CitaDTO dto) {
        Cita entity = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        // Validar relaciones si se cambian
        if (dto.getClienteId() != null) {
            if (!clienteRepository.existsById(dto.getClienteId().longValue())) {
                throw new RuntimeException("Cliente no encontrado con ID: " + dto.getClienteId());
            }
            entity.setClienteId(dto.getClienteId());
        }
        if (dto.getMascotaId() != null) {
            if (!mascotaRepository.existsById(dto.getMascotaId().longValue())) {
                throw new RuntimeException("Mascota no encontrada con ID: " + dto.getMascotaId());
            }
            entity.setMascotaId(dto.getMascotaId());
        }
        if (dto.getMedicoId() != null) {
            if (!medicoRepository.existsById(dto.getMedicoId().longValue())) {
                throw new RuntimeException("Médico no encontrado con ID: " + dto.getMedicoId());
            }
            entity.setMedicoId(dto.getMedicoId());
        }

        entity.setFechaHora(dto.getFechaHora());
        entity.setEstado(dto.getEstado());
        entity.setMotivo(dto.getMotivo());

        Cita actualizada = citaRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new RuntimeException("Cita no encontrada con ID: " + id);
        }
        citaRepository.deleteById(id);
    }
}