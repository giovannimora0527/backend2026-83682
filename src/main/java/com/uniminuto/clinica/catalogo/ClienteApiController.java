package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class ClienteApiController implements ClienteApi {

    static final String CONSULTA = """
            SELECT c.cliente_id AS clienteId, c.usuario_id AS usuarioId, c.tipo_documento AS tipoDocumento,
                   c.numero_documento AS numeroDocumento, c.nombres, c.apellidos,
                   c.fecha_nacimiento AS fechaNacimiento, c.genero, c.telefono, c.direccion, c.activo
            FROM cliente c
            """;
    private static final Set<String> TIPOS = Set.of("CC", "TI", "CE", "PA", "NIT", "PPT");

    private final Db db;

    public ClienteApiController(Db db) {
        this.db = db;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista(CONSULTA + " ORDER BY c.activo DESC, c.apellidos, c.nombres"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> buscarPorDocumento(String documento) {
        return ResponseEntity.ok(db.requerido(CONSULTA + " WHERE c.numero_documento = :d",
                Db.p("d", documento == null ? "" : documento.trim()), "No existe un cliente con ese documento."));
    }

    @Override
    public ResponseEntity<Map<String, Object>> consultar(long id) {
        return ResponseEntity.ok(db.requerido(CONSULTA + " WHERE c.cliente_id = :id", Db.p("id", id),
                "El cliente no existe."));
    }

    /**
     * Mascotas activas de un cliente. Responde 404 si el cliente no existe.
     *
     * @param id identificador del cliente.
     * @return mascotas con su raza y especie.
     */
    @Override
    public ResponseEntity<List<Map<String, Object>>> mascotas(long id) {
        consultar(id);
        return ResponseEntity.ok(db.lista("""
                SELECT m.mascota_id AS mascotaId, m.nombre_mascota AS nombreMascota, m.edad, r.nombre AS raza,
                       r.especie, m.cliente_id AS clienteId
                FROM mascota m JOIN raza r ON r.raza_id = m.raza_id
                WHERE m.cliente_id = :id AND m.activo = 1 ORDER BY m.nombre_mascota
                """, Db.p("id", id)));
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> crear(ClienteRq datos) {
        Map<String, Object> p = validar(datos, null);
        long id = db.insertar("""
                INSERT INTO cliente (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento,
                                     genero, telefono, direccion, activo)
                VALUES (:tipo, :doc, :nombres, :apellidos, :nacimiento, :genero, :telefono, :direccion, 1)
                """, p);
        return consultar(id);
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> actualizar(long id, ClienteRq datos) {
        consultar(id);
        Map<String, Object> p = validar(datos, id);
        p.put("id", id);
        db.ejecutar("""
                UPDATE cliente SET tipo_documento = :tipo, numero_documento = :doc, nombres = :nombres,
                       apellidos = :apellidos, fecha_nacimiento = :nacimiento, genero = :genero,
                       telefono = :telefono, direccion = :direccion
                WHERE cliente_id = :id
                """, p);
        return consultar(id);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> archivar(long id) {
        consultar(id);
        if (db.existe("""
                SELECT 1 FROM cita WHERE cliente_id = :id AND estado IN ('pendiente', 'confirmada') AND fecha_hora >= :ahora
                """, Db.p("id", id, "ahora", Tiempo.ahora()))) {
            throw new ConflictException("El cliente tiene citas pendientes o confirmadas. Cancélalas antes de archivarlo.");
        }
        db.ejecutar("UPDATE cliente SET activo = 0 WHERE cliente_id = :id", Db.p("id", id));
        db.ejecutar("UPDATE mascota SET activo = 0, fecha_modificacion = :f WHERE cliente_id = :id",
                Db.p("id", id, "f", Tiempo.ahora()));
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> validar(ClienteRq d, Long id) {
        if (d == null) {
            throw new BadRequestException("Los datos del cliente son obligatorios.");
        }
        String tipo = Validar.texto(d.tipoDocumento(), "tipo de documento", 10).toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new BadRequestException("Tipo de documento no válido. Use: " + String.join(", ", TIPOS) + ".");
        }
        String documento = Validar.texto(d.numeroDocumento(), "número de documento", 20);
        if (!documento.matches("[A-Za-z0-9-]{4,20}")) {
            throw new BadRequestException("El número de documento solo admite letras, números y guiones (4 a 20).");
        }
        LocalDate nacimiento = Validar.fecha(d.fechaNacimiento(), "fecha de nacimiento", true);
        if (nacimiento.isAfter(Tiempo.hoy()) || nacimiento.isBefore(LocalDate.of(1900, 1, 1))) {
            throw new BadRequestException("La fecha de nacimiento no es válida.");
        }
        String genero = Validar.opcional(d.genero(), "género", 1);
        if (genero != null && !Set.of("M", "F", "O").contains(genero.toUpperCase())) {
            throw new BadRequestException("El género debe ser M, F u O.");
        }
        String telefono = Validar.opcional(d.telefono(), "teléfono", 20);
        if (telefono != null && !telefono.matches("[0-9+ ()-]{7,20}")) {
            throw new BadRequestException("El teléfono no es válido.");
        }
        if (db.existe("SELECT 1 FROM cliente WHERE numero_documento = :doc AND (:id IS NULL OR cliente_id <> :id)",
                Db.p("doc", documento, "id", id))) {
            throw new ConflictException("Ya existe un cliente con el número de documento " + documento + ".");
        }
        return Db.p("tipo", tipo, "doc", documento, "nombres", Validar.texto(d.nombres(), "nombres", 100),
                "apellidos", Validar.texto(d.apellidos(), "apellidos", 100), "nacimiento", nacimiento,
                "genero", genero == null ? null : genero.toUpperCase(), "telefono", telefono,
                "direccion", Validar.opcional(d.direccion(), "dirección", 500));
    }
}
