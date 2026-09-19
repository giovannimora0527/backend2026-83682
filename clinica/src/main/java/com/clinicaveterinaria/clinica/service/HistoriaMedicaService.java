package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.HistoriaMedicaDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoriaMedicaService {

    HistoriaMedicaDTO crear(HistoriaMedicaDTO dto);
    List<HistoriaMedicaDTO> listarTodos();
    HistoriaMedicaDTO buscarPorId(Long id);
    List<HistoriaMedicaDTO> buscarPorPaciente(Integer pacienteId);
    List<HistoriaMedicaDTO> buscarPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
    HistoriaMedicaDTO actualizar(Long id, HistoriaMedicaDTO dto);
    void eliminar(Long id);
}