package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.Cliente;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.PacienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.springframework.stereotype.Service;
import java.util.List;

/** Implementa las operaciones de clientes separando la lógica del controlador. */
@Service
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clientes;
    private final PacienteRepository pacientes;
    public ClienteServiceImpl(ClienteRepository clientes, PacienteRepository pacientes) { this.clientes = clientes; this.pacientes = pacientes; }
    /** {@inheritDoc} */ public List<Cliente> listar() { return clientes.findAll(); }
    /** {@inheritDoc} */ public Cliente obtener(Long id) { return clientes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente solicitado.")); }
    /** {@inheritDoc} */ public Cliente crear(Cliente cliente) { return clientes.save(cliente); }
    /** {@inheritDoc} */ public Cliente actualizar(Long id, Cliente datos) { Cliente c = obtener(id); c.setNombre(datos.getNombre()); c.setTelefono(datos.getTelefono()); c.setCorreo(datos.getCorreo()); c.setDireccion(datos.getDireccion()); return clientes.save(c); }
    /** {@inheritDoc} */ public List<Paciente> listarMascotas(Long id) { obtener(id); return pacientes.findByClienteId(id); }
}
