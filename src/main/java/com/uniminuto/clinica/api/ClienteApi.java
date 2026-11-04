package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteActualizarRq;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    @PostMapping(value = "/{id}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> actualizarCliente(
            @PathVariable("id") Long id,
            @Valid @RequestBody ClienteActualizarRq clienteRq);

    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;


    @GetMapping(value = "/buscar-por-documento",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> buscarClientesPorDocumento(
            @RequestParam String documento
    )
            throws BadRequestException;
}
