package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.RazaDTO;

import java.util.List;

public interface RazaService {

    RazaDTO crear(RazaDTO dto);
    List<RazaDTO> listarTodos();
    RazaDTO buscarPorId(Long id);
    RazaDTO buscarPorNombre(String nombre);
    RazaDTO actualizar(Long id, RazaDTO dto);
    void eliminar(Long id);
}