
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API DE GESTIÓN DE ESPECIALIZACIONES
// ==========================================================

// Entidad que representa la tabla especializacion.
import com.uniminuto.clinica.entity.Especializacion;

// Permite devolver listas de registros.
import java.util.List;

// Permite construir respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Permiten definir las rutas de la API.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ==========================================================
 * INTERFAZ DE LA API DE ESPECIALIZACIONES
 * ==========================================================
 *
 * Define los endpoints utilizados para consultar
 * las especializaciones médicas registradas.
 *
 * Base de datos: clinica
 * Tabla: especializacion
 *
 * Endpoint:
 *
 * GET /clinica/v1/especializacion/listar
 *
 * La implementación de este método se realizará
 * en EspecializacionApiController.java.
 */

// Ruta principal del módulo.
@RequestMapping("/especializacion")
public interface EspecializacionApi {

    // ======================================================
    // MÉTODO 1: LISTAR ESPECIALIZACIONES
    // ======================================================

    /**
     * Devuelve todas las especializaciones
     * registradas en MySQL.
     *
     * Las especializaciones se consultarán
     * ordenadas alfabéticamente por nombre.
     *
     * Ejemplo de respuesta JSON:
     *
     * [
     *   {
     *     "id": 1,
     *     "nombre": "Cardiología 2",
     *     "descripcion": "Especialización médica",
     *     "codigoEspecializacion": "COD_100"
     *   },
     *   {
     *     "id": 2,
     *     "nombre": "Neurología",
     *     "descripcion": "Trastornos del sistema nervioso",
     *     "codigoEspecializacion": "COD_02"
     *   }
     * ]
     *
     * @return HTTP 200 con la lista de especializaciones.
     */
    @GetMapping(
            value = "/listar",
            produces = "application/json"
    )
    ResponseEntity<List<Especializacion>> listarEspecializaciones();

}
