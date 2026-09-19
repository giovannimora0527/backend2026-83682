package com.uniminuto.clinica.api;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.apache.coyote.BadRequestException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    @GetMapping(value = "/listar-formulas-medicas",
            produces = {"application/json"} )

    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;
}
