package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    @GetMapping(value = "/listar-todos",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;
}
