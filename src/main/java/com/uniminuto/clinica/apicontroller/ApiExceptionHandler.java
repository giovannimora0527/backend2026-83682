package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.config.ApiError;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> estado(ResponseStatusException error) {
        int status = error.getStatusCode().value();
        var respuesta = ResponseEntity.status(status);
        if (status == 401) {
            respuesta.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        return respuesta.body(ApiError.de(status, error.getReason()));
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiError> solicitudIncorrecta(BadRequestException error) {
        return ResponseEntity.badRequest().body(ApiError.de(400, error.getMessage()));
    }
}
