package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Anotacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface AnotacionRepository extends JpaRepository<Anotacion, Long>, JpaSpecificationExecutor<Anotacion> {
    List<Anotacion> findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(Long historiaClinicaId);
}
