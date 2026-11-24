package com.uniminuto.clinica.menu;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MenuApiController implements MenuApi {

    private static final String COLUMNAS = """
            m.menu_id AS menuId, m.code, m.title, m.url, m.icon, m.activo,
            m.fecha_creacion AS fechaCreacion, m.fecha_modificacion AS fechaModificacion
            """;

    private final Db db;

    public MenuApiController(Db db) {
        this.db = db;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> propio() {
        return ResponseEntity.ok(db.lista("SELECT " + COLUMNAS + """
                FROM menu m JOIN perfil_menu pm ON pm.menu_id = m.menu_id
                WHERE pm.perfil_id = :perfil AND m.activo = 1
                ORDER BY m.orden, m.menu_id
                """, Db.p("perfil", SesionActual.obtener().perfilId())));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> activos() {
        return ResponseEntity.ok(db.lista("SELECT " + COLUMNAS + " FROM menu m WHERE m.activo = 1 ORDER BY m.orden"));
    }
}
