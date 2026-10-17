package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.dto.HistoriaMedicaDTO;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST de HistoriaMedica.
 *
 * <p>Expone el CRUD completo pedido en el Requerimiento 5 del taller.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/historias")
public class HistoriaMedicaController implements HistoriaMedicaApi {

    /** Servicio con la logica de negocio de HistoriaMedica. */
    private final HistoriaMedicaService historiaMedicaService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param historiaMedicaService servicio de historias medicas
     */
    public HistoriaMedicaController(HistoriaMedicaService historiaMedicaService) {
        this.historiaMedicaService = historiaMedicaService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code POST /api/historias}.</p>
     */
    @Override
    @PostMapping
    public ResponseEntity<HistoriaMedicaDTO> crearHistoriaMedica(@Valid @RequestBody HistoriaMedicaDTO historiaMedicaDTO) {
        HistoriaMedicaDTO creada = historiaMedicaService.crearHistoriaMedica(historiaMedicaDTO);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code GET /api/historias}. Si se envian los
     * parametros opcionales {@code fechaInicio} y {@code fechaFin}, filtra
     * el resultado por ese rango de fechas (Requerimiento 5); si no se
     * envian, devuelve todas las historias medicas.</p>
     */
    @Override
    @GetMapping
    public ResponseEntity<List<HistoriaMedicaDTO>> listarHistoriasMedicas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        // Si el usuario envio las dos fechas, filtramos; si no, listamos todo.
        if (fechaInicio != null && fechaFin != null) {
            return ResponseEntity.ok(historiaMedicaService.listarHistoriasMedicasPorFecha(fechaInicio, fechaFin));
        }
        return ResponseEntity.ok(historiaMedicaService.listarHistoriasMedicas());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code GET /api/historias/{id}}.</p>
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<HistoriaMedicaDTO> obtenerHistoriaMedicaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historiaMedicaService.obtenerHistoriaMedicaPorId(id));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code PUT /api/historias/{id}}.</p>
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<HistoriaMedicaDTO> actualizarHistoriaMedica(@PathVariable Long id,
                                                                       @RequestBody HistoriaMedicaDTO historiaMedicaDTO) {
        return ResponseEntity.ok(historiaMedicaService.actualizarHistoriaMedica(id, historiaMedicaDTO));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code DELETE /api/historias/{id}}.</p>
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHistoriaMedica(@PathVariable Long id) {
        historiaMedicaService.eliminarHistoriaMedica(id);
        return ResponseEntity.noContent().build();
    }
}
