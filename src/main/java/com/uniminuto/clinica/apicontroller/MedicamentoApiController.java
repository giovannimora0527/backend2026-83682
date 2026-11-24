package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    @Autowired
    private MedicamentoService medicamentoService;

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos());
    }

}
