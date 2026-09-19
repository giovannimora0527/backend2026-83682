package com.uniminuto.clinica.entity;
// importamos todas las clases que se necesiten usar
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Table (name = "formula_medica" )
@Data

public class FormulaMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name ="id", nullable = false)
    private Long id ;

    @ManyToOne
    @JoinColumn (name = "cita_id", nullable = false)
    private Cita cita ;

    @ManyToOne
    @JoinColumn (name = "medicamento_id", nullable = false)
    private Medicamento medicamento ;

    @Column (name = "dosis",nullable = false)
    private String dosis ;

    @Column (name= "indicaciones")
    private String indicaciones ;

    @Column (name = "fecha_creacion_registro",nullable = false)
    private  LocalDateTime fechaCreacionRegistro ;

    @Column (name = "fecha_actualizacion_registro")
    private LocalDateTime fechaActualizacionRegistro ;


}
