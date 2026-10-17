package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long>, JpaSpecificationExecutor<HistoriaClinica> {
    default boolean existsByCita_CitaId(Long citaId) { return false; }
}
