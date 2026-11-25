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



    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /** Acceso a datos del módulo (SQL). */
    private final TarifarioRepository repositorio;

    /**
     * @param repositorio acceso a datos del módulo.
     * @param auditoria log de operaciones sensibles.
     */
    public TarifarioService(TarifarioRepository repositorio, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    /**
     * @return especialidades del tarifario.
     */
    public List<Map<String, Object>> especialidades() {
        return repositorio.listarEspecializacion();
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
        if (repositorio.existeEspecializacion(Db.p("c", codigo, "n", nombre, "id", id))) {
            throw new ConflictException("Ya existe una especialidad con ese código o nombre.");
        }
        Map<String, Object> p = Db.p("c", codigo, "n", nombre, "d", Validar.opcional(d.descripcion(), "descripción", 2000),
                "a", activo, "id", id);
        if (id == null) {
            id = repositorio.insertarEspecializacion(p);
        } else {
            repositorio.obtenerEspecializacion(Db.p("id", id), "La especialidad no existe.");
            if (!activo && repositorio.existeMedico(Db.p("id", id))) {
                throw new ConflictException("No se puede inactivar: hay médicos activos con esta especialidad.");
            }
            repositorio.actualizarEspecializacion(p);
        }
        auditoria.registrar("ESPECIALIDAD_GUARDADA", "especializacion", id, "Especialidad " + d.nombre() + (id == null ? " creada" : " actualizada") + (Boolean.FALSE.equals(d.activo()) ? " (inactiva)" : ""));
        return repositorio.buscarEspecializacion(Db.p("id", id));
    }

    /**
     * @param especialidad identificador de la especialidad.
     * @return servicios con su tarifa vigente.
     */
    public List<Map<String, Object>> servicios(long especialidad) {
        repositorio.obtenerEspecializacion(Db.p("id", especialidad), "La especialidad no existe.");
        return repositorio.listarServicioClinico(Db.p("e", especialidad));
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
        repositorio.obtenerEspecializacion(Db.p("id", especialidad), "La especialidad no existe.");
        String nombre = Validar.texto(nombreRq, "nombre del servicio", 120);
        if (repositorio.existeServicioClinico(Db.p("e", especialidad, "n", nombre))) {
            throw new ConflictException("El servicio ya existe en esta especialidad.");
        }
        long id = repositorio.insertarServicioClinico(Db.p("e", especialidad, "n", nombre));
        return repositorio.buscarServicioClinico(Db.p("id", id));
    }

    /**
     * @param servicio identificador del servicio.
     * @return historial de tarifas del servicio.
     */
    public List<Map<String, Object>> tarifas(long servicio) {
        repositorio.obtenerServicioClinico(Db.p("id", servicio), "El servicio no existe.");
        return repositorio.listarTarifaServicio(Db.p("s", servicio));
    }

    /**
     * Registra una nueva vigencia. Si existe una tarifa abierta o que se solapa desde una fecha anterior,
     * se cierra el día anterior al inicio de la nueva. No se permiten vigencias que comiencen antes de
     * una ya registrada (para no reescribir el historial).
     */
    @Transactional
    public Map<String, Object> nuevaTarifa(long servicio, TarifarioApi.TarifaRq t) {
        repositorio.obtenerServicioClinicoEnNuevaTarifa(Db.p("id", servicio), "El servicio no existe.");
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
        if (repositorio.existeTarifaServicio(Db.p("s", servicio, "d", desde))) {
            throw new ConflictException("Ya existe una tarifa que inicia en esa fecha o después. "
                    + "Registra la nueva vigencia con una fecha posterior.");
        }
        repositorio.actualizarTarifaServicio(Db.p("cierre", desde.minusDays(1), "s", servicio, "d", desde));
        long id = repositorio.insertarTarifaServicio(Db.p("s", servicio, "v", valor, "iva", iva, "imp", impuesto, "d", desde, "h", hasta,
                "u", SesionActual.usuarioId(), "ahora", Tiempo.ahora()));
        auditoria.registrar("TARIFA_REGISTRADA", "servicio_clinico", servicio, "Nueva vigencia del servicio " + servicio + ": valor " + t.valor() + ", IVA " + t.iva() + "%, otros " + t.impuesto() + "% desde " + t.desde());
        return tarifas(servicio).stream().filter(x -> Validar.largo(x, "id") == id).findFirst().orElseThrow();
    }

    /** Tarifa vigente de un servicio en una fecha, o null si no hay. */
    public Map<String, Object> vigente(long servicio, LocalDate fecha) {
        return repositorio.buscarTarifaServicio(Db.p("s", servicio, "f", fecha));
    }
}
