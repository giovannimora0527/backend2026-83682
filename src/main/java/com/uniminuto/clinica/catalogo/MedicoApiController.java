package com.uniminuto.clinica.catalogo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controlador REST de médicos: implementa {@link MedicoApi} y delega en {@link MedicoService}. */
@RestController
public class MedicoApiController implements MedicoApi {

    /** Reglas de negocio de médicos. */
    private final MedicoService servicio;

    /**
     * @param servicio servicio de médicos.
     */
    public MedicoApiController(MedicoService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MedicoDto>> listar() {
        return ResponseEntity.ok(servicio.listar(false));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MedicoDto>> disponibles() {
        return ResponseEntity.ok(servicio.listar(true));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MedicoDto> crear(MedicoRq datos) {
        return ResponseEntity.ok(servicio.crear(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MedicoDto> actualizar(long id, MedicoRq datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MedicoDto> cambiarEstado(long id, EstadoRq datos) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, datos));
    }
}
