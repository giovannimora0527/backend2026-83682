package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {

    private Integer clienteId;
    private Integer mascotaId;
    private Integer medicoId;
    private LocalDateTime fechaHora;
    private String estado;
    private String motivo;
}