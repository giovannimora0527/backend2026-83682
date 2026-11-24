package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImpuestoRepository extends JpaRepository<Impuesto, Long> {

    List<Impuesto> findByActivoTrueOrderByPorcentaje();
}
