package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.FormulaRs;
import java.time.LocalDate;
import java.util.List;

public interface FormulaService {

    /** Un MEDICO ve solo las suyas. */
    List<FormulaRs> listar(String texto, LocalDate desde, LocalDate hasta, EstadoFormula estado);

    List<FormulaRs> listarPorCitas(List<Long> citaIds);

    List<FormulaRs> listarPorCita(Long citaId);

    /** Formulas no borrador de la mascota, salvo las de la cita indicada. */
    List<FormulaRs> antecedentes(Integer mascotaId, Long citaExcluida);

    FormulaRs obtener(Long id);

    FormulaRs crear(FormulaRq rq);

    FormulaRs actualizar(Long id, FormulaRq rq);

    FormulaRs confirmar(Long id);

    FormulaRs anular(Long id, String motivo);

    Descarga pdf(Long id);
}
