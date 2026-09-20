package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de {@link HistoriaMedica}; se usa para validar referencias. */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {
}
