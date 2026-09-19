package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    List<FormulaMedica> findByCitaId(Integer citaId);
    List<FormulaMedica> findByMedicamentoId(Integer medicamentoId);
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();
}