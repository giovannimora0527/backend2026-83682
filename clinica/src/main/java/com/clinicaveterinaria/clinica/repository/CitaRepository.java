package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByClienteId(Integer clienteId);
    List<Cita> findByMascotaId(Integer mascotaId);
    List<Cita> findByMedicoId(Integer medicoId);
    List<Cita> findByEstado(String estado);
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);

}

