package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio para clientes.
 */
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    /**
     * Recupera todos los clientes almacenados.
     *
     * @return lista de clientes
     */
    @Override
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }
}
