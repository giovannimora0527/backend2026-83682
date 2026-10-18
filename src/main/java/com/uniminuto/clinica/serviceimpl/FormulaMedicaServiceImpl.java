package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Implementa la consulta, validación, creación y actualización de fórmulas médicas.
 *
 * <p>La tabla {@code formula_medica} solo guarda la cita, el medicamento, la dosis y las
 * indicaciones. La mascota, el cliente y el veterinario se obtienen de la cita asociada.
 * La frecuencia, la duración y la fecha de emisión (cuando difiere de la fecha de registro)
 * se guardan como líneas etiquetadas dentro de {@code indicaciones}, el mismo formato que ya
 * usan los registros existentes ("Frecuencia: ...", "Duración en días: ...").</p>
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {
    private static final Set<String> SORTS = Set.of(
            "fechaCreacion", "formulaMedicaId", "cita.mascota.mascotaId",
            "cita.veterinario.nombres", "medicamentoEntity.nombre");
    private static final Map<String, String> SORT_ALIASES = Map.of(
            "fechaCreacionRegistro", "fechaCreacion",
            // La fecha de emisión se deriva de la fecha de registro de la fórmula.
            "fechaEmision", "fechaCreacion",
            "mascotaId", "cita.mascota.mascotaId",
            "veterinarioId", "cita.veterinario.nombres",
            "medicamento", "medicamentoEntity.nombre");
    private static final String FREQUENCY_PREFIX = "Frecuencia: ";
    private static final String DURATION_PREFIX = "Duración en días: ";
    private static final String INSTRUCTIONS_PREFIX = "Indicaciones: ";
    private static final String EMISSION_PREFIX = "Fecha de emisión: ";
    private final FormulaMedicaRepository formulas;
    private final HistoriaClinicaRepository historias;
    private final VeterinarioRepository veterinarios;
    private final CitaRepository citas;
    private final MedicamentoRepository medicamentos;

    /** Construye el servicio con los repositorios usados para fórmulas y sus relaciones clínicas. */
    public FormulaMedicaServiceImpl(FormulaMedicaRepository formulas,
            HistoriaClinicaRepository historias, VeterinarioRepository veterinarios, CitaRepository citas,
            MedicamentoRepository medicamentos) {
        this.formulas = formulas; this.historias = historias;
        this.veterinarios = veterinarios; this.citas = citas; this.medicamentos = medicamentos;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<FormulaResumenResponse> listar(int page, int size, String sort, String direction,
            Long clienteId, Integer mascotaId, Long vetId, String medicamento, LocalDate fecha) {
        // Translate public/API field names to a whitelisted JPA property path.
        // Keep `mascotaId` compatible with Angular while sorting through the formula's citation.
        sort = sort == null ? null : SORT_ALIASES.getOrDefault(sort, sort);
        Pageable pageable = PageSupport.pageable(page, size, sort, direction, "fechaCreacion", SORTS);
        Specification<FormulaMedica> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            Join<FormulaMedica, Cita> cita = root.join("cita", JoinType.LEFT);
            Join<Cita, Mascota> mascotaCita = cita.join("mascota", JoinType.LEFT);
            Join<Cita, Cliente> clienteCita = cita.join("cliente", JoinType.LEFT);
            Join<Cita, Veterinario> veterinarioCita = cita.join("veterinario", JoinType.LEFT);
            Join<Mascota, Cliente> clienteMascota = mascotaCita.join("cliente", JoinType.LEFT);
            var clienteRelacionado = cb.coalesce(clienteCita.get("clienteId"), clienteMascota.get("clienteId"));
            if (clienteId != null) p.add(cb.equal(clienteRelacionado, clienteId));
            if (mascotaId != null) p.add(cb.equal(mascotaCita.get("mascotaId"), mascotaId));
            if (vetId != null) p.add(cb.equal(veterinarioCita.get("veterinarioId"), vetId));
            if (medicamento != null && !medicamento.isBlank()) {
                Join<FormulaMedica, Medicamento> med = root.join("medicamentoEntity", JoinType.LEFT);
                p.add(cb.like(cb.lower(med.get("nombre")), "%" + medicamento.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (fecha != null) {
                var indicaciones = root.<String>get("indicaciones");
                Predicate emisionGuardada = cb.like(indicaciones, "%" + EMISSION_PREFIX + fecha + "%");
                Predicate sinEmisionGuardada = cb.or(cb.isNull(indicaciones),
                        cb.notLike(indicaciones, "%" + EMISSION_PREFIX + "%"));
                Predicate fechaRegistro = cb.and(sinEmisionGuardada,
                        cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fechaCreacion"), fecha.atStartOfDay()),
                        cb.lessThan(root.<LocalDateTime>get("fechaCreacion"), fecha.plusDays(1).atStartOfDay()));
                p.add(cb.or(emisionGuardada, fechaRegistro));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PageResponse.from(formulas.findAll(spec, pageable).map(this::summary));
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public List<FormulaResponse> listarOrdenadas(boolean ascendente) {
        Sort sort = Sort.by(ascendente ? Sort.Direction.ASC : Sort.Direction.DESC, "fechaCreacion");
        return formulas.findAll(sort).stream().map(f -> response(f, false)).toList();
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public FormulaResponse consultar(Long id) { return response(formulas.findById(id).orElseThrow(this::missing), true); }
    /** {@inheritDoc} */
    @Transactional public FormulaResponse crear(FormulaRequest request) { return save(null, request); }
    /** {@inheritDoc} */
    @Transactional public FormulaResponse actualizar(Long id, FormulaRequest request) {
        FormulaMedica existing = formulas.findById(id).orElseThrow(this::missing);
        return save(existing, request);
    }

    private FormulaResponse save(FormulaMedica existing, FormulaRequest request) {
        validate(request);
        Veterinario veterinario = veterinarios.findById(request.veterinarioId())
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Medico no encontrado"));

        HistoriaClinica historia = request.historiaClinicaId() == null ? null
                : historias.findById(request.historiaClinicaId())
                        .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Historia clinica no encontrada"));
        if (historia != null && historia.getMascota() == null)
            throw new BadRequestException(HttpStatus.CONFLICT, "La historia clinica no tiene una mascota valida");
        if (historia != null && request.mascotaId() != null
                && !request.mascotaId().equals(historia.getMascota().getMascotaId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota indicada no coincide con la historia clinica");
        if (historia != null && historia.getFechaHora() != null
                && request.fechaEmision().isBefore(historia.getFechaHora().toLocalDate()))
            throw new BadRequestException("La fecha de emision no puede ser anterior a la historia clinica");

        Cita cita = resolveCita(existing, request, historia, veterinario);
        // Una cita cancelada no es una consulta realizada: no se permite como nueva asociación.
        // Si la fórmula ya estaba asociada a esa cita, se conserva para no alterar el histórico.
        boolean mismaCita = existing != null && existing.getCita() == cita;
        if (cita.getEstado() == EstadoCita.CANCELADA && !mismaCita)
            throw new BadRequestException(HttpStatus.CONFLICT, "No se puede asociar la formula a una cita cancelada");
        Mascota mascota = cita.getMascota();
        if (mascota == null)
            throw new BadRequestException(HttpStatus.CONFLICT, "La cita no tiene una mascota valida");
        if (request.mascotaId() != null && !request.mascotaId().equals(mascota.getMascotaId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota indicada no coincide con la asociacion clinica");
        if (historia != null && !historia.getMascota().getMascotaId().equals(mascota.getMascotaId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota de la cita no coincide con la historia clinica");
        if (cita.getVeterinario() == null
                || !cita.getVeterinario().getVeterinarioId().equals(veterinario.getVeterinarioId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La cita no coincide con la mascota y el medico indicados");
        if (mascota.getCliente() == null || cita.getCliente() == null
                || !mascota.getCliente().getClienteId().equals(cita.getCliente().getClienteId()))
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "El cliente de la cita no corresponde al propietario de la mascota");
        if (cita.getFechaHora() != null
                && request.fechaEmision().isBefore(cita.getFechaHora().toLocalDate()))
            throw new BadRequestException("La fecha de emision no puede ser anterior a la cita");

        Medicamento medicamento = request.medicamentoId() == null
                ? medicamentos.findFirstByNombreIgnoreCase(request.medicamento().trim())
                        .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND,
                                "El medicamento no existe en el inventario"))
                : medicamentos.findById(request.medicamentoId())
                        .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND,
                                "Medicamento no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();
        FormulaMedica formula = existing == null ? new FormulaMedica() : existing;
        if (formula.getFechaCreacion() == null) formula.setFechaCreacion(ahora);
        formula.setCita(cita);
        formula.setMedicamentoEntity(medicamento);
        formula.setDosisTexto(request.dosis().stripTrailingZeros().toPlainString());
        formula.setIndicaciones(storeInstructions(request, formula.getFechaCreacion().toLocalDate()));
        if (existing != null) formula.setFechaActualizacion(ahora);
        return response(formulas.save(formula), true);
    }

    /**
     * Determina la cita de la fórmula: la indicada, la ya asociada (si sigue coincidiendo)
     * o la cita más reciente no cancelada entre la mascota y el veterinario.
     */
    private Cita resolveCita(FormulaMedica existing, FormulaRequest request, HistoriaClinica historia,
            Veterinario veterinario) {
        if (request.citaId() != null)
            return citas.findById(request.citaId())
                    .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        Integer mascotaId = historia != null ? historia.getMascota().getMascotaId() : request.mascotaId();
        if (existing != null && existing.getCita() != null && existing.getCita().getMascota() != null
                && existing.getCita().getVeterinario() != null
                && existing.getCita().getMascota().getMascotaId().equals(mascotaId)
                && existing.getCita().getVeterinario().getVeterinarioId().equals(veterinario.getVeterinarioId()))
            return existing.getCita();
        return citas.findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdAndEstadoNotOrderByFechaHoraDesc(
                        mascotaId, veterinario.getVeterinarioId(), EstadoCita.CANCELADA)
                .orElseThrow(() -> new BadRequestException(HttpStatus.CONFLICT,
                        "La formula requiere una cita no cancelada entre la mascota y el veterinario"));
    }

    private void validate(FormulaRequest request) {
        if (request == null || request.veterinarioId() == null || request.fechaEmision() == null)
            throw new BadRequestException("veterinarioId y fechaEmision son obligatorios");
        if (request.citaId() == null && request.historiaClinicaId() == null && request.mascotaId() == null)
            throw new BadRequestException("Debe asociar la formula a una cita o historia clinica");
        PageSupport.required(request.medicamento(), "medicamento");
        PageSupport.required(request.frecuencia(), "frecuencia");
        if (request.medicamento().length() > 100 || request.frecuencia().length() > 120)
            throw new BadRequestException("medicamento o frecuencia supera la longitud permitida");
        if (request.dosis() == null || request.dosis().compareTo(BigDecimal.ZERO) <= 0
                || request.dosis().precision() > 12 || request.dosis().scale() > 3)
            throw new BadRequestException("dosis debe ser un valor positivo con maximo 3 decimales");
        if (request.duracionDias() == null || request.duracionDias() <= 0)
            throw new BadRequestException("duracionDias debe ser positiva");
        if (request.indicaciones() != null && request.indicaciones().length() > 5000)
            throw new BadRequestException("indicaciones supera la longitud permitida");
    }

    private String storeInstructions(FormulaRequest request, LocalDate fechaRegistro) {
        StringBuilder text = new StringBuilder(FREQUENCY_PREFIX).append(request.frecuencia().trim())
                .append("\n").append(DURATION_PREFIX).append(request.duracionDias());
        if (!request.fechaEmision().equals(fechaRegistro))
            text.append("\n").append(EMISSION_PREFIX).append(request.fechaEmision());
        if (request.indicaciones() != null && !request.indicaciones().isBlank())
            text.append("\n").append(INSTRUCTIONS_PREFIX).append(request.indicaciones().trim());
        return text.toString();
    }

    private FormulaResponse response(FormulaMedica formula, boolean incluirHistoria) {
        Cita cita = formula.getCita();
        Mascota mascota = cita == null ? null : cita.getMascota();
        Cliente cliente = cita != null && cita.getCliente() != null ? cita.getCliente()
                : mascota == null ? null : mascota.getCliente();
        Veterinario veterinario = cita == null ? null : cita.getVeterinario();
        Long historiaId = !incluirHistoria || mascota == null ? null
                : historias.findFirstByMascota_MascotaIdOrderByFechaHoraAsc(mascota.getMascotaId())
                        .map(HistoriaClinica::getHistoriaClinicaId).orElse(null);
        InstructionData instructionData = parseInstructions(formula.getIndicaciones());
        LocalDate fechaEmision = instructionData.emissionDate() != null ? instructionData.emissionDate()
                : formula.getFechaCreacion() == null ? null : formula.getFechaCreacion().toLocalDate();
        Medicamento medicamento = formula.getMedicamentoEntity();
        return new FormulaResponse(formula.getFormulaMedicaId(), mascota == null ? null : mascota.getMascotaId(),
                mascota == null ? null : mascota.getNombreMascota(), cliente == null ? null : cliente.getClienteId(),
                cliente == null ? null : cliente.getNombres()+" "+cliente.getApellidos(),
                historiaId,
                veterinario == null ? null : veterinario.getVeterinarioId(),
                veterinario == null ? null : veterinario.getNombres()+" "+veterinario.getApellidos(),
                fechaEmision,
                medicamento == null ? null : medicamento.getNombre(), parseDose(formula.getDosisTexto()),
                instructionData.frequency(), instructionData.durationDays(), instructionData.instructions(),
                formula.getFechaCreacion(), formula.getDosisTexto());
    }

    private FormulaResumenResponse summary(FormulaMedica formula) {
        FormulaResponse response = response(formula, false);
        return new FormulaResumenResponse(response.formulaMedicaId(), response.mascotaId(), response.mascotaNombre(),
                response.clienteId(), response.clienteNombre(), response.veterinarioId(), response.veterinarioNombre(),
                response.fechaEmision(), response.medicamento(), response.fechaCreacion(),
                response.dosis(), response.dosisTexto(), response.frecuencia(), response.clienteNombre());
    }

    private BigDecimal parseDose(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            // Legacy rows may contain instructions such as "Una pastilla cada 12 horas".
            // Only treat the entire stored value as a numeric dose.
            return new BigDecimal(text.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private InstructionData parseInstructions(String text) {
        if (text == null) return new InstructionData(null, null, null, null);
        String frequency = null, instructions = null;
        Integer duration = null;
        LocalDate emission = null;
        for (String line : text.split("\\R")) {
            if (line.startsWith(FREQUENCY_PREFIX)) frequency = line.substring(FREQUENCY_PREFIX.length());
            else if (line.startsWith(DURATION_PREFIX)) {
                try { duration = Integer.parseInt(line.substring(DURATION_PREFIX.length()).trim()); } catch (NumberFormatException ignored) { }
            } else if (line.startsWith(EMISSION_PREFIX)) {
                try { emission = LocalDate.parse(line.substring(EMISSION_PREFIX.length()).trim()); } catch (DateTimeParseException ignored) { }
            } else if (line.startsWith(INSTRUCTIONS_PREFIX)) instructions = line.substring(INSTRUCTIONS_PREFIX.length());
            else if (!line.isBlank()) instructions = instructions == null ? line : instructions + "\n" + line;
        }
        return new InstructionData(frequency, duration, instructions, emission);
    }

    private record InstructionData(String frequency, Integer durationDays, String instructions, LocalDate emissionDate) { }
    private BadRequestException missing() { return new BadRequestException(HttpStatus.NOT_FOUND, "Fórmula médica no encontrada"); }
}
