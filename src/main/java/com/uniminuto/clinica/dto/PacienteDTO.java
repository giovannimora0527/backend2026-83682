package com.uniminuto.clinica.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para {@link com.uniminuto.clinica.models.Paciente}.
 *
 * <p>¿Por que usar un DTO y no exponer directamente la entidad? Porque la
 * entidad esta pensada para la base de datos (tiene relaciones, anotaciones
 * de JPA, etc), mientras que el DTO es un objeto simple pensado para viajar
 * por la red (en el JSON de entrada/salida de la API). Esto evita exponer
 * detalles internos y previene errores de referencias circulares al
 * convertir a JSON.</p>
 */
public class PacienteDTO {

    /** Identificador del paciente (nulo cuando se esta creando uno nuevo). */
    private Long id;

    /** Nombre de la mascota. No puede estar vacio. */
    @NotBlank(message = "El nombre del paciente es obligatorio")
    private String nombre;

    /** Especie de la mascota (Perro, Gato, etc). */
    @NotBlank(message = "La especie del paciente es obligatoria")
    private String especie;

    /** Raza de la mascota. */
    private String raza;

    /** Nombre del propietario de la mascota. */
    @NotBlank(message = "El propietario del paciente es obligatorio")
    private String propietario;

    /** Constructor vacio, necesario para que Spring pueda convertir JSON en este objeto. */
    public PacienteDTO() {
    }

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
