
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API DE GESTIÓN DE MEDICAMENTOS
// ==========================================================

// Entidad que representa los medicamentos de MySQL.
import com.uniminuto.clinica.entity.Medicamento;

// Permite devolver listas de medicamentos.
import java.util.List;

// Permite construir respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Permite definir peticiones HTTP GET.
import org.springframework.web.bind.annotation.GetMapping;

// Permite definir la ruta principal del módulo.
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ==========================================================
 * INTERFAZ DE LA API DE MEDICAMENTOS
 * ==========================================================
 *
 * Define las operaciones HTTP disponibles
 * para consultar los medicamentos registrados.
 *
 * Base de datos: clinica
 * Tabla: medicamento
 *
 * Endpoint:
 *
 * GET /clinica/v1/medicamento/listar
 *
 * El controlador implementará esta interfaz
 * y utilizará MedicamentoService.
 */

// ==========================================================
// 1. RUTA PRINCIPAL
// ==========================================================

/**
 * Todas las rutas de esta interfaz
 * comenzarán con /medicamento.
 */
@RequestMapping("/medicamento")
public interface MedicamentoApi {

    // ======================================================
    // 2. ENDPOINT PARA LISTAR MEDICAMENTOS
    // ======================================================

    /**
     * Consulta todos los medicamentos registrados.
     *
     * Método HTTP: GET
     *
     * Ruta:
     * /medicamento/listar
     *
     * La respuesta incluirá:
     *
     * - id
     * - nombre
     * - descripcion
     * - presentacion
     * - fechaCompra
     * - fechaVence
     * - fechaCreacionRegistro
     * - fechaModificacionRegistro
     *
     * Los medicamentos estarán ordenados
     * alfabéticamente por nombre.
     *
     * @return HTTP 200 con la lista en JSON.
     */
    @GetMapping(
            value = "/listar",
            produces = "application/json"
    )
    ResponseEntity<List<Medicamento>> listarMedicamentos();

}
