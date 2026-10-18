
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR DE GESTIÓN DE ESPECIALIZACIONES
// ==========================================================

// Interfaz que define los endpoints del módulo.
import com.uniminuto.clinica.api.EspecializacionApi;

// Entidad relacionada con la tabla especializacion.
import com.uniminuto.clinica.entity.Especializacion;

// Servicio que contiene la lógica de consulta.
import com.uniminuto.clinica.service.EspecializacionService;

// Permite trabajar con listas de registros.
import java.util.List;

// Permite devolver respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Identifica la clase como controlador REST.
import org.springframework.web.bind.annotation.RestController;

/**
 * ==========================================================
 * CONTROLADOR REST DE ESPECIALIZACIONES
 * ==========================================================
 *
 * Este controlador conecta la API con el servicio
 * encargado de consultar las especializaciones.
 *
 * Base de datos: clinica
 * Tabla: especializacion
 *
 * Endpoint:
 *
 * GET /clinica/v1/especializacion/listar
 *
 * Funcionamiento:
 *
 * 1. Angular realiza una petición HTTP GET.
 * 2. Spring Boot recibe la petición.
 * 3. El controlador llama al servicio.
 * 4. El servicio consulta el repositorio.
 * 5. El repositorio obtiene los datos de MySQL.
 * 6. El controlador devuelve la lista en JSON.
 */
@RestController
public class EspecializacionApiController
        implements EspecializacionApi {

    // ======================================================
    // 1. SERVICIO DE ESPECIALIZACIONES
    // ======================================================

    /**
     * Servicio encargado de obtener los registros
     * almacenados en la base de datos.
     */
    private final EspecializacionService especializacionService;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * el servicio de especializaciones.
     *
     * @param especializacionService
     * Servicio que permite consultar MySQL.
     */
    public EspecializacionApiController(
            EspecializacionService especializacionService) {

        this.especializacionService = especializacionService;

    }

    // ======================================================
    // 3. MÉTODO: LISTAR ESPECIALIZACIONES
    // ======================================================

    /**
     * Atiende las solicitudes HTTP GET.
     *
     * Endpoint:
     *
     * GET /clinica/v1/especializacion/listar
     *
     * Devuelve las especializaciones ordenadas
     * alfabéticamente por nombre.
     *
     * Datos devueltos:
     *
     * - id
     * - nombre
     * - descripcion
     * - codigoEspecializacion
     *
     * @return HTTP 200 con la lista en formato JSON.
     */
    @Override
    public ResponseEntity<List<Especializacion>>
    listarEspecializaciones() {

        // Consultar las especializaciones
        // utilizando el servicio de Spring Boot.
        List<Especializacion> especializaciones =
                especializacionService.listarEspecializaciones();

        // Devolver HTTP 200 con los registros.
        return ResponseEntity.ok(especializaciones);

    }

}
