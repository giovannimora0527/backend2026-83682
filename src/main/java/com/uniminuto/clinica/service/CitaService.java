
package com.uniminuto.clinica.service;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// SERVICIO DE GESTIÓN DE CITAS MÉDICAS
// ==========================================================

// Entidad que representa las citas médicas.
import com.uniminuto.clinica.entity.Cita;

// Excepción utilizada por el backend actual.
import org.apache.coyote.BadRequestException;

// Manejo de fechas y horas.
import java.time.LocalDateTime;

// Manejo de listas.
import java.util.List;

/**
 * ==========================================================
 * INTERFAZ: CitaService
 * ==========================================================
 *
 * Define las operaciones de Gestión de Citas.
 *
 * La implementación se realizará en
 * CitaServiceImpl.java.
 *
 * FUNCIONALIDADES:
 *
 * 1. Buscar citas entre dos fechas.
 * 2. Crear una nueva cita.
 * 3. Actualizar una cita existente.
 * 4. Listar todas las citas.
 * 5. Consultar una cita por su ID.
 *
 * REGLAS DE NEGOCIO:
 *
 * - Validar que el cliente exista.
 * - Verificar que la mascota pertenezca al cliente.
 * - Comprobar que el veterinario exista.
 * - Evitar citas en fechas pasadas.
 * - Evitar conflictos de horarios.
 * - Respetar los estados permitidos.
 * - Impedir modificaciones indebidas
 *   en citas canceladas o atendidas.
 *
 * Las validaciones se implementarán
 * en la capa de servicios.
 */
public interface CitaService {

    // ======================================================
    // 1. BUSCAR CITAS POR RANGO DE FECHAS
    // ======================================================

    /**
     * Busca las citas comprendidas entre
     * una fecha inicial y una fecha final.
     *
     * @param fechaInicial Fecha inicial.
     * @param fechaFinal Fecha final.
     * @return Lista de citas ordenadas por fecha.
     */
    List<Cita> buscarCitasPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );


    // ======================================================
    // 2. CREAR UNA NUEVA CITA MÉDICA
    // ======================================================

    /**
     * Registra una cita en MySQL.
     *
     * La implementación deberá validar
     * los datos y la disponibilidad.
     *
     * @param cita Información de la nueva cita.
     * @return Cita registrada.
     */
    Cita crearCita(Cita cita);


    // ======================================================
    // 3. ACTUALIZAR UNA CITA EXISTENTE
    // ======================================================

    /**
     * Actualiza los datos permitidos
     * de una cita existente.
     *
     * @param id Identificador de la cita.
     * @param cita Datos actualizados.
     * @return Cita modificada.
     * @throws BadRequestException Si la
     *         solicitud es inválida.
     */
    Cita actualizarCita(
            Long id,
            Cita cita
    ) throws BadRequestException;


    // ======================================================
    // 4. LISTAR TODAS LAS CITAS MÉDICAS
    // ======================================================

    /**
     * Obtiene todas las citas de la clínica.
     *
     * Los resultados deben aparecer ordenados
     * desde la fecha más reciente hasta
     * la fecha más antigua.
     *
     * Este método se utilizará para
     * mostrar el listado en Angular.
     *
     * @return Lista de todas las citas.
     */
    List<Cita> listarCitas();


    // ======================================================
    // 5. CONSULTAR UNA CITA POR ID
    // ======================================================

    /**
     * Busca una cita mediante su identificador.
     *
     * La implementación deberá comprobar
     * que la cita exista en MySQL.
     *
     * Si no existe, deberá devolver
     * un error HTTP adecuado.
     *
     * @param id Identificador de la cita.
     * @return Información de la cita encontrada.
     */
    Cita consultarCitaPorId(Long id);

}
