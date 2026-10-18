
package com.uniminuto.clinica.entity;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// ENTIDAD ESPECIALIZACIÓN
// ==========================================================

// Anotaciones para relacionar Java con MySQL.
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

// Lombok genera getters, setters y constructores.
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ==========================================================
 * 1. ENTIDAD ESPECIALIZACIÓN
 * ==========================================================
 *
 * Representa la tabla especializacion
 * de la base de datos clinica.
 *
 * Columnas:
 *
 * 1. id
 * 2. nombre
 * 3. descripcion
 * 4. codigo_especializacion
 *
 * Permite a Spring Boot consultar
 * las especializaciones registradas.
 */

@Data
@NoArgsConstructor
@Entity
@Table(name = "especializacion")
public class Especializacion {

    // ======================================================
    // 2. IDENTIFICADOR
    // ======================================================

    /**
     * Llave primaria de la tabla.
     *
     * En MySQL:
     * id BIGINT UNSIGNED AUTO_INCREMENT
     *
     * Utilizamos Long porque el campo
     * de MySQL es BIGINT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ======================================================
    // 3. NOMBRE DE LA ESPECIALIZACIÓN
    // ======================================================

    /**
     * Nombre de la especialización.
     *
     * Ejemplos:
     * - Neurología
     * - Dermatología
     * - Oncología
     *
     * Máximo 100 caracteres.
     * No permite valores nulos.
     * Debe ser único.
     */
    @Column(
            name = "nombre",
            nullable = false,
            length = 100,
            unique = true
    )
    private String nombre;

    // ======================================================
    // 4. DESCRIPCIÓN
    // ======================================================

    /**
     * Explicación de la especialización.
     *
     * En MySQL es de tipo TEXT
     * y permite valores nulos.
     */
    @Column(
            name = "descripcion",
            columnDefinition = "TEXT"
    )
    private String descripcion;

    // ======================================================
    // 5. CÓDIGO DE ESPECIALIZACIÓN
    // ======================================================

    /**
     * Código único de la especialización.
     *
     * Ejemplos:
     * - COD_02
     * - COD_03
     * - COD_04
     *
     * Máximo 10 caracteres.
     */
    @Column(
            name = "codigo_especializacion",
            nullable = false,
            length = 10,
            unique = true
    )
    private String codigoEspecializacion;

}
