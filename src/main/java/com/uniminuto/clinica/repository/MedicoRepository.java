package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    @Query("""
            SELECT m FROM Medico m
            WHERE m.estado = true
              AND NOT EXISTS (
                SELECT c.id FROM Cita c
                WHERE c.medico = m
                  AND c.estado <> com.uniminuto.clinica.entity.enums.EstadoCita.CANCELADA
                  AND c.fechaInicio < :fechaFin AND c.fechaFin > :fechaInicio
            )
            """)
    List<Medico> findDisponiblesPorFecha(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin);

    Optional<Medico> findByUsuarioId(Long usuarioId);

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByRegistroProfesional(String registroProfesional);

    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    boolean existsByRegistroProfesionalAndIdNot(String registroProfesional, Long id);
}
