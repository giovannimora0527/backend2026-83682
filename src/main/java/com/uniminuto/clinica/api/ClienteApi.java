package com.uniminuto.clinica.api;

import com.uniminuto.clinica.dto.ClienteSolicitud;
import com.uniminuto.clinica.entity.Cliente;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cliente")
public interface ClienteApi {

    @GetMapping(value = "/listar-todos",
            produces = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;

    @GetMapping("/{id}") ResponseEntity<Cliente> consultar(@PathVariable long id);
    @PostMapping ResponseEntity<Cliente> crear(@RequestBody ClienteSolicitud solicitud);
    @PutMapping("/{id}") ResponseEntity<Cliente> editar(@PathVariable long id, @RequestBody ClienteSolicitud solicitud);
    @DeleteMapping("/{id}") ResponseEntity<Void> archivar(@PathVariable long id);
}
