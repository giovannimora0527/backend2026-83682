package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.Perfiles;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Controlador REST del catálogo de médicos: implementa {@link MedicoApi} con sus validaciones y reglas de negocio.
 */
@RestController
public class MedicoApiController implements MedicoApi {

    /** Consulta SQL base: médico con especialidad, usuario asociado y número de citas futuras reservadas. */
    private static final String CONSULTA = """
            SELECT m.id, m.activo, m.activo AS estado, m.tipo_documento AS tipoDocumento,
                   m.numero_documento AS numeroDocumento, m.nombres, m.apellidos, m.telefono,
                   m.registro_profesional AS registroProfesional, m.especializacion_id AS especializacionId,
                   e.nombre AS especializacion, e.activo AS especializacionActiva,
                   m.usuario_id AS usuarioId, u.username AS usuario,
                   (SELECT COUNT(*) FROM cita c WHERE c.medico_id = m.id AND c.fecha_hora >= NOW()
                      AND c.estado IN ('pendiente', 'programada', 'confirmada')) AS citasFuturas,
                   (SELECT COUNT(*) FROM cita c WHERE c.medico_id = m.id) AS citasTotales
            FROM medico m
            JOIN especializacion e ON e.id = m.especializacion_id
            LEFT JOIN usuario u ON u.id = m.usuario_id
            """;

