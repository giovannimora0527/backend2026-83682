package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/** Implementa las consultas y la creación de historias clínicas y su anotación inicial. */
/** Implementa las consultas y la creación de historias clínicas y su anotación inicial. */
@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {
    private static final Set<String> SORTS = Set.of("fechaHora", "historiaClinicaId");
    private final HistoriaClinicaRepository historias;
    private final ClienteRepository clientes;
    private final MascotaRepository mascotas;
    private final CitaRepository citas;
    private final VeterinarioRepository veterinarios;
    private final AnotacionRepository anotaciones;

    /** Construye el servicio con repositorios de historias y de las entidades clínicas relacionadas. */
    public HistoriaClinicaServiceImpl(HistoriaClinicaRepository historias, ClienteRepository clientes,
            MascotaRepository mascotas, CitaRepository citas, VeterinarioRepository veterinarios,
            AnotacionRepository anotaciones) {
        this.historias = historias;
        this.clientes = clientes;
        this.mascotas = mascotas;
        this.citas = citas;
        this.veterinarios = veterinarios;
        this.anotaciones = anotaciones;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<HistoriaResumenResponse> listar(int page, int size, String sort, String direction,
            Long clienteId, Integer mascotaId, Long veterinarioId, LocalDate fecha) {
        Pageable pageable = PageSupport.pageable(page, size, sort, direction, "fechaHora", SORTS);
        Specification<HistoriaClinica> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (clienteId != null) p.add(cb.equal(root.get("mascota").get("cliente").get("clienteId"), clienteId));
            if (mascotaId != null) p.add(cb.equal(root.get("mascota").get("mascotaId"), mascotaId));
            if (veterinarioId != null) {
                var subquery = query.subquery(Long.class);
                var note = subquery.from(Anotacion.class);
                subquery.select(note.get("historiaClinica").get("historiaClinicaId"))
                        .where(cb.equal(note.get("veterinario").get("veterinarioId"), veterinarioId));
                p.add(root.get("historiaClinicaId").in(subquery));
            }
            if (fecha != null) {
                java.time.LocalDateTime start = fecha.atStartOfDay();
                p.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), start));
                p.add(cb.lessThan(root.get("fechaHora"), fecha.plusDays(1).atStartOfDay()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PageResponse.from(historias.findAll(spec, pageable).map(this::summary));
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public HistoriaResponse consultar(Long id) {
        return response(historias.findById(id).orElseThrow(() -> missing("Historia clinica")));
    }

    /** {@inheritDoc} */
    @Transactional
    public HistoriaResponse crear(HistoriaRequest r) {
        validate(r);
        Cliente cliente = clientes.findById(r.clienteId()).orElseThrow(() -> missing("Cliente"));
        Mascota mascota = mascotas.findById(r.mascotaId()).orElseThrow(() -> missing("Mascota"));
        if (mascota.getCliente() == null || !mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota no pertenece al cliente indicado");
        }
        Veterinario vet = veterinarios.findById(r.veterinarioId()).orElseThrow(() -> missing("Veterinario"));
        Cita cita = null;
        if (r.citaId() != null) {
            cita = citas.findById(r.citaId()).orElseThrow(() -> missing("Cita"));
            if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())
                    || !cita.getCliente().getClienteId().equals(cliente.getClienteId())) {
                throw new BadRequestException(HttpStatus.CONFLICT,
                        "La mascota y cliente deben coincidir con la cita asociada");
            }
            if (!cita.getVeterinario().getVeterinarioId().equals(vet.getVeterinarioId())) {
                throw new BadRequestException(HttpStatus.CONFLICT,
                        "El veterinario debe coincidir con el de la cita");
            }
            if (cita.getEstado() != EstadoCita.ATENDIDA) {
                throw new BadRequestException(HttpStatus.CONFLICT,
                        "Solo una cita atendida puede asociarse a una historia clinica");
            }
            if (historias.existsByCita_CitaId(cita.getCitaId())) {
                throw new BadRequestException(HttpStatus.CONFLICT, "La cita ya tiene una historia clinica");
            }
        }

        HistoriaClinica entity = new HistoriaClinica();
        entity.setCliente(cliente);
        entity.setMascota(mascota);
        entity.setCita(cita);
        entity.setVeterinario(vet);
        entity.setFechaHora(r.fechaHora());
        entity.setMotivoConsulta(r.motivoConsulta().trim());
        entity.setDiagnostico(r.diagnostico().trim());
        entity.setObservaciones(blankToNull(r.observaciones()));
        HistoriaClinica saved = historias.save(entity);

        // Exactly one annotation stores all clinical text in the existing TEXT column.
        Anotacion note = new Anotacion();
        note.setHistoriaClinica(saved);
        note.setVeterinario(vet);
        note.setFechaHora(r.fechaHora());
        note.setDescripcion(HistoriaClinicaNarrativeCodec.encode(r.motivoConsulta().trim(),
                r.diagnostico().trim(), blankToNull(r.observaciones())));
        anotaciones.save(note);

        return response(saved);
    }

    private void validate(HistoriaRequest r) {
        if (r == null || r.clienteId() == null || r.mascotaId() == null || r.veterinarioId() == null) {
            throw new BadRequestException("clienteId, mascotaId y veterinarioId son obligatorios");
        }
        if (r.fechaHora() == null) throw new BadRequestException("fechaHora es obligatoria");
        PageSupport.required(r.motivoConsulta(), "motivoConsulta");
        PageSupport.required(r.diagnostico(), "diagnostico");
        if (r.motivoConsulta().length() > 500) {
            throw new BadRequestException("motivoConsulta no puede superar 500 caracteres");
        }
    }

    private HistoriaResponse response(HistoriaClinica h) {
        Cliente cliente = h.getCliente() != null ? h.getCliente() : h.getMascota().getCliente();
        Anotacion note = latestNote(h);
        Veterinario vet = h.getVeterinario() != null ? h.getVeterinario() : note == null ? null : note.getVeterinario();
        HistoriaClinicaNarrativeCodec.Narrative clinicalData = clinicalData(h, note);
        return new HistoriaResponse(h.getHistoriaClinicaId(), cliente == null ? null : cliente.getClienteId(),
                cliente == null ? null : cliente.getNombres() + " " + cliente.getApellidos(),
                h.getMascota().getMascotaId(), h.getMascota().getNombreMascota(),
                h.getCita() == null ? null : h.getCita().getCitaId(),
                vet == null ? null : vet.getVeterinarioId(),
                vet == null ? null : vet.getNombres() + " " + vet.getApellidos(),
                h.getFechaHora(), clinicalData.motivoConsulta(), clinicalData.diagnostico(),
                clinicalData.observaciones());
    }

    private HistoriaResumenResponse summary(HistoriaClinica h) {
        Cliente cliente = h.getCliente() != null ? h.getCliente() : h.getMascota().getCliente();
        Anotacion note = latestNote(h);
        Veterinario vet = h.getVeterinario() != null ? h.getVeterinario() : note == null ? null : note.getVeterinario();
        HistoriaClinicaNarrativeCodec.Narrative clinicalData = clinicalData(h, note);
        return new HistoriaResumenResponse(h.getHistoriaClinicaId(), cliente == null ? null : cliente.getClienteId(),
                cliente == null ? null : cliente.getNombres() + " " + cliente.getApellidos(),
                h.getMascota().getMascotaId(), h.getMascota().getNombreMascota(),
                vet == null ? null : vet.getVeterinarioId(),
                vet == null ? null : vet.getNombres() + " " + vet.getApellidos(),
                h.getFechaHora(), clinicalData.motivoConsulta());
    }

    private HistoriaClinicaNarrativeCodec.Narrative clinicalData(HistoriaClinica historia, Anotacion note) {
        // Transient fields are available on the immediate create response only.
        if (historia.getMotivoConsulta() != null || historia.getDiagnostico() != null
                || historia.getObservaciones() != null) {
            return new HistoriaClinicaNarrativeCodec.Narrative(historia.getMotivoConsulta(),
                    historia.getDiagnostico(), historia.getObservaciones(), true);
        }
        // Legacy descriptions stay intact as the reason; they are not parsed into other fields.
        return note == null
                ? new HistoriaClinicaNarrativeCodec.Narrative(null, null, null, false)
                : HistoriaClinicaNarrativeCodec.decode(note.getDescripcion());
    }

    private Anotacion latestNote(HistoriaClinica h) {
        if (h.getHistoriaClinicaId() == null) return null;
        List<Anotacion> notes = anotaciones.findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(
                h.getHistoriaClinicaId());
        return notes.isEmpty() ? null : notes.get(0);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BadRequestException missing(String name) {
        return new BadRequestException(HttpStatus.NOT_FOUND, name + " no encontrada");
    }
}
