package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteActualizarRq;

import java.util.List;

public interface ClienteService {

    Cliente actualizarCliente(Long id, ClienteActualizarRq clienteRq);

    List<Cliente> listarClientes() throws BadRequestException;

    Cliente buscarClientesPorDocumento(String documento) throws BadRequestException;
}
