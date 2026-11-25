package com.uniminuto.clinica.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Administración de usuarios, roles, permisos por rol y consulta del log de auditoría.
 * Los cambios de permisos aplican de inmediato: los permisos se leen de la base en cada solicitud.
 */
@Tag(name = "Usuarios", description = "Usuarios, roles, permisos y trazabilidad")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /** Datos para crear o actualizar un usuario; {@code rol} es el id del perfil. */
    record UsuarioRq(Long id, String username, String password, Long rol, String email, Boolean activo) {
    }

    /** Permisos asignados a un rol (códigos). */
    record PermisosRq(List<String> permisos) {
    }

    /**
     * Listar usuarios (sin contraseñas).
     *
     * @return usuarios con su rol.
     */
    @Operation(summary = "Listar usuarios (sin contraseñas)")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @GetMapping({"/listar", "/listar-ordenado"})
    ResponseEntity<List<UsuarioDto>> listar();

    /**
     * Listar roles del sistema.
     *
     * @return roles.
     */
    @Operation(summary = "Listar roles del sistema")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @GetMapping("/perfiles")
    ResponseEntity<List<Map<String, Object>>> perfiles();

    /**
     * Crear usuario.
     *
     * @param datos datos del usuario.
     * @return usuario creado.
     */
    @Operation(summary = "Crear usuario")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @PostMapping("/guardar")
    ResponseEntity<UsuarioDto> guardar(@RequestBody UsuarioRq datos);

    /**
     * Actualizar usuario (rol, correo, estado y contraseña opcional). Inactivar cierra sus sesiones.
     *
     * @param datos datos del usuario.
     * @return usuario actualizado.
     */
    @Operation(summary = "Actualizar usuario, rol o estado")
    @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
    @PostMapping("/actualizar")
    ResponseEntity<UsuarioDto> actualizar(@RequestBody UsuarioRq datos);

    /**
     * Matriz de permisos: roles, catálogo de permisos y asignaciones.
     *
     * @return {perfiles, permisos, asignaciones}.
     */
    @Operation(summary = "Matriz de permisos por rol")
    @PreAuthorize("hasAuthority('PERMISO_GESTIONAR')")
    @GetMapping("/permisos")
    ResponseEntity<Map<String, Object>> permisos();

    /**
     * Reemplazar los permisos de un rol. El rol ADMINISTRADOR conserva siempre la gestión de usuarios, permisos y
     * auditoría para que el sistema no quede sin administración.
     *
     * @param perfilId identificador del rol.
     * @param datos códigos de permiso.
     * @return matriz actualizada.
     */
    @Operation(summary = "Asignar permisos a un rol")
    @PreAuthorize("hasAuthority('PERMISO_GESTIONAR')")
    @PutMapping("/permisos/{perfilId}")
    ResponseEntity<Map<String, Object>> asignarPermisos(@PathVariable long perfilId, @RequestBody PermisosRq datos);

    /**
     * Log de operaciones sensibles con filtros.
     *
     * @param desde fecha inicial AAAA-MM-DD.
     * @param hasta fecha final AAAA-MM-DD.
     * @param accion acción.
     * @param usuario parte del nombre de usuario.
     * @param pagina página desde 0.
     * @return página del log.
     */
    @Operation(summary = "Consultar el log de auditoría (trazabilidad)")
    @PreAuthorize("hasAuthority('AUDITORIA_CONSULTAR')")
    @GetMapping("/auditoria")
    ResponseEntity<Map<String, Object>> auditoria(@RequestParam(required = false) String desde,
                                                  @RequestParam(required = false) String hasta,
                                                  @RequestParam(required = false) String accion,
                                                  @RequestParam(required = false) String usuario,
                                                  @RequestParam(required = false) Integer pagina);
}
