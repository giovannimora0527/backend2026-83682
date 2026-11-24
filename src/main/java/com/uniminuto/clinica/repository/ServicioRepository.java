package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.enums.TipoServicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @EntityGraph(attributePaths = "especializacion")
    List<Servicio> findAllByOrderByNombre();

    Optional<Servicio> findByCodigo(String codigo);

    List<Servicio> findByTipoServicioAndActivoTrueOrderById(TipoServicio tipo);
}
