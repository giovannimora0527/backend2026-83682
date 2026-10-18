package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.HistoriaFiltro;
import co.edu.uniminuto.veterinaria.dto.HistoriaRequest;
import co.edu.uniminuto.veterinaria.dto.HistoriaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.Cita;
import co.edu.uniminuto.veterinaria.entity.Cliente;
import co.edu.uniminuto.veterinaria.entity.EstadoCita;
import co.edu.uniminuto.veterinaria.entity.EstadoHistoria;
import co.edu.uniminuto.veterinaria.entity.HistoriaClinica;
import co.edu.uniminuto.veterinaria.entity.Mascota;
import co.edu.uniminuto.veterinaria.entity.Veterinario;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.exception.ReglaNegocioException;
import co.edu.uniminuto.veterinaria.repository.CitaRepository;
import co.edu.uniminuto.veterinaria.repository.HistoriaClinicaRepository;
import co.edu.uniminuto.veterinaria.service.HistoriaService;
import co.edu.uniminuto.veterinaria.util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static co.edu.uniminuto.veterinaria.util.Especificaciones.contiene;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.hayTexto;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.inicioDelDia;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.inicioDelDiaSiguiente;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.nombreCompleto;

/**
 * Implementación de {@link HistoriaService}.
 */
@Service
public class HistoriaServiceImpl implements HistoriaService {

    /** Columnas por las que se permite ordenar: nombre lógico -> propiedad de la entidad. */
    private static final Map<String, String> ORDEN_PERMITIDO = Map.of(
            "fechaHora", "fechaHora",
            "mascota", "mascota.nombre",
            "cliente", "cliente.nombres",
            "veterinario", "veterinario.nombres",
            "motivo", "motivo",
            "estado", "estado");

    /** Repositorio de historias clínicas. */
    private final HistoriaClinicaRepository historiaRepository;

    /** Repositorio de citas. */
    private final CitaRepository citaRepository;

    /** Validador de referencias (cliente, mascota, veterinario). */
    private final ValidadorReferencias referencias;

    /**
     * Crea el servicio.
     *
     * @param historiaRepository repositorio de historias clínicas
     * @param citaRepository     repositorio de citas
     * @param referencias        validador de referencias
     */
    public HistoriaServiceImpl(HistoriaClinicaRepository historiaRepository, CitaRepository citaRepository,
                               ValidadorReferencias referencias) {
        this.historiaRepository = historiaRepository;
        this.citaRepository = citaRepository;
        this.referencias = referencias;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<HistoriaResponse> listar(HistoriaFiltro filtro, int pagina, int tamano, String orden,
                                                 String direccion) {
        Pageable pageable = Paginacion.crear(pagina, tamano, orden, direccion, ORDEN_PERMITIDO, "fechaHora",
                Sort.Direction.DESC);
        Page<HistoriaClinica> page = historiaRepository.findAll(construirEspecificacion(filtro), pageable);
        return PageResponse.de(page.map(this::aRespuesta));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public HistoriaResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public HistoriaResponse crear(HistoriaRequest request) {
        Cliente cliente = referencias.clienteActivo(request.clienteId());
        Mascota mascota = referencias.mascotaDelCliente(request.mascotaId(), cliente);

        Cita cita = citaRepository.findById(request.citaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La cita seleccionada no existe."));
        if (!cita.getMascota().getId().equals(mascota.getId())) {
            throw ReglaNegocioException.invalida("La cita seleccionada pertenece a otra mascota.");
        }
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw ReglaNegocioException.invalida("Solo una cita atendida puede generar una historia clínica.");
        }
        if (historiaRepository.existsByCita_Id(cita.getId())) {
            throw ReglaNegocioException.conflicto("La cita seleccionada ya tiene una historia clínica asociada.");
        }

        Veterinario veterinario = referencias.veterinarioActivo(request.veterinarioId());
        LocalDateTime fechaHora = request.fechaHora().withSecond(0).withNano(0);
        if (fechaHora.isAfter(LocalDateTime.now())) {
            throw ReglaNegocioException.invalida("La fecha y hora de la consulta no pueden ser futuras.");
        }

        HistoriaClinica historia = new HistoriaClinica();
        historia.setCliente(cliente);
        historia.setMascota(mascota);
        historia.setCita(cita);
        historia.setVeterinario(veterinario);
        historia.setFechaHora(fechaHora);
        historia.setMotivo(request.motivo().trim());
        historia.setDiagnostico(request.diagnostico().trim());
        historia.setObservaciones(request.observaciones() == null ? null : request.observaciones().trim());
        historia.setEstado(EstadoHistoria.ABIERTA);
        return aRespuesta(historiaRepository.save(historia));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public HistoriaResponse cerrar(Long id) {
        HistoriaClinica historia = buscar(id);
        if (historia.getEstado() == EstadoHistoria.CERRADA) {
            throw ReglaNegocioException.conflicto("La historia clínica ya está cerrada.");
        }
        historia.setEstado(EstadoHistoria.CERRADA);
        return aRespuesta(historiaRepository.save(historia));
    }

    /**
     * Busca una historia o lanza 404.
     *
     * @param id identificador de la historia
     * @return la historia
     */
    private HistoriaClinica buscar(Long id) {
        return historiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La historia clínica solicitada no existe."));
    }

    /**
     * Construye el filtro dinámico del listado a partir de los criterios recibidos.
     *
     * @param f criterios de filtrado
     * @return especificación JPA
     */
    private Specification<HistoriaClinica> construirEspecificacion(HistoriaFiltro f) {
        Specification<HistoriaClinica> spec = (root, query, cb) -> cb.conjunction();

        if (hayTexto(f.cliente())) {
            spec = spec.and((root, query, cb) -> contiene(cb, nombreCompleto(cb, root.get("cliente")), f.cliente()));
        }
        if (hayTexto(f.mascota())) {
            spec = spec.and((root, query, cb) ->
                    contiene(cb, root.get("mascota").<String>get("nombre"), f.mascota()));
        }
        if (hayTexto(f.motivo())) {
            spec = spec.and((root, query, cb) -> contiene(cb, root.<String>get("motivo"), f.motivo()));
        }
        if (f.clienteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cliente").get("id"), f.clienteId()));
        }
        if (f.mascotaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mascota").get("id"), f.mascotaId()));
        }
        if (f.veterinarioId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("veterinario").get("id"), f.veterinarioId()));
        }
        if (f.estado() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estado"), f.estado()));
        }
        if (f.desde() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fechaHora"), inicioDelDia(f.desde())));
        }
        if (f.hasta() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.<LocalDateTime>get("fechaHora"), inicioDelDiaSiguiente(f.hasta())));
        }
        return spec;
    }

    /**
     * Convierte la entidad en el DTO de respuesta.
     *
     * @param h entidad historia clínica
     * @return DTO de la historia
     */
    private HistoriaResponse aRespuesta(HistoriaClinica h) {
        return new HistoriaResponse(h.getId(), h.getFechaHora(), h.getCliente().getId(),
                h.getCliente().getNombreCompleto(), h.getMascota().getId(), h.getMascota().getNombre(),
                h.getCita().getId(), h.getVeterinario().getId(), h.getVeterinario().getNombreCompleto(),
                h.getMotivo(), h.getDiagnostico(), h.getObservaciones(), h.getEstado());
    }
}
