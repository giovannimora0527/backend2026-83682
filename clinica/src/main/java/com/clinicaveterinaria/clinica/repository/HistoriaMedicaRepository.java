package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    List<HistoriaMedica> findByPacienteId(Integer pacienteId);

    List<HistoriaMedica> findByFechaCreacionBetweenOrderByFechaCreacionDesc(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );
}