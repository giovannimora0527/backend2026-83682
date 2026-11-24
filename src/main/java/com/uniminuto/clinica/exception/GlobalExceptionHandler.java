package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return responder(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.Detalle> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.Detalle(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(ErrorResponse.Detalle::campo)
                        .thenComparing(ErrorResponse.Detalle::mensaje))
                .toList();
        String mensaje = detalles.stream()
                .map(detalle -> detalle.campo() + ": " + detalle.mensaje())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST, mensaje, detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST,
                "El body debe ser un JSON valido con las propiedades y los formatos admitidos por el endpoint.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El parametro " + ex.getName() + " no es valido.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El parametro " + ex.getParameterName() + " es obligatorio.");
    }

    @ExceptionHandler(CorreoException.class)
    public ResponseEntity<ErrorResponse> handleCorreo(CorreoException ex) {
        LOGGER.error("Fallo el envio de un correo", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión o su sesión ya no es válida.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Errores propios de Spring MVC: ruta inexistente (404), metodo (405), tipo de contenido (415)...
        if (ex instanceof org.springframework.web.ErrorResponse errorSpring
                && errorSpring.getStatusCode().is4xxClientError()) {
            return responder(errorSpring.getStatusCode(),
                    errorSpring.getStatusCode().value() == 404
                            ? "El recurso solicitado no existe." : "La solicitud no es válida para este recurso.");
        }
        LOGGER.error("Error interno al procesar la solicitud", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno al procesar la solicitud.");
    }

    private static ResponseEntity<ErrorResponse> responder(HttpStatusCode status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, mensaje));
    }
}
