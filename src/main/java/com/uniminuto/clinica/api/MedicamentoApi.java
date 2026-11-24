package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Inventario")
@RequestMapping("/medicamento")
public interface MedicamentoApi {


    @Operation(summary = "Listar medicamentos")
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

}
