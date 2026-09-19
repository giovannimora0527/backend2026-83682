package com.uniminuto.clinica.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad que representa una formula medica (receta) recetada a un
 * paciente, la cual se descuenta del inventario de medicamentos de la
 * clinica.
 *
 * <p>El requerimiento 1 del taller pide poder listar estas formulas
 * ordenadas desde la mas reciente hasta la mas antigua, por eso el campo
 * {@link #fechaCreacion} es tan importante en esta entidad.</p>
 */
@Entity
@Table(name = "formula_medica")
public class FormulaMedica {

    /** Identificador unico de la formula medica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora en la que se creo (receto) la formula medica. */
    private LocalDateTime fechaCreacion;

    /** Nombre del medicamento recetado, tomado del inventario. Ejemplo: "Amoxicilina". */
    private String medicamento;

    /** Dosis indicada. Ejemplo: "500mg cada 12 horas". */
    private String dosis;

    /** Indicaciones adicionales para el propietario de la mascota. */
    private String indicaciones;

    /** Cantidad de unidades del medicamento que se tomaron del inventario. */
    private Integer cantidad;

    /** Paciente (mascota) al que se le receto la formula. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    /** Constructor vacio requerido por JPA. */
    public FormulaMedica() {
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------

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

    /** @return el paciente al que se le receto la formula */
    public Paciente getPaciente() {
        return paciente;
    }

    /** @param paciente nuevo paciente al que se le receto la formula */
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
}
