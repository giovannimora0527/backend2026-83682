package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.exception.GoneException;
import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaService formulaService;

    @Override
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEDICO')")
    public ResponseEntity<List<FormulaRs>> listarFormulas() {
        return ResponseEntity.ok(this.formulaService.listar());
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MiRespuestaRS> guardarFormula() {
        throw new GoneException("Use POST /formula-medica/crear.");
    }

    @Override
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MiRespuestaRS> actualizarFormula() {
        throw new GoneException("Use PUT /formula-medica/{id}.");
    }
}
