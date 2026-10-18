package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Especialización de un veterinario. Entidad de solo lectura en este módulo.
 */
@Entity
@Table(name = "especializacion")
public class Especializacion {

    /** Identificador de la especialización. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Nombre de la especialización. */
    @Column(name = "nombre")
    private String nombre;

    /** Constructor requerido por JPA. */
    protected Especializacion() {
    }

    /** @return identificador */
    public Long getId() {
        return id;
    }

    /** @return nombre de la especialización */
    public String getNombre() {
        return nombre;
    }
}
