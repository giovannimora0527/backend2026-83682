
package com.uniminuto.clinica.entity;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// ENTIDAD DE FÓRMULAS MÉDICAS
// ==========================================================

// Importaciones de JPA.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Manejo de fechas y horas.
import java.time.LocalDateTime;

// Genera automáticamente getters y setters.
import lombok.Data;
import lombok.NoArgsConstructor;

// ==========================================================
// CONFIGURACIÓN DE LA ENTIDAD
// ==========================================================

@Data
@NoArgsConstructor
@Entity
@Table(name = "formula_medica")
public class FormulaMedica {

    // ======================================================
    // 1. IDENTIFICADOR
    // ======================================================

    // Llave primaria automática.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ======================================================
    // 2. CITA MÉDICA
    // ======================================================

    // ID de la cita asociada con la fórmula.
    @Column(name = "cita_id", nullable = false)
    private Integer citaId;

    // ======================================================
    // 3. MEDICAMENTO
    // ======================================================

    // ID del medicamento formulado.
    @Column(name = "medicamento_id", nullable = false)
    private Integer medicamentoId;

    // ======================================================
    // 4. DOSIS
    // ======================================================

    // Cantidad y frecuencia del medicamento.
    @Column(
            name = "dosis",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String dosis;

    // ======================================================
    // 5. INDICACIONES
    // ======================================================

    // Recomendaciones para administrar el medicamento.
    // Este campo puede estar vacío.
    @Column(
            name = "indicaciones",
            columnDefinition = "TEXT"
    )
    private String indicaciones;

    // ======================================================
    // 6. FECHA DE CREACIÓN
    // ======================================================

    // Fecha y hora en que se registró la fórmula.
    @Column(
            name = "fecha_creacion_registro",
            nullable = false
    )
    private LocalDateTime fechaCreacionRegistro;

    // ======================================================
    // 7. FECHA DE ACTUALIZACIÓN
    // ======================================================

    // Fecha y hora de la última modificación.
    // Puede ser nula.
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;

}
