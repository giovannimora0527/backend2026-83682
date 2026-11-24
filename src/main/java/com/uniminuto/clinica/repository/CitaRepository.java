package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Indica si el medico o la mascota tienen una cita no cancelada que se cruza con la nueva. Como todas duran
     * lo mismo, se cruzan si la existente empieza despues de (inicio - duracion) y antes de (inicio + duracion).
     */
    @Query("""
            SELECT COUNT(c) > 0 FROM Cita c
            WHERE (c.medico = :medico OR c.mascota = :mascota)
              AND UPPER(c.estado) <> 'CANCELADA'
              AND c.fechaHora > :desde AND c.fechaHora < :hasta
              AND (:idExcluido IS NULL OR c.id <> :idExcluido)
            """)
    boolean existeSolapamiento(@Param("medico") Medico medico, @Param("mascota") Mascota mascota,
                               @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                               @Param("idExcluido") Long idExcluido);

    List<Cita> findByMascotaOrderByFechaHoraDesc(Mascota mascota);

}
