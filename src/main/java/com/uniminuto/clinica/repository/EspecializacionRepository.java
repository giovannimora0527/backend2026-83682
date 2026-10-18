package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecializacionRepository extends JpaRepository<Especializacion, Integer> {

    /** Especializaciones ordenadas por nombre para los formularios. */
    List<Especializacion> findAllByOrderByNombreAsc();
}
