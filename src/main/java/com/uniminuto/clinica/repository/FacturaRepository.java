package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByCliente_IdOrderByFechaGeneracionDesc(Long clienteId);

    List<Factura> findByMascota_MascotaIdOrderByFechaGeneracionDesc(Integer mascotaId);
}
