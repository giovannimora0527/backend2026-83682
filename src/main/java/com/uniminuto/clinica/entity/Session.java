package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Identificador del JWT; el token completo no se guarda. */
    @Column(name = "jti", unique = true, length = 36)
    private String jti;

    @Column(name = "activa", nullable = false)
    private Boolean activa;

    @Column(name = "fecha_ini_sesion")
    private LocalDateTime fechaIniSesion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
}
