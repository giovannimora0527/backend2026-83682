package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.ClienteResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.Cliente;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.repository.ClienteRepository;
import co.edu.uniminuto.veterinaria.service.ClienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static co.edu.uniminuto.veterinaria.util.Especificaciones.contiene;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.hayTexto;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.nombreCompleto;

/**
 * Implementación de {@link ClienteService}.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    /** Repositorio de clientes. */
    private final ClienteRepository clienteRepository;

    /**
     * Crea el servicio.
     *
     * @param clienteRepository repositorio de clientes
     */
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listar(String texto, int pagina, int tamano) {
        Specification<Cliente> spec = (root, query, cb) -> cb.isTrue(root.<Boolean>get("activo"));
        if (hayTexto(texto)) {
            spec = spec.and((root, query, cb) -> cb.or(
                    contiene(cb, nombreCompleto(cb, root), texto),
                    contiene(cb, root.<String>get("numeroDocumento"), texto)));
        }
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 100),
                Sort.by("nombres", "apellidos", "id"));
        Page<Cliente> page = clienteRepository.findAll(spec, pageable);
        return PageResponse.de(page.map(this::aRespuesta));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente solicitado no existe."));
        return aRespuesta(cliente);
    }

    /**
     * Convierte la entidad en el DTO de respuesta.
     *
     * @param c entidad cliente
     * @return DTO del cliente
     */
    private ClienteResponse aRespuesta(Cliente c) {
        return new ClienteResponse(c.getId(), c.getTipoDocumento(), c.getNumeroDocumento(), c.getNombres(),
                c.getApellidos(), c.getNombreCompleto(), c.getTelefono(), c.getDireccion(), c.isActivo());
    }
}
