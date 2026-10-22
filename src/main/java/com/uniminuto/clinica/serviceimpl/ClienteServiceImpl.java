package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;


    @Override
    public List<Cliente> listarClientes() throws BadRequestException {
        return clienteRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getNombres().compareToIgnoreCase(c2.getNombres()))
                .toList();
    }

    @Override
    public Cliente buscarClientesPorDocumento(String documento) throws BadRequestException {
        Optional<Cliente> clienteOptional = clienteRepository.findByNumeroDocumento(documento);
        if (clienteOptional.isPresent()) {
            return clienteOptional.get();
        } else {
            throw new BadRequestException("Cliente no encontrado con el documento: " + documento);
        }
    }
}
