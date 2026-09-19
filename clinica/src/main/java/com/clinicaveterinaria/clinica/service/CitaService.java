package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.CitaDTO;

import java.time.LocalDateTime;
import java.util.List;


public interface CitaService {

    CitaDTO crear(CitaDTO dto);
    List<CitaDTO> listarTodos();
    CitaDTO buscarPorId(Long id);
    List<CitaDTO> buscarPorCliente(Integer clienteId);
    List<CitaDTO> buscarPorMascota(Integer mascotaId);
    List<CitaDTO> buscarPorMedico(Integer medicoId);
    List<CitaDTO> buscarPorEstado(String estado);
    List<CitaDTO> buscarPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
    CitaDTO actualizar(Long id, CitaDTO dto);
    void eliminar(Long id);
}