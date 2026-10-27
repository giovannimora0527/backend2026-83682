package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class LoginRs {
    private String token;
    private Boolean logueoExitoso;
}
