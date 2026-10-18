package co.edu.uniminuto.veterinaria.controller;

import co.edu.uniminuto.veterinaria.dto.FormulaActualizarRequest;
import co.edu.uniminuto.veterinaria.dto.FormulaDetalleResponse;
import co.edu.uniminuto.veterinaria.dto.FormulaFiltro;
import co.edu.uniminuto.veterinaria.dto.FormulaRequest;
import co.edu.uniminuto.veterinaria.dto.FormulaResumenResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.service.FormulaService;
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
 * Endpoints REST de fórmulas médicas. No existe DELETE: las fórmulas se inactivan (regla de negocio 8).
 * <ul>
 *   <li>GET   /api/formulas                 listar (vista resumida)</li>
 *   <li>GET   /api/formulas/{id}            detalle completo</li>
 *   <li>POST  /api/formulas                 crear</li>
 *   <li>PUT   /api/formulas/{id}            editar</li>
 *   <li>PATCH /api/formulas/{id}/inactivar  inactivar</li>
 *   <li>PATCH /api/formulas/{id}/activar    activar</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/formulas")
public class FormulaController {

    /** Servicio de fórmulas. */
    private final FormulaService formulaService;

    /**
     * Crea el controlador.
     *
     * @param formulaService servicio de fórmulas
     */
    public FormulaController(FormulaService formulaService) {
        this.formulaService = formulaService;
    }

    /**
     * Lista fórmulas (por defecto ordenadas por fecha de creación descendente).
     *
     * @param mascota       texto del nombre de la mascota
     * @param cliente       texto del nombre del cliente
     * @param medicamento   texto del nombre del medicamento
     * @param mascotaId     id de la mascota
     * @param veterinarioId id del veterinario
     * @param activo        true solo vigentes, false solo inactivas
     * @param desde         fecha de emisión inicial (yyyy-MM-dd)
     * @param hasta         fecha de emisión final (yyyy-MM-dd)
     * @param pagina        número de página (base 0)
     * @param tamano        registros por página
     * @param orden         columna de ordenamiento
     * @param direccion     asc o desc
     * @return página de fórmulas resumidas
     */
    @GetMapping
    public PageResponse<FormulaResumenResponse> listar(
            @RequestParam(required = false) String mascota,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String medicamento,
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String direccion) {
        FormulaFiltro filtro = new FormulaFiltro(mascota, cliente, medicamento, mascotaId, veterinarioId, activo,
                desde, hasta);
        return formulaService.listar(filtro, pagina, tamano, orden, direccion);
    }

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param id identificador de la fórmula
     * @return detalle de la fórmula
     */
    @GetMapping("/{id}")
    public FormulaDetalleResponse obtener(@PathVariable Long id) {
        return formulaService.obtener(id);
    }

    /**
     * Crea una fórmula.
     *
     * @param request datos de la fórmula
     * @return la fórmula creada con HTTP 201
     */
    @PostMapping
    public ResponseEntity<FormulaDetalleResponse> crear(@Valid @RequestBody FormulaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formulaService.crear(request));
    }

    /**
     * Edita una fórmula.
     *
     * @param id      identificador de la fórmula
     * @param request nuevos datos
     * @return la fórmula actualizada
     */
    @PutMapping("/{id}")
    public FormulaDetalleResponse actualizar(@PathVariable Long id,
                                             @Valid @RequestBody FormulaActualizarRequest request) {
        return formulaService.actualizar(id, request);
    }

    /**
     * Inactiva una fórmula (en lugar de eliminarla).
     *
     * @param id identificador de la fórmula
     * @return la fórmula inactiva
     */
    @PatchMapping("/{id}/inactivar")
    public FormulaDetalleResponse inactivar(@PathVariable Long id) {
        return formulaService.inactivar(id);
    }

    /**
     * Activa una fórmula inactiva.
     *
     * @param id identificador de la fórmula
     * @return la fórmula activa
     */
    @PatchMapping("/{id}/activar")
    public FormulaDetalleResponse activar(@PathVariable Long id) {
        return formulaService.activar(id);
    }
}
