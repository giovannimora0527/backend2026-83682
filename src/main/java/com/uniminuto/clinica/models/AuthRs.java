package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class AuthRs {
    private String username;
    private Long usuarioId;
    private Boolean login;
}
