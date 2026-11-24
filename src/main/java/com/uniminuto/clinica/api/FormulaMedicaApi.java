package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaRs>> listarFormulas();

    /** Retirado: responde 410. */
    @PostMapping(value = "/guardar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormula();

    /** Retirado: responde 410. */
    @PostMapping(value = "/actualizar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormula();
}
