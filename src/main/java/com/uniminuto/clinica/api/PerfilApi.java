package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.PerfilRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Usuarios y perfiles")
@RequestMapping("/perfil")
public interface PerfilApi {

    @Operation(summary = "Listar perfiles")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<PerfilRs>> listar();
}
