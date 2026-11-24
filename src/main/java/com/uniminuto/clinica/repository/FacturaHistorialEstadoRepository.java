package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FacturaHistorialEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaHistorialEstadoRepository extends JpaRepository<FacturaHistorialEstado, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<FacturaHistorialEstado> findByFactura_IdOrderByFechaAscIdAsc(Long facturaId);
}
