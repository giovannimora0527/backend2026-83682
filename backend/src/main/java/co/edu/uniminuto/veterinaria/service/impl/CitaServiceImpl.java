package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.CitaActualizarRequest;
import co.edu.uniminuto.veterinaria.dto.CitaFiltro;
import co.edu.uniminuto.veterinaria.dto.CitaRequest;
import co.edu.uniminuto.veterinaria.dto.CitaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.Cita;
import co.edu.uniminuto.veterinaria.entity.Cliente;
import co.edu.uniminuto.veterinaria.entity.EstadoCita;
import co.edu.uniminuto.veterinaria.entity.HistoriaClinica;
import co.edu.uniminuto.veterinaria.entity.Mascota;
import co.edu.uniminuto.veterinaria.entity.Veterinario;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.exception.ReglaNegocioException;
import co.edu.uniminuto.veterinaria.repository.CitaRepository;
import co.edu.uniminuto.veterinaria.repository.HistoriaClinicaRepository;
import co.edu.uniminuto.veterinaria.service.CitaService;
import co.edu.uniminuto.veterinaria.util.Paginacion;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
 * Implementación de {@link CitaService}. Aquí viven las reglas de negocio críticas de las citas:
 * el backend es el responsable de validarlas aunque Angular también las valide.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Columnas por las que se permite ordenar: nombre lógico -> propiedad de la entidad. */
    private static final Map<String, String> ORDEN_PERMITIDO = Map.of(
            "fechaHora", "fechaHora",
            "cliente", "cliente.nombres",
            "mascota", "mascota.nombre",
            "veterinario", "veterinario.nombres",
            "estado", "estado");

    /** Minutos de margen antes y después de una cita en los que el veterinario/mascota no pueden tener otra. */
    private static final long MARGEN_MINUTOS = 15;

    /** Valor usado como "no excluir ninguna cita" al buscar cruces (ningún id es negativo). */
    private static final long SIN_EXCLUIR = -1L;

    /** Repositorio de citas. */
    private final CitaRepository citaRepository;

    /** Repositorio de historias clínicas (para saber si una cita ya generó historia). */
    private final HistoriaClinicaRepository historiaRepository;

    /** Validador de referencias (cliente, mascota, veterinario). */
    private final ValidadorReferencias referencias;

    /**
     * Crea el servicio.
     *
     * @param citaRepository     repositorio de citas
     * @param historiaRepository repositorio de historias clínicas
     * @param referencias        validador de referencias
     */
    public CitaServiceImpl(CitaRepository citaRepository, HistoriaClinicaRepository historiaRepository,
                           ValidadorReferencias referencias) {
        this.citaRepository = citaRepository;
        this.historiaRepository = historiaRepository;
        this.referencias = referencias;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CitaResponse> listar(CitaFiltro filtro, int pagina, int tamano, String orden,
                                             String direccion) {
        Pageable pageable = Paginacion.crear(pagina, tamano, orden, direccion, ORDEN_PERMITIDO, "fechaHora",
                Sort.Direction.DESC);
        Page<Cita> page = citaRepository.findAll(construirEspecificacion(filtro), pageable);
        return PageResponse.de(page.map(this::aRespuesta));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaResponse crear(CitaRequest request) {
        // Reglas 1, 2 y 3: cliente existente, mascota del cliente y veterinario existente.
        Cliente cliente = referencias.clienteActivo(request.clienteId());
        Mascota mascota = referencias.mascotaDelCliente(request.mascotaId(), cliente);
        Veterinario veterinario = referencias.veterinarioActivo(request.veterinarioId());

        EstadoCita estado = request.estado() == null ? EstadoCita.PENDIENTE : request.estado();
        if (estado == EstadoCita.ATENDIDA || estado == EstadoCita.CANCELADA) {
            throw ReglaNegocioException.invalida(
                    "Una cita nueva solo puede registrarse como Pendiente, Confirmada o Programada.");
        }

        LocalDateTime fechaHora = normalizar(request.fechaHora());
        validarFechaFutura(fechaHora);                                                        // Regla 6
        validarDisponibilidad(veterinario.getId(), mascota.getId(), fechaHora, SIN_EXCLUIR);  // Reglas 4 y 5

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setVeterinario(veterinario);
        cita.setFechaHora(fechaHora);
        cita.setEstado(estado);
        cita.setMotivo(request.motivo().trim());
        return aRespuesta(citaRepository.save(cita));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaResponse actualizar(Long id, CitaActualizarRequest request) {
        Cita cita = buscar(id);

        // Regla 8: una cita cancelada no se modifica como si siguiera activa.
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw ReglaNegocioException.conflicto(
                    "Una cita cancelada no puede modificarse. Use la opción «Reactivar» para volver a activarla.");
        }
        // Una cita atendida es información histórica: no se altera.
        if (cita.getEstado() == EstadoCita.ATENDIDA) {
            throw ReglaNegocioException.conflicto(
                    "Una cita atendida es información histórica y ya no puede modificarse.");
        }

        EstadoCita nuevoEstado = request.estado() == null ? cita.getEstado() : request.estado();
        if (!cita.getEstado().puedePasarA(nuevoEstado)) {
            throw ReglaNegocioException.invalida("No se puede cambiar una cita de «" + cita.getEstado().getEtiqueta()
                    + "» a «" + nuevoEstado.getEtiqueta() + "».");
        }

        Veterinario veterinario = referencias.veterinarioActivo(request.veterinarioId());
        LocalDateTime fechaHora = normalizar(request.fechaHora());
        boolean cambiaFecha = !fechaHora.equals(cita.getFechaHora());
        boolean cambiaVeterinario = !veterinario.getId().equals(cita.getVeterinario().getId());

        if (nuevoEstado == EstadoCita.ATENDIDA && (cambiaFecha || cambiaVeterinario)) {
            throw ReglaNegocioException.invalida(
                    "Para marcar la cita como atendida no cambie la fecha, la hora ni el veterinario.");
        }
        if (nuevoEstado != EstadoCita.CANCELADA && (cambiaFecha || cambiaVeterinario)) {
            if (cambiaFecha) {
                validarFechaFutura(fechaHora);
            }
            validarDisponibilidad(veterinario.getId(), cita.getMascota().getId(), fechaHora, cita.getId());
        }

        cita.setVeterinario(veterinario);
        cita.setFechaHora(fechaHora);
        cita.setMotivo(request.motivo().trim());
        cita.setEstado(nuevoEstado);
        return aRespuesta(citaRepository.save(cita));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CitaResponse reactivar(Long id) {
        Cita cita = buscar(id);
        if (cita.getEstado() != EstadoCita.CANCELADA) {
            throw ReglaNegocioException.conflicto("Solo se pueden reactivar las citas canceladas.");
        }
        if (cita.getFechaHora().isBefore(LocalDateTime.now())) {
            throw ReglaNegocioException.invalida(
                    "La cita cancelada tiene una fecha que ya pasó. Registre una cita nueva.");
        }
        validarDisponibilidad(cita.getVeterinario().getId(), cita.getMascota().getId(), cita.getFechaHora(),
                cita.getId());
        cita.setEstado(EstadoCita.PENDIENTE);
        return aRespuesta(citaRepository.save(cita));
    }

    /**
     * Busca una cita o lanza 404.
     *
     * @param id identificador de la cita
     * @return la cita
     */
    private Cita buscar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La cita solicitada no existe."));
    }

    /**
     * Quita segundos y fracciones para que coincida con la precisión de la columna (minutos).
     *
     * @param fechaHora fecha y hora original
     * @return fecha y hora sin segundos
     */
    private LocalDateTime normalizar(LocalDateTime fechaHora) {
        return fechaHora.withSecond(0).withNano(0);
    }

    /**
     * Regla 6: no se pueden registrar citas en fechas anteriores a la actual.
     *
     * @param fechaHora fecha y hora de la cita
     */
    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw ReglaNegocioException.invalida("No se pueden registrar citas en fechas anteriores a la actual.");
        }
    }

    /**
     * Reglas 4 y 5: ni el veterinario ni la mascota pueden tener otra cita (no cancelada) entre 15 minutos
     * antes y 15 minutos después de la hora solicitada.
     *
     * @param veterinarioId veterinario asignado
     * @param mascotaId     mascota de la cita
     * @param fechaHora     fecha y hora solicitada
     * @param excluirId     cita que se ignora (la que se está editando) o {@code -1}
     */
    private void validarDisponibilidad(Long veterinarioId, Long mascotaId, LocalDateTime fechaHora, Long excluirId) {
        LocalDateTime desde = fechaHora.minusMinutes(MARGEN_MINUTOS);
        LocalDateTime hasta = fechaHora.plusMinutes(MARGEN_MINUTOS);

        if (citaRepository.contarCrucesVeterinario(veterinarioId, EstadoCita.CANCELADA, excluirId, desde, hasta) > 0) {
            throw ReglaNegocioException.conflicto("El veterinario no está disponible en ese horario: ya tiene una "
                    + "cita entre 15 minutos antes y 15 minutos después de la hora elegida.");
        }
        if (citaRepository.contarCrucesMascota(mascotaId, EstadoCita.CANCELADA, excluirId, desde, hasta) > 0) {
            throw ReglaNegocioException.conflicto(
                    "Cita duplicada: la mascota ya tiene una cita en ese horario.");
        }
    }

    /**
     * Construye el filtro dinámico del listado a partir de los criterios recibidos.
     *
     * @param f criterios de filtrado
     * @return especificación JPA
     */
    private Specification<Cita> construirEspecificacion(CitaFiltro f) {
        Specification<Cita> spec = (root, query, cb) -> cb.conjunction();

        if (hayTexto(f.cliente())) {
            spec = spec.and((root, query, cb) -> contiene(cb, nombreCompleto(cb, root.get("cliente")), f.cliente()));
        }
        if (hayTexto(f.mascota())) {
            spec = spec.and((root, query, cb) ->
                    contiene(cb, root.get("mascota").<String>get("nombre"), f.mascota()));
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
        if (Boolean.TRUE.equals(f.sinHistoria())) {
            spec = spec.and((root, query, cb) -> {
                Subquery<Long> subconsulta = query.subquery(Long.class);
                Root<HistoriaClinica> historia = subconsulta.from(HistoriaClinica.class);
                subconsulta.select(historia.<Long>get("id")).where(cb.equal(historia.get("cita"), root));
                return cb.not(cb.exists(subconsulta));
            });
        }
        return spec;
    }

    /**
     * Convierte la entidad en el DTO de respuesta.
     *
     * @param c entidad cita
     * @return DTO de la cita
     */
    private CitaResponse aRespuesta(Cita c) {
        return new CitaResponse(c.getId(), c.getFechaHora(), c.getCliente().getId(),
                c.getCliente().getNombreCompleto(), c.getMascota().getId(), c.getMascota().getNombre(),
                c.getVeterinario().getId(), c.getVeterinario().getNombreCompleto(), c.getEstado(), c.getMotivo(),
                historiaRepository.existsByCita_Id(c.getId()));
    }
}
