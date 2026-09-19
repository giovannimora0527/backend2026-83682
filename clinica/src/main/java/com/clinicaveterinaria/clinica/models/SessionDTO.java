package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {

    private Integer userId;
    private String token;
    private LocalDateTime fechaIniSesion;
    private LocalDateTime fechaExpiracion;
}