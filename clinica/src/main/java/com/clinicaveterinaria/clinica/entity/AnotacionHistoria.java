package com.clinicaveterinaria.clinica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "anotacion_historia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnotacionHistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "historia_id", nullable = false)
    private Integer historiaId;

    @Column(name = "medico_id", nullable = false)
    private Integer medicoId;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}