package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
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
import java.time.LocalDateTime;

/** Registro historico: solo se inserta. */
@Immutable
@Entity
@Table(name = "formula_historial_estado")
@Getter
@Setter
public class FormulaHistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaMedica formula;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado_despacho_anterior", length = 30)
    private EstadoDespacho estadoDespachoAnterior;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado_despacho_nuevo", length = 30)
    private EstadoDespacho estadoDespachoNuevo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado_formula_anterior", length = 30)
    private EstadoFormula estadoFormulaAnterior;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado_formula_nuevo", length = 30)
    private EstadoFormula estadoFormulaNuevo;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
}
