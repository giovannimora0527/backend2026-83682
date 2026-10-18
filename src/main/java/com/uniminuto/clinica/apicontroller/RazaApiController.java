
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR DE GESTIÓN DE RAZAS
// ==========================================================

// Interfaz que define los endpoints.
import com.uniminuto.clinica.api.RazaApi;

// Entidad que representa la tabla raza.
import com.uniminuto.clinica.entity.Raza;

// Servicio encargado de consultar MySQL.
import com.uniminuto.clinica.service.RazaService;

// Permite devolver listas de registros.
import java.util.List;

// Permite devolver respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Identifica la clase como controlador REST.
import org.springframework.web.bind.annotation.RestController;

/**
 * ==========================================================
 * CONTROLADOR REST DE RAZAS
 * ==========================================================
 *
 * Este controlador recibe solicitudes HTTP
 * y utiliza RazaService para consultar MySQL.
 *
 * Endpoint:
 *
 * GET /clinica/v1/raza/listar
 *
 * La ruta está definida en RazaApi.java.
 */
@RestController
public class RazaApiController implements RazaApi {

    // ======================================================
    // 1. SERVICIO DE RAZAS
    // ======================================================

    /**
     * Servicio que contiene la lógica
     * para consultar las razas registradas.
     */
    private final RazaService razaService;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot proporciona automáticamente
     * una instancia de RazaService.
     *
     * @param razaService Servicio de razas.
     */
    public RazaApiController(RazaService razaService) {

        this.razaService = razaService;

    }

    // ======================================================
    // 3. MÉTODO: LISTAR RAZAS
    // ======================================================

    /**
     * Atiende las solicitudes:
     *
     * GET /clinica/v1/raza/listar
     *
     * Funcionamiento:
     *
     * 1. Recibe una solicitud GET.
     * 2. Llama al servicio listarRazas().
     * 3. El servicio consulta RazaRepository.
     * 4. El repositorio obtiene los datos de MySQL.
     * 5. Devuelve la lista en formato JSON.
     *
     * @return HTTP 200 con las razas registradas.
     */
    @Override
    public ResponseEntity<List<Raza>> listarRazas() {

        // Consultar las razas mediante el servicio.
        List<Raza> razas = razaService.listarRazas();

        // Devolver HTTP 200 con los resultados.
        return ResponseEntity.ok(razas);

    }

}
