package com.uniminuto.clinica.usuario;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.ClaveUtil;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UsuarioApiController implements UsuarioApi {

    private static final String CONSULTA = """
            SELECT u.id, u.username, u.rol, p.nombre AS perfil, u.email, u.activo, u.fecha_creacion AS fechaCreacion
            FROM usuario u JOIN perfil p ON p.perfil_id = u.rol
            """;

    private final Db db;

    public UsuarioApiController(Db db) {
        this.db = db;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista(CONSULTA + " ORDER BY u.username"));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> perfiles() {
        return ResponseEntity.ok(db.lista("SELECT perfil_id AS perfilId, nombre, activo FROM perfil ORDER BY perfil_id"));
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> guardar(UsuarioRq datos) {
        validar(datos, true);
        if (db.existe("SELECT 1 FROM usuario WHERE username = :u OR email = :e",
                Db.p("u", datos.username().trim(), "e", datos.email().trim()))) {
            throw new ConflictException("El nombre de usuario o el correo ya existen.");
        }
        long id = db.insertar("""
                INSERT INTO usuario (username, password_hash, rol, fecha_creacion, activo, email)
                VALUES (:u, :hash, :rol, :fecha, 1, :e)
                """, Db.p("u", datos.username().trim(), "hash", ClaveUtil.hash(datos.password()), "rol", datos.rol(),
                "fecha", Tiempo.ahora(), "e", datos.email().trim()));
        return ResponseEntity.ok(db.uno(CONSULTA + " WHERE u.id = :id", Db.p("id", id)));
    }

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> actualizar(UsuarioRq datos) {
        validar(datos, false);
        Validar.requerido(datos.id(), "id");
        db.requerido("SELECT id FROM usuario WHERE id = :id", Db.p("id", datos.id()), "El usuario no existe.");
        if (db.existe("SELECT 1 FROM usuario WHERE (username = :u OR email = :e) AND id <> :id",
                Db.p("u", datos.username().trim(), "e", datos.email().trim(), "id", datos.id()))) {
            throw new ConflictException("El nombre de usuario o el correo ya existen.");
        }
        boolean activo = datos.activo() == null || datos.activo();
        if (!activo && datos.id() == SesionActual.usuarioId()) {
            throw new BadRequestException("No puedes inactivar tu propio usuario.");
        }
        db.ejecutar("UPDATE usuario SET username = :u, email = :e, rol = :rol, activo = :activo WHERE id = :id",
                Db.p("u", datos.username().trim(), "e", datos.email().trim(), "rol", datos.rol(),
                        "activo", activo, "id", datos.id()));
        if (datos.password() != null && !datos.password().isBlank()) {
            db.ejecutar("UPDATE usuario SET password_hash = :h WHERE id = :id",
                    Db.p("h", ClaveUtil.hash(datos.password()), "id", datos.id()));
        }
        if (!activo) {
            db.ejecutar("UPDATE session SET activo = 0 WHERE user_id = :id", Db.p("id", datos.id()));
        }
        return ResponseEntity.ok(db.uno(CONSULTA + " WHERE u.id = :id", Db.p("id", datos.id())));
    }

    @Override
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of("status", 200, "message", "Servicio de usuarios disponible"));
    }

    private void validar(UsuarioRq datos, boolean nuevo) {
        if (datos == null) {
            throw new BadRequestException("Los datos del usuario son obligatorios.");
        }
        Validar.texto(datos.username(), "usuario", 50);
        String email = Validar.texto(datos.email(), "correo", 255);
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BadRequestException("El correo electrónico no es válido.");
        }
        Validar.requerido(datos.rol(), "perfil");
        db.requerido("SELECT perfil_id FROM perfil WHERE perfil_id = :p", Db.p("p", datos.rol()), "El perfil no existe.");
        if (nuevo || (datos.password() != null && !datos.password().isBlank())) {
            if (datos.password() == null || datos.password().length() < 8) {
                throw new BadRequestException("La contraseña debe tener al menos 8 caracteres.");
            }
        }
    }
}
