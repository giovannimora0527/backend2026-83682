package com.uniminuto.clinica.usuario;

import com.uniminuto.clinica.auditoria.AuditoriaService;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.seguridad.ClaveUtil;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reglas de negocio de usuarios, roles y permisos.
 * <ul>
 *   <li>Nombre de usuario y correo únicos; contraseña de al menos 8 caracteres.</li>
 *   <li>Nadie puede inactivar su propio usuario; inactivar cierra las sesiones abiertas.</li>
 *   <li>El rol ADMINISTRADOR conserva siempre USUARIO_GESTIONAR, PERMISO_GESTIONAR y AUDITORIA_CONSULTAR.</li>
 *   <li>Toda creación, cambio de rol, estado, contraseña o permisos queda en el log de auditoría.</li>
 * </ul>
 */
@Service
public class UsuarioService {

    /** Rol administrador. */
    static final long ROL_ADMINISTRADOR = 1;
    /** Permisos que el administrador no puede perder (evita dejar el sistema sin administración). */
    static final Set<String> PERMISOS_ADMIN_OBLIGATORIOS = Set.of("USUARIO_GESTIONAR", "PERMISO_GESTIONAR", "AUDITORIA_CONSULTAR");

    /** Acceso a datos de usuarios y permisos. */
    private final UsuarioRepository repositorio;
    /** Log de operaciones sensibles. */
    private final AuditoriaService auditoria;

