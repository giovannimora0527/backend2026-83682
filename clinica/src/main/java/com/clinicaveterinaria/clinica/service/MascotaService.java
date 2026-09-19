package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.MascotaDTO;

import java.util.List;

public interface MascotaService {

    MascotaDTO crear(MascotaDTO dto);
    List<MascotaDTO> listarTodos();
    MascotaDTO buscarPorId(Long id);
    List<MascotaDTO> buscarPorCliente(Integer clienteId);
    List<MascotaDTO> buscarPorRaza(Integer razaId);
    MascotaDTO actualizar(Long id, MascotaDTO dto);
    void eliminar(Long id);
}