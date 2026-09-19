package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.EspecializacionDTO;

import java.util.List;

public interface EspecializacionService {

    EspecializacionDTO crear(EspecializacionDTO dto);
    List<EspecializacionDTO> listarTodos();
    EspecializacionDTO buscarPorId(Long id);
    EspecializacionDTO buscarPorNombre(String nombre);
    EspecializacionDTO buscarPorCodigo(String codigo);
    EspecializacionDTO actualizar(Long id, EspecializacionDTO dto);
    void eliminar(Long id);
}