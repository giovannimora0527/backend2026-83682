package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.SessionDTO;

import java.util.List;

public interface SessionService {

    SessionDTO crear(SessionDTO dto);
    List<SessionDTO> listarTodos();
    SessionDTO buscarPorId(Long id);
    SessionDTO buscarPorToken(String token);
    List<SessionDTO> buscarPorUsuario(Integer userId);
    SessionDTO actualizar(Long id, SessionDTO dto);
    void eliminar(Long id);
}