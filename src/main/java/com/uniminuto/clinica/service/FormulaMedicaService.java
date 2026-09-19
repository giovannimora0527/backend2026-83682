package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;

import java.util.List;

/**
 * Servicio encargado de definir las operaciones
 * relacionadas con las fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Obtiene todas las fórmulas médicas ordenadas
     * desde la más reciente hasta la más antigua.
     *
     * @return lista de fórmulas médicas ordenadas por fecha de creación.
     */
    List<FormulaMedica> obtenerFormulasMedicas();
}