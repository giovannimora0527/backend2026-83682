package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class AnotacionHistoriaRq {
    private Integer mascotaId;
    private Integer historiaId;
    private Long medicoId;
    private String descripcion;
}
