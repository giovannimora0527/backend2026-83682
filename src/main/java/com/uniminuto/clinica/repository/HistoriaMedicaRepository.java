package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link HistoriaMedica}.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Devuelve todas las historias medicas ordenadas por fecha de creacion,
     * de la mas reciente a la mas antigua.
     *
     * @return lista de historias medicas ordenadas de mas reciente a mas antigua
     */
    List<HistoriaMedica> findAllByOrderByFechaCreacionDesc();

    /**
     * Busca todas las historias medicas cuya fecha de creacion este entre
     * {@code fechaInicio} y {@code fechaFin} (ambas incluidas), ordenadas
     * desde la mas reciente hasta la mas antigua.
     *
     * @param fechaInicio fecha desde la cual buscar (inclusive)
     * @param fechaFin    fecha hasta la cual buscar (inclusive)
     * @return lista de historias medicas encontradas, ordenadas de mas reciente a mas antigua
     */
    List<HistoriaMedica> findByFechaCreacionBetweenOrderByFechaCreacionDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
