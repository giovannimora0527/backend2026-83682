package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Session;
import com.clinicaveterinaria.clinica.models.SessionDTO;
import com.clinicaveterinaria.clinica.repository.SessionRepository;
import com.clinicaveterinaria.clinica.repository.UsuarioRepository;
import com.clinicaveterinaria.clinica.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final UsuarioRepository usuarioRepository;  // ← Para validar relación

    private Session toEntity(SessionDTO dto) {
        Session entity = new Session();
        entity.setUserId(dto.getUserId());
        entity.setToken(dto.getToken());
        entity.setFechaIniSesion(dto.getFechaIniSesion());
        entity.setFechaExpiracion(dto.getFechaExpiracion());
        return entity;
    }

    private SessionDTO toDTO(Session entity) {
        return new SessionDTO(
                entity.getUserId(),
                entity.getToken(),
                entity.getFechaIniSesion(),
                entity.getFechaExpiracion()
        );
    }

    @Override
    public SessionDTO crear(SessionDTO dto) {
        // Validaciones básicas
        if (dto.getUserId() == null) {
            throw new RuntimeException("El usuario es obligatorio.");
        }
        if (dto.getToken() == null || dto.getToken().isEmpty()) {
            throw new RuntimeException("El token es obligatorio.");
        }

        // VALIDAR RELACIÓN: El usuario debe existir
        if (!usuarioRepository.existsById(dto.getUserId().longValue())) {
            throw new RuntimeException("Usuario no encontrado con ID: " + dto.getUserId());
        }

        // Asignar fecha de inicio si no viene
        if (dto.getFechaIniSesion() == null) {
            dto.setFechaIniSesion(LocalDateTime.now());
        }
        // Asignar fecha de expiración si no viene (1 día después)
        if (dto.getFechaExpiracion() == null) {
            dto.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        }

        Session entity = toEntity(dto);
        Session guardada = sessionRepository.save(entity);
        return toDTO(guardada);
    }

    @Override
    public List<SessionDTO> listarTodos() {
        return sessionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDTO buscarPorId(Long id) {
        Session entity = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session no encontrada con ID: " + id));
        return toDTO(entity);
    }

    @Override
    public SessionDTO buscarPorToken(String token) {
        Session entity = sessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session no encontrada con token: " + token));
        return toDTO(entity);
    }

    @Override
    public List<SessionDTO> buscarPorUsuario(Integer userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SessionDTO actualizar(Long id, SessionDTO dto) {
        Session entity = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session no encontrada con ID: " + id));

        if (dto.getUserId() != null) {
            if (!usuarioRepository.existsById(dto.getUserId().longValue())) {
                throw new RuntimeException("Usuario no encontrado con ID: " + dto.getUserId());
            }
            entity.setUserId(dto.getUserId());
        }

        entity.setToken(dto.getToken());
        entity.setFechaIniSesion(dto.getFechaIniSesion());
        entity.setFechaExpiracion(dto.getFechaExpiracion());

        Session actualizada = sessionRepository.save(entity);
        return toDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Session no encontrada con ID: " + id);
        }
        sessionRepository.deleteById(id);
    }
}