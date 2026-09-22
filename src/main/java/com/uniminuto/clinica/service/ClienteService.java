package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;

import java.util.List;

/**
 * Define las operaciones de negocio relacionadas con clientes.
 */
public interface ClienteService {

    /**
     * Recupera todos los clientes disponibles en el sistema.
     *
     * @return lista de clientes
     */
    List<Cliente> obtenerClientes();
}
