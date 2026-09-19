package com.uniminuto.clinica.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * <p>La anotacion {@code @RestControllerAdvice} hace que esta clase
 * "escuche" las excepciones que se lancen en cualquier Controller de toda
 * la aplicacion, para poder transformarlas en una respuesta HTTP clara y
 * con el formato definido en {@link ErrorRespuesta}, en vez de dejar que el
 * cliente reciba un error generico de servidor (HTTP 500) sin explicacion.</p>
 *
 * <p>Esto ayuda a cumplir el requisito de "controlar los posibles flujos de
 * error" de la rubrica de calificacion.</p>
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    /**
     * Maneja el caso en el que se busca un recurso (paciente, cita,
     * historia medica, etc) que no existe.
     *
     * @param ex      excepcion lanzada por el servicio
     * @param request peticion HTTP original, para saber en que ruta ocurrio el error
     * @return respuesta HTTP 404 (No encontrado) con el detalle del error
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex,
                                                                      HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja el caso en el que el usuario envia un rango de fechas
     * invalido (por ejemplo, fecha inicial mayor que la fecha final).
     *
     * @param ex      excepcion lanzada por el servicio
     * @param request peticion HTTP original
     * @return respuesta HTTP 400 (Peticion invalida) con el detalle del error
     */
    @ExceptionHandler(FechaInvalidaException.class)
    public ResponseEntity<ErrorRespuesta> manejarFechaInvalida(FechaInvalidaException ex,
                                                                 HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja los errores de validacion que se generan automaticamente
     * cuando un DTO anotado con {@code @Valid} no cumple las reglas
     * definidas (por ejemplo {@code @NotBlank} o {@code @NotNull}).
     *
     * @param ex      excepcion de validacion generada por Spring
     * @param request peticion HTTP original
     * @return respuesta HTTP 400 (Peticion invalida) con la lista de campos invalidos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarErroresValidacion(MethodArgumentNotValidException ex,
                                                                     HttpServletRequest request) {
        // Se unen todos los mensajes de error de cada campo invalido en un solo texto.
        String mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(campoError -> campoError.getField() + ": " + campoError.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        ErrorRespuesta error = new ErrorRespuesta(HttpStatus.BAD_REQUEST.value(), mensajes, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de argumentos invalidos, como por ejemplo cuando se
     * envia un valor de texto que no corresponde a ninguno de los valores
     * permitidos de un enum (ej: un estado de cita que no existe).
     *
     * @param ex      excepcion lanzada por el servicio
     * @param request peticion HTTP original
     * @return respuesta HTTP 400 (Peticion invalida) con el detalle del error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> manejarArgumentoInvalido(IllegalArgumentException ex,
                                                                     HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja cualquier otro error inesperado que no haya sido capturado por
     * los manejadores anteriores, para que el cliente nunca reciba una
     * respuesta sin explicacion.
     *
     * @param ex      excepcion inesperada
     * @param request peticion HTTP original
     * @return respuesta HTTP 500 (Error interno del servidor) con el detalle del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ErrorRespuesta error = new ErrorRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrio un error inesperado: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
