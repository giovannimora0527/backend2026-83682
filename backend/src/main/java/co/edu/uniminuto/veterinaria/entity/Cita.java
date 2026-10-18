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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Cita médica de una mascota con un veterinario (Mascota 1:N Cita, Cita N:1 Veterinario).
 */
@Entity
@Table(name = "cita")
public class Cita {

    /** Identificador de la cita. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Cliente que solicita la cita (debe ser el dueño de la mascota). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Mascota atendida. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    /** Veterinario asignado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id")
    private Veterinario veterinario;

    /** Fecha y hora de la cita. */
    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    /** Estado actual de la cita. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCita estado;

    /** Motivo de la consulta. */
    @Column(name = "motivo")
    private String motivo;

    /** Fecha de creación del registro. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de la última modificación del registro. */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Asigna la fecha de creación antes de insertar. */
    @PrePersist
    void antesDeCrear() {
        this.fechaCreacion = LocalDateTime.now().withNano(0);
    }

    /** Actualiza la fecha de modificación antes de cada actualización. */
    @PreUpdate
    void antesDeActualizar() {
        this.fechaModificacion = LocalDateTime.now().withNano(0);
    }

    /** @return identificador de la cita */
    public Long getId() {
        return id;
    }

    /** @return cliente de la cita */
    public Cliente getCliente() {
        return cliente;
    }

    /** @param cliente cliente de la cita */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /** @return mascota de la cita */
    public Mascota getMascota() {
        return mascota;
    }

    /** @param mascota mascota de la cita */
    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    /** @return veterinario asignado */
    public Veterinario getVeterinario() {
        return veterinario;
    }

    /** @param veterinario veterinario asignado */
    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    /** @return fecha y hora de la cita */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /** @param fechaHora fecha y hora de la cita */
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    /** @return estado de la cita */
    public EstadoCita getEstado() {
        return estado;
    }

    /** @param estado nuevo estado de la cita */
    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    /** @return motivo de la consulta */
    public String getMotivo() {
        return motivo;
    }

    /** @param motivo motivo de la consulta */
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
