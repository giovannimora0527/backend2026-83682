package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos de médicos veterinarios (tabla medico y relaciones con especialidad, usuario y citas). */
@Repository
public class MedicoRepository {

    /** Consulta base: médico con especialidad, usuario asociado y citas. */
    private static final String CONSULTA = """
            SELECT m.id, m.activo, m.tipo_documento, m.numero_documento, m.nombres, m.apellidos, m.telefono,
                   m.registro_profesional, m.especializacion_id, e.nombre AS especializacion, e.activo AS especializacion_activa,
                   m.usuario_id, u.username AS usuario,
                   (SELECT COUNT(*) FROM cita c WHERE c.medico_id = m.id AND c.fecha_hora >= NOW()
                      AND c.estado IN ('pendiente', 'programada', 'confirmada')) AS citas_futuras,
                   (SELECT COUNT(*) FROM cita c WHERE c.medico_id = m.id) AS citas_totales
            FROM medico m
            JOIN especializacion e ON e.id = m.especializacion_id
            LEFT JOIN usuario u ON u.id = m.usuario_id
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public MedicoRepository(Db db) {
        this.db = db;
    }

    /**
     * @param soloActivos true para solo los médicos asignables a citas.
     * @return médicos por apellido y nombre.
     */
    public List<MedicoDto> listar(boolean soloActivos) {
        return db.lista(CONSULTA + (soloActivos ? " WHERE m.activo = 1" : "") + " ORDER BY m.apellidos, m.nombres")
                .stream().map(MedicoRepository::aDto).toList();
    }

    /**
     * @param id médico.
     * @return médico o null.
     */
    public MedicoDto buscar(long id) {
        Map<String, Object> f = db.uno(CONSULTA + " WHERE m.id = :id", Db.p("id", id));
        return f == null ? null : aDto(f);
    }

    /**
     * Bloquea la fila del médico (FOR UPDATE).
     *
     * @param id médico.
     * @return estado de la especialidad del médico o null si no existe.
     */
    public Boolean bloquearYEspecialidadActiva(long id) {
        Map<String, Object> f = db.uno("""
                SELECT e.activo FROM medico m JOIN especializacion e ON e.id = m.especializacion_id WHERE m.id = :id FOR UPDATE
                """, Db.p("id", id));
        return f == null ? null : Validar.bool(f, "activo");
    }

    /**
     * @param especialidad identificador de la especialidad.
     * @return estado de la especialidad o null si no existe.
     */
    public Boolean especialidadActiva(long especialidad) {
        Map<String, Object> f = db.uno("SELECT activo FROM especializacion WHERE id = :id", Db.p("id", especialidad));
        return f == null ? null : Validar.bool(f, "activo");
    }

    /**
     * @param documento número de documento.
     * @param excluir médico a excluir o null.
     * @return true si otro médico tiene el documento.
     */
    public boolean existeDocumento(String documento, Long excluir) {
        return db.existe("SELECT 1 FROM medico WHERE numero_documento = :d AND (:id IS NULL OR id <> :id)",
                Db.p("d", documento, "id", excluir));
    }

    /**
     * @param registro registro profesional.
     * @param excluir médico a excluir o null.
     * @return true si otro médico tiene el registro.
     */
    public boolean existeRegistro(String registro, Long excluir) {
        return db.existe("SELECT 1 FROM medico WHERE registro_profesional = :r AND (:id IS NULL OR id <> :id)",
                Db.p("r", registro, "id", excluir));
    }

    /**
     * @param usuarioId usuario.
     * @return nombre del rol del usuario o null si no existe.
     */
    public String rolDeUsuario(long usuarioId) {
        Map<String, Object> f = db.uno("SELECT p.nombre FROM usuario u JOIN perfil p ON p.perfil_id = u.rol WHERE u.id = :id",
                Db.p("id", usuarioId));
        return f == null ? null : Validar.cadena(f, "nombre");
    }

    /**
     * @param usuarioId usuario.
     * @param excluir médico a excluir o null.
     * @return true si el usuario ya está asociado a otro médico.
     */
    public boolean usuarioAsociado(long usuarioId, Long excluir) {
        return db.existe("SELECT 1 FROM medico WHERE usuario_id = :u AND (:id IS NULL OR id <> :id)",
                Db.p("u", usuarioId, "id", excluir));
    }

    /**
     * @param id médico.
     * @param ahora fecha y hora actual.
     * @return citas futuras reservadas.
     */
    public long citasFuturas(long id, LocalDateTime ahora) {
        return db.contar("""
                SELECT COUNT(*) FROM cita WHERE medico_id = :id AND fecha_hora >= :ahora
                  AND estado IN ('pendiente', 'programada', 'confirmada')
                """, Db.p("id", id, "ahora", ahora));
    }

    /** Inicio del horario base de atención (16 horas diarias). */
    static final String HORARIO_BASE_INICIO = "06:00:00";
    /** Fin del horario base de atención. */
    static final String HORARIO_BASE_FIN = "22:00:00";

    /**
     * Registra el horario base del médico: 06:00-22:00 de lunes (1) a domingo (7).
     * Usada por {@link MedicoService} (crear).
     *
     * @param medico identificador del médico.
     */
    public void insertarHorarioBase(long medico) {
        db.ejecutar("""
                INSERT INTO medico_horario (medico_id, dia_semana, hora_inicio, hora_fin)
                SELECT :m, d.dia, :inicio, :fin
                FROM (SELECT 1 AS dia UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
                      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7) d
                """, Db.p("m", medico, "inicio", HORARIO_BASE_INICIO, "fin", HORARIO_BASE_FIN));
    }

    /**
     * @param d datos validados.
     * @return id generado.
     */
    public long insertar(MedicoService.Datos d) {
        return db.insertar("""
                INSERT INTO medico (tipo_documento, numero_documento, nombres, apellidos, telefono,
                                    registro_profesional, especializacion_id, usuario_id, activo)
                VALUES (:tipo, :doc, :nombres, :apellidos, :tel, :registro, :esp, :usuario, :activo)
                """, parametros(d));
    }

    /**
     * @param id médico.
     * @param d datos validados.
     */
    public void actualizar(long id, MedicoService.Datos d) {
        Map<String, Object> p = parametros(d);
        p.put("id", id);
        db.ejecutar("""
                UPDATE medico SET tipo_documento = :tipo, numero_documento = :doc, nombres = :nombres,
                       apellidos = :apellidos, telefono = :tel, registro_profesional = :registro,
                       especializacion_id = :esp, usuario_id = :usuario, activo = :activo
                WHERE id = :id
                """, p);
    }

    /**
     * @param id médico.
     * @param activo nuevo estado.
     */
    public void cambiarEstado(long id, boolean activo) {
        db.ejecutar("UPDATE medico SET activo = :a WHERE id = :id", Db.p("a", activo, "id", id));
    }

    /**
     * @param d datos.
     * @return parámetros SQL.
     */
    private static Map<String, Object> parametros(MedicoService.Datos d) {
        return Db.p("tipo", d.tipo(), "doc", d.documento(), "nombres", d.nombres(), "apellidos", d.apellidos(),
                "tel", d.telefono(), "registro", d.registro(), "esp", d.especializacionId(), "usuario", d.usuarioId(),
                "activo", d.activo());
    }

    /**
     * @param f fila.
     * @return DTO.
     */
    private static MedicoDto aDto(Map<String, Object> f) {
        boolean activo = Validar.bool(f, "activo");
        return new MedicoDto(Validar.largo(f, "id"), activo, activo, Validar.cadena(f, "tipo_documento"),
                Validar.cadena(f, "numero_documento"), Validar.cadena(f, "nombres"), Validar.cadena(f, "apellidos"),
                Validar.cadena(f, "telefono"), Validar.cadena(f, "registro_profesional"), Validar.largo(f, "especializacion_id"),
                Validar.cadena(f, "especializacion"), Validar.bool(f, "especializacion_activa"), Validar.largoNulo(f, "usuario_id"),
                Validar.cadena(f, "usuario"), Validar.largo(f, "citas_futuras"), Validar.largo(f, "citas_totales"));
    }
}
