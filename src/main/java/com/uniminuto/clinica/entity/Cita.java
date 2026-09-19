package com.uniminuto.clinica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una cita médica en el sistema.
 *
 * @author TuNombre
 * @version 1.0
 */
@Entity
@Table(name = "cita")
public class Cita {

    /**
     * Identificador único de la cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    /**
     * Identificador del cliente.
     */
    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    /**
     * Identificador de la mascota.
     */
    @Column(name = "mascota_id", nullable = false)
    private Integer mascotaId;

    /**
     * Identificador del médico.
     */
    @Column(name = "medico_id", nullable = false)
    private Integer medicoId;

    /**
     * Fecha y hora de la cita.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estado de la cita (programada, completada, cancelada).
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /**
     * Motivo de la consulta.
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    // Constructores
    public Cita() {
    }

    public Cita(Integer clienteId, Integer mascotaId, Integer medicoId,
                LocalDateTime fechaHora, String estado, String motivo) {
        this.clienteId = clienteId;
        this.mascotaId = mascotaId;
        this.medicoId = medicoId;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.motivo = motivo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Integer mascotaId) {
        this.mascotaId = mascotaId;
    }

    public Integer getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Integer medicoId) {
        this.medicoId = medicoId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", mascotaId=" + mascotaId +
                ", medicoId=" + medicoId +
                ", fechaHora=" + fechaHora +
                ", estado='" + estado + '\'' +
                ", motivo='" + motivo + '\'' +
                '}';
    }
}