package com.uniminuto.clinica.catalogo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controlador REST de especialidades: implementa {@link EspecializacionApi} y delega en {@link EspecializacionService}. */
@RestController
public class EspecializacionApiController implements EspecializacionApi {

    /** Consulta del catálogo de especialidades. */
    private final EspecializacionService servicio;

    /**
     * @param servicio servicio de especialidades.
     */
    public EspecializacionApiController(EspecializacionService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<EspecializacionDto>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }
}
