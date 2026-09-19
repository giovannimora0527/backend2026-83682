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
     * Fecha de la anotacion. Es opcional al crear: si no se envia, el
     * servicio la completa automaticamente con la fecha y hora actual.
     */
    private LocalDateTime fecha;

    /** Texto de la anotacion. Es obligatorio. */
    @NotBlank(message = "La descripcion de la anotacion es obligatoria")
    private String descripcion;

    /** Id de la historia medica a la que pertenece esta anotacion. Es obligatorio. */
    @NotNull(message = "El id de la historia medica es obligatorio")
    private Long historiaMedicaId;

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
