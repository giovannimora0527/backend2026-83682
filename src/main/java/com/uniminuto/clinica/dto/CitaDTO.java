package com.uniminuto.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO utilizado para crear, actualizar y devolver informacion de una
 * {@link com.uniminuto.clinica.models.Cita}.
 */
public class CitaDTO {

    /** Identificador de la cita (nulo cuando se esta creando una nueva). */
    private Long id;

    /** Fecha y hora de la cita. Es obligatoria. */
    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDateTime fecha;

    /** Motivo de la consulta. Es obligatorio. */
    @NotBlank(message = "El motivo de la cita es obligatorio")
    private String motivo;

    /**
     * Estado de la cita como texto (ej: "PENDIENTE", "CONFIRMADA",
     * "COMPLETADA", "CANCELADA"). Se maneja como texto en el DTO para que
     * sea facil de enviar desde cualquier cliente (Postman, frontend, etc).
     */
    private String estado;

    /** Id del paciente (mascota) al que pertenece la cita. Es obligatorio. */
    @NotNull(message = "El id del paciente es obligatorio")
    private Long pacienteId;

    /** Nombre del paciente. Solo se usa como informacion de salida (no se envia al crear). */
    private String nombrePaciente;

    /** Constructor vacio, necesario para convertir JSON en este objeto. */
    public CitaDTO() {
    }

    /** @return el id de la cita */
    public Long getId() {
        return id;
    }

    /** @param id nuevo id de la cita */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return la fecha y hora de la cita */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /** @param fecha nueva fecha y hora de la cita */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    /** @return el motivo de la consulta */
    public String getMotivo() {
        return motivo;
    }

    /** @param motivo nuevo motivo de la consulta */
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    /** @return el estado de la cita como texto */
    public String getEstado() {
        return estado;
    }

    /** @param estado nuevo estado de la cita como texto */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /** @return el id del paciente asociado */
    public Long getPacienteId() {
        return pacienteId;
    }

    /** @param pacienteId nuevo id del paciente asociado */
    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    /** @return el nombre del paciente asociado (solo lectura) */
    public String getNombrePaciente() {
        return nombrePaciente;
    }

    /** @param nombrePaciente nuevo nombre del paciente asociado */
    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }
}
