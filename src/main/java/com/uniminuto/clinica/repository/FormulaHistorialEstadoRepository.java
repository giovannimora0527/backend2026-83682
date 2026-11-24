package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaHistorialEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormulaHistorialEstadoRepository extends JpaRepository<FormulaHistorialEstado, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<FormulaHistorialEstado> findByFormula_IdOrderByFechaAscIdAsc(Long formulaId);
}
