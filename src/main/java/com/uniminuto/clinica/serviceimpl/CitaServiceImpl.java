package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import jakarta.persistence.criteria.Predicate;
import java.time.*;
import java.util.*;

/** Implementa las consultas, validaciones y operaciones de escritura de citas. */
/** Implementa las consultas, validaciones y operaciones de escritura de citas. */
@Service
public class CitaServiceImpl implements CitaService {
    private static final Set<String> SORTS = Set.of("fechaHora", "estado", "citaId",
            "cliente.nombres", "mascota.nombreMascota", "veterinario.nombres");
    /** Columnas que ordena la tabla de Angular traducidas a propiedades de la entidad Cita. */
    private static final Map<String, String> SORT_ALIASES = Map.of(
            "fecha", "fechaHora", "hora", "fechaHora",
            "clienteId", "cliente.nombres", "mascotaId", "mascota.nombreMascota", "veterinarioId", "veterinario.nombres");
    private final CitaRepository citas;
    private final ClienteRepository clientes;
    private final MascotaRepository mascotas;
    private final VeterinarioRepository veterinarios;

    /** Construye el servicio con los repositorios de citas y sus entidades relacionadas. */
    public CitaServiceImpl(CitaRepository citas, ClienteRepository clientes, MascotaRepository mascotas, VeterinarioRepository veterinarios) {
        this.citas = citas; this.clientes = clientes; this.mascotas = mascotas; this.veterinarios = veterinarios;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<CitaResponse> listar(int page, int size, String sort, String direction, Long clienteId,
            Integer mascotaId, Long veterinarioId, LocalDate fecha, EstadoCita estado) {
        if (page < 0 || size < 1 || size > 100) throw new BadRequestException("page debe ser >= 0 y size debe estar entre 1 y 100");
        Pageable pageable = sort == null || sort.isBlank()
                ? PageRequest.of(page, size, Sort.by(Sort.Order.desc("fechaHora")))
                : PageSupport.pageable(page, size, SORT_ALIASES.getOrDefault(sort, sort), direction, "fechaHora", SORTS);
        // La columna Hora ordena por la hora del día (no por fecha y hora), mediante HOUR/MINUTE.
        boolean ordenarPorHora = "hora".equals(sort);
        boolean horaAscendente = ordenarPorHora && pageable.getSort().getOrderFor("fechaHora").isAscending();
        if (ordenarPorHora) pageable = PageRequest.of(page, size);
        Specification<Cita> spec = (root, query, cb) -> {
            if (ordenarPorHora && !Long.class.equals(query.getResultType())) {
                var hora = cb.function("hour", Integer.class, root.get("fechaHora"));
                var minuto = cb.function("minute", Integer.class, root.get("fechaHora"));
                query.orderBy(horaAscendente ? List.of(cb.asc(hora), cb.asc(minuto), cb.asc(root.get("fechaHora")))
                        : List.of(cb.desc(hora), cb.desc(minuto), cb.desc(root.get("fechaHora"))));
            }
            List<Predicate> p = new ArrayList<>();
            if (clienteId != null) p.add(cb.equal(root.get("cliente").get("clienteId"), clienteId));
            if (mascotaId != null) p.add(cb.equal(root.get("mascota").get("mascotaId"), mascotaId));
            if (veterinarioId != null) p.add(cb.equal(root.get("veterinario").get("veterinarioId"), veterinarioId));
            if (fecha != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), fecha.atStartOfDay()));
                p.add(cb.lessThan(root.get("fechaHora"), fecha.plusDays(1).atStartOfDay()));
            }
            if (estado != null) p.add(cb.equal(root.get("estado"), estado));
            return cb.and(p.toArray(Predicate[]::new));
        };
        // Si el usuario elige otro orden, prevalece el Pageable sobre el orden predeterminado de la consulta.
        Page<CitaResponse> result = citas.findAll(spec, pageable).map(this::response);
        return PageResponse.from(result);
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public List<CitaResponse> filtrarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFinal) {
        if (fechaInicio == null || fechaFinal == null)
            throw new BadRequestException("fechaInicio y fechaFinal son obligatorias");
        if (fechaInicio.isAfter(fechaFinal))
            throw new BadRequestException("fechaInicio no puede ser posterior a fechaFinal");
        return citas.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFinal)
                .stream().map(this::response).toList();
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public CitaResponse consultar(Long id) { return response(citas.findById(id).orElseThrow(() -> missing("Cita"))); }

    /** {@inheritDoc} */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CitaResponse crear(CitaRequest request) {
        validate(request, null);
        Cliente cliente = clientes.findById(request.clienteId()).orElseThrow(() -> missing("Cliente"));
        Mascota mascota = mascotas.lockById(request.mascotaId()).orElseThrow(() -> missing("Mascota"));
        if (mascota.getCliente() == null || !mascota.getCliente().getClienteId().equals(cliente.getClienteId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota no pertenece al cliente indicado");
        Veterinario veterinario = veterinarios.lockById(request.veterinarioId()).orElseThrow(() -> missing("Veterinario"));
        checkAvailability(veterinario, mascota, request.fecha(), request.hora(), null);
        Cita cita = new Cita();
        cita.setCliente(cliente); cita.setMascota(mascota); cita.setVeterinario(veterinario);
        cita.setFecha(request.fecha()); cita.setHora(request.hora()); cita.setMotivoConsulta(request.motivoConsulta().trim());
        cita.setEstado(request.estado() == null ? EstadoCita.PENDIENTE : request.estado());
        return response(citas.save(cita));
    }

    /** {@inheritDoc} */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CitaResponse actualizar(Long id, CitaRequest request) {
        Cita cita = citas.findById(id).orElseThrow(() -> missing("Cita"));
        if (cita.getEstado() == EstadoCita.CANCELADA) throw new BadRequestException(HttpStatus.CONFLICT, "Una cita cancelada no se puede editar ni reactivar mediante esta operación");
        if (cita.getEstado() == EstadoCita.ATENDIDA) throw new BadRequestException(HttpStatus.CONFLICT, "Una cita atendida conserva su información histórica");
        validate(request, cita);
        Cliente cliente = clientes.findById(request.clienteId()).orElseThrow(() -> missing("Cliente"));
        Mascota mascota = mascotas.lockById(request.mascotaId()).orElseThrow(() -> missing("Mascota"));
        if (mascota.getCliente() == null || !mascota.getCliente().getClienteId().equals(cliente.getClienteId()))
            throw new BadRequestException(HttpStatus.CONFLICT, "La mascota no pertenece al cliente indicado");
        Veterinario veterinario = veterinarios.lockById(request.veterinarioId()).orElseThrow(() -> missing("Veterinario"));
        checkAvailability(veterinario, mascota, request.fecha(), request.hora(), id);
        EstadoCita requested = request.estado() == null ? cita.getEstado() : request.estado();
        if (!validTransition(cita.getEstado(), requested)) throw new BadRequestException(HttpStatus.CONFLICT, "Transición de estado de cita no permitida");
        // Las citas activas (pendiente, confirmada o programada) pueden reprogramarse; las atendidas y
        // canceladas ya se rechazaron arriba (PDF 2.3, reglas 8 y 9).
        cita.setCliente(cliente); cita.setMascota(mascota); cita.setVeterinario(veterinario);
        cita.setFecha(request.fecha()); cita.setHora(request.hora()); cita.setMotivoConsulta(request.motivoConsulta().trim()); cita.setEstado(requested);
        return response(citas.save(cita));
    }

    private void validate(CitaRequest r, Cita current) {
        if (r == null || r.clienteId() == null || r.clienteId() <= 0 || r.mascotaId() == null || r.mascotaId() <= 0 || r.veterinarioId() == null || r.veterinarioId() <= 0)
            throw new BadRequestException("clienteId, mascotaId y veterinarioId son obligatorios y deben ser positivos");
        if (r.fecha() == null || r.hora() == null) throw new BadRequestException("fecha y hora son obligatorias");
        // Al editar, una cita cuya fecha ya pasó puede guardarse si no se cambia su fecha ni su hora.
        boolean mismaFechaHora = current != null && Objects.equals(current.getFecha(), r.fecha()) && Objects.equals(current.getHora(), r.hora());
        if (!mismaFechaHora && LocalDateTime.of(r.fecha(), r.hora()).isBefore(LocalDateTime.now())) throw new BadRequestException("No se pueden registrar citas en el pasado");
        PageSupport.required(r.motivoConsulta(), "motivoConsulta");
        if (r.motivoConsulta().trim().length() > 500) throw new BadRequestException("motivoConsulta no puede superar 500 caracteres");
        if (r.estado() != null && !EnumSet.allOf(EstadoCita.class).contains(r.estado())) throw new BadRequestException("Estado de cita inválido");
        if (current == null && (r.estado() == EstadoCita.ATENDIDA || r.estado() == EstadoCita.CANCELADA))
            throw new BadRequestException("Una cita nueva debe iniciar pendiente, confirmada o programada");
    }

    /**
     * Valida la disponibilidad (PDF 2.2, reglas 4 y 5): el veterinario no puede tener otra cita activa
     * 15 minutos antes o después (también cuando el margen cruza la medianoche) y la mascota no puede
     * tener otra cita activa a la misma hora. Las citas canceladas no ocupan agenda.
     */
    private void checkAvailability(Veterinario vet, Mascota pet, LocalDate date, LocalTime time, Long excludedId) {
        LocalDateTime inicio = LocalDateTime.of(date, time);
        Duration margen = Duration.ofMinutes(15);
        List<LocalDate> dias = new ArrayList<>(List.of(date));
        if (time.isBefore(LocalTime.MIDNIGHT.plus(margen))) dias.add(date.minusDays(1));
        if (time.isAfter(LocalTime.MAX.minus(margen))) dias.add(date.plusDays(1));
        for (LocalDate dia : dias) {
            for (Cita existing : citas.findByVeterinario_VeterinarioIdAndFecha(vet.getVeterinarioId(), dia)) {
                if (existing.getVeterinario() == null || existing.getFechaHora() == null) continue;
                if (Objects.equals(existing.getCitaId(), excludedId) || existing.getEstado() == EstadoCita.CANCELADA) continue;
                if (Duration.between(existing.getFechaHora(), inicio).abs().compareTo(margen) < 0)
                    throw new BadRequestException(HttpStatus.CONFLICT, "El veterinario debe tener al menos 15 minutos entre citas");
            }
        }
        for (Cita existing : citas.findByMascota_MascotaIdAndFecha(pet.getMascotaId(), date)) {
            if (existing.getMascota() == null || existing.getHora() == null) continue;
            if (Objects.equals(existing.getCitaId(), excludedId) || existing.getEstado() == EstadoCita.CANCELADA) continue;
            if (existing.getHora().equals(time)) throw new BadRequestException(HttpStatus.CONFLICT, "La mascota ya tiene una cita a esa hora");
        }
    }

    private boolean validTransition(EstadoCita from, EstadoCita to) {
        if (from == to) return true;
        return from == EstadoCita.PENDIENTE && EnumSet.of(EstadoCita.CONFIRMADA, EstadoCita.PROGRAMADA, EstadoCita.CANCELADA).contains(to)
                || (from == EstadoCita.CONFIRMADA || from == EstadoCita.PROGRAMADA) && EnumSet.of(EstadoCita.ATENDIDA, EstadoCita.CANCELADA).contains(to);
    }
    private CitaResponse response(Cita c) {
        Cliente cliente = c.getCliente(); Mascota mascota = c.getMascota(); Veterinario veterinario = c.getVeterinario();
        return new CitaResponse(c.getCitaId(), cliente == null ? null : cliente.getClienteId(),
                cliente == null ? null : fullName(cliente.getNombres(), cliente.getApellidos()),
                mascota == null ? null : mascota.getMascotaId(), mascota == null ? null : mascota.getNombreMascota(),
                veterinario == null ? null : veterinario.getVeterinarioId(),
                veterinario == null ? null : fullName(veterinario.getNombres(), veterinario.getApellidos()),
                c.getFecha(), c.getHora(), c.getMotivoConsulta(), c.getEstado());
    }
    private String fullName(String first, String last) { return (first == null ? "" : first) + " " + (last == null ? "" : last); }
    private BadRequestException missing(String type) {
        boolean femenino = type.equals("Cita") || type.equals("Mascota");
        return new BadRequestException(HttpStatus.NOT_FOUND, type + (femenino ? " no encontrada" : " no encontrado"));
    }
}
