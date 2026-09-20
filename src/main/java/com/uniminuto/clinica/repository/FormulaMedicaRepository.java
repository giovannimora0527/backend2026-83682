package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** Acceso a datos de {@link FormulaMedica}. */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /** Lista el inventario ordenado de la mas reciente a la mas antigua. */
    List<FormulaMedica> findAllByOrderByFechaCreacionDesc();
}
