
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE CITAS MÉDICAS
// ==========================================================

// Entidad que representa la tabla cita.
import com.uniminuto.clinica.entity.Cita;

// Herramientas de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Manejo de fechas y horas.
import java.time.LocalDateTime;

// Manejo de listas.
import java.util.List;

/**
 * Repositorio encargado de consultar las citas
 * médicas almacenadas en MySQL.
 *
 * Base de datos: clinica
 * Tabla: cita
 *
 * Proporciona operaciones básicas de JPA:
 *
 * - save()
 * - findAll()
 * - findById()
 * - existsById()
 * - deleteById()
 *
 * Además, contiene consultas personalizadas
 * para el segundo parcial de Programación Web.
 */
@Repository
public interface CitaRepository
        extends JpaRepository<Cita, Long> {

    // ======================================================
    // 1. LISTAR TODAS LAS CITAS ORDENADAS
    // ======================================================

    /**
     * Obtiene todas las citas médicas.
     *
     * Orden:
     * Fecha y hora descendente.
     *
     * La cita más reciente aparece primero.
     *
     * @return Lista de citas ordenadas.
     */
    List<Cita> findAllByOrderByFechaHoraDesc();


    // ======================================================
    // 2. BUSCAR CITAS ENTRE DOS FECHAS
    // ======================================================

    /**
     * Busca las citas comprendidas entre
     * una fecha inicial y una fecha final.
     *
     * Conservamos este método del
     * repositorio original.
     *
     * @param fechaInicial Fecha inicial.
     * @param fechaFinal Fecha final.
     * @return Citas ordenadas de mayor a menor fecha.
     */
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );


    // ======================================================
    // 3. CONSULTAR CITAS DE UN CLIENTE
    // ======================================================

    /**
     * Obtiene las citas asociadas a un cliente.
     *
     * @param clienteId Identificador del cliente.
     * @return Citas del cliente ordenadas por fecha.
     */
    List<Cita> findByClienteIdOrderByFechaHoraDesc(
            Integer clienteId
    );


    // ======================================================
    // 4. CONSULTAR CITAS DE UNA MASCOTA
    // ======================================================

    /**
     * Obtiene las citas médicas de una mascota.
     *
     * @param mascotaId Identificador de la mascota.
     * @return Lista de citas de la mascota.
     */
    List<Cita> findByMascotaIdOrderByFechaHoraDesc(
            Integer mascotaId
    );


    // ======================================================
    // 5. CONSULTAR CITAS DE UN VETERINARIO
    // ======================================================

    /**
     * Obtiene las citas asignadas a un médico.
     *
     * @param medicoId Identificador del veterinario.
     * @return Citas del médico ordenadas por fecha.
     */
    List<Cita> findByMedicoIdOrderByFechaHoraDesc(
            Integer medicoId
    );


    // ======================================================
    // 6. VALIDAR DISPONIBILIDAD DEL VETERINARIO
    // ======================================================

    /**
     * Cuenta las citas activas de un veterinario
     * dentro de un intervalo de tiempo.
     *
     * REGLA DE NEGOCIO:
     *
     * Un veterinario no debe tener otra cita
     * dentro del margen de 15 minutos antes
     * o después del horario solicitado.
     *
     * El servicio calculará las fechas:
     *
     * inicio = fechaSolicitada.minusMinutes(15)
     * fin = fechaSolicitada.plusMinutes(15)
     *
     * Las citas canceladas no bloquean el horario.
     *
     * Al editar una cita, su propio ID podrá
     * excluirse de la búsqueda.
     *
     * @param medicoId Identificador del veterinario.
     * @param inicio Inicio del intervalo.
     * @param fin Final del intervalo.
     * @param idExcluir Cita que debe excluirse.
     *                  Usar 0L al crear una cita.
     * @return Cantidad de citas que generan conflicto.
     */
    @Query("""
        SELECT COUNT(c)
        FROM Cita c
        WHERE c.medicoId = :medicoId
          AND c.fechaHora BETWEEN :inicio AND :fin
          AND LOWER(c.estado) <> 'cancelada'
          AND c.id <> :idExcluir
        """)
    long contarConflictosMedico(
            @Param("medicoId") Integer medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("idExcluir") Long idExcluir
    );


    // ======================================================
    // 7. EVITAR DOS CITAS SIMULTÁNEAS DE UNA MASCOTA
    // ======================================================

    /**
     * Comprueba si una mascota tiene otra cita
     * activa exactamente en el mismo horario.
     *
     * No se consideran las citas canceladas.
     *
     * El ID de la cita actual puede excluirse
     * durante una actualización.
     *
     * @param mascotaId Identificador de la mascota.
     * @param fechaHora Fecha y hora solicitadas.
     * @param idExcluir ID que se debe excluir.
     *                  Usar 0L al crear.
     * @return Cantidad de conflictos encontrados.
     */
    @Query("""
        SELECT COUNT(c)
        FROM Cita c
        WHERE c.mascotaId = :mascotaId
          AND c.fechaHora = :fechaHora
          AND LOWER(c.estado) <> 'cancelada'
          AND c.id <> :idExcluir
        """)
    long contarConflictosMascota(
            @Param("mascotaId") Integer mascotaId,
            @Param("fechaHora") LocalDateTime fechaHora,
            @Param("idExcluir") Long idExcluir
    );

}
