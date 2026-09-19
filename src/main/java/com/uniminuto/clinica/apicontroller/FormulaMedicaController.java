package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.dto.FormulaMedicaDTO;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de FormulaMedica.
 *
 * <p>Expone el servicio pedido en el Requerimiento 1 del taller: listar
 * las formulas medicas del inventario ordenadas de la mas reciente a la
 * mas antigua.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/formulas")
public class FormulaMedicaController implements FormulaMedicaApi {

    /** Servicio con la logica de negocio de FormulaMedica. */
    private final FormulaMedicaService formulaMedicaService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param formulaMedicaService servicio de formulas medicas
     */
    public FormulaMedicaController(FormulaMedicaService formulaMedicaService) {
        this.formulaMedicaService = formulaMedicaService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code GET /api/formulas}.</p>
     */
    @Override
    @GetMapping
    public ResponseEntity<List<FormulaMedicaDTO>> listarFormulasMedicas() {
        return ResponseEntity.ok(formulaMedicaService.listarFormulasMedicas());
    }
}
