package com.uniminuto.clinica.dto;

import java.time.LocalDateTime;

/**
 * DTO de solo lectura (salida) para {@link com.uniminuto.clinica.models.FormulaMedica}.
 *
 * <p>El enunciado solo pide poder LISTAR las formulas medicas, por eso este
 * DTO no tiene anotaciones de validacion: no se usa para recibir datos de
 * entrada, solo para devolver informacion al cliente que consulta la API.</p>
 */
public class FormulaMedicaDTO {

    /** Identificador de la formula medica. */
    private Long id;

    /** Fecha y hora en la que se creo la formula medica. */
    private LocalDateTime fechaCreacion;

    /** Nombre del medicamento recetado. */
    private String medicamento;

    /** Dosis indicada. */
    private String dosis;

    /** Indicaciones adicionales. */
    private String indicaciones;

    /** Cantidad de unidades tomadas del inventario. */
    private Integer cantidad;

    /** Nombre del paciente al que se le receto la formula. */
    private String nombrePaciente;

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
