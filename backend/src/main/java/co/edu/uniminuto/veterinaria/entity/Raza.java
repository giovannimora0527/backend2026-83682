package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Raza de una mascota (con su especie). Entidad de solo lectura en este módulo.
 */
@Entity
@Table(name = "raza")
public class Raza {

    /** Identificador de la raza. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raza_id")
    private Long id;

    /** Nombre de la raza. */
    @Column(name = "nombre")
    private String nombre;

    /** Especie a la que pertenece. */
    @Column(name = "especie")
    private String especie;

    /** Constructor requerido por JPA. */
    protected Raza() {
    }

    /** @return identificador de la raza */
    public Long getId() {
        return id;
    }

    /** @return nombre de la raza */
    public String getNombre() {
        return nombre;
    }

    /** @return especie */
    public String getEspecie() {
        return especie;
    }
}
