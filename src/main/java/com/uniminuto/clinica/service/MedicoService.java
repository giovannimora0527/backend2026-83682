package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface MedicoService {

    List<Medico> listarMedicos();

    Medico buscarMedico(String tipoDocumento, String numeroDocumento)
            throws BadRequestException;

    Medico buscarMedicoPorRegistroProfesional(String registroProfesional)
            throws BadRequestException;
}
