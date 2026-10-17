package com.uniminuto.clinica.exception;
import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
/** Envía mensajes claros sin mostrar errores internos del servidor. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400,"BAD REQUEST",ex.getMessage()));
    }
    @ExceptionHandler(org.apache.coyote.BadRequestException.class)
    public ResponseEntity<ErrorResponse> legacyBadRequest(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400,"BAD REQUEST",ex.getMessage()));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> businessError(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        return ResponseEntity.status(ex.getStatusCode())
            .body(new ErrorResponse(status, "ERROR", ex.getReason()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex) {
        // La causa técnica se consulta en el log de desarrollo, no en Angular.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500,"ERROR", "No fue posible completar la operación."));
    }
}
