package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Tarifa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    @EntityGraph(attributePaths = {"servicio", "servicio.especializacion", "impuesto"})
    @Query("SELECT t FROM Tarifa t WHERE (:servicioId IS NULL OR t.servicio.id = :servicioId) "
            + "AND (:soloVigentes = false OR t.activa = true) ORDER BY t.servicio.nombre, t.vigenteDesde DESC, t.id DESC")
    List<Tarifa> buscar(@Param("servicioId") Long servicioId, @Param("soloVigentes") boolean soloVigentes);

    @EntityGraph(attributePaths = "impuesto")
    Optional<Tarifa> findByServicio_IdAndActivaTrue(Long servicioId);

    @EntityGraph(attributePaths = {"servicio", "impuesto"})
    List<Tarifa> findByActivaTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tarifa t WHERE t.id = :id")
    Optional<Tarifa> bloquear(@Param("id") Long id);
}
