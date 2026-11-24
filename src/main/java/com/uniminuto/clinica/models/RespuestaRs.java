package com.uniminuto.clinica.models;

/** Respuesta de exito de los endpoints nuevos (C1). */
public record RespuestaRs<T>(int status, String message, T data) {

    public static <T> RespuestaRs<T> ok(String message, T data) {
        return new RespuestaRs<>(200, message, data);
    }
}
