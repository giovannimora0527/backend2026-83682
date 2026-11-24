package com.uniminuto.clinica.clinica;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link TarifarioApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class TarifarioApiController implements TarifarioApi {

    /** Dependencia inyectada por constructor: servicio. */
    private final TarifarioService servicio;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param servicio dependencia servicio.
     */
    public TarifarioApiController(TarifarioService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> especialidades() {
        return ResponseEntity.ok(servicio.especialidades());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> crearEspecialidad(EspecialidadRq datos) {
        return ResponseEntity.ok(servicio.guardarEspecialidad(null, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> actualizarEspecialidad(long id, EspecialidadRq datos) {
        return ResponseEntity.ok(servicio.guardarEspecialidad(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> servicios(long id) {
        return ResponseEntity.ok(servicio.servicios(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> agregarServicio(long id, ServicioRq datos) {
        return ResponseEntity.ok(servicio.agregarServicio(id, datos == null ? null : datos.nombre()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> tarifas(long id) {
        return ResponseEntity.ok(servicio.tarifas(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> nuevaTarifa(long id, TarifaRq datos) {
        return ResponseEntity.ok(servicio.nuevaTarifa(id, datos));
    }
}
