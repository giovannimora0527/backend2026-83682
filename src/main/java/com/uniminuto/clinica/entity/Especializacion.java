package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "especializacion")
@Data

public class Especializacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    @Column (name="id",nullable = false)
    private Long id ;

    @Column(name= "nombre",nullable = false)
    private String  nombre ;

    @Column (name = "descripcion" )
    private String descripcion ;

    @Column(name="codigo_especializacion",nullable = false)
    private String codigoEspecializacion ;

}
