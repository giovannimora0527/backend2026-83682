
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API PARA GESTIÓN DE RAZAS
// ==========================================================

// Entidad que representa los registros de MySQL.
import com.uniminuto.clinica.entity.Raza;

// Permite devolver listas de registros.
import java.util.List;

// Permite construir respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Anotaciones que definen las rutas de la API.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ==========================================================
 * INTERFAZ DE LA API DE RAZAS
 * ==========================================================
 *
 * Define los endpoints disponibles para
 * el módulo de Gestión de Razas.
 *
 * Dirección que utilizaremos:
 *
 * GET /clinica/v1/raza/listar
 *
 * La implementación del endpoint se realizará
 * en RazaApiController.java.
 */

// Ruta principal del recurso.
@RequestMapping("/raza")
public interface RazaApi {

    // ======================================================
    // MÉTODO 1: LISTAR TODAS LAS RAZAS
    // ======================================================

    /**
     * Recupera las razas registradas en MySQL.
     *
     * Ejemplo:
     *
     * GET http://localhost:8080/clinica/v1/raza/listar
     *
     * Respuesta esperada:
     *
     * [
     *   {
     *     "razaId": 1,
     *     "nombre": "Gato",
     *     "especie": "Felino"
     *   },
     *   {
     *     "razaId": 2,
     *     "nombre": "Perro",
     *     "especie": "Canino"
     *   }
     * ]
     *
     * @return Respuesta HTTP 200 con la lista de razas.
     */
    @GetMapping(
            value = "/listar",
            produces = "application/json"
    )
    ResponseEntity<List<Raza>> listarRazas();

}
