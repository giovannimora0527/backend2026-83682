
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR REST DE HISTORIAS MÉDICAS
// ==========================================================

// Interfaz que contiene las rutas de la API.
import com.uniminuto.clinica.api.HistoriaMedicaApi;

// Entidad que representa la tabla historia_medica.
import com.uniminuto.clinica.entity.HistoriaMedica;

// Servicio que consulta las historias médicas.
import com.uniminuto.clinica.service.HistoriaMedicaService;

// Respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Anotación para controladores REST.
import org.springframework.web.bind.annotation.RestController;

// Manejo de listas.
import java.util.List;

// ==========================================================
// 1. CONTROLADOR REST
// ==========================================================

/**
 * Implementa los endpoints definidos en
 * HistoriaMedicaApi.
 *
 * Permite consultar:
 *
 * 1. Todas las historias médicas.
 * 2. Historias médicas por paciente.
 *
 * No modifica otros módulos del sistema.
 */

@RestController
public class HistoriaMedicaApiController
        implements HistoriaMedicaApi {

    // ======================================================
    // 2. SERVICIO DE HISTORIAS MÉDICAS
    // ======================================================

    private final HistoriaMedicaService
            historiaMedicaService;

    // ======================================================
    // 3. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * el servicio mediante el constructor.
     */

    public HistoriaMedicaApiController(
            HistoriaMedicaService historiaMedicaService
    ) {

        this.historiaMedicaService =
                historiaMedicaService;

    }

    // ======================================================
    // 4. LISTAR TODAS LAS HISTORIAS MÉDICAS
    // ======================================================

    /**
     * GET /historia-medica/listar-todos
     *
     * Consulta todos los registros
     * de la tabla historia_medica.
     */

    @Override
    public ResponseEntity<List<HistoriaMedica>>
    listarHistoriasMedicas() {

        // Consultar las historias mediante el servicio.
        List<HistoriaMedica> historias =
                historiaMedicaService
                        .obtenerHistoriasMedicas();

        // Devolver respuesta HTTP 200 OK.
        return ResponseEntity.ok(historias);

    }

    // ======================================================
    // 5. CONSULTAR HISTORIAS POR PACIENTE
    // ======================================================

    /**
     * GET /historia-medica/listar-por-paciente/{pacienteId}
     *
     * Consulta las historias relacionadas
     * con un paciente específico.
     */

    @Override
    public ResponseEntity<List<HistoriaMedica>>
    listarHistoriasPorPaciente(
            Integer pacienteId
    ) {

        // Consultar las historias del paciente.
        List<HistoriaMedica> historias =
                historiaMedicaService
                        .obtenerHistoriasPorPaciente(
                                pacienteId
                        );

        // Devolver respuesta HTTP 200 OK.
        return ResponseEntity.ok(historias);

    }

}
