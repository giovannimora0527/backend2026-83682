package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.FormulaMedicaDTO;
import java.util.List;

/** Operaciones de negocio para fórmulas médicas. */
public interface FormulaMedicaService {
    /** Lista fórmulas de la más reciente a la más antigua. */
    List<FormulaMedicaDTO> listarFormulasMedicas();
    /** Busca una fórmula por identificador. */
    FormulaMedicaDTO obtenerPorId(Long id);
    /** Registra una fórmula después de validar sus relaciones y datos. */
    FormulaMedicaDTO crear(FormulaMedicaDTO dto);
    /** Actualiza los campos permitidos de una fórmula existente. */
    FormulaMedicaDTO actualizar(Long id, FormulaMedicaDTO dto);
}
