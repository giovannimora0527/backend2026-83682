package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.ViaAdministracion;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;

@Entity
@Table(name = "formula_medica_detalle", uniqueConstraints = @UniqueConstraint(columnNames = {"formula_id", "medicamento_id"}))
@Getter
@Setter
public class FormulaMedicaDetalle extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaMedica formula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "dosis", nullable = false, precision = 10, scale = 2)
    private BigDecimal dosis;

    @Column(name = "unidad_dosis", nullable = false, length = 30)
    private String unidadDosis;

    @Column(name = "frecuencia_horas", nullable = false)
    private Integer frecuenciaHoras;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "via_administracion", nullable = false, length = 30)
    private ViaAdministracion viaAdministracion;

    @Column(name = "cantidad_prescrita", nullable = false)
    private Integer cantidadPrescrita;

    @Column(name = "cantidad_despachada", nullable = false)
    private Integer cantidadDespachada = 0;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
