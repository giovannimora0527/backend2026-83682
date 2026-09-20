package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.FormulaMedicaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/** Contrato REST del inventario de formulas medicas (requerimiento 1). */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista el inventario de formulas medicas.
     *
     * @return formulas ordenadas de la mas reciente a la mas antigua.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<FormulaMedicaRs>> listarFormulas();
}
