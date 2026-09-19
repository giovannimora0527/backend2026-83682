package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link FormulaMedica}.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Devuelve todas las formulas medicas ordenadas por fecha de creacion,
     * de la mas reciente a la mas antigua. Cumple el requerimiento 1 del
     * taller.
     *
     * @return lista de formulas medicas ordenadas de mas reciente a mas antigua
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}
