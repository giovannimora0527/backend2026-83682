package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link AnotacionHistoria}.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Devuelve todas las anotaciones que pertenecen a una historia medica
     * especifica, ordenadas de la mas reciente a la mas antigua.
     *
     * @param historiaMedicaId id de la historia medica
     * @return lista de anotaciones de esa historia medica
     */
    List<AnotacionHistoria> findByHistoriaMedicaIdOrderByFechaDesc(Long historiaMedicaId);
}
