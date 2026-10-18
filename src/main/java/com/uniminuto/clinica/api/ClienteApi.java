package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/cliente")
public interface ClienteApi {

    /**
     * Lista los clientes registrados. {@code /listar} lo usa Angular; {@code /all} es la ruta del parcial.
     *
     * @return lista de clientes.
     */
    @GetMapping(value = {"/listar", "/all"},
            produces = {"application/json"})
    ResponseEntity<List<Cliente>> listarClientes()
            throws BadRequestException;

    /**
     * Busca un cliente por su número de documento (ruta del parcial).
     *
     * @param numeroDocumento número de documento del cliente.
     * @return cliente encontrado.
     */
    @GetMapping(value = "/cliente-documento",
            produces = {"application/json"})
    ResponseEntity<Cliente> getClientesByNumeroDocumento(
            @RequestParam String numeroDocumento)
            throws BadRequestException;

    /**
     * Consulta un cliente mediante su identificador ({@code cliente_id}).
     *
     * @param id identificador del cliente.
     * @return cliente encontrado.
     */
    @GetMapping(value = "/{id}", produces = {"application/json"})
    ResponseEntity<Cliente> consultarCliente(@PathVariable Long id);

    /**
     * Lista las mascotas asociadas a un cliente.
     *
     * @param id identificador del cliente.
     * @return mascotas del cliente.
     */
    @GetMapping(value = "/{id}/mascotas", produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotasCliente(@PathVariable Long id)
            throws BadRequestException;
}
