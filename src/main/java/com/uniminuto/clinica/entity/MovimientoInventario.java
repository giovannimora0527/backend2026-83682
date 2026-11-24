package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.TipoMovimiento;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro historico: solo se inserta. */
@Immutable
@Entity
@Table(name = "movimiento_inventario")
@Getter
@Setter
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoMovimiento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteMedicamento lote;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "saldo_lote_resultante", nullable = false)
    private Integer saldoLoteResultante;

    @Column(name = "saldo_producto_resultante", nullable = false)
    private Integer saldoProductoResultante;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id")
    private FormulaMedica formula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id")
    private Despacho despacho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_detalle_id")
    private DespachoDetalle despachoDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id")
    private CargaInventario carga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_revertido_id")
    private MovimientoInventario movimientoRevertido;

    @Column(name = "observacion", length = 500)
    private String observacion;
}
