package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
}
