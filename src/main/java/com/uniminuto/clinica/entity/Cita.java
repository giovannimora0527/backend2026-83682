
package com.uniminuto.clinica.entity;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// ENTIDAD CITA
// ==========================================================

// Importaciones de JPA para relacionar Java con MySQL.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Lombok genera getters, setters y otros métodos.
import lombok.Data;

// Permite manejar fecha y hora.
import java.time.LocalDateTime;

/**
 * ==========================================================
 * ENTIDAD: CITA
 * ==========================================================
 *
 * Representa una cita médica registrada
 * en la Clínica Veterinaria.
 *
 * Tabla de MySQL: cita
 *
 * Campos:
 * - id
 * - cliente_id
 * - mascota_id
 * - medico_id
 * - fecha_hora
 * - estado
 * - motivo
 *
 * La entidad almacena los identificadores de
 * clientes, mascotas y médicos.
 *
 * Las relaciones entre estos registros se
 * validarán desde la capa de servicios.
 *
 * @author UNIMINUTO
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    // ======================================================
    // 1. IDENTIFICADOR DE LA CITA
    // ======================================================

    /**
     * Identificador único de la cita.
     *
     * Corresponde a:
     * id BIGINT UNSIGNED AUTO_INCREMENT
     *
     * La base de datos genera el valor
     * automáticamente al crear una cita.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    // ======================================================
    // 2. IDENTIFICADOR DEL CLIENTE
    // ======================================================

    /**
     * Identificador del propietario
     * que solicita la cita médica.
     *
     * Columna: cliente_id
     *
     * Es obligatorio.
     *
     * El servicio deberá verificar que
     * el cliente exista en MySQL.
     */
    @Column(
            name = "cliente_id",
            nullable = false
    )
    private Integer clienteId;


    // ======================================================
    // 3. IDENTIFICADOR DE LA MASCOTA
    // ======================================================

    /**
     * Identificador de la mascota
     * que recibirá atención veterinaria.
     *
     * Columna: mascota_id
     *
     * El servicio deberá validar:
     *
     * 1. Que la mascota exista.
     * 2. Que pertenezca al cliente seleccionado.
     */
    @Column(
            name = "mascota_id",
            nullable = false
    )
    private Integer mascotaId;


    // ======================================================
    // 4. IDENTIFICADOR DEL MÉDICO
    // ======================================================

    /**
     * Identificador del veterinario
     * encargado de atender la cita.
     *
     * Columna: medico_id
     *
     * El servicio deberá comprobar:
     *
     * 1. Que el médico exista.
     * 2. Que esté disponible.
     * 3. Que no tenga citas incompatibles
     *    con el horario solicitado.
     */
    @Column(
            name = "medico_id",
            nullable = false
    )
    private Integer medicoId;


    // ======================================================
    // 5. FECHA Y HORA DE LA CITA
    // ======================================================

    /**
     * Fecha y hora programadas
     * para la atención veterinaria.
     *
     * Columna: fecha_hora
     *
     * Tipo MySQL: DATETIME
     *
     * Ejemplo:
     * 2026-10-20T14:30:00
     *
     * El servicio deberá impedir registrar
     * citas en fechas anteriores a la actual.
     */
    @Column(
            name = "fecha_hora",
            nullable = false
    )
    private LocalDateTime fechaHora;


    // ======================================================
    // 6. ESTADO DE LA CITA
    // ======================================================

    /**
     * Indica el estado actual de la cita.
     *
     * Columna: estado
     *
     * Tipo MySQL: VARCHAR(20)
     *
     * Estados requeridos:
     *
     * - pendiente
     * - confirmada
     * - atendida
     * - cancelada
     * - programada
     *
     * El servicio deberá controlar
     * las transiciones permitidas.
     *
     * Se mantiene String para conservar
     * compatibilidad con los datos existentes.
     */
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private String estado;


    // ======================================================
    // 7. MOTIVO DE LA CONSULTA
    // ======================================================

    /**
     * Describe el motivo por el que
     * el cliente solicita la cita.
     *
     * Columna: motivo
     *
     * Tipo MySQL: TEXT
     *
     * Ejemplos:
     * - Consulta general.
     * - Vacunación.
     * - Control veterinario.
     * - Revisión odontológica.
     *
     * La columna permite valores nulos.
     */
    @Column(
            name = "motivo",
            columnDefinition = "TEXT"
    )
    private String motivo;

}
