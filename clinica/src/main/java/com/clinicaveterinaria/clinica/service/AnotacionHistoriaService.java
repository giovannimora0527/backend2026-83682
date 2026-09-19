package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.AnotacionHistoriaDTO;

import java.util.List;

public interface AnotacionHistoriaService {

    AnotacionHistoriaDTO crear(AnotacionHistoriaDTO dto);
    List<AnotacionHistoriaDTO> listarTodos();
    AnotacionHistoriaDTO buscarPorId(Long id);
    List<AnotacionHistoriaDTO> buscarPorHistoria(Integer historiaId);
    List<AnotacionHistoriaDTO> buscarPorMedico(Integer medicoId);
    AnotacionHistoriaDTO actualizar(Long id, AnotacionHistoriaDTO dto);
    /*void eliminar(Long id);*/
}