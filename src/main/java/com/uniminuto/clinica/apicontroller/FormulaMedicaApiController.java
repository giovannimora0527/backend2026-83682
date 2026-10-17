
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controlador REST encargado de gestionar las fórmulas médicas.
 * Permite listar, consultar, crear y actualizar fórmulas.
 */
@RestController
@RequestMapping("/formulas-medicas")
public class FormulaMedicaApiController {

    /**
     * Servicio que ejecuta las operaciones de las fórmulas médicas.
     */
    private final FormulaMedicaService service;

    /**
     * Construye el controlador e inyecta el servicio de fórmulas médicas.
     *
     * @param service servicio encargado de gestionar las fórmulas médicas
     */
    public FormulaMedicaApiController(FormulaMedicaService service) {
        this.service = service;
    }

    /**
     * Lista las fórmulas médicas con paginación, ordenamiento y filtros opcionales.
     *
     * @param page número de página que se consulta
     * @param size cantidad de registros por página
     * @param sort campo por el que se ordenan los resultados
     * @param direction dirección del ordenamiento
     * @param clienteId identificador del cliente para filtrar
     * @param mascotaId identificador de la mascota para filtrar
     * @param veterinarioId identificador del veterinario para filtrar
     * @param medicamento nombre o texto del medicamento para filtrar
     * @param fecha fecha de emisión para filtrar las fórmulas
     * @return respuesta HTTP con la página de fórmulas encontradas
     */
    @GetMapping
    public ResponseEntity<PageResponse<FormulaResumenResponse>> listar(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) String sort,
            @RequestParam(defaultValue="DESC") String direction,
            @RequestParam(required=false) Long clienteId,
            @RequestParam(required=false) Integer mascotaId,
            @RequestParam(required=false) Long veterinarioId,
            @RequestParam(required=false) String medicamento,
            @RequestParam(required=false)
            @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(service.listar(page, size, sort, direction,
                clienteId, mascotaId, veterinarioId, medicamento, fecha));
    }

    /**
     * Consulta una fórmula médica por su identificador.
     *
     * @param id identificador de la fórmula médica
     * @return respuesta HTTP con los datos de la fórmula consultada
     */
    @GetMapping("/{id}")
    public ResponseEntity<FormulaResponse> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(service.consultar(id));
    }

    /**
     * Registra una nueva fórmula médica.
     *
     * @param request datos de la fórmula que se va a registrar
     * @return respuesta HTTP con estado 201 y la fórmula creada
     */
    @PostMapping
    public ResponseEntity<FormulaResponse> crear(@RequestBody FormulaRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }

    /**
     * Actualiza una fórmula médica existente.
     *
     * @param id identificador de la fórmula que se va a actualizar
     * @param request nuevos datos de la fórmula médica
     * @return respuesta HTTP con los datos actualizados
     */
    @PutMapping("/{id}")
    public ResponseEntity<FormulaResponse> actualizar(
            @PathVariable Long id,
            @RequestBody FormulaRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }
}
