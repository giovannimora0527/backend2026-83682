
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR DE MÉDICOS VETERINARIOS
// ==========================================================

// API que contiene las rutas de médicos.
import com.uniminuto.clinica.api.MedicoApi;

// Entidad que representa a los médicos.
import com.uniminuto.clinica.entity.Medico;

// Servicio encargado de consultar los médicos.
import com.uniminuto.clinica.service.MedicoService;

// Respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Identifica esta clase como controlador REST.
import org.springframework.web.bind.annotation.RestController;

// Manejo de listas.
import java.util.List;

/**
 * ==========================================================
 * CLASE: MedicoApiController
 * ==========================================================
 *
 * Controlador REST encargado de atender
 * las solicitudes relacionadas con médicos.
 *
 * Implementa los endpoints definidos
 * en la interfaz MedicoApi.
 *
 * El controlador recibe la petición desde Angular,
 * consulta los médicos mediante MedicoService
 * y devuelve los resultados en formato JSON.
 */
@RestController
public class MedicoApiController implements MedicoApi {

    // ======================================================
    // 1. SERVICIO DE MÉDICOS
    // ======================================================

    /**
     * Servicio encargado de ejecutar
     * las operaciones relacionadas con médicos.
     */
    private final MedicoService medicoService;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * la implementación de MedicoService.
     *
     * @param medicoService Servicio de médicos.
     */
    public MedicoApiController(
            MedicoService medicoService
    ) {
        this.medicoService = medicoService;
    }

    // ======================================================
    // 3. LISTAR MÉDICOS VETERINARIOS
    // ======================================================

    /**
     * Atiende las peticiones del endpoint:
     *
     * GET /clinica/v1/medico/listar
     *
     * Funcionamiento:
     *
     * 1. Recibe la solicitud HTTP GET.
     * 2. Llama al método listarMedicos().
     * 3. El servicio consulta MySQL.
     * 4. Devuelve los médicos ordenados
     *    por apellidos y nombres.
     *
     * @return Respuesta HTTP 200 con la lista
     *         de médicos en formato JSON.
     */
    @Override
    public ResponseEntity<List<Medico>> listarMedicos() {

        // Consultar los médicos mediante el servicio.
        List<Medico> medicos =
                medicoService.listarMedicos();

        // Devolver HTTP 200 con los resultados.
        return ResponseEntity.ok(medicos);
    }

}
