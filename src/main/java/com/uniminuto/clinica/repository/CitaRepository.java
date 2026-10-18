package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {
    java.util.Optional<Cita> findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdOrderByFechaHoraDesc(Integer mascotaId, Long veterinarioId);

    /** Cita más reciente entre la mascota y el veterinario cuyo estado sea distinto del indicado (p. ej. no cancelada). */
    java.util.Optional<Cita> findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdAndEstadoNotOrderByFechaHoraDesc(
            Integer mascotaId, Long veterinarioId, com.uniminuto.clinica.entity.EstadoCita estado);

    /** Citas entre dos fechas, de la más reciente a la más antigua (servicio del parcial). */
    List<Cita> findAllByFechaHoraBetweenOrderByFechaHoraDesc(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    /** Cantidad de citas registradas para una mascota. */
    @Query(value = "select count(*) from cita where mascota_id = :mascotaId", nativeQuery = true)
    long contarPorMascota(@Param("mascotaId") Integer mascotaId);
    @Query("select c from Cita c where c.veterinario.veterinarioId = :veterinarioId and function('date', c.fechaHora) = :fecha")
    List<Cita> findByVeterinario_VeterinarioIdAndFecha(@Param("veterinarioId") Long veterinarioId,
            @Param("fecha") LocalDate fecha);

    @Query("select c from Cita c where c.mascota.mascotaId = :mascotaId and function('date', c.fechaHora) = :fecha")
    List<Cita> findByMascota_MascotaIdAndFecha(@Param("mascotaId") Integer mascotaId,
            @Param("fecha") LocalDate fecha);
}
