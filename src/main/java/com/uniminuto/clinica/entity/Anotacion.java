package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import java.time.LocalDateTime;

/**
 * Evolución asociada a una historia clínica (tabla {@code anotacion_historia}).
 * La observación y el diagnóstico de evolución se guardan como líneas etiquetadas
 * dentro de {@code descripcion}, la única columna de texto disponible.
 */
@Entity
@Table(name = "anotacion_historia")
@Getter
@Setter
public class Anotacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long anotacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "medico_id", nullable = false)
    private Veterinario veterinario;

    @Column(name = "fecha")
    private LocalDateTime fechaHora;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}
