package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "anotacion_historia")
@Data
public class AnotacionHistoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id ;
    @Column (name ="historia_id",nullable = false)
    private Integer historiaId ;
    @Column (name="medico_id",nullable = false)
    private Integer medicoId ;
    @Column (name ="fecha",nullable = false)
    private LocalDateTime fecha ;
    @Column (name= "descripcion",nullable = false)
    private String descripcion ;





}
