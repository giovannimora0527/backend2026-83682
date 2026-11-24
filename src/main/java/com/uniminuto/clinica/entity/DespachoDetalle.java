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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "despacho_detalle", uniqueConstraints = @UniqueConstraint(columnNames = {"despacho_id", "formula_detalle_id", "lote_id"}))
@Getter
@Setter
public class DespachoDetalle extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id", nullable = false)
    private Despacho despacho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_detalle_id", nullable = false)
    private FormulaMedicaDetalle formulaDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteMedicamento lote;

    @Column(name = "cantidad_entregada", nullable = false)
    private Integer cantidadEntregada;

    @Column(name = "precio_venta_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioVentaUnitario;

    @Column(name = "porcentaje_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIva;

    @Column(name = "facturado", nullable = false)
    private Boolean facturado = false;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
