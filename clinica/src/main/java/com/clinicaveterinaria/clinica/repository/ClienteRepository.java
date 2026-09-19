package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    Optional<Cliente> findByUsuarioId(Integer usuarioId);

}