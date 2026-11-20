package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.dto.ClienteSolicitud;
import com.uniminuto.clinica.entity.Cliente;
import org.springframework.http.HttpStatus;
import com.uniminuto.clinica.service.AlcanceMedico;
import com.uniminuto.clinica.service.ClienteService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClienteApiController implements ClienteApi {

    private final ClienteService clienteService;
    private final AlcanceMedico alcance;

    public ClienteApiController(ClienteService clienteService, AlcanceMedico alcance) {
        this.clienteService = clienteService;
        this.alcance = alcance;
    }

    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        Integer medicoId = alcance.medicoId();
        List<Cliente> clientes = clienteService.obtenerClientes();
        if (medicoId != null) {
            var asignados = alcance.clientes(medicoId);
            clientes = clientes.stream().filter(cliente -> asignados.contains(cliente.getClienteId())).toList();
        }
        return ResponseEntity.ok(clientes);
    }
    @Override public ResponseEntity<Cliente> consultar(long id) {
        return ResponseEntity.ok(clienteService.consultar(id));
    }

    @Override public ResponseEntity<Cliente> crear(ClienteSolicitud solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(solicitud));
    }

    @Override public ResponseEntity<Cliente> editar(long id, ClienteSolicitud solicitud) {
        return ResponseEntity.ok(clienteService.editar(id, solicitud));
    }

    @Override public ResponseEntity<Void> archivar(long id) {
        clienteService.archivar(id);
        return ResponseEntity.noContent().build();
    }
}
