package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {
    @Autowired
    private FormulaMedicaService formulaMedicaService ;
    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException {

        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasMedicas());
    }
}
