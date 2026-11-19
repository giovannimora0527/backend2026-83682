package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita,Long> {
    List<Cita> findAllByOrderByFechaHoraDesc();
    List<Cita> findByMedicoIdOrderByFechaHoraDesc(Integer medicoId);
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);
    List<Cita> findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraDesc(Integer medicoId,LocalDateTime desde,LocalDateTime hasta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cita c where c.id=:id")
    Optional<Cita> bloquearPorId(@Param("id") Long id);
}
