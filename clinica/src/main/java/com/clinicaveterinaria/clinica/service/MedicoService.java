package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.MedicoDTO;

import java.util.List;

public interface MedicoService {

    MedicoDTO crear(MedicoDTO dto);
    List<MedicoDTO> listarTodos();
    MedicoDTO buscarPorId(Long id);
    MedicoDTO buscarPorDocumento(String numeroDocumento);
    MedicoDTO buscarPorRegistro(String registroProfesional);
    MedicoDTO actualizar(Long id, MedicoDTO dto);
    void eliminar(Long id);
}