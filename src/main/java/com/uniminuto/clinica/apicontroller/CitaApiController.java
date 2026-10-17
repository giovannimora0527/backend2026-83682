
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controlador REST encargado de gestionar las citas veterinarias.
 * Permite listar, consultar, crear y actualizar citas.
 */
@RestController
@RequestMapping("/citas")
public class CitaApiController {

    /**
     * Servicio encargado de ejecutar las operaciones de las citas.
     */
    private final CitaService service;

    /**
     * Construye el controlador e inyecta el servicio de citas.
     *
     * @param service servicio encargado de gestionar las citas
     */
    public CitaApiController(CitaService service) {
        this.service = service;
    }

    /**
     * Lista las citas aplicando paginación, ordenamiento y filtros opcionales.
     *
     * @param page número de página que se consulta
     * @param size cantidad de registros por página
     * @param sort campo por el que se ordenan los resultados
     * @param direction dirección del ordenamiento
     * @param clienteId identificador del cliente para filtrar
     * @param mascotaId identificador de la mascota para filtrar
     * @param veterinarioId identificador del veterinario para filtrar
     * @param fecha fecha utilizada para filtrar las citas
     * @param estado estado de la cita para filtrar
     * @return respuesta HTTP con la página de citas encontradas
     */
    @GetMapping
    public ResponseEntity<PageResponse<CitaResponse>> listar(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) String sort,
            @RequestParam(defaultValue="DESC") String direction,
            @RequestParam(required=false) Long clienteId,
            @RequestParam(required=false) Integer mascotaId,
            @RequestParam(required=false) Long veterinarioId,
            @RequestParam(required=false)
            @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required=false) EstadoCita estado) {

        return ResponseEntity.ok(
                service.listar(page, size, sort, direction,
                        clienteId, mascotaId, veterinarioId, fecha, estado));
    }

    /**
     * Consulta una cita por su identificador.
     *
     * @param id identificador de la cita que se desea consultar
     * @return respuesta HTTP con los datos de la cita
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(service.consultar(id));
    }

    /**
     * Registra una nueva cita veterinaria.
     *
     * @param request datos de la cita que se va a registrar
     * @return respuesta HTTP con estado 201 y los datos de la cita creada
     */
    @PostMapping
    public ResponseEntity<CitaResponse> crear(@RequestBody CitaRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    /**
     * Actualiza una cita existente.
     *
     * @param id identificador de la cita que se va a actualizar
     * @param request nuevos datos de la cita
     * @return respuesta HTTP con los datos de la cita actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<CitaResponse> actualizar(
            @PathVariable Long id,
            @RequestBody CitaRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }
}
