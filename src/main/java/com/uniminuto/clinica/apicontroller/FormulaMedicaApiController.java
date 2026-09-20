package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.models.FormulaMedicaRs;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** Implementa {@link FormulaMedicaApi}. Sin try/catch: los errores los resuelve GlobalExceptionHandler. */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /** Servicio de formulas medicas. */
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedicaRs>> listarFormulas() {
        return ResponseEntity.ok(formulaMedicaService.listarFormulasRecientes());
    }
}
