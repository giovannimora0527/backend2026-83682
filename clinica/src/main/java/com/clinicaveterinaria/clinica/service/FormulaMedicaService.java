package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.entity.FormulaMedica;
import com.clinicaveterinaria.clinica.models.FormulaMedicaDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface FormulaMedicaService {

    FormulaMedicaDTO crear(FormulaMedicaDTO dto);
    List<FormulaMedicaDTO> listarTodos();
    FormulaMedicaDTO buscarPorId(Long id);
    List<FormulaMedicaDTO> buscarPorCita(Integer citaId);
    List<FormulaMedicaDTO> buscarPorMedicamento(Integer medicamentoId);
    List<FormulaMedicaDTO> OrdenarListaFecha();
    FormulaMedicaDTO actualizar(Long id, FormulaMedicaDTO dto);
    void eliminar(Long id);
}