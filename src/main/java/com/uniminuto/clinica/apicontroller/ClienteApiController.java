package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.service.ClienteService;
import com.uniminuto.clinica.service.MascotaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST encargado de gestionar las consultas de clientes
 * y las mascotas asociadas a cada cliente.
 */
@RestController
public class ClienteApiController implements ClienteApi {

    private final ClienteService clienteService;
    private final MascotaService mascotaService;

    public ClienteApiController(ClienteService clienteService, MascotaService mascotaService) {
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
    }

    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.obtenerClientes());
    }

    @Override
    public ResponseEntity<Cliente> getClientesByNumeroDocumento(String numeroDocumento) throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.obtenerClientePorDocumento(numeroDocumento));
    }

    @Override
    public ResponseEntity<Cliente> consultarCliente(Long id) {
        return ResponseEntity.ok(clienteService.obtenerCliente(id));
    }

    /**
     * Primero verifica que el cliente exista y después consulta sus mascotas.
     */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotasCliente(Long id) throws BadRequestException {
        clienteService.obtenerCliente(id);
        return ResponseEntity.ok(mascotaService.listarMascotasPorCliente(id));
    }
}
