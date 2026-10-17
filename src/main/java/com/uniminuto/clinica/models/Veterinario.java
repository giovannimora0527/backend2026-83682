package com.uniminuto.clinica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Profesional veterinario responsable de citas y decisiones clínicas. */
@Entity
@Table(name = "veterinario")
public class Veterinario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String nombre;
    @NotBlank private String tarjetaProfesional;
    @Email private String correo;
    private String telefono;
    private boolean activo = true;
    public Veterinario() {}
    public Veterinario(String nombre, String tarjetaProfesional, String correo, String telefono) { this.nombre=nombre; this.tarjetaProfesional=tarjetaProfesional; this.correo=correo; this.telefono=telefono; this.activo=true; }
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;}
    public String getTarjetaProfesional(){return tarjetaProfesional;} public void setTarjetaProfesional(String tarjetaProfesional){this.tarjetaProfesional=tarjetaProfesional;}
    public String getCorreo(){return correo;} public void setCorreo(String correo){this.correo=correo;}
    public String getTelefono(){return telefono;} public void setTelefono(String telefono){this.telefono=telefono;}
    public boolean isActivo(){return activo;} public void setActivo(boolean activo){this.activo=activo;}
}
