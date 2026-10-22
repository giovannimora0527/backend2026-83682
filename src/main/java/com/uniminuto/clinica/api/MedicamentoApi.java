package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicamento")
public interface MedicamentoApi {


    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

}
