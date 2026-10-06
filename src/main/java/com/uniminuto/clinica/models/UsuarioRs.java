package com.uniminuto.clinica.models;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioRs {

    /*
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre de usuario.
     */
    private String username;

    /**
     * Rol del usuario.
     */
    private String rol;

    /**
     * Fecha de creación del usuario.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el usuario está activo.
     */
    private Boolean activo;

    /**
     * Correo electrónico.
     */
    private String email;
}
