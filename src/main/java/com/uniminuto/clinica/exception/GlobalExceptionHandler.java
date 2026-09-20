package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Manejador centralizado de errores: evita try/catch repetidos en cada controlador. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Datos invalidos -> HTTP 400. */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return construirRespuesta(ex.getStatus(), "BAD REQUEST", ex.getMessage());
    }

    /** Recurso inexistente -> HTTP 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return construirRespuesta(ex.getStatus(), "NOT FOUND", ex.getMessage());
    }

    /** Falta un parametro obligatorio en la URL -> HTTP 400. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametroFaltante(
            MissingServletRequestParameterException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "BAD REQUEST",
                "Falta el parametro obligatorio: " + ex.getParameterName());
    }

    /** Parametro con formato invalido, tipicamente una fecha mal escrita -> HTTP 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoInvalido(
            MethodArgumentTypeMismatchException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "BAD REQUEST",
                "El parametro '" + ex.getName() + "' es invalido. "
                        + "Las fechas se envian como yyyy-MM-ddTHH:mm:ss");
    }

    /** Cualquier otro error no contemplado -> HTTP 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL SERVER ERROR", ex.getMessage());
    }

    /** Arma el {@link ErrorResponse} y lo envuelve en el status recibido. */
    private ResponseEntity<ErrorResponse> construirRespuesta(
            HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), error, message));
    }
}
