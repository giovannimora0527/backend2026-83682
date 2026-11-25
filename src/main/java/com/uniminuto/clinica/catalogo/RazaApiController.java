package com.uniminuto.clinica.catalogo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controlador REST del catálogo de razas: implementa {@link RazaApi} y delega en {@link RazaService}. */
@RestController
public class RazaApiController implements RazaApi {

    /** Reglas del catálogo de razas. */
    private final RazaService servicio;

    /**
     * @param servicio servicio de razas.
     */
    public RazaApiController(RazaService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<RazaDto>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<RazaDto> guardar(RazaRq datos) {
        return ResponseEntity.ok(servicio.crear(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<RazaDto> actualizar(int id, RazaRq datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> eliminar(int id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
