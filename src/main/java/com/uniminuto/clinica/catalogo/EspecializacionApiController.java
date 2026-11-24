package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link EspecializacionApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class EspecializacionApiController implements EspecializacionApi {

    /** Dependencia inyectada por constructor: db. */
    private final Db db;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param db dependencia db.
     */
    public EspecializacionApiController(Db db) {
        this.db = db;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista("""
                SELECT id, nombre, descripcion, codigo_especializacion AS codigoEspecializacion, activo
                FROM especializacion ORDER BY nombre
                """));
    }
}
