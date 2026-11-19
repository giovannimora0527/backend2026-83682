
package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Representa los usuarios registrados
 * en la tabla usuario de MySQL.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    // Identificador del usuario.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Nombre utilizado para iniciar sesión.
    @Column(name = "username", nullable = false)
    private String username;

    // Contraseña protegida mediante hash.
    @Column(name = "password_hash")
    private String passwordHash;

    // Rol anterior del sistema.
    @Column(name = "rol")
    private String rol;

    // Correo electrónico.
    @Column(name = "email")
    private String email;

    // Identificador del perfil asignado.
    @Column(name = "perfil_id")
    private Integer perfilId;

    // Indica si el usuario está activo.
    @Column(name = "activo")
    private Boolean activo;

    // Fecha de creación.
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
