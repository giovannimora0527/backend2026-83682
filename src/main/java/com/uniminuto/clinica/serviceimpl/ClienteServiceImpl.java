package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio para la gestión de Clientes.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Lista todos los clientes ordenados alfabéticamente por sus nombres.
     * @return Lista de clientes.
     * @throws BadRequestException si ocurre un error.
     */
    @Override
    public List<Cliente> listarClientes() throws BadRequestException {
        return clienteRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getNombres().compareToIgnoreCase(c2.getNombres()))
                .toList();
    }

    /**
     * Busca un cliente por su número de documento.
     * @param numeroDocumento Número de documento del cliente.
     * @return El cliente encontrado.
     * @throws BadRequestException si el cliente no existe.
     */
    @Override
    public Cliente getClienteByNumeroDocumento(String numeroDocumento) throws BadRequestException {
        return clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new BadRequestException("Cliente no encontrado con el número de documento: " + numeroDocumento));
    }
}