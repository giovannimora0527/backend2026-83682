package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.FormulaRs;

import java.util.List;

public interface FormulaService {

    /** Un MEDICO ve solo las suyas. */
    List<FormulaRs> listar();

    List<FormulaRs> listarPorCitas(List<Long> citaIds);
}
