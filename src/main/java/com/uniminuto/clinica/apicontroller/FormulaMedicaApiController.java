
package com.uniminuto.clinica.apicontroller;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// CONTROLADOR DE FÓRMULAS MÉDICAS
// ==========================================================

// Interfaz que define los endpoints.
import com.uniminuto.clinica.api.FormulaMedicaApi;

// Entidad que representa los registros de MySQL.
import com.uniminuto.clinica.entity.FormulaMedica;

// Servicio que consulta las fórmulas médicas.
import com.uniminuto.clinica.service.FormulaMedicaService;

// Manejo de listas.
import java.util.List;

// Permite devolver respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Identifica esta clase como controlador REST.
import org.springframework.web.bind.annotation.RestController;


// ==========================================================
// 1. CONFIGURACIÓN DEL CONTROLADOR
// ==========================================================

/**
 * Controlador encargado de recibir las solicitudes
 * relacionadas con las fórmulas médicas.
 *
 * Implementa las rutas definidas en
 * FormulaMedicaApi.java.
 */
@RestController
public class FormulaMedicaApiController
        implements FormulaMedicaApi {

    // ======================================================
    // 2. SERVICIO DE FÓRMULAS MÉDICAS
    // ======================================================

    private final FormulaMedicaService formulaMedicaService;

    /**
     * Inyecta el servicio mediante el constructor.
     */
    public FormulaMedicaApiController(
            FormulaMedicaService formulaMedicaService
    ) {
        this.formulaMedicaService = formulaMedicaService;
    }

    // ======================================================
    // 3. CONSULTAR TODAS LAS FÓRMULAS MÉDICAS
    // ======================================================

    /**
     * Atiende la solicitud GET:
     *
     * /formula-medica/listar-todos
     *
     * Funcionamiento:
     *
     * 1. Recibe la solicitud HTTP.
     * 2. Llama a obtenerFormulasMedicas().
     * 3. El servicio consulta MySQL.
     * 4. Devuelve las fórmulas en formato JSON.
     *
     * @return HTTP 200 con la lista de fórmulas.
     */
    @Override
    public ResponseEntity<List<FormulaMedica>>
    listarFormulasMedicas() {

        // Consultar las fórmulas mediante el servicio.
        List<FormulaMedica> formulas =
                formulaMedicaService.obtenerFormulasMedicas();

        // Devolver el resultado al cliente Angular.
        return ResponseEntity.ok(formulas);
    }

}
