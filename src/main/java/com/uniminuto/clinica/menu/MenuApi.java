package com.uniminuto.clinica.menu;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Tag(name = "Menú", description = "Opciones de menú según el perfil autenticado")
@RequestMapping("/menu")
public interface MenuApi {

    @Operation(summary = "Menús del perfil del usuario autenticado")
    @GetMapping("/propio")
    ResponseEntity<List<Map<String, Object>>> propio();

    @Operation(summary = "Todos los menús activos (administración)")
    @Permitido(Perfiles.ADMINISTRADOR)
    @GetMapping("/listar-activos")
    ResponseEntity<List<Map<String, Object>>> activos();
}
