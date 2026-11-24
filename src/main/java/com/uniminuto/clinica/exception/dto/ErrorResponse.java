package com.uniminuto.clinica.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;

import java.util.List;

/** Formato de error del contrato (C1). */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<Detalle> detalles;

    public ErrorResponse(HttpStatusCode status, String message) {
        this(status, message, null);
    }

    public ErrorResponse(HttpStatusCode status, String message, List<Detalle> detalles) {
        HttpStatus conocido = HttpStatus.resolve(status.value());
        this.status = status.value();
        this.error = conocido == null ? "Error" : conocido.getReasonPhrase();
        this.message = message;
        this.detalles = detalles;
    }

    public record Detalle(String campo, String mensaje) {
    }
}