    /** Tipos de documento permitidos para un profesional. */
    static final Set<String> TIPOS = Set.of("CC", "CE", "PA", "PPT");
    /** Cédula de ciudadanía: solo dígitos, entre 6 y 10. */
    static final Pattern CEDULA = Pattern.compile("^\\d{6,10}$");
    /** Otros documentos: letras y dígitos, entre 5 y 15. */
    static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{5,15}$");
    /** Nombres y apellidos: letras (con tildes), espacios, apóstrofos y guiones. */
    static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} '\\-]{1,99}$");
    /** Teléfono: entre 7 y 10 dígitos. */
    static final Pattern TELEFONO = Pattern.compile("^\\d{7,10}$");
    /** Registro profesional: letras, dígitos y guiones, entre 3 y 50. */
    static final Pattern REGISTRO = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9\\-]{2,49}$");

    /** Acceso a datos. */
    private final Db db;

    /**
     * @param db acceso a datos.
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
        return ResponseEntity.ok(consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> actualizar(long id, MedicoRq datos) {
        db.requerido("SELECT id FROM medico WHERE id = :id FOR UPDATE", Db.p("id", id), "El médico no existe.");
        Map<String, Object> p = validar(datos, id);
        if (!(Boolean) p.get("activo")) {
            verificarSinCitasFuturas(id);
        }
        p.put("id", id);
        db.ejecutar("""
                UPDATE medico SET tipo_documento = :tipo, numero_documento = :doc, nombres = :nombres,
                       apellidos = :apellidos, telefono = :tel, registro_profesional = :registro,
                       especializacion_id = :esp, usuario_id = :usuario, activo = :activo
                WHERE id = :id
                """, p);
        return ResponseEntity.ok(consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> cambiarEstado(long id, EstadoRq datos) {
        Validar.requerido(datos == null ? null : datos.activo(), "activo");
        Map<String, Object> m = db.requerido("""
                SELECT m.id, e.activo AS especializacionActiva FROM medico m
                JOIN especializacion e ON e.id = m.especializacion_id WHERE m.id = :id FOR UPDATE
                """, Db.p("id", id), "El médico no existe.");
        if (datos.activo()) {
            if (!Validar.bool(m, "especializacionActiva")) {
                throw new BadRequestException("No se puede activar: la especialidad del médico está inactiva.");
            }
        } else {
            verificarSinCitasFuturas(id);
        }
        db.ejecutar("UPDATE medico SET activo = :a WHERE id = :id", Db.p("a", datos.activo(), "id", id));
        return ResponseEntity.ok(consultar(id));
    }

    /**
     * Impide inactivar a un médico que todavía tiene citas futuras reservadas.
     *
     * @param id identificador del médico.
     */
    private void verificarSinCitasFuturas(long id) {
        long futuras = db.contar("""
                SELECT COUNT(*) FROM cita WHERE medico_id = :id AND fecha_hora >= :ahora
                  AND estado IN ('pendiente', 'programada', 'confirmada')
                """, Db.p("id", id, "ahora", Tiempo.ahora()));
        if (futuras > 0) {
            throw new ConflictException("No se puede inactivar: el médico tiene " + futuras
                    + " cita(s) futura(s) reservada(s). Reasígnelas o cancélelas primero.");
        }
    }

    /**
     * @param id identificador del médico.
     * @return médico con su especialidad, usuario y citas.
     */
    private Map<String, Object> consultar(long id) {
        return db.uno(CONSULTA + " WHERE m.id = :id", Db.p("id", id));
    }

    /**
     * Valida formato y reglas de negocio de los datos del médico.
     *
     * @param d datos recibidos.
     * @param id médico que se actualiza, o null si es nuevo.
     * @return parámetros listos para la sentencia SQL.
     */
    private Map<String, Object> validar(MedicoRq d, Long id) {
        Map<String, Object> p = validarFormato(d);
        Map<String, Object> esp = db.requerido("SELECT activo FROM especializacion WHERE id = :id",
                Db.p("id", d.especializacionId()), "La especialidad no existe.");
        if (!Validar.bool(esp, "activo") && (Boolean) p.get("activo")) {
            throw new BadRequestException("La especialidad está inactiva.");
        }
        if (db.existe("SELECT 1 FROM medico WHERE numero_documento = :doc AND (:id IS NULL OR id <> :id)",
                Db.p("doc", p.get("doc"), "id", id))) {
            throw new ConflictException("Ya existe un médico con el documento " + p.get("doc") + ".");
        }
        if (db.existe("SELECT 1 FROM medico WHERE registro_profesional = :reg AND (:id IS NULL OR id <> :id)",
                Db.p("reg", p.get("registro"), "id", id))) {
            throw new ConflictException("Ya existe un médico con el registro profesional " + p.get("registro") + ".");
        }
        if (d.usuarioId() != null) {
            Map<String, Object> u = db.requerido("""
                    SELECT p.nombre FROM usuario u JOIN perfil p ON p.perfil_id = u.rol WHERE u.id = :id
                    """, Db.p("id", d.usuarioId()), "El usuario asociado no existe.");
            if (!Perfiles.VETERINARIO.equals(Perfiles.codigo(Validar.cadena(u, "nombre")))) {
                throw new BadRequestException("El usuario asociado debe tener perfil Médico.");
            }
            if (db.existe("SELECT 1 FROM medico WHERE usuario_id = :u AND (:id IS NULL OR id <> :id)",
                    Db.p("u", d.usuarioId(), "id", id))) {
                throw new ConflictException("El usuario ya está asociado a otro médico.");
            }
        }
        return p;
    }

    /**
     * Valida el formato de los campos (sin consultar la base de datos).
     *
     * @param d datos recibidos.
     * @return parámetros normalizados.
     */
    static Map<String, Object> validarFormato(MedicoRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos del médico son obligatorios.");
        }
        String tipo = Validar.texto(d.tipoDocumento(), "tipo de documento", 10).toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(tipo)) {
            throw new BadRequestException("Tipo de documento no válido. Use CC, CE, PA o PPT.");
        }
        String doc = Validar.texto(d.numeroDocumento(), "número de documento", 20).replace(" ", "");
        if ("CC".equals(tipo) ? !CEDULA.matcher(doc).matches() : !DOCUMENTO.matcher(doc).matches()) {
            throw new BadRequestException("CC".equals(tipo)
                    ? "La cédula debe tener entre 6 y 10 dígitos."
                    : "El documento debe tener entre 5 y 15 letras o dígitos.");
        }
        String nombres = Validar.texto(d.nombres(), "nombres", 100).replaceAll("\\s+", " ");
        String apellidos = Validar.texto(d.apellidos(), "apellidos", 100).replaceAll("\\s+", " ");
        if (!NOMBRE.matcher(nombres).matches() || !NOMBRE.matcher(apellidos).matches()) {
            throw new BadRequestException("Nombres y apellidos solo pueden contener letras y espacios.");
        }
        String tel = Validar.opcional(d.telefono(), "teléfono", 20);
        if (tel != null && !TELEFONO.matcher(tel).matches()) {
            throw new BadRequestException("El teléfono debe tener entre 7 y 10 dígitos.");
        }
        String registro = Validar.texto(d.registroProfesional(), "registro profesional", 50).toUpperCase(Locale.ROOT);
        if (!REGISTRO.matcher(registro).matches()) {
            throw new BadRequestException("El registro profesional solo admite letras, dígitos y guiones (3 a 50).");
        }
        Validar.requerido(d.especializacionId(), "especialidad");
        return Db.p("tipo", tipo, "doc", doc, "nombres", nombres, "apellidos", apellidos, "tel", tel,
                "registro", registro, "esp", d.especializacionId(), "usuario", d.usuarioId(),
                "activo", d.activo() == null || d.activo());
    }
}
