package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface FormulaService {

    List<FormulaMedica> findAllFormulas();

    MiRespuestaRS guardarFormula(FormulaRq formulaRq) throws BadRequestException;

    MiRespuestaRS actualizarFormula(FormulaRq formulaRq) throws BadRequestException;
}
