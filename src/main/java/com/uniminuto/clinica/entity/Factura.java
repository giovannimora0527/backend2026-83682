package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.EstadoFactura;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura")
@Getter
@Setter
public class Factura extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id", nullable = false)
    private AtencionMedica atencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "cliente_nombre", nullable = false, length = 201)
    private String clienteNombre;

    @Column(name = "cliente_documento", nullable = false, length = 40)
    private String clienteDocumento;

    @Column(name = "mascota_nombre", nullable = false, length = 100)
    private String mascotaNombre;

    @Column(name = "medico_nombre", nullable = false, length = 201)
    private String medicoNombre;

    @Column(name = "especialidad_nombre", nullable = false, length = 100)
    private String especialidadNombre;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_descuento", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalDescuento;

    @Column(name = "total_impuestos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalImpuestos;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "total_pagado", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPagado = BigDecimal.ZERO;

    @Column(name = "saldo", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoFactura estado;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_anulacion_id")
    private Usuario usuarioAnulacion;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
