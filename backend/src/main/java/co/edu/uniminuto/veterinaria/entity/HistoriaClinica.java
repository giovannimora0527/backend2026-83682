package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Historia clínica generada por una cita atendida (Mascota 1:N Historia clínica).
 * Puede tener múltiples anotaciones y fórmulas médicas.
 */
@Entity
@Table(name = "historia_clinica")
public class HistoriaClinica {

    /** Identificador de la historia clínica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Cliente dueño de la mascota. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Mascota a la que pertenece la historia. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /** Cita atendida que originó la historia (una cita genera una sola historia). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    /** Veterinario que realizó la consulta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    /** Fecha y hora de la consulta. */
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    /** Motivo de la consulta. */
    @Column(name = "motivo")
    private String motivo;

    /** Diagnóstico de la consulta. */
    @Column(name = "diagnostico")
    private String diagnostico;

    /** Observaciones adicionales. */
    @Column(name = "observaciones")
    private String observaciones;

    /** Estado de la historia (abierta o cerrada). */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoHistoria estado;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Asigna la fecha de creación antes de insertar. */
    @PrePersist
    void antesDeCrear() {
        this.fechaCreacion = LocalDateTime.now().withNano(0);
    }

    /** @return identificador */
    public Long getId() {
        return id;
    }

    /** @return cliente dueño */
    public Cliente getCliente() {
        return cliente;
    }

    /** @param cliente cliente dueño */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /** @return mascota */
    public Mascota getMascota() {
        return mascota;
    }

    /** @param mascota mascota */
    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    /** @return cita de origen */
    public Cita getCita() {
        return cita;
    }

    /** @param cita cita de origen */
    public void setCita(Cita cita) {
        this.cita = cita;
    }

    /** @return veterinario */
    public Veterinario getVeterinario() {
        return veterinario;
    }

    /** @param veterinario veterinario */
    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    /** @return fecha y hora de la consulta */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /** @param fechaHora fecha y hora de la consulta */
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /** @return motivo de consulta */
    public String getMotivo() {
        return motivo;
    }

    /** @param motivo motivo de consulta */
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    /** @return diagnóstico */
    public String getDiagnostico() {
        return diagnostico;
    }

    /** @param diagnostico diagnóstico */
    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    /** @return observaciones */
    public String getObservaciones() {
        return observaciones;
    }

    /** @param observaciones observaciones */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /** @return estado de la historia */
    public EstadoHistoria getEstado() {
        return estado;
    }

    /** @param estado estado de la historia */
    public void setEstado(EstadoHistoria estado) {
        this.estado = estado;
    }
}
