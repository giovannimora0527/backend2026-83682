package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface FormulaMedicaService {

    List<FormulaMedica> listarFormulasOrdenadas(boolean asc);


    MiRespuestaRS crearFormula(FormulaMedicaRq formulaRq) throws BadRequestException;

    MiRespuestaRS actualizarFormula(FormulaMedicaRq formulaRq) throws BadRequestException;
}
