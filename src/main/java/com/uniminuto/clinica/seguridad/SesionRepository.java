package com.uniminuto.clinica.seguridad;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Acceso a datos de la autenticación: sesión activa del token y permisos del rol (tablas session, usuario, perfil). */
@Repository
public class SesionRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public SesionRepository(Db db) {
        this.db = db;
    }

    /**
     * Sesión activa y vigente de un token, con su usuario, rol y médico asociado.
     *
     * @param tokenSha256 SHA-256 del token JWT.
     * @param ahora fecha y hora actual.
     * @return fila de la sesión o null si no existe, expiró o el usuario/rol están inactivos.
     */
    public Map<String, Object> sesionActiva(String tokenSha256, LocalDateTime ahora) {
        return db.uno("""
                SELECT s.session_id, u.id AS usuario_id, u.username, p.perfil_id, p.nombre AS perfil,
                       m.id AS medico_id
                FROM session s
                JOIN usuario u ON u.id = s.user_id
                JOIN perfil p ON p.perfil_id = u.rol
                LEFT JOIN medico m ON m.usuario_id = u.id
                WHERE s.token = :token AND s.activo = 1 AND s.fecha_expiracion > :ahora
                  AND u.activo = 1 AND p.activo = 1
                """, Db.p("token", tokenSha256, "ahora", ahora));
    }

    /**
     * Permisos asignados a un rol.
     *
     * @param perfilId identificador del rol.
     * @return códigos de permiso.
     */
    public Set<String> permisos(long perfilId) {
        Set<String> r = new LinkedHashSet<>();
        for (Map<String, Object> f : db.lista("""
                SELECT p.codigo FROM perfil_permiso pp JOIN permiso p ON p.permiso_id = pp.permiso_id
                WHERE pp.perfil_id = :perfil ORDER BY p.codigo
                """, Db.p("perfil", perfilId))) {
            r.add(Validar.cadena(f, "codigo"));
        }
        return r;
    }
}
