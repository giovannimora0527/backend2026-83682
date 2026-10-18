package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.Cita;
import co.edu.uniminuto.veterinaria.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * Acceso a datos de citas, incluidas las consultas de cruce de horarios.
 */
public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    /**
     * Cuenta las citas de un veterinario (no canceladas) dentro de un rango de fechas.
     *
     * @param veterinarioId veterinario a consultar
     * @param excluido      estado que no se considera (CANCELADA)
     * @param excluirId     cita que se ignora (la que se está editando; -1 si no aplica)
     * @param desde         inicio del rango (inclusive)
     * @param hasta         fin del rango (inclusive)
     * @return cantidad de citas que se cruzan
     */
    @Query("select count(c) from Cita c where c.veterinario.id = :veterinarioId and c.estado <> :excluido "
            + "and c.id <> :excluirId and c.fechaHora between :desde and :hasta")
    long contarCrucesVeterinario(@Param("veterinarioId") Long veterinarioId,
                                 @Param("excluido") EstadoCita excluido,
                                 @Param("excluirId") Long excluirId,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta);

    /**
     * Cuenta las citas de una mascota (no canceladas) dentro de un rango de fechas.
     *
     * @param mascotaId mascota a consultar
     * @param excluido  estado que no se considera (CANCELADA)
     * @param excluirId cita que se ignora (la que se está editando; -1 si no aplica)
     * @param desde     inicio del rango (inclusive)
     * @param hasta     fin del rango (inclusive)
     * @return cantidad de citas que se cruzan
     */
    @Query("select count(c) from Cita c where c.mascota.id = :mascotaId and c.estado <> :excluido "
            + "and c.id <> :excluirId and c.fechaHora between :desde and :hasta")
    long contarCrucesMascota(@Param("mascotaId") Long mascotaId,
                             @Param("excluido") EstadoCita excluido,
                             @Param("excluirId") Long excluirId,
                             @Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta);
}
