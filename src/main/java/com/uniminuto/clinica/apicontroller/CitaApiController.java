
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR REST DE GESTIÓN DE CITAS MÉDICAS
// ==========================================================

// Interfaz que define los endpoints de citas.
import com.uniminuto.clinica.api.CitaApi;

// Entidad que representa la tabla cita.
import com.uniminuto.clinica.entity.Cita;

// Servicio que contiene las reglas de negocio.
import com.uniminuto.clinica.service.CitaService;

// Excepción utilizada en el proyecto.
import org.apache.coyote.BadRequestException;

// Permite construir respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Identifica el controlador REST.
import org.springframework.web.bind.annotation.RestController;

// Manejo de fechas y horas.
import java.time.LocalDateTime;

// Manejo de listas.
import java.util.List;

/**
 * ==========================================================
 * CLASE: CitaApiController
 * ==========================================================
 *
 * Controlador REST encargado de gestionar
 * las solicitudes HTTP del módulo de citas.
 *
 * Proyecto: Clínica Veterinaria - UNIMINUTO
 *
 * Ruta principal:
 *
 * /clinica/v1/cita
 *
 * ENDPOINTS:
 *
 * 1. GET  /filtrar-fechas
 * 2. POST /crear
 * 3. PUT  /actualizar/{id}
 * 4. GET  /listar
 * 5. GET  /consultar/{id}
 *
 * Este controlador recibe las solicitudes
 * del frontend Angular y utiliza CitaService
 * para procesar las operaciones.
 *
 * Las validaciones de clientes, mascotas,
 * veterinarios, estados y horarios
 * se realizan en CitaServiceImpl.
 */
@RestController
public class CitaApiController implements CitaApi {

    // ======================================================
    // 1. SERVICIO DE CITAS
    // ======================================================

    /**
     * Servicio utilizado para ejecutar
     * las operaciones de gestión de citas.
     */
    private final CitaService citaService;


    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Constructor utilizado para inyectar
     * el servicio de citas.
     *
     * Spring Boot proporciona automáticamente
     * la implementación de CitaService.
     *
     * @param citaService Servicio de citas.
     */
    public CitaApiController(CitaService citaService) {

        this.citaService = citaService;

    }


    // ======================================================
    // 3. FILTRAR CITAS POR FECHAS
    // ======================================================

    /**
     * Consulta citas dentro de un rango de fechas.
     *
     * Endpoint:
     *
     * GET /clinica/v1/cita/filtrar-fechas
     *
     * Las fechas son recibidas desde Angular
     * mediante parámetros de consulta.
     *
     * El servicio comprueba que el rango
     * de fechas sea válido.
     *
     * @param fechaInicial Fecha inicial de búsqueda.
     * @param fechaFinal Fecha final de búsqueda.
     * @return Respuesta HTTP con las citas encontradas.
     * @throws BadRequestException Si ocurre un error
     *         relacionado con la solicitud.
     */
    @Override
    public ResponseEntity<List<Cita>> filtrarCitasPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) throws BadRequestException {

        // Consultar las citas mediante el servicio.
        List<Cita> citasEncontradas =
                citaService.buscarCitasPorFechas(
                        fechaInicial,
                        fechaFinal
                );

        // Devolver HTTP 200 con los resultados.
        return ResponseEntity.ok(citasEncontradas);

    }


    // ======================================================
    // 4. CREAR UNA NUEVA CITA
    // ======================================================

    /**
     * Registra una nueva cita veterinaria.
     *
     * Endpoint:
     *
     * POST /clinica/v1/cita/crear
     *
     * El servicio valida:
     *
     * - Existencia del cliente.
     * - Existencia de la mascota.
     * - Relación entre cliente y mascota.
     * - Existencia del veterinario.
     * - Fecha y hora.
     * - Disponibilidad del horario.
     * - Estado permitido.
     *
     * @param cita Información de la cita.
     * @return Respuesta HTTP con la cita creada.
     */
    @Override
    public ResponseEntity<Cita> crearCita(Cita cita) {

        // Registrar la cita mediante el servicio.
        Cita nuevaCita = citaService.crearCita(cita);

        // Devolver la cita registrada.
        return ResponseEntity.ok(nuevaCita);

    }


    // ======================================================
    // 5. ACTUALIZAR UNA CITA
    // ======================================================

    /**
     * Actualiza la información de una cita.
     *
     * Endpoint:
     *
     * PUT /clinica/v1/cita/actualizar/{id}
     *
     * El servicio debe comprobar:
     *
     * - Que la cita exista.
     * - Que su estado permita modificaciones.
     * - Que las relaciones sean correctas.
     * - Que el horario esté disponible.
     *
     * @param id Identificador de la cita.
     * @param cita Datos actualizados.
     * @return Respuesta HTTP con la cita modificada.
     * @throws BadRequestException Si la cita no existe.
     */
    @Override
    public ResponseEntity<Cita> actualizarCita(
            Long id,
            Cita cita
    ) throws BadRequestException {

        // Solicitar la actualización al servicio.
        Cita citaActualizada =
                citaService.actualizarCita(id, cita);

        // Devolver el resultado.
        return ResponseEntity.ok(citaActualizada);

    }


    // ======================================================
    // 6. LISTAR TODAS LAS CITAS - NUEVO
    // ======================================================

    /**
     * Obtiene todas las citas registradas.
     *
     * Endpoint:
     *
     * GET /clinica/v1/cita/listar
     *
     * Las citas se ordenan por fecha y hora
     * descendente, mostrando primero
     * las más recientes.
     *
     * Este endpoint será utilizado
     * por el DataTable de Angular.
     *
     * @return Respuesta HTTP con todas las citas.
     */
    @Override
    public ResponseEntity<List<Cita>> listarCitas() {

        // Obtener todas las citas ordenadas.
        List<Cita> citas = citaService.listarCitas();

        // Devolver HTTP 200 y el listado.
        return ResponseEntity.ok(citas);

    }


    // ======================================================
    // 7. CONSULTAR UNA CITA POR ID - NUEVO
    // ======================================================

    /**
     * Consulta una cita mediante su identificador.
     *
     * Endpoint:
     *
     * GET /clinica/v1/cita/consultar/{id}
     *
     * Ejemplo:
     *
     * GET /clinica/v1/cita/consultar/1
     *
     * Si la cita existe:
     *
     * - Devuelve HTTP 200.
     * - Devuelve los datos de la cita.
     *
     * Si no existe:
     *
     * - El servicio devuelve HTTP 404.
     *
     * @param id Identificador de la cita.
     * @return Respuesta HTTP con la cita encontrada.
     */
    @Override
    public ResponseEntity<Cita> consultarCitaPorId(Long id) {

        // Buscar una cita mediante su ID.
        Cita citaEncontrada =
                citaService.consultarCitaPorId(id);

        // Devolver HTTP 200 y los datos.
        return ResponseEntity.ok(citaEncontrada);

    }

}
