package com.uniminuto.clinica.clinica;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TarifarioApiController implements TarifarioApi {

    private final TarifarioService servicio;

    public TarifarioApiController(TarifarioService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> especialidades() {
        return ResponseEntity.ok(servicio.especialidades());
    }

    @Override
    public ResponseEntity<Map<String, Object>> crearEspecialidad(EspecialidadRq datos) {
        return ResponseEntity.ok(servicio.guardarEspecialidad(null, datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizarEspecialidad(long id, EspecialidadRq datos) {
        return ResponseEntity.ok(servicio.guardarEspecialidad(id, datos));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> servicios(long id) {
        return ResponseEntity.ok(servicio.servicios(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> agregarServicio(long id, ServicioRq datos) {
        return ResponseEntity.ok(servicio.agregarServicio(id, datos == null ? null : datos.nombre()));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> tarifas(long id) {
        return ResponseEntity.ok(servicio.tarifas(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> nuevaTarifa(long id, TarifaRq datos) {
        return ResponseEntity.ok(servicio.nuevaTarifa(id, datos));
    }
}
