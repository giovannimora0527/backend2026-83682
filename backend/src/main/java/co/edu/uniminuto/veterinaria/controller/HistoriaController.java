package co.edu.uniminuto.veterinaria.controller;

import co.edu.uniminuto.veterinaria.dto.AnotacionFiltro;
import co.edu.uniminuto.veterinaria.dto.AnotacionRequest;
import co.edu.uniminuto.veterinaria.dto.AnotacionResponse;
import co.edu.uniminuto.veterinaria.dto.HistoriaFiltro;
import co.edu.uniminuto.veterinaria.dto.HistoriaRequest;
import co.edu.uniminuto.veterinaria.dto.HistoriaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.EstadoHistoria;
import co.edu.uniminuto.veterinaria.service.AnotacionService;
import co.edu.uniminuto.veterinaria.service.HistoriaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Endpoints REST de historias clínicas y sus anotaciones.
 * <ul>
 *   <li>GET   /api/historias                              listar</li>
 *   <li>GET   /api/historias/{id}                         consultar</li>
 *   <li>POST  /api/historias                              crear</li>
 *   <li>PATCH /api/historias/{id}/cerrar                  cerrar</li>
 *   <li>GET   /api/historias/{id}/anotaciones             listar anotaciones</li>
 *   <li>GET   /api/historias/{id}/anotaciones/{anotId}    detalle de una anotación</li>
 *   <li>POST  /api/historias/{id}/anotaciones             crear anotación</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/historias")
public class HistoriaController {

    /** Servicio de historias clínicas. */
    private final HistoriaService historiaService;

    /** Servicio de anotaciones. */
    private final AnotacionService anotacionService;

    /**
     * Crea el controlador.
     *
     * @param historiaService  servicio de historias clínicas
     * @param anotacionService servicio de anotaciones
     */
    public HistoriaController(HistoriaService historiaService, AnotacionService anotacionService) {
        this.historiaService = historiaService;
        this.anotacionService = anotacionService;
    }

    /**
     * Lista historias clínicas (por defecto ordenadas por fecha y hora descendente).
     *
     * @param cliente       texto del nombre del cliente
     * @param mascota       texto del nombre de la mascota
     * @param motivo        texto del motivo de consulta
     * @param clienteId     id del cliente
     * @param mascotaId     id de la mascota
     * @param veterinarioId id del veterinario
     * @param estado        estado de la historia
     * @param desde         fecha inicial (yyyy-MM-dd)
     * @param hasta         fecha final (yyyy-MM-dd)
     * @param pagina        número de página (base 0)
     * @param tamano        registros por página
     * @param orden         columna de ordenamiento
     * @param direccion     asc o desc
     * @return página de historias
     */
    @GetMapping
    public PageResponse<HistoriaResponse> listar(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String mascota,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) EstadoHistoria estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String direccion) {
        HistoriaFiltro filtro = new HistoriaFiltro(cliente, mascota, motivo, clienteId, mascotaId, veterinarioId,
                estado, desde, hasta);
        return historiaService.listar(filtro, pagina, tamano, orden, direccion);
    }

    /**
     * Consulta una historia clínica.
     *
     * @param id identificador de la historia
     * @return la historia
     */
    @GetMapping("/{id}")
    public HistoriaResponse obtener(@PathVariable Long id) {
        return historiaService.obtener(id);
    }

    /**
     * Crea una historia clínica.
     *
     * @param request datos de la historia
     * @return la historia creada con HTTP 201
     */
    @PostMapping
    public ResponseEntity<HistoriaResponse> crear(@Valid @RequestBody HistoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.crear(request));
    }

    /**
     * Cierra una historia clínica.
     *
     * @param id identificador de la historia
     * @return la historia cerrada
     */
    @PatchMapping("/{id}/cerrar")
    public HistoriaResponse cerrar(@PathVariable Long id) {
        return historiaService.cerrar(id);
    }

    /**
     * Lista las anotaciones de una historia (por defecto de la más reciente a la más antigua).
     *
     * @param id            identificador de la historia
     * @param texto         texto contenido en descripción, observación o diagnóstico/evolución
     * @param veterinarioId id del veterinario
     * @param desde         fecha inicial (yyyy-MM-dd)
     * @param hasta         fecha final (yyyy-MM-dd)
     * @param pagina        número de página (base 0)
     * @param tamano        registros por página
     * @param orden         columna de ordenamiento
     * @param direccion     asc o desc
     * @return página de anotaciones
     */
    @GetMapping("/{id}/anotaciones")
    public PageResponse<AnotacionResponse> listarAnotaciones(
            @PathVariable Long id,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamano,
            @RequestParam(required = false) String orden,
            @RequestParam(required = false) String direccion) {
        AnotacionFiltro filtro = new AnotacionFiltro(texto, veterinarioId, desde, hasta);
        return anotacionService.listar(id, filtro, pagina, tamano, orden, direccion);
    }

    /**
     * Consulta el detalle de una anotación.
     *
     * @param id          identificador de la historia
     * @param anotacionId identificador de la anotación
     * @return la anotación
     */
    @GetMapping("/{id}/anotaciones/{anotacionId}")
    public AnotacionResponse obtenerAnotacion(@PathVariable Long id, @PathVariable Long anotacionId) {
        return anotacionService.obtener(id, anotacionId);
    }

    /**
     * Agrega una anotación a una historia.
     *
     * @param id      identificador de la historia
     * @param request datos de la anotación
     * @return la anotación creada con HTTP 201
     */
    @PostMapping("/{id}/anotaciones")
    public ResponseEntity<AnotacionResponse> crearAnotacion(@PathVariable Long id,
                                                            @Valid @RequestBody AnotacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(anotacionService.crear(id, request));
    }
}
