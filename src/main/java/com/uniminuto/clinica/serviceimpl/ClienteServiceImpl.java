package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.ClienteSolicitud;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.service.ClienteService;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.uniminuto.clinica.calendario.AgendaServicio.*;

@Service
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clientes;

    public ClienteServiceImpl(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Override public List<Cliente> obtenerClientes() {
        return clientes.findAll();
    }

    @Override public Cliente consultar(long id) {
        return clientes.findById(id).orElseThrow(() -> noExiste("Cliente"));
    }

    @Override @Transactional public Cliente crear(ClienteSolicitud solicitud) {
        var cliente = new Cliente();
        cliente.setActivo(true);
        return guardar(cliente, solicitud);
    }

    @Override @Transactional public Cliente editar(long id, ClienteSolicitud solicitud) {
        return guardar(clientes.bloquear(id).orElseThrow(() -> noExiste("Cliente")), solicitud);
    }

    @Override @Transactional public void archivar(long id) {
        clientes.bloquear(id).orElseThrow(() -> noExiste("Cliente")).setActivo(false);
    }

    private Cliente guardar(Cliente cliente, ClienteSolicitud datos) {
        if (datos == null || datos.fechaNacimiento() == null || datos.fechaNacimiento().isAfter(ahora().toLocalDate())
                || datos.fechaNacimiento().getYear() < 1000) {
            throw invalido("Indica una fecha de nacimiento válida.");
        }
        cliente.setTipoDocumento(texto(datos.tipoDocumento(), 10, true));
        cliente.setNumeroDocumento(texto(datos.numeroDocumento(), 20, true));
        cliente.setNombres(texto(datos.nombres(), 100, true));
        cliente.setApellidos(texto(datos.apellidos(), 100, true));
        cliente.setFechaNacimiento(datos.fechaNacimiento());
        cliente.setGenero(texto(datos.genero(), 1, false));
        cliente.setTelefono(texto(datos.telefono(), 20, false));
        cliente.setDireccion(texto(datos.direccion(), 1000, false));
        try {
            return clientes.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException error) {
            for (Throwable causa = error; causa != null; causa = causa.getCause()) {
                if (causa instanceof SQLException sql && sql.getErrorCode() == 1062) {
                    throw conflicto("Ya existe un cliente con ese documento.");
                }
            }
            throw error;
        }
    }

    private static String texto(String dato, int maximo, boolean requerido) {
        String valor = dato == null ? "" : dato.strip();
        if (valor.length() > maximo || requerido && valor.isEmpty()) {
            throw invalido("Revisa los campos obligatorios y la longitud de los datos del cliente.");
        }
        return valor.isEmpty() ? null : valor;
    }
}
