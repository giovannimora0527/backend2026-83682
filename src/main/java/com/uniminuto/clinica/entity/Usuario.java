package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    /** * Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** * Nombre de usuario. */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /** * Contraseña almacenada como hash. */
    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    /** * Rol del usuario. */
    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    /** * Fecha de creación del usuario. */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** * Indica si el usuario está activo. */
    @Column(name = "activo")
    private Boolean activo;

    /**
     * Correo electrónico.
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;
}
