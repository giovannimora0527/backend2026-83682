package com.uniminuto.clinica.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa a un paciente (mascota) de la clinica veterinaria.
 *
 * <p>Un paciente es el "dueño" de las citas, formulas medicas e historias
 * medicas: todo gira alrededor de que mascota estamos atendiendo. Esta
 * entidad no la pide explicitamente el enunciado, pero es necesaria para
 * poder relacionar {@link Cita} y {@link HistoriaMedica} con alguien.</p>
 *
 * <p>La anotacion {@code @Entity} le indica a Hibernate (JPA) que esta clase
 * representa una tabla en la base de datos. Cada atributo se convierte en
 * una columna de esa tabla.</p>
 */
@Entity
@Table(name = "paciente")
public class Paciente {

    /**
     * Identificador unico del paciente en la base de datos.
     * {@code @GeneratedValue} le indica a la base de datos que ella misma
     * debe generar este valor automaticamente (autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la mascota. Ejemplo: "Firulais". */
    private String nombre;

    /** Especie de la mascota. Ejemplo: "Perro", "Gato". */
    private String especie;

    /** Raza de la mascota. Ejemplo: "Labrador". */
    private String raza;

    /** Nombre del propietario (dueño) de la mascota. */
    private String propietario;

    /**
     * Constructor vacio.
     * JPA lo necesita obligatoriamente para poder crear objetos de esta
     * clase cuando lee datos desde la base de datos.
     */
    public Paciente() {
    }

    /**
     * Constructor con los datos basicos de un paciente, util para crear
     * pacientes de prueba desde el codigo.
     *
     * @param nombre      nombre de la mascota
     * @param especie     especie de la mascota
     * @param raza        raza de la mascota
     * @param propietario nombre del dueño
     */
    public Paciente(String nombre, String especie, String raza, String propietario) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.propietario = propietario;
    }

    // ---------------------------------------------------------------
    // Getters y Setters: permiten leer y modificar los atributos
    // privados de la clase desde afuera de forma controlada.
    // ---------------------------------------------------------------

    /** @return el id del paciente */
    public Long getId() {
        return id;
    }

    /** @param id nuevo id del paciente */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return el nombre de la mascota */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nuevo nombre de la mascota */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return la especie de la mascota */
    public String getEspecie() {
        return especie;
    }

    /** @param especie nueva especie de la mascota */
    public void setEspecie(String especie) {
        this.especie = especie;
    }

    /** @return la raza de la mascota */
    public String getRaza() {
        return raza;
    }

    /** @param raza nueva raza de la mascota */
    public void setRaza(String raza) {
        this.raza = raza;
    }

    /** @return el nombre del propietario */
    public String getPropietario() {
        return propietario;
    }

    /** @param propietario nuevo nombre del propietario */
    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }
}
