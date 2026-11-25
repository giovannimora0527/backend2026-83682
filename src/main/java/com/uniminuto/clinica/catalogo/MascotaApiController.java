package com.uniminuto.clinica.catalogo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controlador REST de mascotas: implementa {@link MascotaApi} y delega en {@link MascotaService}. */
@RestController
public class MascotaApiController implements MascotaApi {

    /** Reglas de negocio de mascotas. */
    private final MascotaService servicio;

    /**
     * @param servicio servicio de mascotas.
     */
    public MascotaApiController(MascotaService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MascotaDto>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MascotaDto> consultar(int id) {
        return ResponseEntity.ok(servicio.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MascotaDto> crear(MascotaRq datos) {
        return ResponseEntity.ok(servicio.crear(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MascotaDto> actualizar(int id, MascotaRq datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MascotaDto> cambiarEstado(int id, EstadoRq datos) {
        return ResponseEntity.ok(servicio.cambiarEstado(id, datos));
    }
}
