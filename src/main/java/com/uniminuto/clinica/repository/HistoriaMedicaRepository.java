package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar el acceso a datos de historias médicas.
 * Proporciona métodos para consultar y persistir historias médicas en la base de datos,
 * incluyendo filtrado por rango de fechas.
 *
 * @author ANRUIZSS
 * @version 1.0
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Encuentra historias médicas dentro de un rango de fechas específico,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha y hora inicial del rango de búsqueda (inclusive).
     * @param fechaFin Fecha y hora final del rango de búsqueda (inclusive).
     * @return Lista de historias médicas dentro del rango de fechas especificado,
     *         ordenadas por fecha de creación descendente.
     */
    List<HistoriaMedica> findByFechaCreacionBetweenOrderByFechaCreacionDesc(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin);
}