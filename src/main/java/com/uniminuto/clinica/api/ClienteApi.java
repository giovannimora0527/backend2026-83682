package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;


    @GetMapping(value = "/buscar-by-numero-documento",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> getClientesByNumeroDocumento(
            @RequestParam("numeroDocumento") String numeroDocumento)
            throws BadRequestException;
}
