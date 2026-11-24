package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;

import java.util.List;

public interface FormulaMedicaService {

    List<FormulaMedica> listarFormulasOrdenadas(boolean asc);
}
