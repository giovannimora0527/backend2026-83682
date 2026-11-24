package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RazaApiController implements RazaApi {

    private static final String CONSULTA = """
            SELECT raza_id AS razaId, raza_id AS id, nombre, especie, fecha_creacion AS fechaCreacion,
                   fecha_modificacion AS fechaModificacion
            FROM raza
            """;

    private final Db db;

    public RazaApiController(Db db) {
        this.db = db;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista(CONSULTA + " ORDER BY especie, nombre"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> guardar(RazaRq datos) {
        if (datos == null) {
            throw new BadRequestException("Los datos de la raza son obligatorios.");
        }
        String nombre = Validar.texto(datos.nombre(), "nombre", 100);
        String especie = Validar.texto(datos.especie(), "especie", 100);
        if (db.existe("SELECT 1 FROM raza WHERE nombre = :n AND especie = :e", Db.p("n", nombre, "e", especie))) {
            throw new ConflictException("La raza ya está registrada para esa especie.");
        }
        long id = db.insertar("INSERT INTO raza (nombre, especie, fecha_creacion) VALUES (:n, :e, :f)",
                Db.p("n", nombre, "e", especie, "f", Tiempo.ahora()));
        return ResponseEntity.ok(db.uno(CONSULTA + " WHERE raza_id = :id", Db.p("id", id)));
    }
}
