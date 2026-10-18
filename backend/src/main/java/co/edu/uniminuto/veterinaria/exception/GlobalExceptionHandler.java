package co.edu.uniminuto.veterinaria.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas HTTP consistentes ({@link ApiError}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Registro de eventos del servidor (el detalle técnico queda aquí, no en la respuesta). */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Registro inexistente: HTTP 404.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * Regla de negocio incumplida: HTTP 409 o 422 según la excepción.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return construir(ex.getEstado(), ex.getMessage(), request, null);
    }

    /**
     * Fallo de validación de campos (Bean Validation): HTTP 400 con el detalle por campo.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return construir(HttpStatus.BAD_REQUEST, "Hay campos obligatorios o con valores inválidos.", request, campos);
    }

    /**
     * Cuerpo JSON ilegible o con valores de tipo incorrecto: HTTP 400.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarCuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen un formato válido.", request, null);
    }

    /**
     * Parámetro de URL con tipo incorrecto: HTTP 400.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> manejarTipoParametro(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor inválido.", request, null);
    }

    /**
     * Violación de una restricción de la base de datos (única, llave foránea): HTTP 409.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        LOG.warn("Violación de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT,
                "La operación no se pudo completar porque entra en conflicto con información existente.", request, null);
    }

    /**
     * Método HTTP no permitido en la ruta: HTTP 405.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "Operación no permitida para este recurso.", request, null);
    }

    /**
     * Ruta inexistente: HTTP 404.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> manejarRutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado.", request, null);
    }

    /**
     * Cualquier otro error inesperado: HTTP 500 sin exponer detalles técnicos.
     *
     * @param ex      excepción
     * @param request solicitud actual
     * @return respuesta de error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarInesperado(Exception ex, HttpServletRequest request) {
        LOG.error("Error inesperado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente de nuevo más tarde.", request, null);
    }

    /**
     * Construye la respuesta de error estándar.
     *
     * @param estado  código HTTP
     * @param mensaje mensaje para el usuario
     * @param request solicitud actual
     * @param campos  errores por campo (puede ser nulo)
     * @return respuesta con el cuerpo {@link ApiError}
     */
    private ResponseEntity<ApiError> construir(HttpStatus estado, String mensaje, HttpServletRequest request,
                                               Map<String, String> campos) {
        ApiError cuerpo = new ApiError(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje,
                request.getRequestURI(), campos);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
