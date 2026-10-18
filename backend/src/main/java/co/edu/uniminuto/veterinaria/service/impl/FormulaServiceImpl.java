package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.FormulaActualizarRequest;
import co.edu.uniminuto.veterinaria.dto.FormulaDetalleResponse;
import co.edu.uniminuto.veterinaria.dto.FormulaFiltro;
import co.edu.uniminuto.veterinaria.dto.FormulaRequest;
import co.edu.uniminuto.veterinaria.dto.FormulaResumenResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.entity.Cliente;
import co.edu.uniminuto.veterinaria.entity.EstadoHistoria;
import co.edu.uniminuto.veterinaria.entity.FormulaMedica;
import co.edu.uniminuto.veterinaria.entity.HistoriaClinica;
import co.edu.uniminuto.veterinaria.entity.Mascota;
import co.edu.uniminuto.veterinaria.entity.Medicamento;
import co.edu.uniminuto.veterinaria.entity.Veterinario;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.exception.ReglaNegocioException;
import co.edu.uniminuto.veterinaria.repository.FormulaMedicaRepository;
import co.edu.uniminuto.veterinaria.repository.HistoriaClinicaRepository;
import co.edu.uniminuto.veterinaria.service.FormulaService;
import co.edu.uniminuto.veterinaria.util.Paginacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static co.edu.uniminuto.veterinaria.util.Especificaciones.contiene;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.hayTexto;
import static co.edu.uniminuto.veterinaria.util.Especificaciones.nombreCompleto;

/**
 * Implementación de {@link FormulaService} con las reglas de negocio 1 a 8 de las fórmulas médicas.
 */
@Service
public class FormulaServiceImpl implements FormulaService {

    /** Columnas por las que se permite ordenar: nombre lógico -> propiedad de la entidad. */
    private static final Map<String, String> ORDEN_PERMITIDO = Map.of(
            "fechaCreacion", "fechaCreacion",
            "fechaEmision", "fechaEmision",
            "mascota", "mascota.nombre",
            "cliente", "mascota.cliente.nombres",
            "veterinario", "veterinario.nombres",
            "medicamento", "medicamento.nombre",
            "activo", "activo");

    /** Valor usado como "no excluir ninguna fórmula" al buscar duplicados (ningún id es negativo). */
    private static final long SIN_EXCLUIR = -1L;

    /** Repositorio de fórmulas. */
    private final FormulaMedicaRepository formulaRepository;

    /** Repositorio de historias clínicas. */
    private final HistoriaClinicaRepository historiaRepository;

    /** Validador de referencias (mascota, veterinario, medicamento). */
    private final ValidadorReferencias referencias;

