package com.uniminuto.clinica.menu;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link MenuApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class MenuApiController implements MenuApi {

    /** Consulta SQL base reutilizada por los métodos del controlador. */
    private static final String COLUMNAS = """
            m.menu_id AS menuId, m.code, m.title, m.url, m.icon, m.activo,
            m.fecha_creacion AS fechaCreacion, m.fecha_modificacion AS fechaModificacion
            """;

    /** Dependencia inyectada por constructor: db. */
    private final Db db;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param db dependencia db.
     */
    public MenuApiController(Db db) {
        this.db = db;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> propio() {
        return ResponseEntity.ok(db.lista("SELECT " + COLUMNAS + """
                FROM menu m JOIN perfil_menu pm ON pm.menu_id = m.menu_id
                WHERE pm.perfil_id = :perfil AND m.activo = 1
                ORDER BY m.orden, m.menu_id
                """, Db.p("perfil", SesionActual.obtener().perfilId())));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> activos() {
        return ResponseEntity.ok(db.lista("SELECT " + COLUMNAS + " FROM menu m WHERE m.activo = 1 ORDER BY m.orden"));
    }
}
