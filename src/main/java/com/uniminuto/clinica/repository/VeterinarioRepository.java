package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Veterinario v where v.veterinarioId = :id")
    Optional<Veterinario> lockById(@Param("id") Long id);

    /** Busca un médico por tipo y número de documento (servicio del parcial). */
    Optional<Veterinario> findFirstByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    /** Busca un médico por registro profesional (servicio del parcial). */
    Optional<Veterinario> findFirstByRegistroProfesional(String registroProfesional);

    /** Columnas UNIQUE de la tabla medico: numero_documento y registro_profesional. */
    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumentoAndVeterinarioIdNot(String numeroDocumento, Long veterinarioId);

    boolean existsByRegistroProfesional(String registroProfesional);

    boolean existsByRegistroProfesionalAndVeterinarioIdNot(String registroProfesional, Long veterinarioId);
}
