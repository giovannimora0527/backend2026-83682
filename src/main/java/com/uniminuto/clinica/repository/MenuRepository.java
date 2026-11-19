
package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio para consultar los menus
 * y sus permisos desde MySQL.
 */
public interface MenuRepository
        extends JpaRepository<Menu, Integer> {

    /**
     * Obtener todos los menus activos.
     */
    List<Menu> findByActivoTrueOrderByMenuIdAsc();

    /**
     * Consultar los menus autorizados
     * para un usuario segun su perfil.
     *
     * Relaciona:
     * usuario -> perfil -> perfil_menu -> menu
     */
    @Query(value = """
        SELECT DISTINCT m.*
        FROM menu m
        INNER JOIN perfil_menu pm
            ON m.menu_id = pm.menu_id
        INNER JOIN perfil p
            ON pm.perfil_id = p.perfil_id
        INNER JOIN usuario u
            ON u.perfil_id = p.perfil_id
        WHERE u.id = :usuarioId
          AND u.activo = 1
          AND p.activo = 1
          AND m.activo = 1
        ORDER BY m.menu_id
        """, nativeQuery = true)
    List<Menu> obtenerMenusPorUsuario(
            @Param("usuarioId") Long usuarioId
    );
}
