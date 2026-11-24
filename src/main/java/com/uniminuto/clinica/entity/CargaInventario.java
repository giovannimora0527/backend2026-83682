package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.EstadoCarga;
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
@Table(name = "carga_inventario")
@Getter
@Setter
public class CargaInventario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "formato", nullable = false, length = 10)
    private String formato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_confirmacion_id")
    private Usuario usuarioConfirmacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoCarga estado;

    @Column(name = "total_filas", nullable = false)
    private Integer totalFilas = 0;

    @Column(name = "filas_validas", nullable = false)
    private Integer filasValidas = 0;

    @Column(name = "filas_con_error", nullable = false)
    private Integer filasConError = 0;

    @Column(name = "filas_importadas", nullable = false)
    private Integer filasImportadas = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