    /**
     * Crea el servicio.
     *
     * @param formulaRepository  repositorio de fórmulas
     * @param historiaRepository repositorio de historias clínicas
     * @param referencias        validador de referencias
     */
    public FormulaServiceImpl(FormulaMedicaRepository formulaRepository,
                              HistoriaClinicaRepository historiaRepository, ValidadorReferencias referencias) {
        this.formulaRepository = formulaRepository;
        this.historiaRepository = historiaRepository;
        this.referencias = referencias;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<FormulaResumenResponse> listar(FormulaFiltro filtro, int pagina, int tamano, String orden,
                                                       String direccion) {
        Pageable pageable = Paginacion.crear(pagina, tamano, orden, direccion, ORDEN_PERMITIDO, "fechaCreacion",
                Sort.Direction.DESC);
        Page<FormulaMedica> page = formulaRepository.findAll(construirEspecificacion(filtro), pageable);
        return PageResponse.de(page.map(this::aResumen));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public FormulaDetalleResponse obtener(Long id) {
        return aDetalle(buscar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FormulaDetalleResponse crear(FormulaRequest request) {
        // Reglas 1 y 2: la mascota existe y pertenece a un cliente registrado (y activo).
        Mascota mascota = referencias.mascota(request.mascotaId());
        Cliente cliente = mascota.getCliente();
        if (cliente == null || !cliente.isActivo()) {
            throw ReglaNegocioException.invalida("La mascota debe pertenecer a un cliente registrado y activo.");
        }
        // Regla 3: el veterinario existe.
        Veterinario veterinario = referencias.veterinarioActivo(request.veterinarioId());

        HistoriaClinica historia = historiaRepository.findById(request.historiaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La historia clínica o consulta seleccionada no existe."));
        // Regla 7: la consulta debe ser de la misma mascota.
        if (!historia.getMascota().getId().equals(mascota.getId())) {
            throw ReglaNegocioException.invalida("La consulta seleccionada pertenece a otra mascota.");
        }
        validarHistoriaAbierta(historia);

        // Regla 4: la fecha de emisión no puede ser anterior a la consulta (ni futura).
        if (request.fechaEmision().isBefore(historia.getFechaHora().toLocalDate())) {
            throw ReglaNegocioException.invalida("La fecha de emisión no puede ser anterior a la fecha de la consulta.");
        }
        if (request.fechaEmision().isAfter(LocalDate.now())) {
            throw ReglaNegocioException.invalida("La fecha de emisión no puede ser futura.");
        }

        Medicamento medicamento = referencias.medicamentoVigente(request.medicamentoId());
        validarSinDuplicado(historia.getId(), medicamento.getId(), SIN_EXCLUIR);

        FormulaMedica formula = new FormulaMedica();
        formula.setHistoria(historia);
        formula.setMascota(mascota);
        formula.setVeterinario(veterinario);
        formula.setMedicamento(medicamento);
        formula.setFechaEmision(request.fechaEmision());
        aplicarPosologia(formula, request.dosisValor(), request.dosisUnidad(), request.frecuenciaHoras(),
                request.duracionDias(), request.indicaciones());
        formula.setActivo(true);
        return aDetalle(formulaRepository.save(formula));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FormulaDetalleResponse actualizar(Long id, FormulaActualizarRequest request) {
        FormulaMedica formula = buscar(id);
        if (!formula.isActivo()) {
            throw ReglaNegocioException.conflicto("Una fórmula inactiva no puede modificarse.");
        }
        validarHistoriaAbierta(formula.getHistoria());

        Medicamento medicamento = referencias.medicamentoVigente(request.medicamentoId());
        validarSinDuplicado(formula.getHistoria().getId(), medicamento.getId(), formula.getId());

        formula.setMedicamento(medicamento);
        aplicarPosologia(formula, request.dosisValor(), request.dosisUnidad(), request.frecuenciaHoras(),
                request.duracionDias(), request.indicaciones());
        return aDetalle(formulaRepository.save(formula));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FormulaDetalleResponse inactivar(Long id) {
        FormulaMedica formula = buscar(id);
        if (!formula.isActivo()) {
            throw ReglaNegocioException.conflicto("La fórmula ya está inactiva.");
        }
        formula.setActivo(false);
        return aDetalle(formulaRepository.save(formula));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public FormulaDetalleResponse activar(Long id) {
        FormulaMedica formula = buscar(id);
        if (formula.isActivo()) {
            throw ReglaNegocioException.conflicto("La fórmula ya está activa.");
        }
        validarSinDuplicado(formula.getHistoria().getId(), formula.getMedicamento().getId(), formula.getId());
        formula.setActivo(true);
        return aDetalle(formulaRepository.save(formula));
    }

    /**
     * Busca una fórmula o lanza 404.
     *
     * @param id identificador de la fórmula
     * @return la fórmula
     */
    private FormulaMedica buscar(Long id) {
        return formulaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La fórmula solicitada no existe."));
    }

    /**
     * Una historia cerrada es de solo lectura: no admite fórmulas nuevas ni cambios.
     *
     * @param historia historia clínica a revisar
     */
    private void validarHistoriaAbierta(HistoriaClinica historia) {
        if (historia.getEstado() == EstadoHistoria.CERRADA) {
            throw ReglaNegocioException.conflicto("La historia clínica está cerrada y no admite cambios en fórmulas.");
        }
    }

    /**
     * Evita duplicar información: no puede haber dos fórmulas activas del mismo medicamento en una historia.
     *
     * @param historiaId    historia clínica
     * @param medicamentoId medicamento
     * @param excluirId     fórmula que se ignora (la que se edita) o {@code -1}
     */
    private void validarSinDuplicado(Long historiaId, Long medicamentoId, Long excluirId) {
        if (formulaRepository.contarActivasDuplicadas(historiaId, medicamentoId, excluirId) > 0) {
            throw ReglaNegocioException.conflicto(
                    "Ya existe una fórmula activa de este medicamento para la misma consulta.");
        }
    }

    /**
     * Copia a la fórmula los datos de dosis, frecuencia, duración e indicaciones.
     *
     * @param formula         fórmula a modificar
     * @param dosisValor      cantidad de la dosis
     * @param dosisUnidad     unidad de la dosis
     * @param frecuenciaHoras frecuencia en horas
     * @param duracionDias    duración en días
     * @param indicaciones    indicaciones
     */
    private void aplicarPosologia(FormulaMedica formula, BigDecimal dosisValor, String dosisUnidad,
                                  Integer frecuenciaHoras, Integer duracionDias, String indicaciones) {
        formula.setDosisValor(dosisValor);
        formula.setDosisUnidad(dosisUnidad.trim());
        formula.setFrecuenciaHoras(frecuenciaHoras);
        formula.setDuracionDias(duracionDias);
        formula.setIndicaciones(indicaciones.trim());
    }

    /**
     * Construye el filtro dinámico del listado a partir de los criterios recibidos.
     *
     * @param f criterios de filtrado
     * @return especificación JPA
     */
    private Specification<FormulaMedica> construirEspecificacion(FormulaFiltro f) {
        Specification<FormulaMedica> spec = (root, query, cb) -> cb.conjunction();

        if (hayTexto(f.mascota())) {
            spec = spec.and((root, query, cb) ->
                    contiene(cb, root.get("mascota").<String>get("nombre"), f.mascota()));
        }
        if (hayTexto(f.cliente())) {
            spec = spec.and((root, query, cb) ->
                    contiene(cb, nombreCompleto(cb, root.get("mascota").get("cliente")), f.cliente()));
        }
        if (hayTexto(f.medicamento())) {
            spec = spec.and((root, query, cb) ->
                    contiene(cb, root.get("medicamento").<String>get("nombre"), f.medicamento()));
        }
        if (f.mascotaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mascota").get("id"), f.mascotaId()));
        }
        if (f.veterinarioId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("veterinario").get("id"), f.veterinarioId()));
        }
        if (f.activo() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), f.activo()));
        }
        if (f.desde() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.<LocalDate>get("fechaEmision"), f.desde()));
        }
        if (f.hasta() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.<LocalDate>get("fechaEmision"), f.hasta()));
        }
        return spec;
    }

    /**
     * Texto resumido de la posología, por ejemplo "250 mg cada 12 h por 7 días".
     *
     * @param f fórmula
     * @return posología legible
     */
    private String posologia(FormulaMedica f) {
        return f.getDosisValor().stripTrailingZeros().toPlainString() + " " + f.getDosisUnidad()
                + " cada " + f.getFrecuenciaHoras() + " h por " + f.getDuracionDias() + " días";
    }

    /**
     * Convierte la entidad en la vista RESUMIDA para el listado.
     *
     * @param f entidad fórmula
     * @return resumen de la fórmula
     */
    private FormulaResumenResponse aResumen(FormulaMedica f) {
        return new FormulaResumenResponse(f.getId(), f.getFechaCreacion(), f.getFechaEmision(),
                f.getMascota().getNombre(), f.getMascota().getCliente().getNombreCompleto(),
                f.getVeterinario().getNombreCompleto(), f.getMedicamento().getNombre(), posologia(f),
                f.isActivo());
    }

    /**
     * Convierte la entidad en la vista COMPLETA para el detalle.
     *
     * @param f entidad fórmula
     * @return detalle de la fórmula
     */
    private FormulaDetalleResponse aDetalle(FormulaMedica f) {
        HistoriaClinica h = f.getHistoria();
        return new FormulaDetalleResponse(f.getId(), h.getId(), h.getCita().getId(), h.getFechaHora(), h.getMotivo(),
                f.getMascota().getCliente().getId(), f.getMascota().getCliente().getNombreCompleto(),
                f.getMascota().getId(), f.getMascota().getNombre(), f.getVeterinario().getId(),
                f.getVeterinario().getNombreCompleto(), f.getMedicamento().getId(), f.getMedicamento().getNombre(),
                f.getMedicamento().getPresentacion(), f.getFechaEmision(), f.getDosisValor(), f.getDosisUnidad(),
                f.getFrecuenciaHoras(), f.getDuracionDias(), f.getIndicaciones(), f.isActivo(),
                f.getFechaCreacion(), f.getFechaActualizacion());
    }
}
