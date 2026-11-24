package com.uniminuto.clinica.repository;

import java.time.LocalDate;
import com.uniminuto.clinica.entity.LoteMedicamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoteMedicamentoRepository extends JpaRepository<LoteMedicamento, Long> {

    List<LoteMedicamento> findByMedicamento_IdOrderByFechaIngresoDescIdDesc(Long medicamentoId);

    boolean existsByMedicamento_IdAndNumeroLoteIgnoreCase(Long medicamentoId, String numeroLote);

    @Query("SELECT l FROM LoteMedicamento l JOIN FETCH l.medicamento m WHERE m.codigo IN :codigos")
    List<LoteMedicamento> buscarPorCodigos(@Param("codigos") Collection<String> codigos);

    @Query("SELECT COALESCE(SUM(l.cantidadDisponible), 0) FROM LoteMedicamento l "
            + "WHERE l.medicamento.id = :medicamentoId AND l.activo = true")
    long existencias(@Param("medicamentoId") Long medicamentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LoteMedicamento l WHERE l.id = :id")
    Optional<LoteMedicamento> bloquear(@Param("id") Long id);

    /** Pares [medicamentoId, existencia vigente]. */
    @Query("SELECT l.medicamento.id, SUM(l.cantidadDisponible) FROM LoteMedicamento l "
            + "WHERE l.medicamento.id IN :ids AND l.activo = true "
            + "AND (l.fechaVencimiento IS NULL OR l.fechaVencimiento >= :hoy) GROUP BY l.medicamento.id")
    List<Object[]> existenciasVigentes(@Param("ids") Collection<Long> ids, @Param("hoy") LocalDate hoy);

    @Query("SELECT l FROM LoteMedicamento l WHERE l.medicamento.id IN :ids AND l.activo = true "
            + "AND l.cantidadDisponible > 0")
    List<LoteMedicamento> conExistencias(@Param("ids") Collection<Long> ids);
}
