package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones a respuestas JSON homogéneas: {status, error, mensaje}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Registro de errores del servidor. */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Errores de negocio y validación lanzados por la aplicación.
     *
     * @param ex excepción con su código HTTP.
     * @return respuesta con el mensaje.
     */
    @ExceptionHandler(ClinicaException.class)
    public ResponseEntity<ErrorResponse> manejarClinica(ClinicaException ex) {
        return respuesta(ex.getStatus(), ex.getMessage());
    }

    /**
     * Violaciones de restricciones de la base de datos (únicos, llaves foráneas, CHECK) → 409.
     *
     * @param ex excepción de integridad.
     * @return respuesta 409 con un mensaje comprensible.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
        LOG.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        String detalle = ex.getMostSpecificCause().getMessage();
        String mensaje = detalle != null && detalle.contains("Duplicate")
                ? "Ya existe un registro con los mismos datos únicos."
                : "La operación viola una restricción de integridad de la base de datos.";
        return respuesta(HttpStatus.CONFLICT, mensaje);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, NumberFormatException.class})
    /**
     * Cuerpo o parámetros con formato inválido → 400.
     *
     * @param ex excepción de lectura o conversión.
     * @return respuesta 400.
     */
    public ResponseEntity<ErrorResponse> manejarSolicitud(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud tiene datos con formato inválido.");
    }

    /**
     * Archivo de carga mayor al permitido → 400.
     *
     * @param ex excepción de tamaño.
     * @return respuesta 400.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> manejarTamano(MaxUploadSizeExceededException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El archivo supera el tamaño máximo permitido (3 MB).");
    }

    /**
     * Método HTTP no soportado → 405.
     *
     * @param ex excepción.
     * @return respuesta 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> manejarMetodo(HttpRequestMethodNotSupportedException ex) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no soportado para este recurso.");
    }

    /**
     * Ruta inexistente → 404.
     *
     * @param ex excepción.
     * @return respuesta 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRuta(NoResourceFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "El recurso solicitado no existe.");
    }

    /**
     * Operación sin el permiso requerido por {@code @PreAuthorize} → 403.
     *
     * @param ex excepción de autorización.
     * @return respuesta 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAcceso(AccessDeniedException ex) {
        return respuesta(HttpStatus.FORBIDDEN, "Tu perfil no tiene permiso para esta operación.");
    }

    /**
     * Cualquier otro error → 500 sin exponer detalles técnicos (se registran en el log).
     *
     * @param ex excepción.
     * @return respuesta 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex) {
        LOG.error("Error no controlado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta nuevamente o contacta al administrador.");
    }

    /**
     * @param status código HTTP.
     * @param mensaje mensaje para el usuario.
     * @return respuesta con el cuerpo {status, error, mensaje}.
     */
    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.name(), mensaje));
    }
}
