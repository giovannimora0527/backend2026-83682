package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca un cliente por número de documento (servicio del parcial). */
    java.util.Optional<Cliente> findFirstByNumeroDocumento(String numeroDocumento);
}
