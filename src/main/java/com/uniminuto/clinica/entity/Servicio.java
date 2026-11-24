package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.TipoServicio;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "servicio")
@Getter
@Setter
public class Servicio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_servicio", nullable = false, length = 30)
    private TipoServicio tipoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especializacion_id")
    private Especializacion especializacion;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 30;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
