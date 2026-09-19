package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table (name= "medico" )
@Data

public class Medico {
    // llave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "id", nullable = false)
    private Long id ;
    @Column (name = "tipo_documento",nullable = false)
    private String tipoDocumento;
    @Column (name = "numero_documento",nullable = false)
    private String numeroDocumento ;
    @Column (name="nombres",nullable = false)
    private String nombres ;
    @Column (name="apellidos",nullable = false)
    private String apellidos ;
    @Column (name= "telefono")
    private String telefono ;
    @Column (name = "registro_profesional", nullable = false)
    private  String registroProfesional ;
    @ManyToOne
    @JoinColumn (name ="especializacion_id",nullable = false)
    private Especializacion especializacion ;
}
