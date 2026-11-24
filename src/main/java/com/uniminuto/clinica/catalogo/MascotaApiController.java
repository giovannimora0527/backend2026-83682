package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link MascotaApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class MascotaApiController implements MascotaApi {

    /** Consulta SQL base reutilizada por los métodos del controlador. */
    private static final String CONSULTA = """
            SELECT m.mascota_id, m.nombre_mascota, m.edad, m.activo, m.fecha_registro, m.fecha_modificacion,
                   r.raza_id, r.nombre AS raza_nombre, r.especie, r.fecha_creacion AS raza_creacion,
                   c.cliente_id, c.tipo_documento, c.numero_documento, c.nombres, c.apellidos, c.telefono
            FROM mascota m
            JOIN raza r ON r.raza_id = m.raza_id
            JOIN cliente c ON c.cliente_id = m.cliente_id
            """;

    /** Dependencia inyectada por constructor: db. */
    private final Db db;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param db dependencia db.
     */
    public MascotaApiController(Db db) {
        this.db = db;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista(CONSULTA + " ORDER BY m.nombre_mascota").stream()
                .map(MascotaApiController::aDetalle).toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> consultar(int id) {
        return ResponseEntity.ok(aDetalle(db.requerido(CONSULTA + " WHERE m.mascota_id = :id", Db.p("id", id),
                "La mascota no existe.")));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> crear(MascotaRq datos) {
        Map<String, Object> p = validar(datos);
        p.put("fecha", Tiempo.ahora());
        long id = db.insertar("""
                INSERT INTO mascota (nombre_mascota, edad, raza_id, cliente_id, activo, fecha_registro)
                VALUES (:nombre, :edad, :raza, :cliente, 1, :fecha)
                """, p);
        return consultar((int) id);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> actualizar(int id, MascotaRq datos) {
        consultar(id);
        Map<String, Object> p = validar(datos);
        p.put("id", id);
        p.put("fecha", Tiempo.ahora());
        p.put("activo", datos.activo() == null || datos.activo());
        db.ejecutar("""
                UPDATE mascota SET nombre_mascota = :nombre, edad = :edad, raza_id = :raza, cliente_id = :cliente,
                       activo = :activo, fecha_modificacion = :fecha
                WHERE mascota_id = :id
                """, p);
        return consultar(id);
    }

    /**
     * Valida los datos de la mascota (cliente y raza existentes, fecha de nacimiento no futura).
     *
     * @param d datos recibidos.
     * @return parámetros listos para la sentencia SQL.
     */
    private Map<String, Object> validar(MascotaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la mascota son obligatorios.");
        }
        String nombre = Validar.texto(d.nombreMascota(), "nombre de la mascota", 100);
        int edad = Validar.entero(d.edad(), "edad", 0);
        if (edad > 40) {
            throw new BadRequestException("La edad de la mascota no es válida.");
        }
        Validar.requerido(d.razaId(), "raza");
        Validar.requerido(d.clienteId(), "propietario");
        db.requerido("SELECT raza_id FROM raza WHERE raza_id = :id", Db.p("id", d.razaId()), "La raza no existe.");
        Map<String, Object> cliente = db.requerido("SELECT activo FROM cliente WHERE cliente_id = :id",
                Db.p("id", d.clienteId()), "El propietario no existe.");
        if (!Validar.bool(cliente, "activo")) {
            throw new BadRequestException("El propietario está archivado.");
        }
        return Db.p("nombre", nombre, "edad", edad, "raza", d.razaId(), "cliente", d.clienteId());
    }

    /** Estructura anidada que usa la pantalla de mascotas: {raza:{...}, cliente:{...}}. */
    static Map<String, Object> aDetalle(Map<String, Object> f) {
        Map<String, Object> raza = new LinkedHashMap<>();
        raza.put("id", f.get("raza_id"));
        raza.put("razaId", f.get("raza_id"));
        raza.put("nombre", f.get("raza_nombre"));
        raza.put("especie", f.get("especie"));
        raza.put("fechaCreacion", f.get("raza_creacion"));
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("clienteId", f.get("cliente_id"));
        cliente.put("tipoDocumento", f.get("tipo_documento"));
        cliente.put("numeroDocumento", f.get("numero_documento"));
        cliente.put("nombres", f.get("nombres"));
        cliente.put("apellidos", f.get("apellidos"));
        cliente.put("telefono", f.get("telefono"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mascotaId", f.get("mascota_id"));
        m.put("nombreMascota", f.get("nombre_mascota"));
        m.put("edad", f.get("edad"));
        m.put("activo", f.get("activo"));
        m.put("fechaRegistro", f.get("fecha_registro"));
        m.put("fechaModificacion", f.get("fecha_modificacion"));
        m.put("razaId", f.get("raza_id"));
        m.put("clienteId", f.get("cliente_id"));
        m.put("raza", raza);
        m.put("cliente", cliente);
        return m;
    }
}
