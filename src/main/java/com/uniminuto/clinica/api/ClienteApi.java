package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato HTTP para las operaciones relacionadas con clientes.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    /**
     * Obtiene la lista completa de clientes registrados.
     *
     * @return respuesta HTTP con la lista de clientes
     * @throws BadRequestException si ocurre un error en la validación de la solicitud
     */
    @GetMapping(
            value = "/listar-todos",
            produces = "application/json"
    )
    ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException;
}
