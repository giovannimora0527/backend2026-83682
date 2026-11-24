package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ClienteActualizarRq;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import jakarta.validation.Validator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final Validator validator;
    private final TransactionTemplate transactionTemplate;

    public ClienteServiceImpl(ClienteRepository clienteRepository, Validator validator,
                              PlatformTransactionManager transactionManager) {
        this.clienteRepository = clienteRepository;
        this.validator = validator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Cliente actualizarCliente(Long id, ClienteActualizarRq clienteRq) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del cliente debe ser mayor a cero.");
        }
        this.validar(clienteRq);
        String documento = clienteRq.getNumeroDocumento().trim();
        try {
            return transactionTemplate.execute(status -> {
                Cliente cliente = clienteRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("El cliente con ID " + id + " no existe."));
                comprobarDocumentoDisponible(documento, id);

                this.copiarDatos(clienteRq, documento, cliente);
                return clienteRepository.save(cliente);
            });
        } catch (DataIntegrityViolationException ex) {
            // Consultar despues del rollback tambien cubre duplicados concurrentes al confirmar.
            comprobarDocumentoDisponible(documento, id);
            throw ex;
        }
    }

    @Override
    public Cliente crearCliente(ClienteActualizarRq clienteRq) {
        this.validar(clienteRq);
        String documento = clienteRq.getNumeroDocumento().trim();
        try {
            return transactionTemplate.execute(status -> {
                comprobarDocumentoDisponible(documento, null);
                Cliente cliente = new Cliente();
                this.copiarDatos(clienteRq, documento, cliente);
                cliente.setActivo(true);
                return clienteRepository.save(cliente);
            });
        } catch (DataIntegrityViolationException ex) {
            comprobarDocumentoDisponible(documento, null);
            throw ex;
        }
    }

    private void validar(ClienteActualizarRq clienteRq) {
        if (clienteRq == null) {
            throw new BadRequestException("La informacion del cliente es obligatoria.");
        }
        var errores = validator.validate(clienteRq);
        if (!errores.isEmpty()) {
            String mensaje = errores.stream()
                    .map(error -> error.getPropertyPath() + ": " + error.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new BadRequestException(mensaje);
        }
    }

    private void copiarDatos(ClienteActualizarRq clienteRq, String documento, Cliente cliente) {
        cliente.setTipoDocumento(clienteRq.getTipoDocumento().trim());
        cliente.setNumeroDocumento(documento);
        cliente.setNombres(clienteRq.getNombres().trim());
        cliente.setApellidos(clienteRq.getApellidos().trim());
        cliente.setFechaNacimiento(clienteRq.getFechaNacimiento());
        cliente.setGenero(clienteRq.getGenero());
        cliente.setTelefono(clienteRq.getTelefono());
        cliente.setCorreo(clienteRq.getCorreo());
        cliente.setDireccion(clienteRq.getDireccion());
    }

    private void comprobarDocumentoDisponible(String documento, Long id) {
        clienteRepository.findByNumeroDocumento(documento)
                .filter(cliente -> !cliente.getId().equals(id))
                .ifPresent(cliente -> {
                    throw new ConflictException("numeroDocumento", "El numero de documento ya pertenece a otro cliente.");
                });
    }

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
            throw new NotFoundException("Cliente no encontrado con el documento: " + documento);
        }
    }
}
