package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<Despacho> findByFormula_IdAndEstado(Long formulaId, EstadoPreparacion estado);

    @EntityGraph(attributePaths = {"formula", "usuario"})
    List<Despacho> findByFormula_IdInAndEstado(Collection<Long> formulaIds, EstadoPreparacion estado);

    Optional<Despacho> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT d.formula.id FROM Despacho d WHERE d.id = :id")
    Optional<Long> formulaDe(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Despacho d WHERE d.id = :id")
    Optional<Despacho> bloquear(@Param("id") Long id);

    @EntityGraph(attributePaths = {"formula", "formula.medico", "formula.cliente", "formula.mascota", "usuario",
            "usuarioReversion"})
    @Query("""
            SELECT d FROM Despacho d
            WHERE d.estado <> com.uniminuto.clinica.entity.enums.EstadoPreparacion.EN_PREPARACION
              AND d.fechaInicio >= :desde AND d.fechaInicio < :hasta
            ORDER BY d.fechaInicio DESC, d.id DESC
            """)
    List<Despacho> historial(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
