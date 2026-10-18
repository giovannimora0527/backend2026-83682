package com.uniminuto.clinica.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Usuario del sistema (tabla {@code usuario}). El hash de la contraseña nunca se serializa. */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** Nombre de usuario. */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /** Contraseña almacenada como hash. */
    @JsonIgnore
    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    /** Rol del usuario. */
    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    /** Fecha de creación del usuario. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Indica si el usuario está activo. */
    @Column(name = "activo")
    private Boolean activo;

    /** Correo electrónico. */
    @Column(name = "email", nullable = false, length = 255)
    private String email;
}
