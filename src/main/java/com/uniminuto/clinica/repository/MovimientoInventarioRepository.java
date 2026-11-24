package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = {"medicamento", "lote", "usuario", "formula", "despacho"})
    @Query("""
            SELECT m FROM MovimientoInventario m
            WHERE (:medicamentoId IS NULL OR m.medicamento.id = :medicamentoId)
              AND (:tipo IS NULL OR m.tipo = :tipo)
              AND m.fecha >= :desde AND m.fecha < :hasta
            ORDER BY m.fecha DESC, m.id DESC
            """)
    List<MovimientoInventario> buscar(@Param("medicamentoId") Long medicamentoId, @Param("tipo") TipoMovimiento tipo,
                                      @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
