package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaRq {

    private Long citaId;
    private Long clienteId;
    private Integer mascotaId;
    private Long medicoId;
    private LocalDateTime fechaHora;
    private String estado;
    private String motivo;
}