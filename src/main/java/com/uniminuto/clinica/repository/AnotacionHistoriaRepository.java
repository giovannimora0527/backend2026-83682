package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio encargado de realizar las consultas
 * relacionadas con las anotaciones de las historias médicas.
 */
@Repository
public interface AnotacionHistoriaRepository
        extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Busca las anotaciones comprendidas entre una fecha inicial
     * y una fecha final, ordenadas desde la más reciente
     * hasta la más antigua.
     *
     * @param fechaInicial fecha y hora inicial de búsqueda.
     * @param fechaFinal fecha y hora final de búsqueda.
     * @return lista de anotaciones encontradas.
     */
    List<AnotacionHistoria> findByFechaBetweenOrderByFechaDesc(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );
}