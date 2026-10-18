package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long>, JpaSpecificationExecutor<HistoriaClinica> {
    default boolean existsByCita_CitaId(Long citaId) { return false; }

    /** Primera historia registrada para la mascota (columna paciente_id). */
    Optional<HistoriaClinica> findFirstByMascota_MascotaIdOrderByFechaHoraAsc(Integer mascotaId);

    /** Cantidad de historias clínicas de la mascota (paciente_id). */
    @org.springframework.data.jpa.repository.Query(value = "select count(*) from historia_medica where paciente_id = :mascotaId", nativeQuery = true)
    long contarPorMascota(@org.springframework.data.repository.query.Param("mascotaId") Integer mascotaId);
}
