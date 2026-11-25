package com.uniminuto.clinica.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controlador REST de clientes: implementa {@link ClienteApi} y delega en {@link ClienteService}. */
@RestController
public class ClienteApiController implements ClienteApi {

    /** Reglas de negocio de clientes. */
    private final ClienteService servicio;

    /**
     * @param servicio servicio de clientes.
     */
    public ClienteApiController(ClienteService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<ClienteDto>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClienteDto> buscarPorDocumento(String documento) {
        return ResponseEntity.ok(servicio.buscarPorDocumento(documento));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClienteDto> consultar(long id) {
        return ResponseEntity.ok(servicio.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MascotaDeClienteDto>> mascotas(long id) {
        return ResponseEntity.ok(servicio.mascotas(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClienteDto> crear(ClienteRq datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClienteDto> actualizar(long id, ClienteRq datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> archivar(long id) {
        servicio.archivar(id);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ClienteDto> reactivar(long id) {
        return ResponseEntity.ok(servicio.reactivar(id));
    }
}
