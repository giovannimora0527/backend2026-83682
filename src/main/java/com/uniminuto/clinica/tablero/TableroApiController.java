package com.uniminuto.clinica.tablero;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Controlador REST del tablero de pendientes: implementa {@link TableroApi} y delega en {@link TableroService}. */
@RestController
public class TableroApiController implements TableroApi {

    /** Servicio que calcula los pendientes. */
    private final TableroService servicio;

    /**
     * @param servicio servicio del tablero.
     */
    public TableroApiController(TableroService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> pendientes() {
        return ResponseEntity.ok(servicio.pendientes());
    }
}
