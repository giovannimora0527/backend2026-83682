package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;

import java.util.List;

/**
 * Define las operaciones disponibles para consultar los clientes
 * registrados en la clínica veterinaria.
 */
public interface ClienteService {

    /**
     * Obtiene la lista de todos los clientes registrados.
     *
     * @return lista de clientes encontrados
     */
    List<Cliente> obtenerClientes();

    /**
     * Busca un cliente mediante su identificador.
     *
     * @param id identificador único del cliente
     * @return cliente encontrado
     */
    Cliente obtenerCliente(Long id);

    /**
     * Busca un cliente mediante su número de documento.
     *
     * @param numeroDocumento número de documento del cliente
     * @return cliente encontrado
     */
    Cliente obtenerClientePorDocumento(String numeroDocumento);
}
