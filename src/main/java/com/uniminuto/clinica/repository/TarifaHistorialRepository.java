package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.TarifaHistorial;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TarifaHistorialRepository extends JpaRepository<TarifaHistorial, Long> {

    @EntityGraph(attributePaths = {"servicio", "usuario"})
    List<TarifaHistorial> findByServicio_IdOrderByFechaDescIdDesc(Long servicioId);

    @EntityGraph(attributePaths = {"servicio", "usuario"})
    List<TarifaHistorial> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaDescIdDesc(
            LocalDateTime desde, LocalDateTime hasta);
}
