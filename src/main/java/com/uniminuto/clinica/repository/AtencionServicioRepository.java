package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AtencionServicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface AtencionServicioRepository extends JpaRepository<AtencionServicio, Long> {

    @EntityGraph(attributePaths = "servicio")
    List<AtencionServicio> findByAtencion_IdOrderById(Long atencionId);

    @EntityGraph(attributePaths = "servicio")
    List<AtencionServicio> findByAtencion_IdInOrderById(Collection<Long> atencionIds);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM AtencionServicio s WHERE s.atencion.id = :atencionId")
    void eliminarPorAtencion(@Param("atencionId") Long atencionId);
}
