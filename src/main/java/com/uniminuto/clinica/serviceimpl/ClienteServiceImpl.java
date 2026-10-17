package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.http.HttpStatus;
import com.uniminuto.clinica.exception.BadRequestException;

/** Implementa la consulta de clientes mediante su repositorio. */
/** Implementa la consulta de clientes mediante su repositorio. */
@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /** {@inheritDoc} */
    @Override
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }
}
