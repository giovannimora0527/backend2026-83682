package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FacturaDetalle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaDetalleRepository extends JpaRepository<FacturaDetalle, Long> {

    @EntityGraph(attributePaths = "despachoDetalle")
    List<FacturaDetalle> findByFactura_IdOrderById(Long facturaId);
}
