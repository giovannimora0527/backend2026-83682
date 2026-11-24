package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Controlador REST del catálogo de razas: implementa {@link RazaApi} con sus validaciones y reglas de negocio.
 */
@RestController
public class RazaApiController implements RazaApi {

    /** Consulta SQL base: raza con el número de mascotas que la usan. */
    private static final String CONSULTA = """
            SELECT r.raza_id AS razaId, r.raza_id AS id, r.nombre, r.especie, r.fecha_creacion AS fechaCreacion,
                   r.fecha_modificacion AS fechaModificacion,
                   (SELECT COUNT(*) FROM mascota m WHERE m.raza_id = r.raza_id) AS mascotas
            FROM raza r
            """;

    /** Solo letras (con tildes), espacios, guiones y apóstrofos; entre 2 y 100 caracteres. */
    static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} '\\-]{1,99}$");

    /** Acceso a datos. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public RazaApiController(Db db) {
        this.db = db;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista(CONSULTA + " ORDER BY r.especie, r.nombre"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> guardar(RazaRq datos) {
        String[] v = validar(datos);
        if (db.existe("SELECT 1 FROM raza WHERE nombre = :n AND especie = :e", Db.p("n", v[0], "e", v[1]))) {
            throw new ConflictException("La raza " + v[0] + " ya está registrada para la especie " + v[1] + ".");
        }
        long id = db.insertar("INSERT INTO raza (nombre, especie, fecha_creacion) VALUES (:n, :e, :f)",
                Db.p("n", v[0], "e", v[1], "f", Tiempo.ahora()));
        return ResponseEntity.ok(consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> actualizar(int id, RazaRq datos) {
        Map<String, Object> actual = db.requerido(CONSULTA + " WHERE r.raza_id = :id FOR UPDATE",
                Db.p("id", id), "La raza no existe.");
        String[] v = validar(datos);
        if (db.existe("SELECT 1 FROM raza WHERE nombre = :n AND especie = :e AND raza_id <> :id",
                Db.p("n", v[0], "e", v[1], "id", id))) {
            throw new ConflictException("Ya existe otra raza " + v[0] + " para la especie " + v[1] + ".");
        }
        long mascotas = Validar.largo(actual, "mascotas");
        if (mascotas > 0 && !v[1].equalsIgnoreCase(Validar.cadena(actual, "especie"))) {
            throw new ConflictException("No se puede cambiar la especie: la raza tiene " + mascotas
                    + " mascota(s) registrada(s).");
        }
        db.ejecutar("UPDATE raza SET nombre = :n, especie = :e, fecha_modificacion = :f WHERE raza_id = :id",
                Db.p("n", v[0], "e", v[1], "f", Tiempo.ahora(), "id", id));
        return ResponseEntity.ok(consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Void> eliminar(int id) {
        db.requerido("SELECT raza_id FROM raza WHERE raza_id = :id FOR UPDATE", Db.p("id", id), "La raza no existe.");
        long mascotas = db.contar("SELECT COUNT(*) FROM mascota WHERE raza_id = :id", Db.p("id", id));
        if (mascotas > 0) {
            throw new ConflictException("No se puede eliminar: la raza tiene " + mascotas
                    + " mascota(s) registrada(s).");
        }
        db.ejecutar("DELETE FROM raza WHERE raza_id = :id", Db.p("id", id));
        return ResponseEntity.noContent().build();
    }

    /**
     * @param id identificador de la raza.
     * @return raza con su número de mascotas.
     */
    private Map<String, Object> consultar(long id) {
        return db.uno(CONSULTA + " WHERE r.raza_id = :id", Db.p("id", id));
    }

    /**
     * Valida y normaliza nombre y especie (primera letra en mayúscula).
     *
     * @param d datos recibidos.
     * @return arreglo {nombre, especie}.
     */
    static String[] validar(RazaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la raza son obligatorios.");
        }
        String nombre = capital(Validar.texto(d.nombre(), "nombre", 100));
        String especie = capital(Validar.texto(d.especie(), "especie", 100));
        if (!NOMBRE.matcher(nombre).matches()) {
            throw new BadRequestException("El nombre de la raza solo puede contener letras y espacios (mínimo 2).");
        }
        if (!NOMBRE.matcher(especie).matches()) {
            throw new BadRequestException("La especie solo puede contener letras y espacios (mínimo 2).");
        }
        return new String[]{nombre, especie};
    }

    /**
     * @param texto texto sin espacios extremos.
     * @return texto con espacios simples y la primera letra en mayúscula.
     */
    static String capital(String texto) {
        String limpio = texto.replaceAll("\\s+", " ");
        return limpio.substring(0, 1).toUpperCase(Locale.ROOT) + limpio.substring(1);
    }
}
