package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class EspecializacionApiController implements EspecializacionApi {

    private final Db db;

    public EspecializacionApiController(Db db) {
        this.db = db;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista("""
                SELECT id, nombre, descripcion, codigo_especializacion AS codigoEspecializacion, activo
                FROM especializacion ORDER BY nombre
                """));
    }
}
