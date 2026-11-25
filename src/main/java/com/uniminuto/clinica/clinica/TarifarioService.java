package com.uniminuto.clinica.clinica;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Requerimiento 3.1: especialidades, servicios y tarifas. Las tarifas se manejan por vigencias:
 * registrar una nueva tarifa cierra la anterior (no la modifica ni la elimina), de modo que la tabla
 * {@code tarifa_servicio} es a la vez configuración y log histórico de cambios.
 */
@Service
public class TarifarioService {

    /** Consulta base de especialidades con su número de servicios. */
    private static final String ESPECIALIDAD = """
            SELECT id, codigo_especializacion AS codigo, nombre, descripcion, activo FROM especializacion
            """;

    /** Acceso a datos. */
    private final Db db;

    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /**
     * @param db acceso a datos.
     * @param auditoria log de operaciones sensibles.
     */
    public TarifarioService(Db db, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.db = db;
        this.auditoria = auditoria;
    }

    /**
     * @return especialidades del tarifario.
     */
    public List<Map<String, Object>> especialidades() {
        return db.lista(ESPECIALIDAD + " ORDER BY nombre");
    }

    /**
     * Crea o actualiza una especialidad.
     *
     * @param id especialidad a actualizar, o null para crear.
     * @param d datos.
     * @return especialidad guardada.
     */
    @Transactional
    public Map<String, Object> guardarEspecialidad(Long id, TarifarioApi.EspecialidadRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la especialidad son obligatorios.");
        }
        String codigo = Validar.texto(d.codigo(), "código", 10).toUpperCase();
        String nombre = Validar.texto(d.nombre(), "nombre", 100);
        boolean activo = d.activo() == null || d.activo();
        if (db.existe("SELECT 1 FROM especializacion WHERE (codigo_especializacion = :c OR nombre = :n) "
                + "AND (:id IS NULL OR id <> :id)", Db.p("c", codigo, "n", nombre, "id", id))) {
            throw new ConflictException("Ya existe una especialidad con ese código o nombre.");
        }
        Map<String, Object> p = Db.p("c", codigo, "n", nombre, "d", Validar.opcional(d.descripcion(), "descripción", 2000),
                "a", activo, "id", id);
        if (id == null) {
            id = db.insertar("INSERT INTO especializacion (codigo_especializacion, nombre, descripcion, activo) "
                    + "VALUES (:c, :n, :d, :a)", p);
        } else {
            db.requerido("SELECT id FROM especializacion WHERE id = :id", Db.p("id", id), "La especialidad no existe.");
            if (!activo && db.existe("SELECT 1 FROM medico WHERE especializacion_id = :id AND activo = 1", Db.p("id", id))) {
                throw new ConflictException("No se puede inactivar: hay médicos activos con esta especialidad.");
            }
            db.ejecutar("UPDATE especializacion SET codigo_especializacion = :c, nombre = :n, descripcion = :d, "
                    + "activo = :a WHERE id = :id", p);
        }
        auditoria.registrar("ESPECIALIDAD_GUARDADA", "especializacion", id, "Especialidad " + d.nombre() + (id == null ? " creada" : " actualizada") + (Boolean.FALSE.equals(d.activo()) ? " (inactiva)" : ""));
        return db.uno(ESPECIALIDAD + " WHERE id = :id", Db.p("id", id));
    }

    /**
     * @param especialidad identificador de la especialidad.
     * @return servicios con su tarifa vigente.
     */
    public List<Map<String, Object>> servicios(long especialidad) {
        db.requerido("SELECT id FROM especializacion WHERE id = :id", Db.p("id", especialidad), "La especialidad no existe.");
        return db.lista("""
                SELECT id, especializacion_id AS especialidadId, nombre, activo
                FROM servicio_clinico WHERE especializacion_id = :e ORDER BY nombre
                """, Db.p("e", especialidad));
    }

    /**
     * Agrega un servicio a la especialidad.
     *
     * @param especialidad identificador de la especialidad.
     * @param nombreRq nombre del servicio.
     * @return servicio creado.
     */
    @Transactional
    public Map<String, Object> agregarServicio(long especialidad, String nombreRq) {
        db.requerido("SELECT id FROM especializacion WHERE id = :id", Db.p("id", especialidad), "La especialidad no existe.");
        String nombre = Validar.texto(nombreRq, "nombre del servicio", 120);
        if (db.existe("SELECT 1 FROM servicio_clinico WHERE especializacion_id = :e AND nombre = :n",
                Db.p("e", especialidad, "n", nombre))) {
            throw new ConflictException("El servicio ya existe en esta especialidad.");
        }
        long id = db.insertar("INSERT INTO servicio_clinico (especializacion_id, nombre, activo) VALUES (:e, :n, 1)",
                Db.p("e", especialidad, "n", nombre));
        return db.uno("SELECT id, especializacion_id AS especialidadId, nombre, activo FROM servicio_clinico WHERE id = :id",
                Db.p("id", id));
    }

    /**
     * @param servicio identificador del servicio.
     * @return historial de tarifas del servicio.
     */
    public List<Map<String, Object>> tarifas(long servicio) {
        db.requerido("SELECT id FROM servicio_clinico WHERE id = :id", Db.p("id", servicio), "El servicio no existe.");
        return db.lista("""
                SELECT t.id, t.valor, t.iva_porcentaje AS iva, t.impuesto_porcentaje AS impuesto, t.vigente_desde AS desde,
                       t.vigente_hasta AS hasta, t.usuario_id AS usuarioId, u.username AS usuario, t.fecha_registro AS fecha
                FROM tarifa_servicio t JOIN usuario u ON u.id = t.usuario_id
                WHERE t.servicio_id = :s ORDER BY t.vigente_desde DESC, t.id DESC
                """, Db.p("s", servicio));
    }

    /**
     * Registra una nueva vigencia. Si existe una tarifa abierta o que se solapa desde una fecha anterior,
     * se cierra el día anterior al inicio de la nueva. No se permiten vigencias que comiencen antes de
     * una ya registrada (para no reescribir el historial).
     */
    @Transactional
    public Map<String, Object> nuevaTarifa(long servicio, TarifarioApi.TarifaRq t) {
        db.requerido("SELECT id FROM servicio_clinico WHERE id = :id FOR UPDATE", Db.p("id", servicio),
                "El servicio no existe.");
        if (t == null) {
            throw new BadRequestException("Los datos de la tarifa son obligatorios.");
        }
        BigDecimal valor = Validar.dinero(t.valor(), "valor");
        BigDecimal iva = Validar.porcentaje(t.iva(), "IVA");
        BigDecimal impuesto = Validar.porcentaje(t.impuesto(), "otros impuestos");
        LocalDate desde = Validar.fecha(t.desde(), "vigente desde", true);
        LocalDate hasta = Validar.fecha(t.hasta(), "vigente hasta", false);
        if (hasta != null && hasta.isBefore(desde)) {
            throw new BadRequestException("La fecha final de vigencia no puede ser anterior a la inicial.");
        }
        if (db.existe("SELECT 1 FROM tarifa_servicio WHERE servicio_id = :s AND vigente_desde >= :d",
                Db.p("s", servicio, "d", desde))) {
            throw new ConflictException("Ya existe una tarifa que inicia en esa fecha o después. "
                    + "Registra la nueva vigencia con una fecha posterior.");
        }
        db.ejecutar("""
                UPDATE tarifa_servicio SET vigente_hasta = :cierre
                WHERE servicio_id = :s AND (vigente_hasta IS NULL OR vigente_hasta >= :d)
                """, Db.p("cierre", desde.minusDays(1), "s", servicio, "d", desde));
        long id = db.insertar("""
                INSERT INTO tarifa_servicio (servicio_id, valor, iva_porcentaje, impuesto_porcentaje, vigente_desde,
                       vigente_hasta, usuario_id, fecha_registro)
                VALUES (:s, :v, :iva, :imp, :d, :h, :u, :ahora)
                """, Db.p("s", servicio, "v", valor, "iva", iva, "imp", impuesto, "d", desde, "h", hasta,
                "u", SesionActual.usuarioId(), "ahora", Tiempo.ahora()));
        auditoria.registrar("TARIFA_REGISTRADA", "servicio_clinico", servicio, "Nueva vigencia del servicio " + servicio + ": valor " + t.valor() + ", IVA " + t.iva() + "%, otros " + t.impuesto() + "% desde " + t.desde());
        return tarifas(servicio).stream().filter(x -> Validar.largo(x, "id") == id).findFirst().orElseThrow();
    }

    /** Tarifa vigente de un servicio en una fecha, o null si no hay. */
    public Map<String, Object> vigente(long servicio, LocalDate fecha) {
        return db.uno("""
                SELECT id, valor, iva_porcentaje AS iva, impuesto_porcentaje AS impuesto, vigente_desde AS desde,
                       vigente_hasta AS hasta
                FROM tarifa_servicio
                WHERE servicio_id = :s AND vigente_desde <= :f AND (vigente_hasta IS NULL OR vigente_hasta >= :f)
                ORDER BY vigente_desde DESC LIMIT 1
                """, Db.p("s", servicio, "f", fecha));
    }
}
