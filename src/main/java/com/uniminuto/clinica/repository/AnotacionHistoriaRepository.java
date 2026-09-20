package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/** Acceso a datos de {@link AnotacionHistoria}. */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /** Todas las anotaciones, de la mas reciente a la mas antigua. */
    List<AnotacionHistoria> findAllByOrderByFechaAnotacionDesc();

    /** Anotaciones dentro del rango [inicio, fin], de la mas reciente a la mas antigua. */
    List<AnotacionHistoria> findByFechaAnotacionBetweenOrderByFechaAnotacionDesc(
            LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
