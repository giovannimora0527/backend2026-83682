package com.uniminuto.clinica.api;

import com.uniminuto.clinica.dto.FormulaMedicaDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interfaz que define el "contrato" del servicio REST de formulas medicas.
 */
public interface FormulaMedicaApi {

    /**
     * Lista todas las formulas medicas del inventario, ordenadas de la mas
     * reciente a la mas antigua.
     *
     * @return respuesta HTTP con la lista de formulas medicas
     */
    ResponseEntity<List<FormulaMedicaDTO>> listarFormulasMedicas();
}
