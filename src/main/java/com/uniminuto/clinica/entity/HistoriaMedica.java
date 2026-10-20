
package com.uniminuto.clinica.entity;

// ==========================================================
// IMPORTACIONES
// ==========================================================

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ==========================================================
// ENTIDAD HISTORIA MÉDICA
// ==========================================================

@Entity
@Table(name = "historia_medica")
@Data
@NoArgsConstructor
public class HistoriaMedica {

    // ======================================================
    // 1. ID GENERADO AUTOMÁTICAMENTE POR MYSQL
    // ======================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ======================================================
    // 2. IDENTIFICADOR DEL PACIENTE
    // ======================================================

    @Column(name = "paciente_id", nullable = false)
    private Integer pacienteId;

    // ======================================================
    // 3. FECHA DE CREACIÓN
    // ======================================================

    // MySQL utiliza CURRENT_TIMESTAMP.
    // No enviamos este campo en las operaciones INSERT.
    // Tampoco permitimos modificarlo mediante UPDATE.

    @Column(
            name = "fecha_creacion",
            insertable = false,
            updatable = false
    )
    private LocalDateTime fechaCreacion;

}
