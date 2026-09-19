package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data ;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
@Entity
@Table (name ="cita")
@Data

public class Cita {
// llave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id",nullable = false)
    private Long id ;
    // Muchas citas puedes estar asociadas a un mismo cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id",nullable = false)
    private Cliente cliente ;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false )
    private Mascota mascota ;

    @ManyToOne
    @JoinColumn (name ="medico_id",nullable = false)
    private Medico medico ;
    @Column (name="fecha_hora",nullable = false)
    private LocalDateTime fechaHora;

    @Column (name="estado",nullable = false)
    private String estado ;
    @Column (name = "motivo")
    private  String motivo ;

}
