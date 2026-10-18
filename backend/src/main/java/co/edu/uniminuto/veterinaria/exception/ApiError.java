package co.edu.uniminuto.veterinaria.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cuerpo estándar de todas las respuestas de error de la API.
 * No expone información técnica (trazas, nombres de clases, SQL).
 *
 * @param fecha        momento del error
 * @param estado       código HTTP numérico
 * @param error        descripción corta del código HTTP
 * @param mensaje      mensaje comprensible para el usuario
 * @param ruta         ruta solicitada
 * @param camposInvalidos errores por campo (solo en validaciones), clave = nombre del campo
 */
public record ApiError(LocalDateTime fecha,
                       int estado,
                       String error,
                       String mensaje,
                       String ruta,
                       Map<String, String> camposInvalidos) {
}
