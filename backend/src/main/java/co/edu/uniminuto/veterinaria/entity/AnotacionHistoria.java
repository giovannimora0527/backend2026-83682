package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
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
 * Anotación de seguimiento de una historia clínica (Historia 1:N Anotación).
 */
@Entity
@Table(name = "anotacion_historia")
public class AnotacionHistoria {

    /** Identificador de la anotación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Historia clínica a la que pertenece. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "historia_id")
    private HistoriaClinica historia;

    /** Veterinario que escribe la anotación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    /** Fecha y hora en que se registró la anotación (la asigna el servidor). */
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    /** Descripción de lo realizado. */
    @Column(name = "descripcion")
    private String descripcion;

    /** Observación del veterinario. */
    @Column(name = "observacion")
    private String observacion;

    /** Diagnóstico o evolución del paciente. */
    @Column(name = "diagnostico_evolucion")
    private String diagnosticoEvolucion;

    /** @return identificador */
    public Long getId() {
        return id;
    }

    /** @return historia clínica */
    public HistoriaClinica getHistoria() {
        return historia;
    }

    /** @param historia historia clínica */
    public void setHistoria(HistoriaClinica historia) {
        this.historia = historia;
    }

    /** @return veterinario autor */
    public Veterinario getVeterinario() {
        return veterinario;
    }

    /** @param veterinario veterinario autor */
    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    /** @return fecha y hora de la anotación */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /** @param fechaHora fecha y hora de la anotación */
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /** @return descripción */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion descripción */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return observación */
    public String getObservacion() {
        return observacion;
    }

    /** @param observacion observación */
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    /** @return diagnóstico o evolución */
    public String getDiagnosticoEvolucion() {
        return diagnosticoEvolucion;
    }

    /** @param diagnosticoEvolucion diagnóstico o evolución */
    public void setDiagnosticoEvolucion(String diagnosticoEvolucion) {
        this.diagnosticoEvolucion = diagnosticoEvolucion;
    }
}
