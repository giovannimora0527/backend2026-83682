package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface ClienteService {

    List<Cliente> listarClientes() throws BadRequestException;


    Cliente getClienteByNumeroDocumento(String numeroDocumento) throws BadRequestException;
}
