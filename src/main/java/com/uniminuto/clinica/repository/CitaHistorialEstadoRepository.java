package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.CitaHistorialEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CitaHistorialEstadoRepository extends JpaRepository<CitaHistorialEstado, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<CitaHistorialEstado> findByCita_IdOrderByFechaAscIdAsc(Long citaId);
}
