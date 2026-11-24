package com.uniminuto.clinica.entity;

import com.uniminuto.clinica.entity.enums.AccionCarga;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "carga_inventario_fila", uniqueConstraints = @UniqueConstraint(columnNames = {"carga_id", "numero_fila"}))
@Getter
@Setter
public class CargaInventarioFila extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private CargaInventario carga;

    @Column(name = "numero_fila", nullable = false)
    private Integer numeroFila;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_json", nullable = false)
    private Map<String, String> datosJson;

    @Column(name = "valida", nullable = false)
    private Boolean valida;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "accion", length = 30)
    private AccionCarga accion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errores_json", nullable = false)
    private List<ErrorFila> erroresJson;

    @Column(name = "importada", nullable = false)
    private Boolean importada = false;

    @Column(name = "error_importacion", length = 500)
    private String errorImportacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private LoteMedicamento lote;
}
