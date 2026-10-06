package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaRq {

    private Long id;
    private Integer mascotaId;
    private Long medicoId;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
}