    /**
     * @param repositorio repositorio de usuarios.
     * @param auditoria servicio de auditoría.
     */
    public UsuarioService(UsuarioRepository repositorio, AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    /** @return usuarios. */
    public List<UsuarioDto> listar() {
        return repositorio.listar();
    }

    /** @return roles. */
    public List<Map<String, Object>> perfiles() {
        return repositorio.perfiles();
    }

    /**
     * Crea un usuario.
     *
     * @param d datos.
     * @return usuario creado.
     */
    @Transactional
    public UsuarioDto crear(UsuarioApi.UsuarioRq d) {
        validar(d, true);
        String username = d.username().trim();
        String email = d.email().trim();
        if (repositorio.existeUsuarioOCorreo(username, email, null)) {
            throw new ConflictException("El nombre de usuario o el correo ya existen.");
        }
        long id = repositorio.insertar(username, ClaveUtil.hash(d.password()), d.rol(), email, Tiempo.ahora());
        UsuarioDto u = repositorio.buscar(id);
        auditoria.registrar("USUARIO_CREADO", "usuario", id, "Usuario " + username + " con rol " + u.perfil());
        return u;
    }

    /**
     * Actualiza un usuario.
     *
     * @param d datos.
     * @return usuario actualizado.
     */
    @Transactional
    public UsuarioDto actualizar(UsuarioApi.UsuarioRq d) {
        validar(d, false);
        Validar.requerido(d.id(), "id");
        UsuarioDto antes = repositorio.buscar(d.id());
        if (antes == null) {
            throw new NotFoundException("El usuario no existe.");
        }
        String username = d.username().trim();
        String email = d.email().trim();
        if (repositorio.existeUsuarioOCorreo(username, email, d.id())) {
            throw new ConflictException("El nombre de usuario o el correo ya existen.");
        }
        boolean activo = d.activo() == null || d.activo();
        if (!activo && d.id() == SesionActual.usuarioId()) {
            throw new BadRequestException("No puedes inactivar tu propio usuario.");
        }
        repositorio.actualizar(d.id(), username, email, d.rol(), activo);
        boolean cambiaClave = d.password() != null && !d.password().isBlank();
        if (cambiaClave) {
            repositorio.cambiarClave(d.id(), ClaveUtil.hash(d.password()));
        }
        if (!activo) {
            repositorio.cerrarSesiones(d.id());
        }
        UsuarioDto despues = repositorio.buscar(d.id());
        List<String> cambios = new ArrayList<>();
        if (antes.rol() != despues.rol()) {
            cambios.add("rol " + antes.perfil() + " → " + despues.perfil());
        }
        if (antes.activo() != despues.activo()) {
            cambios.add(despues.activo() ? "activado" : "inactivado (sesiones cerradas)");
        }
        if (cambiaClave) {
            cambios.add("contraseña cambiada");
        }
        if (!antes.email().equals(despues.email()) || !antes.username().equals(despues.username())) {
            cambios.add("datos de acceso");
        }
        auditoria.registrar("USUARIO_ACTUALIZADO", "usuario", d.id(), "Usuario " + username
                + (cambios.isEmpty() ? ": sin cambios relevantes" : ": " + String.join(", ", cambios)));
        return despues;
    }

    /**
     * @return matriz de permisos {perfiles, permisos, asignaciones (perfilId → códigos)}.
     */
    public Map<String, Object> matrizPermisos() {
        Map<String, List<String>> asignaciones = new LinkedHashMap<>();
        for (Map<String, Object> f : repositorio.asignaciones()) {
            asignaciones.computeIfAbsent(String.valueOf(f.get("perfilId")), k -> new ArrayList<>()).add(Validar.cadena(f, "codigo"));
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("perfiles", repositorio.perfiles());
        r.put("permisos", repositorio.permisos());
        r.put("asignaciones", asignaciones);
        r.put("obligatoriosAdministrador", PERMISOS_ADMIN_OBLIGATORIOS);
        return r;
    }

    /**
     * Reemplaza los permisos de un rol. Aplican desde la siguiente solicitud de cada usuario del rol.
     *
     * @param perfilId rol.
     * @param datos códigos de permiso.
     * @return matriz actualizada.
     */
    @Transactional
    public Map<String, Object> asignarPermisos(long perfilId, UsuarioApi.PermisosRq datos) {
        if (!repositorio.existePerfil(perfilId)) {
            throw new NotFoundException("El rol no existe.");
        }
        if (datos == null || datos.permisos() == null) {
            throw new BadRequestException("Indica la lista de permisos del rol.");
        }
        Set<String> codigos = new LinkedHashSet<>();
        for (String c : datos.permisos()) {
            String codigo = c == null ? "" : c.trim().toUpperCase();
            if (!repositorio.existePermiso(codigo)) {
                throw new BadRequestException("El permiso " + c + " no existe.");
            }
            codigos.add(codigo);
        }
        if (perfilId == ROL_ADMINISTRADOR && !codigos.containsAll(PERMISOS_ADMIN_OBLIGATORIOS)) {
            throw new ConflictException("El rol Administrador debe conservar la gestión de usuarios, permisos y auditoría.");
        }
        repositorio.reemplazarPermisos(perfilId, new ArrayList<>(codigos));
        String rol = repositorio.perfiles().stream().filter(p -> Validar.largo(p, "perfilId") == perfilId)
                .map(p -> Validar.cadena(p, "nombre")).findFirst().orElse(String.valueOf(perfilId));
        auditoria.registrar("PERMISOS_ACTUALIZADOS", "perfil", perfilId, "Rol " + rol + ": " + codigos.size()
                + " permisos (" + String.join(", ", codigos) + ")");
        return matrizPermisos();
    }

    /**
     * Valida los datos del usuario.
     *
     * @param d datos.
     * @param nuevo true si se crea.
     */
    private void validar(UsuarioApi.UsuarioRq d, boolean nuevo) {
        if (d == null) {
            throw new BadRequestException("Los datos del usuario son obligatorios.");
        }
        String username = Validar.texto(d.username(), "usuario", 50);
        if (!username.matches("^[A-Za-z0-9._-]{3,50}$")) {
            throw new BadRequestException("El usuario solo admite letras, números, punto, guion y guion bajo (3 a 50).");
        }
        String email = Validar.texto(d.email(), "correo", 255);
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BadRequestException("El correo electrónico no es válido.");
        }
        Validar.requerido(d.rol(), "perfil");
        if (!repositorio.existePerfil(d.rol())) {
            throw new NotFoundException("El perfil no existe.");
        }
        if (nuevo || (d.password() != null && !d.password().isBlank())) {
            if (d.password() == null || d.password().length() < 8) {
                throw new BadRequestException("La contraseña debe tener al menos 8 caracteres.");
            }
        }
    }
}
