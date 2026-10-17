package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
/** Acceso a datos de clientes. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {}
