package com.uniminuto.clinica.menu;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** Contrato REST del menú lateral según el perfil del usuario. */
@Tag(name = "Menú", description = "Opciones de menú según el perfil autenticado")
@RequestMapping("/menu")
public interface MenuApi {

    /**
     * Menús del perfil del usuario autenticado.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Menús del perfil del usuario autenticado")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/propio")
    ResponseEntity<List<Map<String, Object>>> propio();

    /**
     * Todos los menús activos (administración).
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Todos los menús activos (administración)")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @GetMapping("/listar-activos")
    ResponseEntity<List<Map<String, Object>>> activos();
}
