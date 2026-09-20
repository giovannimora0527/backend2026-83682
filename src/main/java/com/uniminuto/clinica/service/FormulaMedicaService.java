package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.FormulaMedicaRs;
import java.util.List;

/** Logica de negocio de las formulas medicas. */
public interface FormulaMedicaService {

    /** Inventario completo, de la formula mas reciente a la mas antigua. */
    List<FormulaMedicaRs> listarFormulasRecientes();
}
