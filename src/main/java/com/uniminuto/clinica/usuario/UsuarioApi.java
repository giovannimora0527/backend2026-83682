package com.uniminuto.clinica.usuario;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** Administración de usuarios y perfiles (solo ADMINISTRADOR). */
@Tag(name = "Usuarios", description = "Administración de usuarios y roles")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /** Datos para crear o actualizar un usuario; {@code rol} es el id del perfil. */
    record UsuarioRq(Long id, String username, String password, Long rol, String email, Boolean activo) {
    }

    /**
     * Listar usuarios (sin contraseñas).
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar usuarios (sin contraseñas)")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @GetMapping({"/listar", "/listar-ordenado"})
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Listar perfiles del sistema.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar perfiles del sistema")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @GetMapping("/perfiles")
    ResponseEntity<List<Map<String, Object>>> perfiles();

    /**
     * Crear usuario.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Crear usuario")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @PostMapping("/guardar")
    ResponseEntity<Map<String, Object>> guardar(@RequestBody UsuarioRq datos);

    /**
     * Actualizar usuario (la contraseña es opcional).
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Actualizar usuario (la contraseña es opcional)")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @PostMapping("/actualizar")
    ResponseEntity<Map<String, Object>> actualizar(@RequestBody UsuarioRq datos);

    /**
     * Prueba de conectividad del servicio.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Prueba de conectividad del servicio")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @GetMapping("/test")
    ResponseEntity<Map<String, Object>> test();
}
