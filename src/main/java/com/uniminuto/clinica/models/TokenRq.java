package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class TokenRq {
    private Long usuarioId;
    private String token;
    private String username;
}
