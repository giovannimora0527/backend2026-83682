package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FormulaMedicaDetalleRepository extends JpaRepository<FormulaMedicaDetalle, Long> {

    @EntityGraph(attributePaths = "medicamento")
    List<FormulaMedicaDetalle> findByFormula_IdInOrderById(Collection<Long> formulaIds);
}
