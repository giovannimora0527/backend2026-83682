package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.UsuarioDTO;

import java.util.List;

public interface UsuarioService {

    UsuarioDTO crear(UsuarioDTO dto);
    List<UsuarioDTO> listarTodos();
    UsuarioDTO buscarPorId(Long id);
    UsuarioDTO buscarPorUsername(String username);
    UsuarioDTO buscarPorEmail(String email);
    UsuarioDTO actualizar(Long id, UsuarioDTO dto);
    void eliminar(Long id);
}