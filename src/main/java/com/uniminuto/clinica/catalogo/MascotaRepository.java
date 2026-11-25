package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos de mascotas (tabla mascota con raza, cliente, citas e historias). */
@Repository
public class MascotaRepository {

    /** Consulta base de mascotas. */
    private static final String CONSULTA = """
            SELECT m.mascota_id, m.nombre_mascota, m.edad, m.activo, m.fecha_registro, m.fecha_modificacion,
                   r.raza_id, r.nombre AS raza_nombre, r.especie, r.fecha_creacion AS raza_creacion,
                   c.cliente_id, c.tipo_documento, c.numero_documento, c.nombres, c.apellidos, c.telefono,
                   c.activo AS cliente_activo,
                   (SELECT COUNT(*) FROM cita ci WHERE ci.mascota_id = m.mascota_id AND ci.fecha_hora >= NOW()
                      AND ci.estado IN ('pendiente', 'programada', 'confirmada')) AS citas_futuras,
                   (SELECT COUNT(*) FROM cita ci WHERE ci.mascota_id = m.mascota_id) AS citas_totales,
                   (SELECT COUNT(*) FROM historia_medica h WHERE h.paciente_id = m.mascota_id) AS historias
            FROM mascota m
            JOIN raza r ON r.raza_id = m.raza_id
            JOIN cliente c ON c.cliente_id = m.cliente_id
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public MascotaRepository(Db db) {
        this.db = db;
    }

    /** @return mascotas por nombre. */
    public List<MascotaDto> listar() {
        return db.lista(CONSULTA + " ORDER BY m.nombre_mascota").stream().map(MascotaRepository::aDto).toList();
    }

    /**
     * @param id mascota.
     * @return mascota o null.
     */
    public MascotaDto buscar(int id) {
        Map<String, Object> f = db.uno(CONSULTA + " WHERE m.mascota_id = :id", Db.p("id", id));
        return f == null ? null : aDto(f);
    }

    /**
     * Bloquea la mascota y devuelve su estado y el de su propietario.
     *
     * @param id mascota.
     * @return {activo, cliente_activo} o null si no existe.
     */
    public Map<String, Object> bloquearEstados(int id) {
        return db.uno("""
                SELECT m.activo, c.activo AS cliente_activo FROM mascota m JOIN cliente c ON c.cliente_id = m.cliente_id
                WHERE m.mascota_id = :id FOR UPDATE
                """, Db.p("id", id));
    }

    /**
     * @param razaId raza.
     * @return true si existe.
     */
    public boolean existeRaza(int razaId) {
        return db.existe("SELECT 1 FROM raza WHERE raza_id = :id", Db.p("id", razaId));
    }

    /**
     * @param clienteId propietario.
     * @return estado del propietario o null si no existe.
     */
    public Boolean clienteActivo(long clienteId) {
        Map<String, Object> f = db.uno("SELECT activo FROM cliente WHERE cliente_id = :id", Db.p("id", clienteId));
        return f == null ? null : Validar.bool(f, "activo");
    }

    /**
     * @param clienteId propietario.
     * @param nombre nombre de la mascota.
     * @param excluir mascota a excluir o null.
     * @return true si el propietario ya tiene una mascota activa con ese nombre.
     */
    public boolean existeNombreActivo(long clienteId, String nombre, Integer excluir) {
        return db.existe("""
                SELECT 1 FROM mascota WHERE cliente_id = :cliente AND nombre_mascota = :nombre AND activo = 1
                  AND (:id IS NULL OR mascota_id <> :id)
                """, Db.p("cliente", clienteId, "nombre", nombre, "id", excluir));
    }

    /**
     * @param id mascota.
     * @param ahora fecha y hora actual.
     * @return citas futuras reservadas.
     */
    public long citasFuturas(int id, LocalDateTime ahora) {
        return db.contar("""
                SELECT COUNT(*) FROM cita WHERE mascota_id = :id AND fecha_hora >= :ahora
                  AND estado IN ('pendiente', 'programada', 'confirmada')
                """, Db.p("id", id, "ahora", ahora));
    }

    /**
     * @param d datos validados.
     * @param ahora fecha de registro.
     * @return id generado.
     */
    public long insertar(MascotaService.Datos d, LocalDateTime ahora) {
        return db.insertar("""
                INSERT INTO mascota (nombre_mascota, edad, raza_id, cliente_id, activo, fecha_registro)
                VALUES (:nombre, :edad, :raza, :cliente, 1, :fecha)
                """, Db.p("nombre", d.nombre(), "edad", d.edad(), "raza", d.razaId(), "cliente", d.clienteId(), "fecha", ahora));
    }

    /**
     * @param id mascota.
     * @param d datos validados.
     * @param activo estado.
     * @param ahora fecha de modificación.
     */
    public void actualizar(int id, MascotaService.Datos d, boolean activo, LocalDateTime ahora) {
        db.ejecutar("""
                UPDATE mascota SET nombre_mascota = :nombre, edad = :edad, raza_id = :raza, cliente_id = :cliente,
                       activo = :activo, fecha_modificacion = :fecha
                WHERE mascota_id = :id
                """, Db.p("nombre", d.nombre(), "edad", d.edad(), "raza", d.razaId(), "cliente", d.clienteId(),
                "activo", activo, "fecha", ahora, "id", id));
    }

    /**
     * @param id mascota.
     * @param activo nuevo estado.
     * @param ahora fecha de modificación.
     */
    public void cambiarEstado(int id, boolean activo, LocalDateTime ahora) {
        db.ejecutar("UPDATE mascota SET activo = :a, fecha_modificacion = :f WHERE mascota_id = :id",
                Db.p("a", activo, "f", ahora, "id", id));
    }

    /**
     * @param f fila.
     * @return DTO con raza y propietario.
     */
    private static MascotaDto aDto(Map<String, Object> f) {
        int razaId = Validar.numero(f, "raza_id");
        return new MascotaDto(Validar.numero(f, "mascota_id"), Validar.cadena(f, "nombre_mascota"), Validar.numero(f, "edad"),
                Validar.bool(f, "activo"), (LocalDateTime) f.get("fecha_registro"), (LocalDateTime) f.get("fecha_modificacion"),
                razaId, Validar.largo(f, "cliente_id"), Validar.largo(f, "citas_futuras"), Validar.largo(f, "citas_totales"),
                Validar.largo(f, "historias"),
                new MascotaDto.RazaResumen(razaId, razaId, Validar.cadena(f, "raza_nombre"), Validar.cadena(f, "especie"),
                        (LocalDateTime) f.get("raza_creacion")),
                new MascotaDto.ClienteResumen(Validar.largo(f, "cliente_id"), Validar.cadena(f, "tipo_documento"),
                        Validar.cadena(f, "numero_documento"), Validar.cadena(f, "nombres"), Validar.cadena(f, "apellidos"),
                        Validar.cadena(f, "telefono"), Validar.bool(f, "cliente_activo")));
    }
}
