package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST encargado de gestionar las peticiones relacionadas con clientes.
 */
@RestController
@RequiredArgsConstructor
public class ClienteApiController implements ClienteApi {

    private final ClienteService clienteService;

    /**
     * Endpoint para listar todos los clientes.
     *
     * @return respuesta HTTP con la colección de clientes
     * @throws BadRequestException si ocurre un error en la validación de la solicitud
     */
    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(clienteService.obtenerClientes());
    }
}
