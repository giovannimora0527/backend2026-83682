
package com.uniminuto.clinica.entity;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// ENTIDAD: MÉDICO VETERINARIO
// ==========================================================

// Importaciones de JPA.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Lombok genera automáticamente getters y setters.
import lombok.Data;

/**
 * Entidad que representa un médico veterinario.
 *
 * Esta clase se relaciona con la tabla
 * "medico" de la base de datos "clinica".
 *
 * Cada atributo corresponde a una columna
 * existente en MySQL.
 */
@Entity
@Table(name = "medico")
@Data
public class Medico {

    // ======================================================
    // 1. IDENTIFICADOR DEL MÉDICO
    // ======================================================

    /**
     * Llave primaria.
     * MySQL genera el identificador automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ======================================================
    // 2. TIPO DE DOCUMENTO
    // ======================================================

    /**
     * Ejemplos: CC, CE, PAS.
     * Máximo 10 caracteres.
     */
    @Column(
            name = "tipo_documento",
            nullable = false,
            length = 10
    )
    private String tipoDocumento;

    // ======================================================
    // 3. NÚMERO DE DOCUMENTO
    // ======================================================

    /**
     * Número de identificación del médico.
     * No puede repetirse.
     */
    @Column(
            name = "numero_documento",
            nullable = false,
            unique = true,
            length = 20
    )
    private String numeroDocumento;

    // ======================================================
    // 4. NOMBRES DEL MÉDICO
    // ======================================================

    @Column(
            name = "nombres",
            nullable = false,
            length = 100
    )
    private String nombres;

    // ======================================================
    // 5. APELLIDOS DEL MÉDICO
    // ======================================================

    @Column(
            name = "apellidos",
            nullable = false,
            length = 100
    )
    private String apellidos;

    // ======================================================
    // 6. TELÉFONO
    // ======================================================

    /**
     * Campo opcional.
     * Puede estar vacío en la base de datos.
     */
    @Column(
            name = "telefono",
            length = 20
    )
    private String telefono;

    // ======================================================
    // 7. REGISTRO PROFESIONAL
    // ======================================================

    /**
     * Identificador profesional del veterinario.
     * Debe ser único.
     */
    @Column(
            name = "registro_profesional",
            nullable = false,
            unique = true,
            length = 50
    )
    private String registroProfesional;

    // ======================================================
    // 8. ESPECIALIZACIÓN DEL MÉDICO
    // ======================================================

    /**
     * Identificador de la especialización.
     *
     * Se utiliza Integer porque la columna
     * especializacion_id es de tipo INT.
     *
     * Por ahora almacenamos el identificador,
     * sin agregar relaciones JPA.
     */
    @Column(
            name = "especializacion_id",
            nullable = false
    )
    private Integer especializacionId;

}
