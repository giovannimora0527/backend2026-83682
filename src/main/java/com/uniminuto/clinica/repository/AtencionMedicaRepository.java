package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AtencionMedica;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AtencionMedicaRepository extends JpaRepository<AtencionMedica, Long> {

    Optional<AtencionMedica> findByCita_Id(Long citaId);

    @EntityGraph(attributePaths = {"cita", "medico"})
    List<AtencionMedica> findByMascota_MascotaIdAndFinalizadaTrueOrderByFechaInicioDesc(Integer mascotaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AtencionMedica a WHERE a.id = :id")
    Optional<AtencionMedica> bloquear(@Param("id") Long id);
}
