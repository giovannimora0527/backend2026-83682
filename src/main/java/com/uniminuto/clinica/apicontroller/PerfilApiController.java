package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.PerfilApi;
import com.uniminuto.clinica.models.PerfilRs;
import com.uniminuto.clinica.service.PerfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PerfilApiController implements PerfilApi {

    private final PerfilService perfilService;

    public PerfilApiController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @Override
    public ResponseEntity<List<PerfilRs>> listar() {
        return ResponseEntity.ok(perfilService.listar());
    }
}
