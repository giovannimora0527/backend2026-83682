package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.uniminuto.clinica.entity.enums.TipoProducto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicamento")
@Data
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, columnDefinition = "CHAR(8)")
    private String codigo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_producto", nullable = false, length = 20)
    private TipoProducto tipoProducto = TipoProducto.MEDICAMENTO;

    @Column(name = "requiere_invima", nullable = false)
    private Boolean requiereInvima = true;

    @Column(name = "registro_invima", length = 50)
    private String registroInvima;

    @Column(name = "unidad_medida", nullable = false, length = 30)
    private String unidadMedida = "unidad";

    @Column(name = "precio_venta", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "impuesto_id")
    private Impuesto impuesto;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "fecha_proximo_pedido")
    private LocalDate fechaProximoPedido;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "presentacion", length = 100)
    private String presentacion;

    /** Historico: las fechas vigentes estan en los lotes. */
    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro;

    @Column(name = "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;
}
