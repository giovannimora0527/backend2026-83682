package com.clinica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una Cita médica en el sistema.
 * Mapea la tabla 'citas' en la base de datos.
 */
@Entity
@Table(name = "citas")
public class Cita {

    /** Identificador único autogenerado de la cita. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre o motivo del paciente. */
    private String descripcion;

    /** Fecha y hora de la cita. */
    private LocalDateTime fecha;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Cita() {
    }

    /**
     * Constructor con parámetros.
     *
     * @param descripcion Detalle o motivo de la cita.
     * @param fecha       Fecha y hora programada.
     */
    public Cita(String descripcion, LocalDateTime fecha) {
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}