package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazaRepository extends JpaRepository<Raza, Long> {

    Optional<Raza> findByNombre(String nombre);
}