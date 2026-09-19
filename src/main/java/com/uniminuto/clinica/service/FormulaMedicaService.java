package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.apache.coyote.BadRequestException ;
import java.util.List;

public interface FormulaMedicaService {
    // Devuelve la lista de fórmulas médicas ordenadas
    // desde la fecha de creación más reciente hasta la más antigua
    List<FormulaMedica> listarFormulasMedicas ()throws BadRequestException;
}
