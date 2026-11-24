package com.uniminuto.clinica.historia;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST de historias clínicas, anotaciones y fórmulas. Solo delega en {@link HistoriaService}.
 */
@RestController
public class HistoriaApiController implements HistoriaApi {

    /** Reglas de negocio del módulo. */
    private final HistoriaService servicio;

    /**
     * @param servicio servicio de historias clínicas.
     */
    public HistoriaApiController(HistoriaService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> historias(String fechaInicio, String fechaFin, Integer mascotaId,
                                                               Long clienteId) {
        return ResponseEntity.ok(servicio.listarHistorias(fechaInicio, fechaFin, mascotaId, clienteId));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> historiasPorPaciente(int pacienteId) {
        return ResponseEntity.ok(servicio.listarHistorias(null, null, pacienteId, null));
    }

    @Override
    public ResponseEntity<Map<String, Object>> historia(long id) {
        return ResponseEntity.ok(servicio.detalleHistoria(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> crearHistoria(HistoriaRq datos) {
        return ResponseEntity.status(201).body(servicio.crearHistoria(datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizarHistoria(long id, HistoriaRq datos) {
        return ResponseEntity.ok(servicio.actualizarHistoria(id, datos));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> anotacionesDeHistoria(long id) {
        return ResponseEntity.ok(servicio.anotacionesDeHistoria(id));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> anotaciones(String fechaInicio, String fechaFin, Long historiaId) {
        return ResponseEntity.ok(servicio.listarAnotaciones(fechaInicio, fechaFin, historiaId));
    }

    @Override
    public ResponseEntity<Map<String, Object>> anotacion(long id) {
        return ResponseEntity.ok(servicio.anotacion(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> crearAnotacion(AnotacionRq datos) {
        return ResponseEntity.status(201).body(servicio.crearAnotacion(datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizarAnotacion(long id, AnotacionRq datos) {
        return ResponseEntity.ok(servicio.actualizarAnotacion(id, datos));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> formulas() {
        return ResponseEntity.ok(servicio.listarFormulas());
    }

    @Override
    public ResponseEntity<Map<String, Object>> formula(long id) {
        return ResponseEntity.ok(servicio.formula(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> crearFormula(FormulaRq datos) {
        return ResponseEntity.status(201).body(servicio.crearFormula(datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizarFormula(long id, FormulaRq datos) {
        return ResponseEntity.ok(servicio.actualizarFormula(id, datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> estadoFormula(long id, EstadoRq datos) {
        return ResponseEntity.ok(servicio.cambiarEstadoFormula(id, datos == null ? null : datos.activo()));
    }
}
