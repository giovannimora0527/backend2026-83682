package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.PerfilRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/perfil")
public interface PerfilApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<PerfilRs>> listar();
}
