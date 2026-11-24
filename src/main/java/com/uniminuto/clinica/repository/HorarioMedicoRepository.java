package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HorarioMedicoRepository extends JpaRepository<HorarioMedico, Long> {

    List<HorarioMedico> findByMedico_IdOrderByDiaSemanaAscHoraInicioAsc(Long medicoId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM HorarioMedico h WHERE h.medico.id = :medicoId")
    void eliminarPorMedico(@Param("medicoId") Long medicoId);

    List<HorarioMedico> findByMedico_IdAndActivoTrue(Long medicoId);

    List<HorarioMedico> findByActivoTrue();
}
