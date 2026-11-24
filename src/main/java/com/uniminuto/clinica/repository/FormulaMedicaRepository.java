package com.uniminuto.clinica.repository;

import java.util.Collection;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    @Query("""
            SELECT f FROM FormulaMedica f
            WHERE (:medicoId IS NULL OR f.medico.id = :medicoId)
              AND (:estado IS NULL OR f.estadoFormula = :estado)
              AND f.fechaGeneracion >= :desde AND f.fechaGeneracion < :hasta
            ORDER BY f.fechaGeneracion DESC, f.id DESC
            """)
    List<FormulaMedica> buscar(@Param("medicoId") Long medicoId, @Param("estado") EstadoFormula estado,
                               @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    List<FormulaMedica> findByCita_IdInOrderByFechaGeneracionDesc(List<Long> citaIds);

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    List<FormulaMedica> findByMascota_MascotaIdAndEstadoFormulaNotOrderByFechaGeneracionDesc(
            Integer mascotaId, EstadoFormula estado);

    boolean existsByCita_IdAndEstadoFormula(Long citaId, EstadoFormula estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FormulaMedica f WHERE f.id = :id")
    Optional<FormulaMedica> bloquear(@Param("id") Long id);

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    @Query("""
            SELECT f FROM FormulaMedica f
            WHERE f.estadoFormula = com.uniminuto.clinica.entity.enums.EstadoFormula.CONFIRMADA
              AND f.estadoDespacho IN :estados
              AND f.fechaGeneracion >= :desde AND f.fechaGeneracion < :hasta
            ORDER BY f.fechaGeneracion, f.id
            """)
    List<FormulaMedica> bandeja(@Param("estados") Collection<EstadoDespacho> estados,
                                @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
