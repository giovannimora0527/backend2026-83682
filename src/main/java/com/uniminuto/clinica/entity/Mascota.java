package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "mascota")
@Data
public class Mascota {

    @Id
    @Column(name = "mascota_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mascotaId;

    @Column(name = "nombre_mascota", nullable = false)
    private String nombreMascota;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "raza_id")
    private Raza raza;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
