package com.uniminuto.clinica.entity;
// estamos importando unas clases que vamos a usar
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data ;
import java.time.LocalDate;
import java.time.LocalDateTime;

//lombok @data Me evita escribir codigo repetitivo para los get y los set
@Entity
@Table (name = "medicamento")
@Data
public class Medicamento {
    //Lave primaria de la tabla medicamento
    @Id
    //Es el identificador que genera automaticamente la base de datos cuando valla a registrar algo
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false) // le estamos diciendo que este campo no debe ser nulo
    private Long id ;
//Esto es para guardar el nombre del medicamento
    @Column (name = "nombre", nullable = false)
    private String nombre;
    // Descripcion del medicamento
    @Column (name =  "descripcion") //Aca no le pongo lo de null porque puede ser nul o no nul en nuestra tabla
    private  String descripcion;
    // presentacion del medicamento
    @Column (name = "presentacion") //Aca no le pongo lo de null porque puede ser nul o no nul en nuestra tabla
    private String presentacion ;
    //Fecha en la que se realizo la compra del medicamento
    @Column (name = "fecha_compra", nullable = false)
    private  LocalDate fechaCompra;
    @Column (name= "fecha_vence", nullable = false)
    private  LocalDate fechaVence;

    @Column (name= "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacionRegistro ;

    @Column (name= "fecha_modificacion_registro")
    private LocalDateTime fechaModificacionRegistro;


}
