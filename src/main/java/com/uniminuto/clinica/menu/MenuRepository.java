package com.uniminuto.clinica.menu;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos del menú lateral (tablas menu y perfil_menu). */
@Repository
public class MenuRepository {

    /** Columnas del menú. */
    private static final String COLUMNAS = "m.menu_id, m.code, m.title, m.url, m.icon, m.activo, m.fecha_creacion, m.fecha_modificacion";

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public MenuRepository(Db db) {
        this.db = db;
    }

    /**
     * @param perfilId rol.
     * @return opciones activas del rol, en orden.
     */
    public List<MenuDto> delPerfil(long perfilId) {
        return db.lista("SELECT " + COLUMNAS + """
                 FROM menu m JOIN perfil_menu pm ON pm.menu_id = m.menu_id
                WHERE pm.perfil_id = :perfil AND m.activo = 1
                ORDER BY m.orden, m.menu_id
                """, Db.p("perfil", perfilId)).stream().map(MenuRepository::aDto).toList();
    }

    /** @return todas las opciones activas. */
    public List<MenuDto> activos() {
        return db.lista("SELECT " + COLUMNAS + " FROM menu m WHERE m.activo = 1 ORDER BY m.orden").stream()
                .map(MenuRepository::aDto).toList();
    }

    /**
     * @param f fila.
     * @return DTO.
     */
    private static MenuDto aDto(Map<String, Object> f) {
        return new MenuDto(Validar.numero(f, "menu_id"), Validar.cadena(f, "code"), Validar.cadena(f, "title"),
                Validar.cadena(f, "url"), Validar.cadena(f, "icon"), Validar.bool(f, "activo"),
                (LocalDateTime) f.get("fecha_creacion"), (LocalDateTime) f.get("fecha_modificacion"));
    }
}
