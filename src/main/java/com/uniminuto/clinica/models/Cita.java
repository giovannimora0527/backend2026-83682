package com.uniminuto.clinica.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidad que representa una cita medica agendada en la clinica veterinaria.
 *
 * <p>Cada cita tiene una fecha y hora, un motivo de consulta, un estado
 * (ver {@link EstadoCita}) y esta asociada a un {@link Paciente}.</p>
 */
@Entity
@Table(name = "cita")
public class Cita {

    /** Identificador unico de la cita, generado automaticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora en la que se realizara (o se realizo) la cita. */
    private LocalDateTime fecha;

    /** Motivo de la consulta. Ejemplo: "Vacunacion", "Control general". */
    private String motivo;

    /**
     * Estado actual de la cita.
     * {@code @Enumerated(EnumType.STRING)} le dice a JPA que guarde el
     * estado como texto (ej: "PENDIENTE") en vez de como numero, para que
     * la base de datos sea mas facil de leer.
     */
    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    /**
     * Paciente (mascota) al que pertenece esta cita.
     * {@code @ManyToOne} indica que muchas citas pueden pertenecer a un
     * mismo paciente. {@code FetchType.LAZY} hace que el paciente solo se
     * cargue desde la base de datos cuando realmente se necesite, para que
     * la aplicacion sea mas eficiente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    /** Constructor vacio requerido por JPA. */
    public Cita() {
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------

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

    /** @return el estado actual de la cita */
    public EstadoCita getEstado() {
        return estado;
    }

    /** @param estado nuevo estado de la cita */
    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    /** @return el paciente asociado a la cita */
    public Paciente getPaciente() {
        return paciente;
    }

    /** @param paciente nuevo paciente asociado a la cita */
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
}
