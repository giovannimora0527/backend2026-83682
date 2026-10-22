
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;

import java.util.List;

/**
 * Servicio encargado de definir las operaciones
 * para gestionar las fórmulas médicas
 * de la clínica veterinaria UNIMINUTO.
 */
public interface FormulaMedicaService {

    // ==========================================
    // 1. LISTAR TODAS LAS FÓRMULAS MÉDICAS
    // ==========================================

    /**
     * Consulta todas las fórmulas registradas.
     *
     * @return lista de fórmulas médicas.
     */
    List<FormulaMedica> obtenerFormulasMedicas();


    // ==========================================
    // 2. CONSULTAR UNA FÓRMULA POR SU ID
    // ==========================================

    /**
     * Busca una fórmula médica específica.
     *
     * @param id identificador de la fórmula.
     * @return fórmula médica encontrada.
     */
    FormulaMedica obtenerFormulaPorId(Long id);


    // ==========================================
    // 3. CREAR UNA NUEVA FÓRMULA MÉDICA
    // ==========================================

    /**
     * Registra una fórmula médica.
     *
     * @param formula datos de la nueva fórmula.
     * @return fórmula médica guardada.
     */
    FormulaMedica crearFormula(FormulaMedica formula);


    // ==========================================
    // 4. ACTUALIZAR UNA FÓRMULA MÉDICA
    // ==========================================

    /**
     * Modifica una fórmula médica existente.
     *
     * @param id identificador de la fórmula.
     * @param formula datos que se actualizarán.
     * @return fórmula médica actualizada.
     */
    FormulaMedica actualizarFormula(
            Long id,
            FormulaMedica formula
    );
}
