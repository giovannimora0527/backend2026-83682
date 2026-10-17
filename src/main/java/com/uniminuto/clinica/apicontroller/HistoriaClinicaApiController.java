
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controlador REST encargado de consultar y registrar historias clínicas
 * y de exponer las anotaciones asociadas a cada historia.
 */
@RestController
@RequestMapping("/historias-clinicas")
public class HistoriaClinicaApiController {

    /** Servicio encargado de gestionar las historias clínicas. */
    private final HistoriaClinicaService service;

    /** Servicio encargado de consultar las anotaciones clínicas. */
    private final AnotacionService anotaciones;

    /**
     * Construye el controlador con los servicios requeridos.
     *
     * @param service servicio de historias clínicas
     * @param anotaciones servicio de anotaciones clínicas
     */
    public HistoriaClinicaApiController(
            HistoriaClinicaService service,
            AnotacionService anotaciones) {
        this.service = service;
        this.anotaciones = anotaciones;
    }

    /**
     * Lista las historias clínicas con paginación, ordenamiento y filtros opcionales.
     *
     * @param page número de página que se consulta
     * @param size cantidad de registros por página
     * @param sort campo por el que se ordenan los resultados
     * @param direction dirección del ordenamiento
     * @param clienteId identificador del cliente para filtrar
     * @param mascotaId identificador de la mascota para filtrar
     * @param veterinarioId identificador del veterinario para filtrar
     * @param fecha fecha de la historia clínica para filtrar
     * @return respuesta HTTP con la página de historias clínicas
     */
    @GetMapping
    public ResponseEntity<PageResponse<HistoriaResumenResponse>> listar(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) String sort,
            @RequestParam(defaultValue="DESC") String direction,
            @RequestParam(required=false) Long clienteId,
            @RequestParam(required=false) Integer mascotaId,
            @RequestParam(required=false) Long veterinarioId,
            @RequestParam(required=false)
            @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(
                service.listar(page, size, sort, direction,
                        clienteId, mascotaId, veterinarioId, fecha));
    }

    /**
     * Consulta una historia clínica y obtiene sus anotaciones más recientes.
     *
     * @param id identificador de la historia clínica
     * @return respuesta HTTP con el detalle de la historia y sus anotaciones
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistoriaDetalleResponse> consultar(@PathVariable Long id) {
        HistoriaResponse historia = service.consultar(id);
        PageResponse<AnotacionResponse> p =
                anotaciones.listarHistoria(id, 0, 20, "fechaHora", "DESC");
        return ResponseEntity.ok(new HistoriaDetalleResponse(historia, p));
    }

    /**
     * Registra una nueva historia clínica.
     *
     * @param request datos de la historia clínica que se va a registrar
     * @return respuesta HTTP con estado 201 y la historia creada
     */
    @PostMapping
    public ResponseEntity<HistoriaResponse> crear(@RequestBody HistoriaRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    /**
     * Lista las anotaciones asociadas a una historia clínica.
     *
     * @param id identificador de la historia clínica
     * @param page número de página que se consulta
     * @param size cantidad de anotaciones por página
     * @param sort campo por el que se ordenan las anotaciones
     * @param direction dirección del ordenamiento
     * @return respuesta HTTP con la página de anotaciones
     */
    @GetMapping("/{id}/anotaciones")
    public ResponseEntity<PageResponse<AnotacionResponse>> anotaciones(
            @PathVariable Long id,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) String sort,
            @RequestParam(defaultValue="DESC") String direction) {

        return ResponseEntity.ok(
                anotaciones.listarHistoria(id, page, size, sort, direction));
    }
}
