package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    List<Despacho> findByFormula_IdAndEstado(Long formulaId, EstadoPreparacion estado);
}
