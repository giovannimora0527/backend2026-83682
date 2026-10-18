package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Mascota que pertenece a un cliente (relación Cliente 1:N Mascota). Entidad de solo lectura aquí.
 */
@Entity
@Table(name = "mascota")
public class Mascota {

    /** Identificador de la mascota. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mascota_id")
    private Long id;

    /** Nombre de la mascota. */
    @Column(name = "nombre_mascota")
    private String nombre;

    /** Edad en años. */
    @Column(name = "edad")
    private Integer edad;

    /** Raza de la mascota. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raza_id")
    private Raza raza;

    /** Cliente dueño de la mascota. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Indica si la mascota está activa. */
    @Column(name = "activo")
    private boolean activo;

    /** Constructor requerido por JPA. */
    protected Mascota() {
    }

    /** @return identificador de la mascota */
    public Long getId() {
        return id;
    }

    /** @return nombre de la mascota */
    public String getNombre() {
        return nombre;
    }

    /** @return edad en años */
    public Integer getEdad() {
        return edad;
    }

    /** @return raza */
    public Raza getRaza() {
        return raza;
    }

    /** @return cliente dueño */
    public Cliente getCliente() {
        return cliente;
    }

    /** @return {@code true} si la mascota está activa */
    public boolean isActivo() {
        return activo;
    }
}
