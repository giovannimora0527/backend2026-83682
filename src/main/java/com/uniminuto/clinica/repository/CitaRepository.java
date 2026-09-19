package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Cita.
 * Proporciona métodos para acceder a datos de citas médicas.
 *
 * @author TuNombre
 * @version 1.0
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Encuentra citas dentro de un rango de fechas específico,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha inicial del rango de búsqueda.
     * @param fechaFin Fecha final del rango de búsqueda.
     * @return Lista de citas dentro del rango de fechas especificado.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}