package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Usuario;
import com.clinicaveterinaria.clinica.models.UsuarioDTO;
import com.clinicaveterinaria.clinica.repository.UsuarioRepository;
import com.clinicaveterinaria.clinica.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private Usuario toEntity(UsuarioDTO dto) {
        Usuario entity = new Usuario();
        entity.setUsername(dto.getUsername());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setRol(dto.getRol());
        entity.setFechaCreacion(LocalDateTime.now());
        entity.setActivo(true);
        entity.setEmail(dto.getEmail());
        return entity;
    }

    private UsuarioDTO toDTO(Usuario entity) {
        return new UsuarioDTO(
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getRol(),
                entity.getFechaCreacion(),
                entity.getActivo(),
                entity.getEmail()
        );
    }

    @Override
    public UsuarioDTO crear(UsuarioDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new RuntimeException("El username es obligatorio.");
        }
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new RuntimeException("El email es obligatorio.");
        }
        if (dto.getRol() == null || dto.getRol().isEmpty()) {
            throw new RuntimeException("El rol es obligatorio.");
        }
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el username: " + dto.getUsername());
        }
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el email: " + dto.getEmail());
        }

        Usuario entity = toEntity(dto);
        Usuario guardado = usuarioRepository.save(entity);
        return toDTO(guardado);
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO buscarPorId(Long id) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public UsuarioDTO buscarPorUsername(String username) {
        Usuario entity = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));
        return toDTO(entity);
    }

    @Override
    public UsuarioDTO buscarPorEmail(String email) {
        Usuario entity = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        return toDTO(entity);
    }

    @Override
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        entity.setUsername(dto.getUsername());
        entity.setPasswordHash(dto.getPasswordHash());
        entity.setRol(dto.getRol());
        entity.setEmail(dto.getEmail());
        entity.setActivo(dto.getActivo());

        Usuario actualizado = usuarioRepository.save(entity);
        return toDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}