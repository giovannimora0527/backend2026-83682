package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<Pago> findByFactura_IdOrderByFechaAscIdAsc(Long facturaId);
}
