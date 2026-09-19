package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio encargado de realizar las consultas
 * relacionadas con las citas de la clínica.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca las citas que se encuentren entre una fecha inicial
     * y una fecha final, ordenadas desde la más reciente
     * hasta la más antigua.
     *
     * @param fechaInicial fecha y hora inicial de la búsqueda.
     * @param fechaFinal fecha y hora final de la búsqueda.
     * @return lista de citas encontradas en el rango indicado.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );
}