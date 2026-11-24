package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "configuracion_clinica")
@Getter
@Setter
public class ConfiguracionClinica extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "nit", nullable = false, length = 30)
    private String nit;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "correo", length = 255)
    private String correo;

    @Column(name = "prefijo_factura", nullable = false, length = 10)
    private String prefijoFactura;

    @Column(name = "consecutivo_factura", nullable = false)
    private Long consecutivoFactura = 0L;

    @Column(name = "prefijo_formula", nullable = false, length = 10)
    private String prefijoFormula;

    @Column(name = "consecutivo_formula", nullable = false)
    private Long consecutivoFormula = 0L;

    @Column(name = "prefijo_despacho", nullable = false, length = 10)
    private String prefijoDespacho;

    @Column(name = "consecutivo_despacho", nullable = false)
    private Long consecutivoDespacho = 0L;

    @Column(name = "tope_descuento_secretaria", nullable = false, precision = 5, scale = 2)
    private BigDecimal topeDescuentoSecretaria = BigDecimal.valueOf(10);
}
