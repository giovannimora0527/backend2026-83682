
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR REST DE MEDICAMENTOS
// ==========================================================

// Interfaz que define los endpoints HTTP.
import com.uniminuto.clinica.api.MedicamentoApi;

// Entidad que representa la tabla medicamento.
import com.uniminuto.clinica.entity.Medicamento;

// Servicio que consulta los medicamentos.
import com.uniminuto.clinica.service.MedicamentoService;

// Permite devolver listas de medicamentos.
import java.util.List;

// Permite enviar respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Identifica esta clase como controlador REST.
import org.springframework.web.bind.annotation.RestController;

/**
 * ==========================================================
 * CONTROLADOR DE GESTIÓN DE MEDICAMENTOS
 * ==========================================================
 *
 * Conecta las solicitudes HTTP con el servicio
 * encargado de consultar MySQL.
 *
 * Base de datos: clinica
 * Tabla: medicamento
 *
 * Endpoint:
 *
 * GET /clinica/v1/medicamento/listar
 *
 * Funcionamiento:
 *
 * 1. Angular realiza una solicitud HTTP GET.
 * 2. Spring Boot recibe la solicitud.
 * 3. El controlador llama al servicio.
 * 4. El servicio utiliza el repositorio.
 * 5. El repositorio consulta MySQL.
 * 6. Se devuelve una lista en formato JSON.
 */

// ==========================================================
// 1. DEFINICIÓN DEL CONTROLADOR
// ==========================================================

@RestController
public class MedicamentoApiController
        implements MedicamentoApi {

    // ======================================================
    // 2. SERVICIO DE MEDICAMENTOS
    // ======================================================

    /**
     * Servicio encargado de consultar los
     * medicamentos registrados en MySQL.
     */
    private final MedicamentoService medicamentoService;

    // ======================================================
    // 3. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * el servicio de medicamentos.
     *
     * @param medicamentoService
     * Servicio de consulta de medicamentos.
     */
    public MedicamentoApiController(
            MedicamentoService medicamentoService) {

        this.medicamentoService = medicamentoService;

    }

    // ======================================================
    // 4. MÉTODO PARA LISTAR MEDICAMENTOS
    // ======================================================

    /**
     * Atiende la solicitud HTTP GET.
     *
     * Ruta:
     * /medicamento/listar
     *
     * Consulta todos los medicamentos
     * ordenados alfabéticamente por nombre.
     *
     * @return HTTP 200 con los datos en JSON.
     */
    @Override
    public ResponseEntity<List<Medicamento>>
    listarMedicamentos() {

        // Consultar la lista utilizando el servicio.
        List<Medicamento> medicamentos =
                medicamentoService.listarMedicamentos();

        // Devolver los medicamentos con HTTP 200.
        return ResponseEntity.ok(medicamentos);

    }

}
