package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Reserva de atención para una mascota con un veterinario. */
@Entity
@Table(name = "cita")
@Getter
@Setter
public class Cita {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long citaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "medico_id", nullable = false)
    private Veterinario veterinario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivoConsulta;

    @Convert(converter = EstadoCitaConverter.class)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Transient
    public LocalDate getFecha() { return fechaHora == null ? null : fechaHora.toLocalDate(); }
    public void setFecha(LocalDate fecha) {
        this.fechaHora = fecha == null ? null : LocalDateTime.of(fecha, getHora() == null ? LocalTime.MIDNIGHT : getHora());
    }
    @Transient
    public LocalTime getHora() { return fechaHora == null ? null : fechaHora.toLocalTime(); }
    public void setHora(LocalTime hora) {
        this.fechaHora = hora == null ? null : LocalDateTime.of(getFecha() == null ? LocalDate.now() : getFecha(), hora);
    }
}
