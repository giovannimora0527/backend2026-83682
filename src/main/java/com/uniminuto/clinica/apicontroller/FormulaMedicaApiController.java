
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de Fórmulas Médicas.
 *
 * Recibe las solicitudes HTTP enviadas
 * desde el frontend Angular y utiliza
 * el servicio para consultar o modificar
 * los registros de la base de datos.
 */
@RestController
public class FormulaMedicaApiController
        implements FormulaMedicaApi {

    // ==========================================
    // 1. INYECTAR EL SERVICIO
    // ==========================================

    private final FormulaMedicaService formulaMedicaService;

    /**
     * Constructor utilizado por Spring Boot
     * para proporcionar el servicio.
     */
    public FormulaMedicaApiController(
            FormulaMedicaService formulaMedicaService
    ) {
        this.formulaMedicaService = formulaMedicaService;
    }

    // ==========================================
    // 2. LISTAR TODAS LAS FÓRMULAS MÉDICAS
    // ==========================================

    /**
     * Consulta las fórmulas registradas.
     *
     * GET /formula-medica/listar-todos
     *
     * @return listado de fórmulas médicas.
     */
    @Override
    public ResponseEntity<List<FormulaMedica>>
    listarFormulasMedicas() {

        // Consultar los registros utilizando el servicio.
        List<FormulaMedica> formulas =
                formulaMedicaService.obtenerFormulasMedicas();

        // Devolver respuesta HTTP 200 OK.
        return ResponseEntity.ok(formulas);
    }

    // ==========================================
    // 3. CONSULTAR UNA FÓRMULA POR ID
    // ==========================================

    /**
     * Busca una fórmula médica específica.
     *
     * GET /formula-medica/consultar/{id}
     *
     * @param id identificador de la fórmula.
     * @return fórmula médica encontrada.
     */
    @Override
    public ResponseEntity<FormulaMedica> consultarFormula(
            Long id
    ) {

        // Buscar la fórmula por su identificador.
        FormulaMedica formula =
                formulaMedicaService.obtenerFormulaPorId(id);

        // Devolver la fórmula encontrada.
        return ResponseEntity.ok(formula);
    }

    // ==========================================
    // 4. CREAR UNA NUEVA FÓRMULA MÉDICA
    // ==========================================

    /**
     * Registra una nueva fórmula médica.
     *
     * POST /formula-medica/crear
     *
     * @param formula información recibida desde Angular.
     * @return fórmula médica registrada.
     */
    @Override
    public ResponseEntity<FormulaMedica> crearFormula(
            FormulaMedica formula
    ) {

        // Enviar los datos al servicio.
        // Allí se realizan las validaciones.
        FormulaMedica nuevaFormula =
                formulaMedicaService.crearFormula(formula);

        // Devolver HTTP 201 CREATED.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nuevaFormula);
    }

    // ==========================================
    // 5. ACTUALIZAR UNA FÓRMULA MÉDICA
    // ==========================================

    /**
     * Modifica una fórmula médica existente.
     *
     * PUT /formula-medica/actualizar/{id}
     *
     * @param id identificador de la fórmula.
     * @param formula nuevos datos.
     * @return fórmula actualizada.
     */
    @Override
    public ResponseEntity<FormulaMedica> actualizarFormula(
            Long id,
            FormulaMedica formula
    ) {

        // Buscar y actualizar el registro
        // mediante el servicio.
        FormulaMedica formulaActualizada =
                formulaMedicaService.actualizarFormula(
                        id,
                        formula
                );

        // Devolver HTTP 200 OK.
        return ResponseEntity.ok(formulaActualizada);
    }
}
