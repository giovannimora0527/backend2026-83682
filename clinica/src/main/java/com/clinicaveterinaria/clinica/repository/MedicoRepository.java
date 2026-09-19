package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByNumeroDocumento(String numeroDocumento);
    Optional<Medico> findByRegistroProfesional(String registroProfesional);
}