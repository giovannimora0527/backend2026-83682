
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API DE GESTIÓN DE MÉDICOS VETERINARIOS
// ==========================================================

// Entidad que representa a los médicos.
import com.uniminuto.clinica.entity.Medico;

// Permite devolver respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Anotaciones para definir las rutas de la API.
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Permite devolver listas de médicos.
import java.util.List;

/**
 * ==========================================================
 * INTERFAZ: MedicoApi
 * ==========================================================
 *
 * Define los servicios web relacionados
 * con la gestión de médicos veterinarios.
 *
 * El objetivo es permitir que Angular
 * consulte los médicos registrados en MySQL.
 *
 * Ruta principal:
 *
 * /clinica/v1/medico
 *
 * La ruta /clinica/v1 corresponde al
 * contexto configurado en Spring Boot.
 */
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*"
)
@RequestMapping("/medico")
public interface MedicoApi {

    // ======================================================
    // 1. LISTAR MÉDICOS VETERINARIOS
    // ======================================================

    /**
     * Consulta todos los médicos registrados
     * en la base de datos clinica.
     *
     * Los médicos se devolverán ordenados
     * por apellidos y nombres.
     *
     * Método HTTP:
     * GET
     *
     * Endpoint completo:
     * GET /clinica/v1/medico/listar
     *
     * Ejemplo de información devuelta:
     *
     * [
     *   {
     *     "id": 1,
     *     "nombres": "Nombre",
     *     "apellidos": "Apellido"
     *   }
     * ]
     *
     * El ejemplo anterior es ilustrativo.
     * Los datos reales vendrán de MySQL.
     *
     * @return Respuesta HTTP con la lista de médicos.
     */
    @GetMapping(
            value = "/listar",
            produces = "application/json"
    )
    ResponseEntity<List<Medico>> listarMedicos();

}
