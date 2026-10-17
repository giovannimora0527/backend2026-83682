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
import java.util.*;

/** Implementa la consulta, validación, creación y actualización de fórmulas médicas. */
/** Implementa la consulta, validación, creación y actualización de fórmulas médicas. */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {
    private static final Set<String> SORTS = Set.of(
            "fechaCreacion", "formulaMedicaId", "cita.mascota.mascotaId");
    private static final Map<String, String> SORT_ALIASES = Map.of(
            "fechaCreacionRegistro", "fechaCreacion",
            // Until the schema migration is applied, emission date follows the
            // existing API meaning: the formula creation timestamp.
            "fechaEmision", "fechaCreacion",
            "mascotaId", "cita.mascota.mascotaId");
    private static final String FREQUENCY_PREFIX = "Frecuencia: ";
    private static final String DURATION_PREFIX = "Duración en días: ";
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
            Join<FormulaMedica, HistoriaClinica> historia = root.join("historiaClinica", JoinType.LEFT);
            Join<HistoriaClinica, Mascota> mascotaHistoria = historia.join("mascota", JoinType.LEFT);
            Join<Mascota, Cliente> clienteHistoria = mascotaHistoria.join("cliente", JoinType.LEFT);
            Join<FormulaMedica, Veterinario> veterinarioDirecto = root.join("veterinario", JoinType.LEFT);
            var mascotaRelacionada = cb.coalesce(mascotaCita.get("mascotaId"), mascotaHistoria.get("mascotaId"));
            var clienteRelacionado = cb.coalesce(clienteCita.get("clienteId"), clienteHistoria.get("clienteId"));
            var veterinarioRelacionado = cb.coalesce(veterinarioCita.get("veterinarioId"),
                    veterinarioDirecto.get("veterinarioId"));
            if (clienteId != null) p.add(cb.equal(clienteRelacionado, clienteId));
            if (mascotaId != null) p.add(cb.equal(mascotaRelacionada, mascotaId));
            if (vetId != null) p.add(cb.equal(veterinarioRelacionado, vetId));
            if (medicamento != null && !medicamento.isBlank())
                p.add(cb.like(cb.lower(root.get("medicamentoEntity").get("nombre")), "%" + medicamento.trim().toLowerCase(Locale.ROOT) + "%"));
            if (fecha != null) {
                Predicate fechaEmisionPersistida = cb.equal(root.get("fechaEmision"), fecha);
                Predicate fechaAntigua = cb.and(cb.isNull(root.get("fechaEmision")),
                        cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fechaCreacion"), fecha.atStartOfDay()),
                        cb.lessThan(root.<LocalDateTime>get("fechaCreacion"), fecha.plusDays(1).atStartOfDay()));
                p.add(cb.or(fechaEmisionPersistida, fechaAntigua));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PageResponse.from(formulas.findAll(spec, pageable).map(this::summary));
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public FormulaResponse consultar(Long id) { return response(formulas.findById(id).orElseThrow(this::missing)); }
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
        Cita cita = request.citaId() == null ? null
                : citas.findById(request.citaId())
                        .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        // Compatibility with older Angular payloads that send mascotaId/veterinarioId
        // but omit both association IDs: resolve and persist a real matching appointment.
        if (cita == null && historia == null) {
            cita = citas.findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdOrderByFechaHoraDesc(
                            request.mascotaId(), veterinario.getVeterinarioId())
                    .orElseThrow(() -> new BadRequestException(HttpStatus.CONFLICT,
                            "La formula requiere una cita o historia clinica existente"));
        }

        Mascota mascota;
        if (cita != null) {
            mascota = cita.getMascota();
            if (mascota == null)
                throw new BadRequestException(HttpStatus.CONFLICT, "La cita no tiene una mascota valida");
        } else {
            mascota = historia.getMascota();
            if (mascota == null)
                throw new BadRequestException(HttpStatus.CONFLICT, "La historia clinica no tiene una mascota valida");
        }

        if (request.mascotaId() != null && !request.mascotaId().equals(mascota.getMascotaId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota indicada no coincide con la asociacion clinica");
        if (historia != null && (historia.getMascota() == null
                || !historia.getMascota().getMascotaId().equals(mascota.getMascotaId())))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota de la cita no coincide con la historia clinica");

        if (cita != null) {
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
        } else if (mascota.getCliente() == null) {
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "La mascota de la historia clinica no tiene un cliente asociado");
        }

        if (historia != null && historia.getFechaHora() != null
                && request.fechaEmision().isBefore(historia.getFechaHora().toLocalDate()))
            throw new BadRequestException("La fecha de emision no puede ser anterior a la historia clinica");

        Medicamento medicamento = request.medicamentoId() == null
                ? medicamentos.findFirstByNombreIgnoreCase(request.medicamento().trim())
                        .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND,
                                "El medicamento no existe en el inventario"))
                : medicamentos.findById(request.medicamentoId())
                        .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND,
                                "Medicamento no encontrado"));

        FormulaMedica formula = existing == null ? new FormulaMedica() : existing;
        formula.setCita(cita);
        formula.setHistoriaClinica(historia);
        formula.setVeterinario(veterinario);
        formula.setFechaEmision(request.fechaEmision());
        formula.setMedicamentoEntity(medicamento);
        formula.setDosisTexto(request.dosis().stripTrailingZeros().toPlainString());
        formula.setIndicaciones(storeInstructions(request));
        if (formula.getFechaCreacion() == null) formula.setFechaCreacion(LocalDateTime.now());
        formula.setFechaActualizacion(LocalDateTime.now());
        return response(formulas.save(formula));
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
    private String storeInstructions(FormulaRequest request) {
        String text = FREQUENCY_PREFIX + request.frecuencia().trim() + "\n" + DURATION_PREFIX + request.duracionDias();
        return request.indicaciones() == null || request.indicaciones().isBlank() ? text : text + "\nIndicaciones: " + request.indicaciones().trim();
    }

    private FormulaResponse response(FormulaMedica formula) {
        Cita cita = formula.getCita();
        HistoriaClinica historia = formula.getHistoriaClinica();
        Mascota mascota = cita != null ? cita.getMascota() : historia == null ? null : historia.getMascota();
        Cliente cliente = cita != null && cita.getCliente() != null ? cita.getCliente()
                : mascota == null ? null : mascota.getCliente();
        Veterinario veterinario = cita != null && cita.getVeterinario() != null
                ? cita.getVeterinario() : formula.getVeterinario();
        InstructionData instructionData = parseInstructions(formula.getIndicaciones());
        return new FormulaResponse(formula.getFormulaMedicaId(), mascota == null ? null : mascota.getMascotaId(),
                mascota == null ? null : mascota.getNombreMascota(), cliente == null ? null : cliente.getClienteId(),
                cliente == null ? null : cliente.getNombres()+" "+cliente.getApellidos(),
                historia == null ? null : historia.getHistoriaClinicaId(),
                veterinario == null ? null : veterinario.getVeterinarioId(),
                veterinario == null ? null : veterinario.getNombres()+" "+veterinario.getApellidos(),
                formula.getFechaEmision() != null ? formula.getFechaEmision()
                        : formula.getFechaCreacion() == null ? null : formula.getFechaCreacion().toLocalDate(),
                formula.getMedicamentoEntity().getNombre(), parseDose(formula.getDosisTexto()), instructionData.frequency(),
                instructionData.durationDays(), instructionData.instructions(), formula.getFechaCreacion(),
                formula.getDosisTexto());
    }

    private FormulaResumenResponse summary(FormulaMedica formula) {
        FormulaResponse response = response(formula);
        return new FormulaResumenResponse(response.formulaMedicaId(), response.mascotaId(), response.mascotaNombre(),
                response.clienteId(), response.clienteNombre(), response.veterinarioId(), response.veterinarioNombre(),
                response.fechaEmision(), response.medicamento(), response.fechaCreacion());
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
        if (text == null) return new InstructionData(null, null, null);
        String frequency = null, instructions = null;
        Integer duration = null;
        for (String line : text.split("\\R")) {
            if (line.startsWith(FREQUENCY_PREFIX)) frequency = line.substring(FREQUENCY_PREFIX.length());
            else if (line.startsWith(DURATION_PREFIX)) {
                try { duration = Integer.parseInt(line.substring(DURATION_PREFIX.length()).trim()); } catch (NumberFormatException ignored) { }
            } else if (line.startsWith("Indicaciones: ")) instructions = line.substring("Indicaciones: ".length());
            else if (!line.isBlank()) instructions = instructions == null ? line : instructions + "\n" + line;
        }
        return new InstructionData(frequency, duration, instructions);
    }

    private record InstructionData(String frequency, Integer durationDays, String instructions) { }
    private BadRequestException missing() { return new BadRequestException(HttpStatus.NOT_FOUND, "Fórmula médica no encontrada"); }
}
