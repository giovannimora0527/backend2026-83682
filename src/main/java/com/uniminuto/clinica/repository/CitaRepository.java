package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    String NO_CANCELADA = " AND c.estado <> com.uniminuto.clinica.entity.enums.EstadoCita.CANCELADA "
            + "AND c.fechaInicio < :fin AND c.fechaFin > :inicio AND (:idExcluido IS NULL OR c.id <> :idExcluido)";

    @Override
    @EntityGraph(attributePaths = {"cliente", "mascota", "mascota.raza", "mascota.cliente", "medico",
            "medico.especializacion", "servicio", "servicio.especializacion"})
    List<Cita> findAll();

    @EntityGraph(attributePaths = {"cliente", "mascota", "mascota.raza", "mascota.cliente", "medico",
            "medico.especializacion", "servicio", "servicio.especializacion"})
    List<Cita> findByFechaInicioBetweenOrderByFechaInicio(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @EntityGraph(attributePaths = {"cliente", "mascota", "mascota.raza", "mascota.cliente", "medico",
            "medico.especializacion", "servicio", "servicio.especializacion"})
    List<Cita> findByMascotaOrderByFechaInicioDesc(Mascota mascota);

    /** Citas que tocan el rango, de un medico o de todos, incluidas las canceladas. */
    @EntityGraph(attributePaths = {"cliente", "mascota", "mascota.raza", "mascota.cliente", "medico",
            "medico.especializacion", "servicio", "servicio.especializacion"})
    @Query("SELECT c FROM Cita c WHERE c.fechaInicio < :fin AND c.fechaFin > :inicio "
            + "AND (:medicoId IS NULL OR c.medico.id = :medicoId) ORDER BY c.fechaInicio, c.id")
    List<Cita> agenda(@Param("medicoId") Long medicoId, @Param("inicio") LocalDateTime inicio,
                      @Param("fin") LocalDateTime fin);

    @Query("SELECT c.estado, COUNT(c) FROM Cita c WHERE c.fechaInicio >= :inicio AND c.fechaInicio < :fin "
            + "AND (:medicoId IS NULL OR c.medico.id = :medicoId) GROUP BY c.estado")
    List<Object[]> contarPorEstado(@Param("medicoId") Long medicoId, @Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.medico.id = :medicoId" + NO_CANCELADA)
    boolean medicoOcupado(@Param("medicoId") Long medicoId, @Param("inicio") LocalDateTime inicio,
                          @Param("fin") LocalDateTime fin, @Param("idExcluido") Long idExcluido);

    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.mascota.mascotaId = :mascotaId" + NO_CANCELADA)
    boolean mascotaOcupada(@Param("mascotaId") Integer mascotaId, @Param("inicio") LocalDateTime inicio,
                           @Param("fin") LocalDateTime fin, @Param("idExcluido") Long idExcluido);
}
