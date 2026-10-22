
package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * API de fórmulas médicas.
 *
 * Define las rutas para consultar, crear
 * y actualizar fórmulas médicas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    // ==========================================
    // 1. LISTAR TODAS LAS FÓRMULAS
    // ==========================================

    /**
     * Devuelve las fórmulas registradas.
     *
     * GET /formula-medica/listar-todos
     */
    @GetMapping(
            value = "/listar-todos",
            produces = "application/json"
    )
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas();


    // ==========================================
    // 2. CONSULTAR UNA FÓRMULA POR ID
    // ==========================================

    /**
     * Consulta los detalles de una fórmula.
     *
     * GET /formula-medica/consultar/1
     */
    @GetMapping(
            value = "/consultar/{id}",
            produces = "application/json"
    )
    ResponseEntity<FormulaMedica> consultarFormula(
            @PathVariable("id") Long id
    );


    // ==========================================
    // 3. CREAR UNA FÓRMULA MÉDICA
    // ==========================================

    /**
     * Registra una nueva fórmula.
     *
     * POST /formula-medica/crear
     */
    @PostMapping(
            value = "/crear",
            consumes = "application/json",
            produces = "application/json"
    )
    ResponseEntity<FormulaMedica> crearFormula(
            @RequestBody FormulaMedica formula
    );


    // ==========================================
    // 4. ACTUALIZAR UNA FÓRMULA MÉDICA
    // ==========================================

    /**
     * Actualiza una fórmula existente.
     *
     * PUT /formula-medica/actualizar/1
     */
    @PutMapping(
            value = "/actualizar/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    ResponseEntity<FormulaMedica> actualizarFormula(
            @PathVariable("id") Long id,
            @RequestBody FormulaMedica formula
    );
}
