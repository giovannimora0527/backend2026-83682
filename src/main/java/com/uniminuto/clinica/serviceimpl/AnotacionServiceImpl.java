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
    private static final Set<String> SORTS = Set.of("fechaHora", "anotacionId",
            "historiaClinica.mascota.nombreMascota", "veterinario.nombres");
    /** Columnas que ordena la tabla de Angular traducidas a propiedades de la entidad Anotacion. */
    private static final Map<String, String> SORT_ALIASES = Map.of(
            "historiaClinicaId", "historiaClinica.mascota.nombreMascota", "veterinarioId", "veterinario.nombres");
    private final AnotacionRepository anotaciones;
    private final HistoriaClinicaRepository historias;
    private final VeterinarioRepository veterinarios;
    /** Construye el servicio con los repositorios necesarios para anotaciones, historias y veterinarios. */
    public AnotacionServiceImpl(AnotacionRepository a, HistoriaClinicaRepository h, VeterinarioRepository v) { anotaciones=a; historias=h; veterinarios=v; }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<AnotacionResponse> listar(int page, int size, String sort, String direction, Long historiaId, Long vetId, LocalDate fecha) {
        Pageable pageable = PageSupport.pageable(page, size, sort == null ? null : SORT_ALIASES.getOrDefault(sort, sort), direction, "fechaHora", SORTS);
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
        Pageable pageable = PageSupport.pageable(page, size, sort == null ? null : SORT_ALIASES.getOrDefault(sort, sort), direction, "fechaHora", SORTS);
        Specification<Anotacion> spec = (root, query, cb) -> cb.equal(root.get("historiaClinica").get("historiaClinicaId"), id);
        return PageResponse.from(anotaciones.findAll(spec, pageable).map(this::response));
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    public PageResponse<AnotacionResponse> listarHistoria(Long id, int page, int size, String sort, String direction,
            Long veterinarioId, LocalDate fecha) {
        if (!historias.existsById(id)) throw missing("Historia clínica");
        return listar(page, size, sort, direction, id, veterinarioId, fecha);
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
        String observacion = blankToNull(r.observacion());
        String diagnostico = blankToNull(r.diagnosticoEvolucion());
        if ((observacion != null && observacion.length() > 2000) || (diagnostico != null && diagnostico.length() > 2000))
            throw new BadRequestException("observacion y diagnosticoEvolucion no pueden superar 2000 caracteres");
        Anotacion a = new Anotacion(); a.setHistoriaClinica(h); a.setVeterinario(v); a.setFechaHora(r.fechaHora() == null ? java.time.LocalDateTime.now() : r.fechaHora());
        // anotacion_historia solo tiene la columna descripcion: los campos adicionales se guardan
        // como líneas etiquetadas para no perderlos y se separan de nuevo al leer.
        StringBuilder texto = new StringBuilder(r.descripcion().trim());
        if (observacion != null) texto.append("\n").append(OBSERVACION).append(observacion);
        if (diagnostico != null) texto.append("\n").append(DIAGNOSTICO).append(diagnostico);
        a.setDescripcion(texto.toString());
        return response(anotaciones.save(a));
    }
    private AnotacionResponse response(Anotacion a) {
        String almacenado = a.getDescripcion();
        String descripcion = HistoriaClinicaNarrativeCodec.display(almacenado);
        String observacion = null, diagnostico = null;
        if (almacenado != null && descripcion.equals(almacenado)) {
            StringBuilder resto = new StringBuilder();
            for (String linea : almacenado.split("\\R")) {
                if (linea.startsWith(OBSERVACION)) observacion = linea.substring(OBSERVACION.length());
                else if (linea.startsWith(DIAGNOSTICO)) diagnostico = linea.substring(DIAGNOSTICO.length());
                else resto.append(resto.isEmpty() ? "" : "\n").append(linea);
            }
            descripcion = resto.toString();
        }
        HistoriaClinica historia = a.getHistoriaClinica();
        Veterinario vet = a.getVeterinario();
        return new AnotacionResponse(a.getAnotacionId(), historia == null ? null : historia.getHistoriaClinicaId(),
                vet == null ? null : vet.getVeterinarioId(), vet == null ? null : vet.getNombres()+" "+vet.getApellidos(),
                a.getFechaHora(), descripcion, observacion, diagnostico);
    }
    private static final String OBSERVACION = "Observación: ";
    private static final String DIAGNOSTICO = "Diagnóstico de evolución: ";
    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private BadRequestException missing(String s) {
        return new BadRequestException(HttpStatus.NOT_FOUND, s + (s.equals("Veterinario") ? " no encontrado" : " no encontrada"));
    }
}
