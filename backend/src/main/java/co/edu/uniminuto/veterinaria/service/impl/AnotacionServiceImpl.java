package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.AnotacionFiltro;
import co.edu.uniminuto.veterinaria.dto.AnotacionRequest;
import co.edu.uniminuto.veterinaria.dto.AnotacionResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.AnotacionHistoria;
import co.edu.uniminuto.veterinaria.entity.EstadoHistoria;
import co.edu.uniminuto.veterinaria.entity.HistoriaClinica;
import co.edu.uniminuto.veterinaria.entity.Veterinario;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.exception.ReglaNegocioException;
import co.edu.uniminuto.veterinaria.repository.AnotacionHistoriaRepository;
import co.edu.uniminuto.veterinaria.repository.HistoriaClinicaRepository;
import co.edu.uniminuto.veterinaria.service.AnotacionService;
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

/**
 * Implementación de {@link AnotacionService}.
 */
@Service
public class AnotacionServiceImpl implements AnotacionService {

    /** Columnas por las que se permite ordenar: nombre lógico -> propiedad de la entidad. */
    private static final Map<String, String> ORDEN_PERMITIDO = Map.of(
            "fechaHora", "fechaHora",
            "veterinario", "veterinario.nombres");

    /** Repositorio de anotaciones. */
    private final AnotacionHistoriaRepository anotacionRepository;

    /** Repositorio de historias clínicas. */
    private final HistoriaClinicaRepository historiaRepository;

    /** Validador de referencias (veterinario). */
    private final ValidadorReferencias referencias;

    /**
     * Crea el servicio.
     *
     * @param anotacionRepository repositorio de anotaciones
     * @param historiaRepository  repositorio de historias clínicas
     * @param referencias         validador de referencias
     */
    public AnotacionServiceImpl(AnotacionHistoriaRepository anotacionRepository,
                                HistoriaClinicaRepository historiaRepository, ValidadorReferencias referencias) {
        this.anotacionRepository = anotacionRepository;
        this.historiaRepository = historiaRepository;
        this.referencias = referencias;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AnotacionResponse> listar(Long historiaId, AnotacionFiltro filtro, int pagina, int tamano,
                                                  String orden, String direccion) {
        buscarHistoria(historiaId);
        Pageable pageable = Paginacion.crear(pagina, tamano, orden, direccion, ORDEN_PERMITIDO, "fechaHora",
                Sort.Direction.DESC);
        Page<AnotacionHistoria> page = anotacionRepository.findAll(construirEspecificacion(historiaId, filtro),
                pageable);
        return PageResponse.de(page.map(this::aRespuesta));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public AnotacionResponse obtener(Long historiaId, Long anotacionId) {
        AnotacionHistoria anotacion = anotacionRepository.findByIdAndHistoria_Id(anotacionId, historiaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La anotación solicitada no existe en esta historia clínica."));
        return aRespuesta(anotacion);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AnotacionResponse crear(Long historiaId, AnotacionRequest request) {
        HistoriaClinica historia = buscarHistoria(historiaId);
        if (historia.getEstado() == EstadoHistoria.CERRADA) {
            throw ReglaNegocioException.conflicto(
                    "La historia clínica está cerrada y no admite nuevas anotaciones.");
        }
        Veterinario veterinario = referencias.veterinarioActivo(request.veterinarioId());

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoria(historia);
        anotacion.setVeterinario(veterinario);
        anotacion.setFechaHora(LocalDateTime.now().withNano(0));
        anotacion.setDescripcion(request.descripcion().trim());
        anotacion.setObservacion(request.observacion().trim());
        anotacion.setDiagnosticoEvolucion(request.diagnosticoEvolucion().trim());
        return aRespuesta(anotacionRepository.save(anotacion));
    }

    /**
     * Busca una historia o lanza 404.
     *
     * @param historiaId identificador de la historia
     * @return la historia
     */
    private HistoriaClinica buscarHistoria(Long historiaId) {
        return historiaRepository.findById(historiaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La historia clínica solicitada no existe."));
    }

    /**
     * Construye el filtro dinámico: siempre limita a la historia indicada y suma los criterios opcionales.
     *
     * @param historiaId identificador de la historia
     * @param f          criterios de filtrado
     * @return especificación JPA
     */
    private Specification<AnotacionHistoria> construirEspecificacion(Long historiaId, AnotacionFiltro f) {
        Specification<AnotacionHistoria> spec =
                (root, query, cb) -> cb.equal(root.get("historia").get("id"), historiaId);

        if (hayTexto(f.texto())) {
            spec = spec.and((root, query, cb) -> cb.or(
                    contiene(cb, root.<String>get("descripcion"), f.texto()),
                    contiene(cb, root.<String>get("observacion"), f.texto()),
                    contiene(cb, root.<String>get("diagnosticoEvolucion"), f.texto())));
        }
        if (f.veterinarioId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("veterinario").get("id"), f.veterinarioId()));
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
     * @param a entidad anotación
     * @return DTO de la anotación
     */
    private AnotacionResponse aRespuesta(AnotacionHistoria a) {
        return new AnotacionResponse(a.getId(), a.getHistoria().getId(), a.getFechaHora(),
                a.getVeterinario().getId(), a.getVeterinario().getNombreCompleto(), a.getDescripcion(),
                a.getObservacion(), a.getDiagnosticoEvolucion());
    }
}
