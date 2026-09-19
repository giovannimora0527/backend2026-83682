package com.uniminuto.clinica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una fórmula médica en el sistema.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "formula_medica")
public class FormulaMedica {

    /**
     * Identificador único de la fórmula médica.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    /**
     * Identificador de la cita asociada.
     */
    @Column(name = "cita_id", nullable = false)
    private Integer citaId;

    /**
     * Identificador del medicamento.
     */
    @Column(name = "medicamento_id", nullable = false)
    private Integer medicamentoId;

    /**
     * Dosis del medicamento a administrar.
     */
    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosis;

    /**
     * Indicaciones especiales para el paciente.
     */
    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    /**
     * Fecha de última actualización del registro.
     */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    // Constructores
    public FormulaMedica() {
    }

    public FormulaMedica(Integer citaId, Integer medicamentoId, String dosis,
                         String indicaciones, LocalDateTime fechaCreacionRegistro) {
        this.citaId = citaId;
        this.medicamentoId = medicamentoId;
        this.dosis = dosis;
        this.indicaciones = indicaciones;
        this.fechaCreacionRegistro = fechaCreacionRegistro;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCitaId() {
        return citaId;
    }

    public void setCitaId(Integer citaId) {
        this.citaId = citaId;
    }

    public Integer getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Integer medicamentoId) {
        this.medicamentoId = medicamentoId;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public LocalDateTime getFechaCreacionRegistro() {
        return fechaCreacionRegistro;
    }

    public void setFechaCreacionRegistro(LocalDateTime fechaCreacionRegistro) {
        this.fechaCreacionRegistro = fechaCreacionRegistro;
    }

    public LocalDateTime getFechaActualizacionRegistro() {
        return fechaActualizacionRegistro;
    }

    public void setFechaActualizacionRegistro(LocalDateTime fechaActualizacionRegistro) {
        this.fechaActualizacionRegistro = fechaActualizacionRegistro;
    }

    @Override
    public String toString() {
        return "FormulaMedica{" +
                "id=" + id +
                ", citaId=" + citaId +
                ", medicamentoId=" + medicamentoId +
                ", dosis='" + dosis + '\'' +
                ", indicaciones='" + indicaciones + '\'' +
                ", fechaCreacionRegistro=" + fechaCreacionRegistro +
                ", fechaActualizacionRegistro=" + fechaActualizacionRegistro +
                '}';
    }
}