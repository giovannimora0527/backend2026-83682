package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link Cita}.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca todas las citas cuya fecha este entre {@code fechaInicio} y
     * {@code fechaFin} (ambas incluidas), ordenadas desde la mas reciente
     * hasta la mas antigua.
     *
     * <p>Spring Data JPA es capaz de generar la consulta SQL automaticamente
     * solo leyendo el nombre del metodo: "findBy" + "Fecha" + "Between" +
     * "OrderByFechaDesc" se traduce en un
     * {@code SELECT * FROM cita WHERE fecha BETWEEN ?1 AND ?2 ORDER BY fecha DESC}.
     * No es necesario escribir SQL a mano.</p>
     *
     * @param fechaInicio fecha y hora desde la cual buscar (inclusive)
     * @param fechaFin    fecha y hora hasta la cual buscar (inclusive)
     * @return lista de citas encontradas, ordenadas de la mas reciente a la mas antigua
     */
    List<Cita> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
