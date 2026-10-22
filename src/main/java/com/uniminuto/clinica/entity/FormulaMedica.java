
package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una fórmula médica
 * de la clínica veterinaria UNIMINUTO.
 *
 * Esta clase permite relacionar las fórmulas
 * médicas con la tabla formula_medica de MySQL.
 */
@Entity
@Table(name = "formula_medica")
@Data
@NoArgsConstructor
public class FormulaMedica {

    // ==========================================
    // 1. IDENTIFICADOR DE LA FÓRMULA
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ==========================================
    // 2. IDENTIFICADOR DE LA CITA MÉDICA
    // ==========================================

    // Relaciona la fórmula con una cita existente.
    @Column(name = "cita_id", nullable = false)
    private Integer citaId;

    // ==========================================
    // 3. IDENTIFICADOR DEL MEDICAMENTO
    // ==========================================

    // Medicamento que se prescribe al paciente.
    @Column(name = "medicamento_id", nullable = false)
    private Integer medicamentoId;

    // ==========================================
    // 4. DOSIS DEL MEDICAMENTO
    // ==========================================

    // Cantidad del medicamento que debe recibir.
    @Column(name = "dosis", nullable = false)
    private String dosis;

    // ==========================================
    // 5. FRECUENCIA DE ADMINISTRACIÓN
    // NUEVO CAMPO DEL SEGUNDO PARCIAL
    // ==========================================

    // Ejemplo: Cada 8 horas.
    @Column(name = "frecuencia", length = 100)
    private String frecuencia;

    // ==========================================
    // 6. DURACIÓN DEL TRATAMIENTO
    // NUEVO CAMPO DEL SEGUNDO PARCIAL
    // ==========================================

    // Ejemplo: 7 días.
    @Column(name = "duracion_tratamiento", length = 100)
    private String duracionTratamiento;

    // ==========================================
    // 7. INDICACIONES MÉDICAS
    // ==========================================

    // Ejemplo: Administrar después de comer.
    @Column(name = "indicaciones")
    private String indicaciones;

    // ==========================================
    // 8. FECHA DE EMISIÓN
    // NUEVO CAMPO DEL SEGUNDO PARCIAL
    // ==========================================

    // Fecha en la que se expide la fórmula.
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    // ==========================================
    // 9. FECHA DE CREACIÓN DEL REGISTRO
    // ==========================================

    @Column(
            name = "fecha_creacion_registro",
            nullable = false
    )
    private LocalDateTime fechaCreacionRegistro;

    // ==========================================
    // 10. FECHA DE ACTUALIZACIÓN
    // ==========================================

    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro;
}
