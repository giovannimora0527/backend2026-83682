
package com.uniminuto.clinica.entity;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// ENTIDAD DE GESTIÓN DE MEDICAMENTOS
// ==========================================================

// Importaciones de JPA para conectar la entidad con MySQL.
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

// Lombok genera automáticamente getters y setters.
import lombok.Data;
import lombok.NoArgsConstructor;

// Tipos de datos utilizados para las fechas.
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ==========================================================
 * 1. ENTIDAD MEDICAMENTO
 * ==========================================================
 *
 * Representa la tabla medicamento de MySQL.
 *
 * Base de datos: clinica
 * Tabla: medicamento
 *
 * Campos:
 * 1. id
 * 2. nombre
 * 3. descripcion
 * 4. presentacion
 * 5. fecha_compra
 * 6. fecha_vence
 * 7. fecha_creacion_registro
 * 8. fecha_modificacion_registro
 */

@Data
@NoArgsConstructor
@Entity
@Table(name = "medicamento")
public class Medicamento {

    // ======================================================
    // 2. IDENTIFICADOR DEL MEDICAMENTO
    // ======================================================

    /**
     * Llave primaria.
     *
     * MySQL: BIGINT UNSIGNED AUTO_INCREMENT
     *
     * Se utiliza Long porque el ID es BIGINT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ======================================================
    // 3. NOMBRE DEL MEDICAMENTO
    // ======================================================

    /**
     * Nombre del medicamento.
     *
     * Ejemplos:
     * - Paracetamol
     * - Paracetamol Inyectable
     *
     * Es obligatorio y único.
     */
    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 100
    )
    private String nombre;

    // ======================================================
    // 4. DESCRIPCIÓN
    // ======================================================

    /**
     * Descripción del medicamento.
     *
     * En MySQL es de tipo TEXT.
     * Puede contener valores nulos.
     */
    @Column(
            name = "descripcion",
            columnDefinition = "TEXT"
    )
    private String descripcion;

    // ======================================================
    // 5. PRESENTACIÓN
    // ======================================================

    /**
     * Presentación del medicamento.
     *
     * Ejemplos:
     * - Tabletas 500mg
     * - Tabletas 1200mg
     *
     * Es opcional.
     */
    @Column(
            name = "presentacion",
            length = 100
    )
    private String presentacion;

    // ======================================================
    // 6. FECHA DE COMPRA
    // ======================================================

    /**
     * Fecha en la que se compró el medicamento.
     *
     * MySQL: DATE
     * Java: LocalDate
     *
     * Es obligatoria.
     */
    @Column(
            name = "fecha_compra",
            nullable = false
    )
    private LocalDate fechaCompra;

    // ======================================================
    // 7. FECHA DE VENCIMIENTO
    // ======================================================

    /**
     * Fecha en la que vence el medicamento.
     *
     * MySQL: DATE
     * Java: LocalDate
     *
     * Es obligatoria.
     */
    @Column(
            name = "fecha_vence",
            nullable = false
    )
    private LocalDate fechaVence;

    // ======================================================
    // 8. FECHA DE CREACIÓN DEL REGISTRO
    // ======================================================

    /**
     * Fecha y hora de creación del registro.
     *
     * MySQL: DATETIME
     * Java: LocalDateTime
     *
     * Es obligatoria.
     */
    @Column(
            name = "fecha_creacion_registro",
            nullable = false
    )
    private LocalDateTime fechaCreacionRegistro;

    // ======================================================
    // 9. FECHA DE MODIFICACIÓN DEL REGISTRO
    // ======================================================

    /**
     * Fecha y hora de la última modificación.
     *
     * MySQL: DATETIME
     * Java: LocalDateTime
     *
     * Puede contener valores nulos.
     */
    @Column(
            name = "fecha_modificacion_registro"
    )
    private LocalDateTime fechaModificacionRegistro;

}
