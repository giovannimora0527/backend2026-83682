package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.AnotacionService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.*;

/** Implementa las consultas paginadas y la creación de anotaciones clínicas. */
/** Implementa las consultas paginadas y la creación de anotaciones clínicas. */
@Service
public class AnotacionServiceImpl implements AnotacionService {
    private static final Set<String> SORTS = Set.of("fechaHora", "anotacionId");
    private final AnotacionRepository anotaciones;
    private final HistoriaClinicaRepository historias;
    private final VeterinarioRepository veterinarios;
    /** Construye el servicio con los repositorios necesarios para anotaciones, historias y veterinarios. */
    public AnotacionServiceImpl(AnotacionRepository a, HistoriaClinicaRepository h, VeterinarioRepository v) { anotaciones=a; historias=h; veterinarios=v; }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<AnotacionResponse> listar(int page, int size, String sort, String direction, Long historiaId, Long vetId, LocalDate fecha) {
        Pageable pageable = PageSupport.pageable(page, size, sort, direction, "fechaHora", SORTS);
        Specification<Anotacion> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (historiaId != null) p.add(cb.equal(root.get("historiaClinica").get("historiaClinicaId"), historiaId));
            if (vetId != null) p.add(cb.equal(root.get("veterinario").get("veterinarioId"), vetId));
            if (fecha != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), fecha.atStartOfDay()));
                p.add(cb.lessThan(root.get("fechaHora"), fecha.plusDays(1).atStartOfDay()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PageResponse.from(anotaciones.findAll(spec, pageable).map(this::response));
    }
    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<AnotacionResponse> listarHistoria(Long id, int page, int size, String sort, String direction) {
        if (!historias.existsById(id)) throw missing("Historia clínica");
        Pageable pageable = PageSupport.pageable(page, size, sort, direction, "fechaHora", SORTS);
        Specification<Anotacion> spec = (root, query, cb) -> cb.equal(root.get("historiaClinica").get("historiaClinicaId"), id);
        return PageResponse.from(anotaciones.findAll(spec, pageable).map(this::response));
    }
    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public AnotacionResponse consultar(Long id) { return response(anotaciones.findById(id).orElseThrow(() -> missing("Anotación"))); }
    /** {@inheritDoc} */
    @Transactional
    public AnotacionResponse crear(AnotacionRequest r) {
        if (r == null || r.historiaClinicaId() == null || r.veterinarioId() == null) throw new BadRequestException("historiaClinicaId y veterinarioId son obligatorios");
        PageSupport.required(r.descripcion(), "descripcion");
        if (r.descripcion().length() > 1000) throw new BadRequestException("descripcion no puede superar 1000 caracteres");
        HistoriaClinica h = historias.findById(r.historiaClinicaId()).orElseThrow(() -> missing("Historia clínica"));
        Veterinario v = veterinarios.findById(r.veterinarioId()).orElseThrow(() -> missing("Veterinario"));
        Anotacion a = new Anotacion(); a.setHistoriaClinica(h); a.setVeterinario(v); a.setFechaHora(r.fechaHora() == null ? java.time.LocalDateTime.now() : r.fechaHora());
        a.setDescripcion(r.descripcion().trim()); a.setObservacion(blankToNull(r.observacion())); a.setDiagnosticoEvolucion(blankToNull(r.diagnosticoEvolucion()));
        return response(anotaciones.save(a));
    }
    private AnotacionResponse response(Anotacion a) {
        String descripcion = HistoriaClinicaNarrativeCodec.display(a.getDescripcion());
        return new AnotacionResponse(a.getAnotacionId(), a.getHistoriaClinica().getHistoriaClinicaId(),
                a.getVeterinario().getVeterinarioId(), a.getVeterinario().getNombres()+" "+a.getVeterinario().getApellidos(),
                a.getFechaHora(), descripcion, a.getObservacion(), a.getDiagnosticoEvolucion());
    }
    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private BadRequestException missing(String s) { return new BadRequestException(HttpStatus.NOT_FOUND, s + " no encontrada/o"); }
}
