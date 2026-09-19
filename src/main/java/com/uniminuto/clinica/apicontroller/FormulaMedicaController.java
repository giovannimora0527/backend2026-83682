package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.model.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestionar las fórmulas médicas.
 * Expone endpoints para operaciones relacionadas con fórmulas médicas.
 *
 * @author TuNombre
 * @version 1.0
 */
@RestController
@RequestMapping("/api/formulas-medicas")
public class FormulaMedicaController {

    /**
     * Servicio para gestionar fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    /**
     * Obtiene todas las fórmulas médicas ordenadas por fecha de creación
     * de la más reciente a la más antigua.
     *
     * @return ResponseEntity con la lista de fórmulas médicas y el estado HTTP.
     */
    @GetMapping
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas() {
        List<FormulaMedica> formulas = formulaMedicaService.listarTodasOrdenadasPorFecha();
        return ResponseEntity.ok(formulas);
    }
}