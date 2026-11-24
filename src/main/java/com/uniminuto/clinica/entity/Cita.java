package com.uniminuto.clinica.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
@Data
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @Column(name = "usuario_creacion_id")
    private Long usuarioCreacionId;

    @Column(name = "fecha_creacion_registro", nullable = false, updatable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /** Nombre anterior de fechaInicio, para los consumidores actuales del API. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDateTime getFechaHora() {
        return fechaInicio;
    }

    @PrePersist
    void alCrear() {
        fechaCreacionRegistro = LocalDateTime.now();
    }

    @PreUpdate
    void alModificar() {
        fechaModificacionRegistro = LocalDateTime.now();
    }
}
