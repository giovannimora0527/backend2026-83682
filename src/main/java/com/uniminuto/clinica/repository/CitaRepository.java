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

    List<Cita> findByFechaInicioBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /** Indica si el medico o la mascota tienen una cita no cancelada que se cruza con el rango. */
    @Query("""
            SELECT COUNT(c) > 0 FROM Cita c
            WHERE (c.medico = :medico OR c.mascota = :mascota)
              AND c.estado <> com.uniminuto.clinica.entity.enums.EstadoCita.CANCELADA
              AND c.fechaInicio < :fin AND c.fechaFin > :inicio
              AND (:idExcluido IS NULL OR c.id <> :idExcluido)
            """)
    boolean existeSolapamiento(@Param("medico") Medico medico, @Param("mascota") Mascota mascota,
                               @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                               @Param("idExcluido") Long idExcluido);

    List<Cita> findByMascotaOrderByFechaInicioDesc(Mascota mascota);

}
