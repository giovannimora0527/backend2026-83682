
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.AnotacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controlador REST encargado de gestionar las anotaciones clínicas.
 * Permite listar, consultar y crear anotaciones mediante solicitudes HTTP.
 */
@RestController
@RequestMapping("/anotaciones")
public class AnotacionApiController {

    /**
     * Servicio encargado de ejecutar las operaciones sobre las anotaciones clínicas.
     */
    private final AnotacionService service;

    /**
     * Construye el controlador e inyecta el servicio de anotaciones.
     *
     * @param service servicio que gestiona las anotaciones clínicas
     */
    public AnotacionApiController(AnotacionService service) {
        this.service = service;
    }

    /**
     * Lista las anotaciones clínicas con paginación y filtros opcionales.
     *
     * @param page número de página que se consulta
     * @param size cantidad de registros por página
     * @param sort campo por el que se ordenan los resultados
     * @param direction dirección del ordenamiento
     * @param historiaClinicaId identificador de la historia clínica para filtrar
     * @param veterinarioId identificador del veterinario para filtrar
     * @param fecha fecha de las anotaciones que se desean consultar
     * @return respuesta HTTP con la página de anotaciones encontradas
     */
    @GetMapping
    public ResponseEntity<PageResponse<AnotacionResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long historiaClinicaId,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(
                service.listar(page, size, sort, direction,
                        historiaClinicaId, veterinarioId, fecha));
    }

    /**
     * Consulta una anotación clínica por su identificador.
     *
     * @param id identificador de la anotación que se desea consultar
     * @return respuesta HTTP con los datos de la anotación
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnotacionResponse> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(service.consultar(id));
    }

    /**
     * Registra una nueva anotación clínica.
     *
     * @param request datos de la anotación que se va a registrar
     * @return respuesta HTTP con estado 201 y los datos de la anotación creada
     */
    @PostMapping
    public ResponseEntity<AnotacionResponse> crear(
            @RequestBody AnotacionRequest request) {

        return ResponseEntity.status(201).body(service.crear(request));
    }
}
