package com.uniminuto.clinica.auditoria;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos de la tabla {@code auditoria} (log central de operaciones sensibles). */
@Repository
public class AuditoriaRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public AuditoriaRepository(Db db) {
        this.db = db;
    }

    /**
     * Inserta un registro de auditoría.
     *
     * @param registro datos del registro.
     */
    public void insertar(RegistroAuditoria registro) {
        db.insertar("""
                INSERT INTO auditoria (usuario_id, username, perfil, accion, entidad, entidad_id, detalle, ip, fecha)
                VALUES (:usuario, :username, :perfil, :accion, :entidad, :entidadId, :detalle, :ip, :fecha)
                """, Db.p("usuario", registro.usuarioId(), "username", registro.username(), "perfil", registro.perfil(),
                "accion", registro.accion(), "entidad", registro.entidad(), "entidadId", registro.entidadId(),
                "detalle", registro.detalle(), "ip", registro.ip(), "fecha", registro.fecha()));
    }

    /**
     * Busca registros con filtros opcionales, del más reciente al más antiguo.
     *
     * @param desde fecha y hora inicial (incluida) o null.
     * @param hasta fecha y hora final (excluida) o null.
     * @param accion acción exacta o null.
     * @param usuario texto contenido en el nombre de usuario o null.
     * @param limite máximo de filas.
     * @param desplazamiento filas a omitir (paginación).
     * @return registros.
     */
    public List<Map<String, Object>> buscar(LocalDateTime desde, LocalDateTime hasta, String accion, String usuario,
                                            int limite, int desplazamiento) {
        return db.lista("""
                SELECT id, usuario_id AS usuarioId, username, perfil, accion, entidad, entidad_id AS entidadId,
                       detalle, ip, fecha
                FROM auditoria
                WHERE (:desde IS NULL OR fecha >= :desde) AND (:hasta IS NULL OR fecha < :hasta)
                  AND (:accion IS NULL OR accion = :accion) AND (:usuario IS NULL OR username LIKE :usuario)
                ORDER BY fecha DESC, id DESC
                LIMIT :limite OFFSET :desp
                """, Db.p("desde", desde, "hasta", hasta, "accion", accion,
                "usuario", usuario == null ? null : "%" + usuario + "%", "limite", limite, "desp", desplazamiento));
    }

    /**
     * @return acciones distintas registradas (para el filtro de la pantalla).
     */
    public List<Map<String, Object>> acciones() {
        return db.lista("SELECT DISTINCT accion FROM auditoria ORDER BY accion");
    }
}
