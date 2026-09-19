package com.uniminuto.clinica.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa la historia medica de un paciente: un registro
 * general de su condicion de salud a lo largo del tiempo.
 *
 * <p>Una historia medica puede tener muchas {@link AnotacionHistoria}
 * (anotaciones), que son notas puntuales que los veterinarios van
 * agregando en cada visita (por eso la relacion es de tipo "uno a muchos").</p>
 */
@Entity
@Table(name = "historia_medica")
public class HistoriaMedica {

    /** Identificador unico de la historia medica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora en la que se creo la historia medica. */
    private LocalDateTime fechaCreacion;

    /** Diagnostico general registrado en la historia medica. */
    private String diagnostico;

    /** Observaciones adicionales sobre el estado del paciente. */
    private String observaciones;

    /** Paciente (mascota) al que pertenece esta historia medica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    /**
     * Lista de anotaciones (notas) que se le han hecho a esta historia
     * medica a lo largo del tiempo.
     *
     * <p>{@code mappedBy = "historiaMedica"} indica que quien controla la
     * relacion en la base de datos es el atributo {@code historiaMedica}
     * dentro de la clase {@link AnotacionHistoria} (es decir, la columna
     * de llave foranea vive en la tabla de anotaciones, no aqui).</p>
     *
     * <p>{@code cascade = CascadeType.ALL} hace que si se guarda o borra
     * una historia medica, tambien se guarden o borren automaticamente
     * sus anotaciones asociadas.</p>
     */
    @OneToMany(mappedBy = "historiaMedica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AnotacionHistoria> anotaciones = new ArrayList<>();

    /** Constructor vacio requerido por JPA. */
    public HistoriaMedica() {
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------

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

    /** @return el paciente al que pertenece la historia medica */
    public Paciente getPaciente() {
        return paciente;
    }

    /** @param paciente nuevo paciente al que pertenece la historia medica */
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    /** @return la lista de anotaciones de esta historia medica */
    public List<AnotacionHistoria> getAnotaciones() {
        return anotaciones;
    }

    /** @param anotaciones nueva lista de anotaciones de esta historia medica */
    public void setAnotaciones(List<AnotacionHistoria> anotaciones) {
        this.anotaciones = anotaciones;
    }
}
