package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos del catálogo de razas (tabla raza). */
@Repository
public class RazaRepository {

    /** Consulta base: raza con el número de mascotas que la usan. */
    private static final String CONSULTA = """
            SELECT r.raza_id, r.nombre, r.especie, r.fecha_creacion, r.fecha_modificacion,
                   (SELECT COUNT(*) FROM mascota m WHERE m.raza_id = r.raza_id) AS mascotas
            FROM raza r
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public RazaRepository(Db db) {
        this.db = db;
    }

    /** @return razas por especie y nombre. */
    public List<RazaDto> listar() {
        return db.lista(CONSULTA + " ORDER BY r.especie, r.nombre").stream().map(RazaRepository::aDto).toList();
    }

    /**
     * @param id raza.
     * @param bloquear true para bloquear la fila (FOR UPDATE).
     * @return raza o null.
     */
    public RazaDto buscar(long id, boolean bloquear) {
        Map<String, Object> f = db.uno(CONSULTA + " WHERE r.raza_id = :id" + (bloquear ? " FOR UPDATE" : ""), Db.p("id", id));
        return f == null ? null : aDto(f);
    }

    /**
     * @param nombre nombre normalizado.
     * @param especie especie normalizada.
     * @param excluir raza a excluir (edición) o null.
     * @return true si ya existe el par nombre + especie.
     */
    public boolean existe(String nombre, String especie, Long excluir) {
        return db.existe("SELECT 1 FROM raza WHERE nombre = :n AND especie = :e AND (:id IS NULL OR raza_id <> :id)",
                Db.p("n", nombre, "e", especie, "id", excluir));
    }

    /**
     * @param nombre nombre.
     * @param especie especie.
     * @param ahora fecha de creación.
     * @return id generado.
     */
    public long insertar(String nombre, String especie, LocalDateTime ahora) {
        return db.insertar("INSERT INTO raza (nombre, especie, fecha_creacion) VALUES (:n, :e, :f)",
                Db.p("n", nombre, "e", especie, "f", ahora));
    }

    /**
     * @param id raza.
     * @param nombre nombre.
     * @param especie especie.
     * @param ahora fecha de modificación.
     */
    public void actualizar(long id, String nombre, String especie, LocalDateTime ahora) {
        db.ejecutar("UPDATE raza SET nombre = :n, especie = :e, fecha_modificacion = :f WHERE raza_id = :id",
                Db.p("n", nombre, "e", especie, "f", ahora, "id", id));
    }

    /**
     * @param id raza sin mascotas.
     */
    public void eliminar(long id) {
        db.ejecutar("DELETE FROM raza WHERE raza_id = :id", Db.p("id", id));
    }

    /**
     * @param f fila.
     * @return DTO.
     */
    private static RazaDto aDto(Map<String, Object> f) {
        int id = Validar.numero(f, "raza_id");
        return new RazaDto(id, id, Validar.cadena(f, "nombre"), Validar.cadena(f, "especie"),
                (LocalDateTime) f.get("fecha_creacion"), (LocalDateTime) f.get("fecha_modificacion"), Validar.largo(f, "mascotas"));
    }
}
