package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro historico: solo se inserta. */
@Immutable
@Entity
@Table(name = "tarifa_historial")
@Getter
@Setter
public class TarifaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id", nullable = false)
    private Tarifa tarifa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Column(name = "valor_anterior", precision = 14, scale = 2)
    private BigDecimal valorAnterior;

    @Column(name = "valor_nuevo", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorNuevo;

    @Column(name = "porcentaje_iva_anterior", precision = 5, scale = 2)
    private BigDecimal porcentajeIvaAnterior;

    @Column(name = "porcentaje_iva_nuevo", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIvaNuevo;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
