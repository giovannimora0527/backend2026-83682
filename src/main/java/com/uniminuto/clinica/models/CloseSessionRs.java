package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class CloseSessionRs {
    private int status = 200;
    private String message;
    private boolean success;
}
