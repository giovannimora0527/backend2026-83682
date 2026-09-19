package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.model.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad FormulaMedica.
 * Proporciona métodos para acceder a datos de fórmulas médicas.
 *
 * @author TuNombre
 * @version 1.0
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Encuentra todas las fórmulas médicas ordenadas por fecha de creación
     * de la más reciente a la más antigua.
     *
     * @return Lista de fórmulas médicas ordenadas por fecha de creación.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();
}