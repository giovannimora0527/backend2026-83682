package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.dto.AnotacionHistoriaDTO;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de AnotacionHistoria.
 *
 * <p>Expone los servicios de crear, listar y actualizar anotaciones,
 * pedidos en el Requerimiento 5 del taller.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/anotaciones")
public class AnotacionHistoriaController implements AnotacionHistoriaApi {

    /** Servicio con la logica de negocio de AnotacionHistoria. */
    private final AnotacionHistoriaService anotacionHistoriaService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param anotacionHistoriaService servicio de anotaciones de historia medica
     */
    public AnotacionHistoriaController(AnotacionHistoriaService anotacionHistoriaService) {
        this.anotacionHistoriaService = anotacionHistoriaService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code POST /api/anotaciones}.</p>
     */
    @Override
    @PostMapping
    public ResponseEntity<AnotacionHistoriaDTO> crearAnotacion(@Valid @RequestBody AnotacionHistoriaDTO anotacionDTO) {
        AnotacionHistoriaDTO creada = anotacionHistoriaService.crearAnotacion(anotacionDTO);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code GET /api/anotaciones/historia/{historiaMedicaId}}.</p>
     */
    @Override
    @GetMapping("/historia/{historiaMedicaId}")
    public ResponseEntity<List<AnotacionHistoriaDTO>> listarAnotacionesPorHistoria(@PathVariable Long historiaMedicaId) {
        return ResponseEntity.ok(anotacionHistoriaService.listarAnotacionesPorHistoria(historiaMedicaId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code PUT /api/anotaciones/{id}}.</p>
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AnotacionHistoriaDTO> actualizarAnotacion(@PathVariable Long id,
                                                                      @RequestBody AnotacionHistoriaDTO anotacionDTO) {
        return ResponseEntity.ok(anotacionHistoriaService.actualizarAnotacion(id, anotacionDTO));
    }
}
