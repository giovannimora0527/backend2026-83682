package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.MedicamentoDTO;

import java.util.List;

public interface MedicamentoService {

    MedicamentoDTO crear(MedicamentoDTO dto);
    List<MedicamentoDTO> listarTodos();

    List<MedicamentoDTO> OrdenarListaFecha();

    MedicamentoDTO buscarPorId(Long id);
    MedicamentoDTO buscarPorNombre(String nombre);
    MedicamentoDTO actualizar(Long id, MedicamentoDTO dto);
    void eliminar(Long id);
}