package com.uniminuto.clinica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Cliente o propietario responsable de una o varias mascotas. */
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String nombre;
    @NotBlank private String telefono;
    @Email private String correo;
    private String direccion;
    public Cliente() {}
    public Cliente(String nombre, String telefono, String correo, String direccion) { this.nombre=nombre; this.telefono=telefono; this.correo=correo; this.direccion=direccion; }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;}
    public String getTelefono(){return telefono;} public void setTelefono(String telefono){this.telefono=telefono;}
    public String getCorreo(){return correo;} public void setCorreo(String correo){this.correo=correo;}
    public String getDireccion(){return direccion;} public void setDireccion(String direccion){this.direccion=direccion;}
}
