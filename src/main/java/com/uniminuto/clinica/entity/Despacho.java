package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "despacho")
@Getter
@Setter
public class Despacho extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaMedica formula;

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPreparacion estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "idempotency_key", unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "fecha_reversion")
    private LocalDateTime fechaReversion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_reversion_id")
    private Usuario usuarioReversion;

    @Column(name = "motivo_reversion", length = 500)
    private String motivoReversion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
