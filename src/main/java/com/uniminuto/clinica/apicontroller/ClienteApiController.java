package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
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
}
