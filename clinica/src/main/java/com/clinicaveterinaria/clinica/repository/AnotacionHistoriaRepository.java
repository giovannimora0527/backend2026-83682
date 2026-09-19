package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    List<AnotacionHistoria> findByHistoriaId(Integer historiaId);
    List<AnotacionHistoria> findByMedicoId(Integer medicoId);
}