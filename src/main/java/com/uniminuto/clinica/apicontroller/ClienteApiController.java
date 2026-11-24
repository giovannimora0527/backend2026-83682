package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteActualizarRq;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClienteApiController implements ClienteApi {

    private final ClienteService clienteService;

    public ClienteApiController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<RespuestaRs<Cliente>> crearCliente(ClienteActualizarRq clienteRq) {
        return ResponseEntity.ok(RespuestaRs.ok("Cliente creado correctamente.",
                clienteService.crearCliente(clienteRq)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<Cliente> actualizarCliente(Long id, ClienteActualizarRq clienteRq) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, clienteRq));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SECRETARIA', 'MEDICO')")
    public ResponseEntity<Cliente> buscarClientesPorDocumento(String documento) throws BadRequestException {
        return ResponseEntity.ok(clienteService.buscarClientesPorDocumento(documento));
    }
}
