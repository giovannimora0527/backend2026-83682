package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/** Acceso a datos de {@link Cita}. */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /** Citas dentro del rango [inicio, fin], de la mas reciente a la mas antigua. */
    List<Cita> findByFechaCitaBetweenOrderByFechaCitaDesc(
            LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
