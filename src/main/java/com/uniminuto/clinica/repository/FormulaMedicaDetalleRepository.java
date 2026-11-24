package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface FormulaMedicaDetalleRepository extends JpaRepository<FormulaMedicaDetalle, Long> {

    @EntityGraph(attributePaths = {"medicamento", "medicamento.impuesto"})
    List<FormulaMedicaDetalle> findByFormula_IdInOrderById(Collection<Long> formulaIds);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM FormulaMedicaDetalle d WHERE d.formula.id = :formulaId")
    void eliminarPorFormula(@Param("formulaId") Long formulaId);
}
