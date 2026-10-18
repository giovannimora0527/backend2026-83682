package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Cliente (dueño de una o varias mascotas). Entidad de solo lectura en este módulo.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    /** Identificador del cliente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliente_id")
    private Long id;

    /** Tipo de documento (CC, TI, CE...). */
    @Column(name = "tipo_documento")
    private String tipoDocumento;

    /** Número de documento (único). */
    @Column(name = "numero_documento")
    private String numeroDocumento;

    /** Nombres del cliente. */
    @Column(name = "nombres")
    private String nombres;

    /** Apellidos del cliente. */
    @Column(name = "apellidos")
    private String apellidos;

    /** Fecha de nacimiento. */
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    /** Género (M/F). */
    @Column(name = "genero")
    private String genero;

    /** Teléfono de contacto. */
    @Column(name = "telefono")
    private String telefono;

    /** Dirección de residencia. */
    @Column(name = "direccion")
    private String direccion;

    /** Indica si el cliente está activo. */
    @Column(name = "activo")
    private boolean activo;

    /** Constructor requerido por JPA. */
    protected Cliente() {
    }

    /** @return identificador del cliente */
    public Long getId() {
        return id;
    }

    /** @return tipo de documento */
    public String getTipoDocumento() {
        return tipoDocumento;
    }

    /** @return número de documento */
    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    /** @return nombres */
    public String getNombres() {
        return nombres;
    }

    /** @return apellidos */
    public String getApellidos() {
        return apellidos;
    }

    /** @return fecha de nacimiento */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /** @return género */
    public String getGenero() {
        return genero;
    }

    /** @return teléfono */
    public String getTelefono() {
        return telefono;
    }

    /** @return dirección */
    public String getDireccion() {
        return direccion;
    }

    /** @return {@code true} si el cliente está activo */
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
