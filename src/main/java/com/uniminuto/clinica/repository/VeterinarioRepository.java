package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.models.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
/** Acceso a datos de veterinarios. */
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {}
