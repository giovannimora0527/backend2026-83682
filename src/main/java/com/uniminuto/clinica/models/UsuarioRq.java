package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioRq {
    /*
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre de usuario.
     */
    private String username;

    /**
     * Contraseña a guardar.
     */
    private String password;

    /**
     * Rol del usuario.
     */
    private String rol;

    /**
     * Indica si el usuario está activo.
     */
    private Boolean activo;

    /**
     * Correo electrónico.
     */
    private String email;
}
