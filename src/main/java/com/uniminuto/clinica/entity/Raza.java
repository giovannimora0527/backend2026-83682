
package com.uniminuto.clinica.entity;

// ==========================================================
// 1. IMPORTACIONES
// ==========================================================

// Anotaciones para conectar la clase con MySQL.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Manejo de fechas y horas.
import java.time.LocalDateTime;

// Lombok genera getters, setters y constructores.
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ==========================================================
 * CLÍNICA VETERINARIA - UNIMINUTO
 * ENTIDAD RAZA
 * ==========================================================
 *
 * Representa la tabla "raza" en MySQL.
 *
 * Columnas:
 * - raza_id
 * - nombre
 * - especie
 * - fecha_creacion
 * - fecha_modificacion
 *
 * Esta entidad permitirá consultar los datos
 * de las razas desde Spring Boot.
 */

@Data
@NoArgsConstructor
@Entity
@Table(name = "raza")
public class Raza {

    // ======================================================
    // 2. IDENTIFICADOR DE LA RAZA
    // ======================================================

    /**
     * Llave primaria de la tabla.
     *
     * Se configura para usar un ID autoincremental.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raza_id")
    private Integer razaId;

    // ======================================================
    // 3. NOMBRE DE LA RAZA
    // ======================================================

    /**
     * Nombre registrado en MySQL.
     *
     * Ejemplos:
     * - Gato
     * - Perro
     */
    @Column(name = "nombre")
    private String nombre;

    // ======================================================
    // 4. ESPECIE
    // ======================================================

    /**
     * Especie asociada al registro.
     *
     * Ejemplos:
     * - Felino
     * - Canino
     */
    @Column(name = "especie")
    private String especie;

    // ======================================================
    // 5. FECHA DE CREACIÓN
    // ======================================================

    /**
     * Fecha en que se creó el registro.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // ======================================================
    // 6. FECHA DE MODIFICACIÓN
    // ======================================================

    /**
     * Fecha de la última modificación.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
