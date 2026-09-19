package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.FormulaMedicaDTO;

import java.util.List;

/**
 * Interfaz de servicio para la entidad FormulaMedica.
 *
 * <p>Cubre el Requerimiento 1 del taller: listar las formulas medicas del
 * inventario ordenadas por fecha de creacion, de la mas reciente a la mas
 * antigua.</p>
 */
public interface FormulaMedicaService {

    /**
     * Lista todas las formulas medicas del inventario, ordenadas por fecha
     * de creacion desde la mas reciente hasta la mas antigua.
     *
     * @return lista de formulas medicas ordenadas de mas reciente a mas antigua
     */
    List<FormulaMedicaDTO> listarFormulasMedicas();
}
