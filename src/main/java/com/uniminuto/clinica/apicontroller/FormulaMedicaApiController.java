package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaService formulaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulas() {
        return ResponseEntity.ok(this.formulaService.findAllFormulas());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarFormula(FormulaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaService.guardarFormula(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarFormula(FormulaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaService.actualizarFormula(formulaRq));
    }
}
