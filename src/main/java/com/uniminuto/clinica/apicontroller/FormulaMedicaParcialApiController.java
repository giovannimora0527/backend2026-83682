package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.FormulaResponse;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Conserva la ruta del parcial {@code /formula-medica/listar-ordenado}.
 * El módulo completo de fórmulas está en {@link FormulaMedicaApiController}.
 */
@RestController
@RequestMapping("/formula-medica")
public class FormulaMedicaParcialApiController {

    private final FormulaMedicaService service;

    public FormulaMedicaParcialApiController(FormulaMedicaService service) {
        this.service = service;
    }

    /**
     * Lista las fórmulas ordenadas por fecha de registro.
     *
     * @param asc {@code true} ascendente, {@code false} descendente
     * @return fórmulas ordenadas
     */
    @GetMapping(value = "/listar-ordenado", produces = "application/json")
    public ResponseEntity<List<FormulaResponse>> listarOrdenado(@RequestParam boolean asc) {
        return ResponseEntity.ok(service.listarOrdenadas(asc));
    }
}
