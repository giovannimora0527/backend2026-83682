package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar el acceso a datos de anotaciones de historia médica.
 * Proporciona métodos para consultar y persistir anotaciones en la base de datos.
 *
 * @author ANRUIZSS
 * @version 1.0
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Encuentra todas las anotaciones de una historia médica específica,
     * ordenadas por fecha de la más reciente a la más antigua.
     *
     * @param historiaId Identificador de la historia médica.
     * @return Lista de anotaciones de la historia médica especificada.
     */
    List<AnotacionHistoria> findByHistoriaIdOrderByFechaDesc(Integer historiaId);
}