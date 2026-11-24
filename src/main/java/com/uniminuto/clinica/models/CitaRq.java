package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaRq {

    private Long id;
    private Integer mascotaId;
    private Long medicoId;
    @JsonAlias("fechaInicio")
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
}
