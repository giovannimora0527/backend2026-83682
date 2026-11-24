package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteActualizarRq;
import com.uniminuto.clinica.models.RespuestaRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Clientes")
@RequestMapping("/cliente")
public interface ClienteApi {

    @Operation(summary = "Crear un cliente")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<RespuestaRs<Cliente>> crearCliente(@Valid @RequestBody ClienteActualizarRq clienteRq);

    @Operation(summary = "Actualizar un cliente")
    @PostMapping(value = "/{id}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> actualizarCliente(
            @PathVariable("id") Long id,
            @Valid @RequestBody ClienteActualizarRq clienteRq);

    @Operation(summary = "Listar clientes")
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;


    @Operation(summary = "Buscar un cliente por documento")
    @GetMapping(value = "/buscar-por-documento",
            produces = {"application/json"})
    ResponseEntity<Cliente> buscarClientesPorDocumento(
            @RequestParam String documento
    )
            throws BadRequestException;
}
