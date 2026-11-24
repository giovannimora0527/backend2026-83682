package com.uniminuto.clinica.usuario;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
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
@Permitido(Perfiles.ADMINISTRADOR)
@RequestMapping("/usuario")
public interface UsuarioApi {

    /** Datos para crear o actualizar un usuario; {@code rol} es el id del perfil. */
    record UsuarioRq(Long id, String username, String password, Long rol, String email, Boolean activo) {
    }

    @Operation(summary = "Listar usuarios (sin contraseñas)")
    @GetMapping({"/listar", "/listar-ordenado"})
    ResponseEntity<List<Map<String, Object>>> listar();

    @Operation(summary = "Listar perfiles del sistema")
    @GetMapping("/perfiles")
    ResponseEntity<List<Map<String, Object>>> perfiles();

    @Operation(summary = "Crear usuario")
    @PostMapping("/guardar")
    ResponseEntity<Map<String, Object>> guardar(@RequestBody UsuarioRq datos);

    @Operation(summary = "Actualizar usuario (la contraseña es opcional)")
    @PostMapping("/actualizar")
    ResponseEntity<Map<String, Object>> actualizar(@RequestBody UsuarioRq datos);

    @Operation(summary = "Prueba de conectividad del servicio")
    @GetMapping("/test")
    ResponseEntity<Map<String, Object>> test();
}
