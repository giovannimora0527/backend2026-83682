package co.edu.uniminuto.veterinaria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Medicamento disponible para formular. Entidad de solo lectura en este módulo.
 */
@Entity
@Table(name = "medicamento")
public class Medicamento {

    /** Identificador del medicamento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Nombre comercial o genérico (único). */
    @Column(name = "nombre")
    private String nombre;

    /** Presentación (tabletas, suspensión...). */
    @Column(name = "presentacion")
    private String presentacion;

    /** Fecha de vencimiento: un medicamento vencido no puede formularse. */
    @Column(name = "fecha_vence")
    private LocalDate fechaVence;

    /** Constructor requerido por JPA. */
    protected Medicamento() {
    }

    /** @return identificador */
    public Long getId() {
        return id;
    }

    /** @return nombre */
    public String getNombre() {
        return nombre;
    }

    /** @return presentación */
    public String getPresentacion() {
        return presentacion;
    }

    /** @return fecha de vencimiento */
    public LocalDate getFechaVence() {
        return fechaVence;
    }
}
