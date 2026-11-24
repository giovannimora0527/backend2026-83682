package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link MedicoApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class MedicoApiController implements MedicoApi {

    /** Consulta SQL base reutilizada por los métodos del controlador. */
    private static final String CONSULTA = """
            SELECT m.id, m.activo, m.activo AS estado, m.tipo_documento AS tipoDocumento,
                   m.numero_documento AS numeroDocumento, m.nombres, m.apellidos, m.telefono,
                   m.registro_profesional AS registroProfesional, m.especializacion_id AS especializacionId,
                   e.nombre AS especializacion, m.usuario_id AS usuarioId
            FROM medico m JOIN especializacion e ON e.id = m.especializacion_id
            """;

    /** Dependencia inyectada por constructor: db. */
    private final Db db;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param db dependencia db.
     */
    public MedicoApiController(Db db) {
        this.db = db;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(db.lista(CONSULTA + " ORDER BY m.apellidos, m.nombres"));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> disponibles() {
        return ResponseEntity.ok(db.lista(CONSULTA + " WHERE m.activo = 1 ORDER BY m.apellidos, m.nombres"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> crear(MedicoRq datos) {
        Map<String, Object> p = validar(datos, null);
        long id = db.insertar("""
                INSERT INTO medico (tipo_documento, numero_documento, nombres, apellidos, telefono,
                                    registro_profesional, especializacion_id, usuario_id, activo)
                VALUES (:tipo, :doc, :nombres, :apellidos, :tel, :registro, :esp, :usuario, :activo)
                """, p);
        return ResponseEntity.ok(db.uno(CONSULTA + " WHERE m.id = :id", Db.p("id", id)));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> actualizar(long id, MedicoRq datos) {
        db.requerido("SELECT id FROM medico WHERE id = :id", Db.p("id", id), "El médico no existe.");
        Map<String, Object> p = validar(datos, id);
        p.put("id", id);
        db.ejecutar("""
                UPDATE medico SET tipo_documento = :tipo, numero_documento = :doc, nombres = :nombres,
                       apellidos = :apellidos, telefono = :tel, registro_profesional = :registro,
                       especializacion_id = :esp, usuario_id = :usuario, activo = :activo
                WHERE id = :id
                """, p);
        return ResponseEntity.ok(db.uno(CONSULTA + " WHERE m.id = :id", Db.p("id", id)));
    }

    /**
     * Valida los datos del médico (documento y tarjeta profesional únicos, especialización existente).
     *
     * @param d datos recibidos.
     * @param id médico que se actualiza, o null si es nuevo.
     * @return parámetros listos para la sentencia SQL.
     */
    private Map<String, Object> validar(MedicoRq d, Long id) {
        if (d == null) {
            throw new BadRequestException("Los datos del médico son obligatorios.");
        }
        String doc = Validar.texto(d.numeroDocumento(), "número de documento", 20);
        String registro = Validar.texto(d.registroProfesional(), "registro profesional", 50);
        Validar.requerido(d.especializacionId(), "especialidad");
        Map<String, Object> esp = db.requerido("SELECT activo FROM especializacion WHERE id = :id",
                Db.p("id", d.especializacionId()), "La especialidad no existe.");
        if (!Validar.bool(esp, "activo")) {
            throw new BadRequestException("La especialidad está inactiva.");
        }
        if (db.existe("SELECT 1 FROM medico WHERE (numero_documento = :doc OR registro_profesional = :reg) "
                + "AND (:id IS NULL OR id <> :id)", Db.p("doc", doc, "reg", registro, "id", id))) {
            throw new ConflictException("Ya existe un médico con ese documento o registro profesional.");
        }
        if (d.usuarioId() != null) {
            Map<String, Object> u = db.requerido("""
                    SELECT p.nombre FROM usuario u JOIN perfil p ON p.perfil_id = u.rol WHERE u.id = :id
                    """, Db.p("id", d.usuarioId()), "El usuario asociado no existe.");
            if (!"MEDICO".equals(com.uniminuto.clinica.seguridad.Perfiles.codigo(Validar.cadena(u, "nombre")))) {
                throw new BadRequestException("El usuario asociado debe tener perfil Médico.");
            }
            if (db.existe("SELECT 1 FROM medico WHERE usuario_id = :u AND (:id IS NULL OR id <> :id)",
                    Db.p("u", d.usuarioId(), "id", id))) {
                throw new ConflictException("El usuario ya está asociado a otro médico.");
            }
        }
        return Db.p("tipo", Validar.texto(d.tipoDocumento(), "tipo de documento", 10), "doc", doc,
                "nombres", Validar.texto(d.nombres(), "nombres", 100),
                "apellidos", Validar.texto(d.apellidos(), "apellidos", 100),
                "tel", Validar.opcional(d.telefono(), "teléfono", 20), "registro", registro,
                "esp", d.especializacionId(), "usuario", d.usuarioId(), "activo", d.activo() == null || d.activo());
    }
}
