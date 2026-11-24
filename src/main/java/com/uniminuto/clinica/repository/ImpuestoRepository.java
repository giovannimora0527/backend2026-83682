package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImpuestoRepository extends JpaRepository<Impuesto, Long> {

    List<Impuesto> findByActivoTrueOrderByPorcentaje();

    List<Impuesto> findAllByOrderByPorcentaje();

    Optional<Impuesto> findByNombreIgnoreCase(String nombre);

    @Query("SELECT COUNT(t) > 0 FROM Tarifa t WHERE t.impuesto.id = :id")
    boolean usadoEnTarifas(@Param("id") Long id);

    @Query("SELECT COUNT(t) > 0 FROM Tarifa t WHERE t.impuesto.id = :id AND t.activa = true")
    boolean usadoEnTarifasVigentes(@Param("id") Long id);

    @Query("SELECT COUNT(m) > 0 FROM Medicamento m WHERE m.impuesto.id = :id")
    boolean usadoEnProductos(@Param("id") Long id);
}
