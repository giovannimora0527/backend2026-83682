package com.uniminuto.clinica.usuario;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos de usuarios, roles y permisos (tablas usuario, perfil, permiso, perfil_permiso, session). */
@Repository
public class UsuarioRepository {

    /** Consulta base de usuarios con su rol. */
    private static final String CONSULTA = """
            SELECT u.id, u.username, u.rol, p.nombre AS perfil, u.email, u.activo, u.fecha_creacion
            FROM usuario u JOIN perfil p ON p.perfil_id = u.rol
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public UsuarioRepository(Db db) {
        this.db = db;
    }

    /** @return usuarios ordenados por nombre. */
    public List<UsuarioDto> listar() {
        return db.lista(CONSULTA + " ORDER BY u.username").stream().map(UsuarioRepository::aDto).toList();
    }

    /**
     * @param id identificador.
     * @return usuario o null.
     */
    public UsuarioDto buscar(long id) {
        Map<String, Object> f = db.uno(CONSULTA + " WHERE u.id = :id", Db.p("id", id));
        return f == null ? null : aDto(f);
    }

    /**
     * @param username nombre de usuario.
     * @param email correo.
     * @param excluir usuario a excluir (edición) o null.
     * @return true si el nombre o el correo ya están en uso.
     */
    public boolean existeUsuarioOCorreo(String username, String email, Long excluir) {
        return db.existe("SELECT 1 FROM usuario WHERE (username = :u OR email = :e) AND (:id IS NULL OR id <> :id)",
                Db.p("u", username, "e", email, "id", excluir));
    }

    /**
     * @param id identificador del rol.
     * @return true si existe.
     */
    public boolean existePerfil(long id) {
        return db.existe("SELECT 1 FROM perfil WHERE perfil_id = :p", Db.p("p", id));
    }

    /**
     * @param username nombre de usuario.
     * @param hash hash PBKDF2 de la contraseña.
     * @param rol identificador del rol.
     * @param email correo.
     * @param fecha fecha de creación.
     * @return id generado.
     */
    public long insertar(String username, String hash, long rol, String email, LocalDateTime fecha) {
        return db.insertar("""
                INSERT INTO usuario (username, password_hash, rol, fecha_creacion, activo, email)
                VALUES (:u, :hash, :rol, :fecha, 1, :e)
                """, Db.p("u", username, "hash", hash, "rol", rol, "fecha", fecha, "e", email));
    }

    /**
     * @param id identificador.
     * @param username nombre de usuario.
     * @param email correo.
     * @param rol rol.
     * @param activo estado.
     */
    public void actualizar(long id, String username, String email, long rol, boolean activo) {
        db.ejecutar("UPDATE usuario SET username = :u, email = :e, rol = :rol, activo = :a WHERE id = :id",
                Db.p("u", username, "e", email, "rol", rol, "a", activo, "id", id));
    }

    /**
     * @param id usuario.
     * @param hash nuevo hash de contraseña.
     */
    public void cambiarClave(long id, String hash) {
        db.ejecutar("UPDATE usuario SET password_hash = :h WHERE id = :id", Db.p("h", hash, "id", id));
    }

    /**
     * Cierra todas las sesiones del usuario.
     *
     * @param id usuario.
     */
    public void cerrarSesiones(long id) {
        db.ejecutar("UPDATE session SET activo = 0 WHERE user_id = :id", Db.p("id", id));
    }

    /** @return roles. */
    public List<Map<String, Object>> perfiles() {
        return db.lista("SELECT perfil_id AS perfilId, nombre, activo FROM perfil ORDER BY perfil_id");
    }

    /** @return catálogo de permisos. */
    public List<Map<String, Object>> permisos() {
        return db.lista("SELECT codigo, modulo, descripcion FROM permiso ORDER BY modulo, codigo");
    }

    /** @return pares (perfilId, codigo) asignados. */
    public List<Map<String, Object>> asignaciones() {
        return db.lista("""
                SELECT pp.perfil_id AS perfilId, p.codigo FROM perfil_permiso pp JOIN permiso p ON p.permiso_id = pp.permiso_id
                ORDER BY pp.perfil_id, p.codigo
                """);
    }

    /**
     * @param codigo código de permiso.
     * @return true si existe.
     */
    public boolean existePermiso(String codigo) {
        return db.existe("SELECT 1 FROM permiso WHERE codigo = :c", Db.p("c", codigo));
    }

    /**
     * Reemplaza los permisos de un rol.
     *
     * @param perfilId rol.
     * @param codigos permisos.
     */
    public void reemplazarPermisos(long perfilId, List<String> codigos) {
        db.ejecutar("DELETE FROM perfil_permiso WHERE perfil_id = :p", Db.p("p", perfilId));
        for (String c : codigos) {
            db.ejecutar("""
                    INSERT INTO perfil_permiso (perfil_id, permiso_id) SELECT :p, permiso_id FROM permiso WHERE codigo = :c
                    """, Db.p("p", perfilId, "c", c));
        }
    }

    /**
     * @param f fila de la consulta.
     * @return DTO del usuario.
     */
    private static UsuarioDto aDto(Map<String, Object> f) {
        return new UsuarioDto(Validar.largo(f, "id"), Validar.cadena(f, "username"), Validar.largo(f, "rol"),
                Validar.cadena(f, "perfil"), Validar.cadena(f, "email"), Validar.bool(f, "activo"),
                (LocalDateTime) f.get("fecha_creacion"));
    }
}
