package com.clinicaveterinaria.clinica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "fecha_ini_sesion")
    private LocalDateTime fechaIniSesion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
}