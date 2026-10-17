package com.uniminuto.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO utilizado para crear, actualizar y devolver informacion de una
 * {@link com.uniminuto.clinica.models.AnotacionHistoria}.
 */
public class AnotacionHistoriaDTO {

    /** Identificador de la anotacion (nulo al crear una nueva). */
    private Long id;

    /**
     * Fecha y hora de la anotación, requeridas para mantener la trazabilidad clínica.
     */
    @NotNull(message = "La fecha y hora de la anotación son obligatorias")
    private LocalDateTime fecha;

    /** Texto de la anotacion. Es obligatorio. */
    @NotBlank(message = "La descripcion de la anotacion es obligatoria")
    private String descripcion;

    /** Id de la historia medica a la que pertenece esta anotacion. Es obligatorio. */
    @NotNull(message = "El id de la historia medica es obligatorio")
    private Long historiaMedicaId;

    /** Identificador del veterinario responsable. */
    @NotNull(message = "Debe seleccionar un veterinario")
    private Long veterinarioId;
    private String nombreVeterinario;
    @NotBlank(message = "La observación es obligatoria")
    private String observacion;
    @NotBlank(message = "El diagnóstico o evolución es obligatorio")
    private String diagnosticoEvolucion;

    /** Constructor vacio. */
    public AnotacionHistoriaDTO() {
    }

    /** @return el id de la anotacion */
    public Long getId() {
        return id;
    }

    /** @param id nuevo id de la anotacion */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return la fecha de la anotacion */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /** @param fecha nueva fecha de la anotacion */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Long veterinarioId) { this.veterinarioId = veterinarioId; }
    public String getNombreVeterinario() { return nombreVeterinario; }
    public void setNombreVeterinario(String nombreVeterinario) { this.nombreVeterinario = nombreVeterinario; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getDiagnosticoEvolucion() { return diagnosticoEvolucion; }
    public void setDiagnosticoEvolucion(String diagnosticoEvolucion) { this.diagnosticoEvolucion = diagnosticoEvolucion; }

    /** @return el texto de la anotacion */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion nuevo texto de la anotacion */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return el id de la historia medica asociada */
    public Long getHistoriaMedicaId() {
        return historiaMedicaId;
    }

    /** @param historiaMedicaId nuevo id de la historia medica asociada */
    public void setHistoriaMedicaId(Long historiaMedicaId) {
        this.historiaMedicaId = historiaMedicaId;
    }
}
