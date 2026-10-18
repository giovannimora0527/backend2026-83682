
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API REST PARA LA GESTIÓN DE CITAS MÉDICAS
// ==========================================================

// Entidad de citas médicas.
import com.uniminuto.clinica.entity.Cita;

// Excepción utilizada en el proyecto.
import org.apache.coyote.BadRequestException;

// Formato de fechas.
import org.springframework.format.annotation.DateTimeFormat;

// Respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Anotaciones REST.
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Manejo de fechas.
import java.time.LocalDateTime;

// Listas de citas.
import java.util.List;

/**
 * ==========================================================
 * INTERFAZ: CitaApi
 * ==========================================================
 *
 * Define los endpoints REST del módulo
 * Gestión de Citas de la Clínica Veterinaria.
 *
 * Ruta principal:
 *
 * /clinica/v1/cita
 *
 * Endpoints:
 *
 * 1. GET  /filtrar-fechas
 * 2. POST /crear
 * 3. PUT  /actualizar/{id}
 * 4. GET  /listar
 * 5. GET  /consultar/{id}
 *
 * Las validaciones de negocio se realizan
 * en la capa de servicios.
 */
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*"
)
@RequestMapping("/cita")
public interface CitaApi {

    // ======================================================
    // 1. FILTRAR CITAS POR FECHAS
    // ======================================================

    /**
     * Busca citas dentro de un rango de fechas.
     *
     * Endpoint:
     * GET /cita/filtrar-fechas
     *
     * @param fechaInicial Fecha inicial.
     * @param fechaFinal Fecha final.
     * @return Lista de citas encontradas.
     * @throws BadRequestException Error de solicitud.
     */
    @GetMapping(
            value = "/filtrar-fechas",
            produces = "application/json"
    )
    ResponseEntity<List<Cita>> filtrarCitasPorFechas(

            @RequestParam("fechaInicial")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fechaInicial,

            @RequestParam("fechaFinal")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fechaFinal

    ) throws BadRequestException;


    // ======================================================
    // 2. CREAR UNA CITA
    // ======================================================

    /**
     * Registra una cita en MySQL.
     *
     * Endpoint:
     * POST /cita/crear
     *
     * @param cita Datos de la cita.
     * @return Cita registrada.
     */
    @PostMapping(
            value = "/crear",
            consumes = "application/json",
            produces = "application/json"
    )
    ResponseEntity<Cita> crearCita(
            @RequestBody Cita cita
    );


    // ======================================================
    // 3. ACTUALIZAR UNA CITA
    // ======================================================

    /**
     * Actualiza una cita existente.
     *
     * Endpoint:
     * PUT /cita/actualizar/{id}
     *
     * @param id Identificador de la cita.
     * @param cita Datos actualizados.
     * @return Cita modificada.
     * @throws BadRequestException Error de solicitud.
     */
    @PutMapping(
            value = "/actualizar/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    ResponseEntity<Cita> actualizarCita(

            @PathVariable("id") Long id,

            @RequestBody Cita cita

    ) throws BadRequestException;


    // ======================================================
    // 4. LISTAR TODAS LAS CITAS - NUEVO
    // ======================================================

    /**
     * Consulta todas las citas registradas.
     *
     * Las citas se ordenan desde la fecha
     * más reciente hasta la más antigua.
     *
     * Endpoint:
     * GET /cita/listar
     *
     * Este método permitirá mostrar
     * las citas en el DataTable de Angular.
     *
     * @return Lista completa de citas.
     */
    @GetMapping(
            value = "/listar",
            produces = "application/json"
    )
    ResponseEntity<List<Cita>> listarCitas();


    // ======================================================
    // 5. CONSULTAR UNA CITA POR ID - NUEVO
    // ======================================================

    /**
     * Busca una cita mediante su identificador.
     *
     * Endpoint:
     * GET /cita/consultar/{id}
     *
     * Ejemplo:
     * GET /cita/consultar/1
     *
     * Si el registro no existe, el servicio
     * devolverá un error HTTP 404.
     *
     * @param id Identificador de la cita.
     * @return Información de la cita encontrada.
     */
    @GetMapping(
            value = "/consultar/{id}",
            produces = "application/json"
    )
    ResponseEntity<Cita> consultarCitaPorId(
            @PathVariable("id") Long id
    );

}
