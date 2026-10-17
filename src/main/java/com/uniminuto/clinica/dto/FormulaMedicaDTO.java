package com.uniminuto.clinica.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada y salida para las fórmulas médicas. Contiene los identificadores
 * de las entidades relacionadas para que el servicio valide las relaciones antes de guardar.
 */
public class FormulaMedicaDTO {

    /** Identificador de la formula medica. */
    private Long id;

    /** Fecha y hora en la que se creo la formula medica. */
    private LocalDateTime fechaCreacion;

    /** Nombre del medicamento recetado. */
    @NotBlank(message = "El medicamento es obligatorio")
    private String medicamento;

    /** Dosis indicada. */
    @NotBlank(message = "La dosis es obligatoria")
    private String dosis;

    /** Indicaciones adicionales. */
    private String indicaciones;

    /** Cantidad de unidades tomadas del inventario. */
    @NotNull(message = "La duración/cantidad es obligatoria")
    @Positive(message = "La duración/cantidad debe ser mayor que cero")
    private Integer cantidad;

    /** Nombre del paciente al que se le receto la formula. */
    private String nombrePaciente;

    /** Identificador de la mascota que recibe el tratamiento. */
    @NotNull(message = "Debe seleccionar una mascota")
    private Long pacienteId;

    /** IDs de las relaciones clínicas obligatorias. */
    private Long citaId;
    private Long historiaMedicaId;
    private Long veterinarioId;
    private String nombreVeterinario;
    private LocalDateTime fechaEmision;
    private String frecuencia;
    private Integer duracionDias;

    public Long getCitaId(){return citaId;} public void setCitaId(Long id){this.citaId=id;}
    public Long getHistoriaMedicaId(){return historiaMedicaId;} public void setHistoriaMedicaId(Long id){this.historiaMedicaId=id;}
    public Long getVeterinarioId(){return veterinarioId;} public void setVeterinarioId(Long id){this.veterinarioId=id;}
    public String getNombreVeterinario(){return nombreVeterinario;} public void setNombreVeterinario(String n){this.nombreVeterinario=n;}
    public LocalDateTime getFechaEmision(){return fechaEmision;} public void setFechaEmision(LocalDateTime f){this.fechaEmision=f;}
    public String getFrecuencia(){return frecuencia;} public void setFrecuencia(String f){this.frecuencia=f;}
    public Integer getDuracionDias(){return duracionDias;} public void setDuracionDias(Integer d){this.duracionDias=d;}

    /** @return identificador de la mascota */
    public Long getPacienteId() { return pacienteId; }

    /** @param pacienteId identificador de la mascota */
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    /** Constructor vacio. */
    public FormulaMedicaDTO() {
    }

    /** @return el id de la formula medica */
    public Long getId() {
        return id;
    }

    /** @param id nuevo id de la formula medica */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return la fecha de creacion de la formula */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /** @param fechaCreacion nueva fecha de creacion de la formula */
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /** @return el nombre del medicamento recetado */
    public String getMedicamento() {
        return medicamento;
    }

    /** @param medicamento nuevo nombre del medicamento recetado */
    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    /** @return la dosis indicada */
    public String getDosis() {
        return dosis;
    }

    /** @param dosis nueva dosis indicada */
    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    /** @return las indicaciones adicionales */
    public String getIndicaciones() {
        return indicaciones;
    }

    /** @param indicaciones nuevas indicaciones adicionales */
    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    /** @return la cantidad de unidades usadas del inventario */
    public Integer getCantidad() {
        return cantidad;
    }

    /** @param cantidad nueva cantidad de unidades usadas del inventario */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    /** @return el nombre del paciente asociado */
    public String getNombrePaciente() {
        return nombrePaciente;
    }

    /** @param nombrePaciente nuevo nombre del paciente asociado */
    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }
}
