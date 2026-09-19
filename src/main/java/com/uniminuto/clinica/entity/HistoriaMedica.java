package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Table(name="historia_medica")
@Data
public class HistoriaMedica {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name="id",nullable = false)
    private Long id ;

    @Column(name = "paciente_id", nullable = false)
    private Integer pacienteId;

    @Column (name= "fecha_creacion")
    private LocalDateTime fechaCreacion ;
}
