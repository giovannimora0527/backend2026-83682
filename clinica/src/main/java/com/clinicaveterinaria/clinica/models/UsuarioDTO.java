package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private String username;
    private String passwordHash;
    private String rol;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private String email;
}