package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.DespachoDetalle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface DespachoDetalleRepository extends JpaRepository<DespachoDetalle, Long> {

    @EntityGraph(attributePaths = {"medicamento", "lote"})
    List<DespachoDetalle> findByDespacho_IdInOrderById(Collection<Long> despachoIds);

    /** Lo entregado por la formula en despachos vigentes hasta el indicado, para el comprobante. */
    @Query("SELECT COALESCE(SUM(x.cantidadEntregada), 0) FROM DespachoDetalle x "
            + "WHERE x.formulaDetalle.id = :formulaDetalleId AND x.despacho.id <= :despachoId "
            + "AND (x.despacho.id = :despachoId "
            + "OR x.despacho.estado = com.uniminuto.clinica.entity.enums.EstadoPreparacion.CONFIRMADO)")
    long entregadoHasta(@Param("formulaDetalleId") Long formulaDetalleId, @Param("despachoId") Long despachoId);

    @EntityGraph(attributePaths = {"despacho", "despacho.usuario", "despacho.formula", "despacho.formula.medico",
            "despacho.formula.cliente", "despacho.formula.mascota", "lote"})
    @Query("SELECT x FROM DespachoDetalle x WHERE x.medicamento.id = :medicamentoId ORDER BY x.id DESC")
    List<DespachoDetalle> historial(@Param("medicamentoId") Long medicamentoId);

    /** Lo dispensado de una cita que aun se puede facturar. */
    @EntityGraph(attributePaths = {"medicamento", "lote"})
    @Query("SELECT x FROM DespachoDetalle x WHERE x.despacho.formula.cita.id = :citaId AND x.facturado = false "
            + "AND x.despacho.estado = com.uniminuto.clinica.entity.enums.EstadoPreparacion.CONFIRMADO ORDER BY x.id")
    List<DespachoDetalle> porFacturar(@Param("citaId") Long citaId);
}
