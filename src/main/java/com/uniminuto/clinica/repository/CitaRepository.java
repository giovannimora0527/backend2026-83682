package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface CitaRepository extends  JpaRepository<Cita, Long> {
    List<Cita> findAllByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );
}
