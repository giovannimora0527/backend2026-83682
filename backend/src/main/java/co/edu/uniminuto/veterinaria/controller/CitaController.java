package co.edu.uniminuto.veterinaria.controller;

import co.edu.uniminuto.veterinaria.dto.CitaActualizarRequest;
import co.edu.uniminuto.veterinaria.dto.CitaFiltro;
import co.edu.uniminuto.veterinaria.dto.CitaRequest;
import co.edu.uniminuto.veterinaria.dto.CitaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.EstadoCita;
import co.edu.uniminuto.veterinaria.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoints REST de citas.
 * <ul>
 *   <li>GET   /api/citas                 listar con filtros, orden y paginación</li>
 *   <li>GET   /api/citas/{id}            consultar una cita</li>
 *   <li>POST  /api/citas                 crear</li>
 *   <li>PUT   /api/citas/{id}            editar</li>
 *   <li>PATCH /api/citas/{id}/reactivar  reactivar una cita cancelada</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    /** Servicio de citas. */
    private final CitaService citaService;

    /**
     * Crea el controlador.
     *
     * @param citaService servicio de citas
     */
    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * Lista citas (por defecto ordenadas por fecha y hora descendente).
     *
     * @param cliente       texto del nombre del cliente
     * @param mascota       texto del nombre de la mascota
     * @param clienteId     id del cliente
     * @param mascotaId     id de la mascota
     * @param veterinarioId id del veterinario
     * @param estado        estado de la cita
     * @param desde         fecha inicial (yyyy-MM-dd)
     * @param hasta         fecha final (yyyy-MM-dd)
     * @param sinHistoria   solo citas sin historia clínica
     * @param pagina        número de página (base 0)
     * @param tamano        registros por página
     * @param orden         columna de ordenamiento
     * @param direccion     asc o desc
     * @return página de citas
     */
    @GetMapping
    public PageResponse<CitaResponse> listar(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String mascota,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Boolean sinHistoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String direccion) {
        CitaFiltro filtro = new CitaFiltro(cliente, mascota, clienteId, mascotaId, veterinarioId, estado, desde,
                hasta, sinHistoria);
        return citaService.listar(filtro, pagina, tamano, orden, direccion);
    }

    /**
     * Consulta una cita.
     *
     * @param id identificador de la cita
     * @return la cita
     */
    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }

    /**
     * Crea una cita.
     *
     * @param request datos de la cita
     * @return la cita creada con HTTP 201
     */
    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(request));
    }

    /**
     * Edita una cita.
     *
     * @param id      identificador de la cita
     * @param request nuevos datos
     * @return la cita actualizada
     */
    @PutMapping("/{id}")
    public CitaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CitaActualizarRequest request) {
        return citaService.actualizar(id, request);
    }

    /**
     * Reactiva una cita cancelada (operación explícita).
     *
     * @param id identificador de la cita
     * @return la cita reactivada
     */
    @PatchMapping("/{id}/reactivar")
    public CitaResponse reactivar(@PathVariable Long id) {
        return citaService.reactivar(id);
    }
}
