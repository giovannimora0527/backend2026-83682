package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Medico;
import com.clinicaveterinaria.clinica.models.MedicoDTO;
import com.clinicaveterinaria.clinica.repository.EspecializacionRepository;
import com.clinicaveterinaria.clinica.repository.MedicoRepository;
import com.clinicaveterinaria.clinica.service.MedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecializacionRepository especializacionRepository;  // ← ¡NUEVO!

    private Medico toEntity(MedicoDTO dto) {
        Medico entity = new Medico();
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setNombres(dto.getNombres());
        entity.setApellidos(dto.getApellidos());
        entity.setTelefono(dto.getTelefono());
        entity.setRegistroProfesional(dto.getRegistroProfesional());
        entity.setEspecializacionId(dto.getEspecializacionId());
        return entity;
    }

    private MedicoDTO toDTO(Medico entity) {
        return new MedicoDTO(
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getTelefono(),
                entity.getRegistroProfesional(),
                entity.getEspecializacionId()
        );
    }

    @Override
    public MedicoDTO crear(MedicoDTO dto) {
        // Validaciones básicas
        if (dto.getNombres() == null || dto.getNombres().isEmpty()) {
            throw new RuntimeException("El nombre del médico es obligatorio.");
        }
        if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isEmpty()) {
            throw new RuntimeException("El número de documento es obligatorio.");
        }
        if (dto.getRegistroProfesional() == null || dto.getRegistroProfesional().isEmpty()) {
            throw new RuntimeException("El registro profesional es obligatorio.");
        }

        // ✅ VALIDACIÓN DE RELACIÓN: La especialización debe existir
        if (dto.getEspecializacionId() == null) {
            throw new RuntimeException("La especialización es obligatoria.");
        }
        if (!especializacionRepository.existsById(dto.getEspecializacionId().longValue())) {
            throw new RuntimeException("Especialización no encontrada con ID: " + dto.getEspecializacionId());
        }

        // Validaciones de unicidad
        if (medicoRepository.findByNumeroDocumento(dto.getNumeroDocumento()).isPresent()) {
            throw new RuntimeException("Ya existe un médico con el documento: " + dto.getNumeroDocumento());
        }
        if (medicoRepository.findByRegistroProfesional(dto.getRegistroProfesional()).isPresent()) {
            throw new RuntimeException("Ya existe un médico con el registro: " + dto.getRegistroProfesional());
        }

        Medico entity = toEntity(dto);
        Medico guardado = medicoRepository.save(entity);
        return toDTO(guardado);
    }

    @Override
    public List<MedicoDTO> listarTodos() {
        return medicoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MedicoDTO buscarPorId(Long id) {
        Medico entity = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public MedicoDTO buscarPorDocumento(String numeroDocumento) {
        Medico entity = medicoRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con documento: " + numeroDocumento));
        return toDTO(entity);
    }

    @Override
    public MedicoDTO buscarPorRegistro(String registroProfesional) {
        Medico entity = medicoRepository.findByRegistroProfesional(registroProfesional)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con registro: " + registroProfesional));
        return toDTO(entity);
    }

    @Override
    public MedicoDTO actualizar(Long id, MedicoDTO dto) {
        Medico entity = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id));

        // Validar que la especialización exista
        if (dto.getEspecializacionId() != null) {
            if (!especializacionRepository.existsById(dto.getEspecializacionId().longValue())) {
                throw new RuntimeException("Especialización no encontrada con ID: " + dto.getEspecializacionId());
            }
            entity.setEspecializacionId(dto.getEspecializacionId());
        }

        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setNombres(dto.getNombres());
        entity.setApellidos(dto.getApellidos());
        entity.setTelefono(dto.getTelefono());
        entity.setRegistroProfesional(dto.getRegistroProfesional());

        Medico actualizado = medicoRepository.save(entity);
        return toDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new RuntimeException("Médico no encontrado con ID: " + id);
        }
        medicoRepository.deleteById(id);
    }
}