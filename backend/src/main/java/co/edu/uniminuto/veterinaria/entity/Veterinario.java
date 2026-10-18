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
 * Veterinario del centro (en el dump original se llamaba "medico"). Entidad de solo lectura aquí.
 */
@Entity
@Table(name = "veterinario")
public class Veterinario {

    /** Identificador del veterinario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Nombres. */
    @Column(name = "nombres")
    private String nombres;

    /** Apellidos. */
    @Column(name = "apellidos")
    private String apellidos;

    /** Registro profesional (único). */
    @Column(name = "registro_profesional")
    private String registroProfesional;

    /** Especialización del veterinario. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especializacion_id")
    private Especializacion especializacion;

    /** Indica si el veterinario está activo. */
    @Column(name = "activo")
    private boolean activo;

    /** Constructor requerido por JPA. */
    protected Veterinario() {
    }

    /** @return identificador */
    public Long getId() {
        return id;
    }

    /** @return nombres */
    public String getNombres() {
        return nombres;
    }

    /** @return apellidos */
    public String getApellidos() {
        return apellidos;
    }

    /** @return registro profesional */
    public String getRegistroProfesional() {
        return registroProfesional;
    }

    /** @return especialización */
    public Especializacion getEspecializacion() {
        return especializacion;
    }

    /** @return {@code true} si está activo */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Construye el nombre completo para mostrar.
     *
     * @return nombres y apellidos separados por un espacio
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
