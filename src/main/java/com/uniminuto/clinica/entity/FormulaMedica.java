package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Encabezado de la formula; los medicamentos estan en FormulaMedicaDetalle. */
@Entity
@Table(name = "formula_medica")
@Getter
@Setter
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Se asigna al confirmar. */
    @Column(name = "numero", unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id")
    private AtencionMedica atencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "indicaciones_generales", columnDefinition = "TEXT")
    private String indicacionesGenerales;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado_formula", nullable = false, length = 30)
    private EstadoFormula estadoFormula;

    /** Nulo mientras la formula es un borrador. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado_despacho", length = 30)
    private EstadoDespacho estadoDespacho;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(name = "fecha_creacion_registro", nullable = false, updatable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @PrePersist
    void alCrear() {
        fechaCreacionRegistro = LocalDateTime.now();
        if (fechaGeneracion == null) {
            fechaGeneracion = fechaCreacionRegistro;
        }
    }

    @PreUpdate
    void alModificar() {
        fechaActualizacionRegistro = LocalDateTime.now();
    }
}
