package com.uniminuto.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO utilizado para crear, actualizar y devolver informacion de una
 * {@link com.uniminuto.clinica.models.HistoriaMedica}.
 */
public class HistoriaMedicaDTO {

    /** Identificador de la historia medica (nulo al crear una nueva). */
    private Long id;

    /**
     * Fecha de creacion de la historia medica.
     * Es opcional al crear: si no se envia, el servicio la completa
     * automaticamente con la fecha y hora actual.
     */
    private LocalDateTime fechaCreacion;

    /** Diagnostico general. Es obligatorio. */
    @NotBlank(message = "El diagnostico es obligatorio")
    private String diagnostico;

    /** Observaciones adicionales sobre el paciente. */
    private String observaciones;

    /** Id del paciente al que pertenece la historia medica. Es obligatorio. */
    @NotNull(message = "El id del paciente es obligatorio")
    private Long pacienteId;

    /** Nombre del paciente (solo informativo, para la respuesta). */
    private String nombrePaciente;

    /** Lista resumida de las anotaciones asociadas a esta historia (solo lectura). */
    private List<AnotacionHistoriaDTO> anotaciones = new ArrayList<>();

    /** Constructor vacio. */
    public HistoriaMedicaDTO() {
    }

    /** @return el id de la historia medica */
    public Long getId() {
        return id;
    }

    /** @param id nuevo id de la historia medica */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return la fecha de creacion de la historia medica */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /** @param fechaCreacion nueva fecha de creacion de la historia medica */
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /** @return el diagnostico registrado */
    public String getDiagnostico() {
        return diagnostico;
    }

    /** @param diagnostico nuevo diagnostico registrado */
    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    /** @return las observaciones adicionales */
    public String getObservaciones() {
        return observaciones;
    }

    /** @param observaciones nuevas observaciones adicionales */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /** @return el id del paciente asociado */
    public Long getPacienteId() {
        return pacienteId;
    }

    /** @param pacienteId nuevo id del paciente asociado */
    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    /** @return el nombre del paciente asociado */
    public String getNombrePaciente() {
        return nombrePaciente;
    }

    /** @param nombrePaciente nuevo nombre del paciente asociado */
    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    /** @return la lista de anotaciones asociadas */
    public List<AnotacionHistoriaDTO> getAnotaciones() {
        return anotaciones;
    }

    /** @param anotaciones nueva lista de anotaciones asociadas */
    public void setAnotaciones(List<AnotacionHistoriaDTO> anotaciones) {
        this.anotaciones = anotaciones;
    }
}
