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
 * Entidad que representa una anotacion (nota) dentro de la historia medica
 * de un paciente.
 *
 * <p>Cada vez que un veterinario atiende al paciente y quiere dejar un
 * comentario puntual (por ejemplo: "Se observa mejoria en la herida"),
 * se crea una anotacion nueva asociada a la {@link HistoriaMedica} del
 * paciente.</p>
 */
@Entity
@Table(name = "anotacion_historia")
public class AnotacionHistoria {

    /** Identificador unico de la anotacion. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora en la que se registro la anotacion. */
    private LocalDateTime fecha;

    /** Texto principal de la anotación. */
    private String descripcion;

    /** Observación clínica complementaria. */
    private String observacion;

    /** Diagnóstico o evolución registrada en esta anotación. */
    private String diagnosticoEvolucion;

    /** Profesional responsable de registrar la anotación. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    /**
     * Historia medica a la que pertenece esta anotacion.
     * Esta es la parte "muchos" de la relacion "uno a muchos" definida en
     * {@link HistoriaMedica#getAnotaciones()}: muchas anotaciones pueden
     * pertenecer a una misma historia medica.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_medica_id")
    private HistoriaMedica historiaMedica;

    /** Constructor vacio requerido por JPA. */
    public AnotacionHistoria() {
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------

    /** @return el id de la anotacion */
    public Long getId() {
        return id;
    }

    /** @param id nuevo id de la anotacion */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return la fecha en la que se registro la anotacion */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /** @param fecha nueva fecha de la anotacion */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getDiagnosticoEvolucion() { return diagnosticoEvolucion; }
    public void setDiagnosticoEvolucion(String diagnosticoEvolucion) { this.diagnosticoEvolucion = diagnosticoEvolucion; }
    public Veterinario getVeterinario() { return veterinario; }
    public void setVeterinario(Veterinario veterinario) { this.veterinario = veterinario; }

    /** @return el texto de la anotacion */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion nuevo texto de la anotacion */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return la historia medica a la que pertenece esta anotacion */
    public HistoriaMedica getHistoriaMedica() {
        return historiaMedica;
    }

    /** @param historiaMedica nueva historia medica a la que pertenece esta anotacion */
    public void setHistoriaMedica(HistoriaMedica historiaMedica) {
        this.historiaMedica = historiaMedica;
    }
}
