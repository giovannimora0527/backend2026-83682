package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio encargado de realizar las consultas
 * relacionadas con las fórmulas médicas.
 */
@Repository
public interface FormulaMedicaRepository
        extends JpaRepository<FormulaMedica, Long> {

    /**
     * Obtiene todas las fórmulas médicas ordenadas
     * desde la fecha de creación más reciente
     * hasta la más antigua.
     *
     * @return lista de fórmulas médicas ordenadas por fecha.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();
}