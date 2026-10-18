package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.MascotaResponse;
import co.edu.uniminuto.veterinaria.entity.Mascota;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.repository.ClienteRepository;
import co.edu.uniminuto.veterinaria.repository.MascotaRepository;
import co.edu.uniminuto.veterinaria.service.MascotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link MascotaService}.
 */
@Service
public class MascotaServiceImpl implements MascotaService {

    /** Repositorio de mascotas. */
    private final MascotaRepository mascotaRepository;

    /** Repositorio de clientes (para verificar que el cliente exista). */
    private final ClienteRepository clienteRepository;

    /**
     * Crea el servicio.
     *
     * @param mascotaRepository repositorio de mascotas
     * @param clienteRepository repositorio de clientes
     */
    public MascotaServiceImpl(MascotaRepository mascotaRepository, ClienteRepository clienteRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MascotaResponse> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException("El cliente solicitado no existe.");
        }
        return mascotaRepository.findByCliente_IdAndActivoTrueOrderByNombreAsc(clienteId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public MascotaResponse obtener(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La mascota solicitada no existe."));
        return aRespuesta(mascota);
    }

    /**
     * Convierte la entidad en el DTO de respuesta.
     *
     * @param m entidad mascota
     * @return DTO de la mascota
     */
    private MascotaResponse aRespuesta(Mascota m) {
        return new MascotaResponse(m.getId(), m.getNombre(), m.getEdad(), m.getRaza().getId(),
                m.getRaza().getNombre(), m.getRaza().getEspecie(), m.getCliente().getId(),
                m.getCliente().getNombreCompleto(), m.isActivo());
    }
}
