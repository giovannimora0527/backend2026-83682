package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Especialidad registrada en la tabla existente especializacion. */
@Entity
@Table(name = "especializacion")
@Getter
@Setter
public class Especializacion {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "codigo_especializacion", nullable = false, length = 10)
    private String codigoEspecializacion;
}
