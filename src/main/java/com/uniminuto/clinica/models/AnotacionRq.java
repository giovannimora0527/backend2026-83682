package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class AnotacionRq {

    private Long mascotaId;

    private Long medicoId;

    private String descripcion;
}
