
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API REST DE HISTORIAS MÉDICAS
// ==========================================================

// Entidad HistoriaMedica.
import com.uniminuto.clinica.entity.HistoriaMedica;

// Manejo de respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Anotaciones de Spring Web.
import org.springframework.web.bind.annotation.*;

// Permite devolver una lista de registros.
import java.util.List;

// ==========================================================
// INTERFAZ DE LA API
// ==========================================================

/**
 * Define los endpoints para consultar
 * las historias médicas de los pacientes.
 *
 * Ruta principal:
 *
 * /historia-medica
 */

@CrossOrigin(
        origins = "*",
        allowedHeaders = "*"
)
@RequestMapping("/historia-medica")
public interface HistoriaMedicaApi {

    // ======================================================
    // 1. LISTAR TODAS LAS HISTORIAS MÉDICAS
    // ======================================================

    /**
     * Consulta todas las historias médicas
     * registradas en MySQL.
     *
     * Método HTTP: GET
     *
     * Ruta: /historia-medica/listar-todos
     */

    @GetMapping(
            value = "/listar-todos",
            produces = "application/json"
    )
    ResponseEntity<List<HistoriaMedica>>
    listarHistoriasMedicas();

    // ======================================================
    // 2. BUSCAR HISTORIAS POR PACIENTE
    // ======================================================

    /**
     * Consulta las historias médicas
     * relacionadas con un paciente.
     *
     * Método HTTP: GET
     *
     * Ruta:
     * /historia-medica/listar-por-paciente/{pacienteId}
     */

    @GetMapping(
            value = "/listar-por-paciente/{pacienteId}",
            produces = "application/json"
    )
    ResponseEntity<List<HistoriaMedica>>
    listarHistoriasPorPaciente(
            @PathVariable("pacienteId") Integer pacienteId
    );

}
