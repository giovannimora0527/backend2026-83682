package com.uniminuto.clinica.cita;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.ExcelLibro;
import com.uniminuto.clinica.comun.ExcelLibro.Columna;
import com.uniminuto.clinica.comun.ExcelLibro.Formato;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reglas de negocio de citas médicas y agenda de veterinarios.
 *
 * <ul>
 *   <li>Un veterinario no puede tener dos citas que se superpongan.</li>
 *   <li>Una mascota no puede tener dos citas simultáneas.</li>
 *   <li>No se asignan citas a médicos inactivos ni fuera de su horario o en ausencias.</li>
 *   <li>Las citas canceladas conservan su historial (nunca se eliminan; cada cambio queda en cita_revision).</li>
 *   <li>Cada cita se relaciona con una mascota, su propietario y un médico veterinario.</li>
 * </ul>
 */
@Service
public class CitaService {

    /** Estados válidos de una cita. */
    public static final Set<String> ESTADOS = Set.of("pendiente", "programada", "confirmada", "cancelada", "atendida", "finalizada");
    /** Estados de una cita reservada que aún no se ha atendido (se puede reprogramar). */
    static final Set<String> RESERVADAS = Set.of("pendiente", "programada", "confirmada");
    /** Margen mínimo entre citas del mismo veterinario (regla: 15 minutos antes y 15 después). */
    static final int MARGEN_MINUTOS = 15;
    /** Duración máxima de una cita en minutos. */
    private static final int MINUTOS_MAXIMOS = 240;



    /** Acceso a datos del módulo (SQL). */
    private final CitaRepository repositorio;

    /**
     * @param repositorio acceso a datos del módulo.
     */
    public CitaService(CitaRepository repositorio) {
        this.repositorio = repositorio;
    }

    // ------------------------------------------------------------------ CRUD


    /**
     * Lista las citas de la más reciente a la más antigua. El médico solo recibe las suyas.
     *
     * @return citas con identificadores y nombres de cliente, mascota y veterinario.
     */
    public List<Map<String, Object>> listar() {
        return listarRango(null, null);
    }

    /**
     * Filtra las citas entre una fecha inicial y una final (ambas opcionales e inclusivas),
     * ordenadas desde la más reciente hasta la más antigua.
     *
     * @param fechaInicio fecha inicial AAAA-MM-DD (o fecha y hora).
     * @param fechaFin    fecha final AAAA-MM-DD (o fecha y hora).
     * @return citas del rango.
     */
    public List<Map<String, Object>> listarRango(String fechaInicio, String fechaFin) {
        LocalDate desde = Validar.fecha(fechaInicio, "fecha inicial", false);
        LocalDate hasta = Validar.fecha(fechaFin, "fecha final", false);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la fecha inicial.");
        }
        SesionUsuario s = SesionActual.obtener();
        Long medico = s.es(Perfiles.VETERINARIO) ? SesionActual.medicoId() : null;
        return repositorio.listarCita(Db.p("medico", medico, "desde", desde == null ? null : desde.atStartOfDay(),
                "hasta", hasta == null ? null : hasta.plusDays(1).atStartOfDay()));
    }

    /**
     * Citas de una mascota, de la más reciente a la más antigua (el médico solo ve las suyas).
     *
     * @param mascotaId identificador de la mascota; 404 si no existe.
     * @return citas con nombres y la historia clínica asociada.
     */
    public List<Map<String, Object>> porMascota(int mascotaId) {
        repositorio.obtenerMascota(Db.p("m", mascotaId), "La mascota no existe.");
        SesionUsuario s = SesionActual.obtener();
        Long medico = s.es(Perfiles.VETERINARIO) ? SesionActual.medicoId() : null;
        return repositorio.listarCitaEnPorMascota(Db.p("m", mascotaId, "medico", medico));
    }

    /**
     * Consulta el detalle de una cita (con nombres y la historia clínica asociada, si existe).
     *
     * @param id identificador de la cita.
     * @return cita encontrada; 404 si no existe; 403 si es de otro médico.
     */
    public Map<String, Object> detalle(long id) {
        Map<String, Object> cita = repositorio.obtenerCita(Db.p("id", id), "La cita no existe.");
        SesionUsuario s = SesionActual.obtener();
        if (s.es(Perfiles.VETERINARIO) && Validar.largo(cita, "medicoId") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo puedes consultar tus propias citas.");
        }
        return cita;
    }

    /**
     * Reactiva una cita cancelada (operación explícita): vuelve a quedar pendiente en el horario indicado,
     * validando de nuevo todas las reglas de programación. Queda registrada en el historial de la cita.
     *
     * @param id     cita cancelada.
     * @param cambio nuevo inicio y fin (opcionales: si no se envían se usa el horario original).
     * @return cita reactivada.
     */
    @Transactional
    public Map<String, Object> reactivar(long id, CitaApi.CambioRq cambio) {
        Map<String, Object> actual = bloquear(id);
        if (!"cancelada".equals(actual.get("estado"))) {
            throw new ConflictException("Solo se pueden reactivar citas canceladas.");
        }
        LocalDateTime inicio = cambio == null || cambio.inicio() == null
                ? (LocalDateTime) actual.get("fecha_hora") : Validar.fechaHora(cambio.inicio(), "inicio");
        LocalDateTime fin = cambio == null || cambio.fin() == null
                ? (LocalDateTime) actual.get("fecha_fin") : Validar.fechaHora(cambio.fin(), "fin");
        validarProgramacion(Validar.largo(actual, "medico_id"), Validar.largo(actual, "mascota_id"), inicio, fin, id);
        repositorio.actualizarCita(Db.p("i", inicio, "f", fin, "ahora", Tiempo.ahora(), "id", id));
        registrarRevision(id, actual, "pendiente", inicio, fin);
        return detalle(id);
    }

    /**
     * Consulta una cita verificando que el usuario pueda verla.
     *
     * @param id identificador de la cita.
     * @return cita.
     */
    public Map<String, Object> consultar(long id) {
        return repositorio.obtenerCitaEnConsultar(Db.p("id", id), "La cita no existe.");
    }

    /**
     * Agenda una cita validando cliente, mascota, médico, horario y margen de 15 minutos.
     *
     * @param d datos de la cita.
     * @return cita creada.
     */
    @Transactional
    public Map<String, Object> crear(CitaApi.CitaRq d) {
        Datos datos = validarDatos(conMedicoPropio(d));
        verificarMedicoPropio(datos.medico, SesionActual.obtener());
        if (!RESERVADAS.contains(datos.estado)) {
            throw new BadRequestException("Una cita nueva solo puede quedar pendiente, programada o confirmada.");
        }
        validarProgramacion(datos.medico, datos.mascota, datos.inicio, datos.fin, null);
        long id = repositorio.insertarCita(Db.p("cliente", datos.cliente, "mascota", datos.mascota, "medico", datos.medico,
                "inicio", datos.inicio, "fin", datos.fin, "estado", datos.estado, "motivo", datos.motivo,
                "usuario", SesionActual.usuarioId(), "ahora", Tiempo.ahora()));
        registrarRevision(id, null, datos.estado, datos.inicio, datos.fin);
        return consultar(id);
    }

    /**
     * Reprograma o modifica una cita y registra la revisión.
     *
     * @param id identificador de la cita.
     * @param d nuevos datos.
     * @return cita actualizada.
     */
    @Transactional
    public Map<String, Object> actualizar(long id, CitaApi.CitaRq d) {
        Map<String, Object> actual = bloquear(id);
        String estadoActual = Validar.cadena(actual, "estado");
        if (Set.of("cancelada", "finalizada").contains(estadoActual)) {
            throw new ConflictException("La cita está " + estadoActual + " y ya no puede modificarse.");
        }
        Datos datos = validarDatos(conMedicoPropio(d));
        SesionUsuario s = SesionActual.obtener();
        verificarAcceso(actual, s);
        verificarMedicoPropio(datos.medico, s);
        if ("atendida".equals(datos.estado) && !s.esAdmin() && !"atendida".equals(estadoActual)) {
            throw new ForbiddenException("Solo el médico (al finalizar la atención) o el administrador marcan una cita como atendida.");
        }
        validarTransicion(estadoActual, datos.estado, s);
        validarAtendida(estadoActual, datos.estado, datos.inicio);
        LocalDateTime inicioActual = (LocalDateTime) actual.get("fecha_hora");
        LocalDateTime finActual = (LocalDateTime) actual.get("fecha_fin");
        boolean reprograma = !datos.inicio.equals(inicioActual) || !datos.fin.equals(finActual)
                || datos.medico != Validar.largo(actual, "medico_id") || datos.mascota != Validar.largo(actual, "mascota_id");
        if (reprograma) {
            if (!RESERVADAS.contains(estadoActual)) {
                throw new ConflictException("Solo se reprograman citas pendientes o confirmadas.");
            }
            validarProgramacion(datos.medico, datos.mascota, datos.inicio, datos.fin, id);
        }
        repositorio.actualizarCitaEnActualizar(Db.p("cliente", datos.cliente, "mascota", datos.mascota, "medico", datos.medico,
                "inicio", datos.inicio, "fin", datos.fin, "estado", datos.estado, "motivo", datos.motivo,
                "ahora", Tiempo.ahora(), "id", id));
        registrarRevision(id, actual, datos.estado, datos.inicio, datos.fin);
        return consultar(id);
    }

    // ------------------------------------------------------------------ AGENDA

    /**
     * Citas de un médico en un rango de fechas (máximo un año).
     *
     * @param medico identificador del médico.
     * @param desde fecha inicial AAAA-MM-DD.
     * @param hasta fecha final AAAA-MM-DD.
     * @param estado estado opcional.
     * @return eventos del calendario.
     */
    public List<Map<String, Object>> eventos(long medico, String desde, String hasta, String estado) {
        LocalDate d = Validar.fecha(desde, "desde", true);
        LocalDate h = Validar.fecha(hasta, "hasta", true);
        if (h.isBefore(d)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la inicial.");
        }
        if (d.plusDays(400).isBefore(h)) {
            throw new BadRequestException("El rango máximo de consulta es de un año.");
        }
        String filtroEstado = estado == null || estado.isBlank() ? null : estado.trim().toLowerCase();
        if (filtroEstado != null && !ESTADOS.contains(filtroEstado)) {
            throw new BadRequestException("Estado de cita no válido.");
        }
        return repositorio.listarCitaEnEventos(Db.p("medico", medico, "desde", d.atStartOfDay(), "hasta", h.plusDays(1).atStartOfDay(),
                "estado", filtroEstado));
    }

    /**
     * Exporta a Excel la agenda de un médico.
     *
     * @param medico identificador del médico.
     * @param desde fecha inicial.
     * @param hasta fecha final.
     * @param estado estado opcional.
     * @return archivo .xlsx.
     */
    public byte[] excel(long medico, String desde, String hasta, String estado) {
        List<Map<String, Object>> citas = eventos(medico, desde, hasta, estado);
        Map<String, Object> m = repositorio.buscarMedico(Db.p("id", medico));
        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("Médico", Validar.cadena(m, "nombre"));
        filtros.put("Rango", desde + " a " + hasta);
        filtros.put("Estado", estado == null || estado.isBlank() ? "Todos" : estado);
        for (String e : List.of("pendiente", "programada", "confirmada", "cancelada", "atendida", "finalizada")) {
            long n = citas.stream().filter(c -> e.equals(c.get("estado"))).count();
            filtros.put("Citas " + e, String.valueOf(n));
        }
        List<Columna> columnas = List.of(
                new Columna("id", "Cita", Formato.ENTERO), new Columna("inicio", "Inicio", Formato.FECHAHORA),
                new Columna("fin", "Fin", Formato.FECHAHORA), new Columna("estado", "Estado", Formato.TEXTO),
                new Columna("mascota", "Mascota", Formato.TEXTO), new Columna("especie", "Especie", Formato.TEXTO),
                new Columna("propietario", "Propietario", Formato.TEXTO),
                new Columna("documento", "Documento", Formato.TEXTO), new Columna("telefono", "Teléfono", Formato.TEXTO),
                new Columna("motivo", "Motivo", Formato.TEXTO));
        return ExcelLibro.generar("Reporte de citas del médico", filtros, columnas, citas,
                Map.of("id", citas.size()));
    }

    /**
     * Calcula los espacios libres de un médico en un día (horario menos citas y ausencias).
     *
     * @param medico identificador del médico.
     * @param fechaTexto fecha AAAA-MM-DD.
     * @return franjas libres y ocupadas.
     */
    public Map<String, Object> disponibilidad(long medico, String fechaTexto) {
        LocalDate fecha = Validar.fecha(fechaTexto, "fecha", true);
        Map<String, Object> m = repositorio.obtenerMedico(Db.p("id", medico), "El médico no existe.");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("fecha", fecha);
        r.put("activo", Validar.bool(m, "activo"));
        List<LocalDateTime[]> libres = new ArrayList<>();
        if (Validar.bool(m, "activo")) {
            List<LocalDateTime[]> ocupados = new ArrayList<>();
            // Las citas ocupan su horario más el margen de 15 minutos antes y después; las ausencias, solo su rango.
            for (Map<String, Object> o : repositorio.listarCitaEnDisponibilidad(Db.p("m", medico, "d", fecha.atStartOfDay(), "h", fecha.plusDays(1).atStartOfDay()))) {
                ocupados.add(new LocalDateTime[]{(LocalDateTime) o.get("i"), (LocalDateTime) o.get("f")});
            }
            LocalDateTime ahora = proximoInicio(Tiempo.ahora());
            for (Map<String, Object> franja : franjas(medico, fecha)) {
                LocalDateTime inicio = fecha.atTime(LocalTime.parse(Validar.cadena(franja, "inicio")));
                LocalDateTime fin = fecha.atTime(LocalTime.parse(Validar.cadena(franja, "fin")));
                if (inicio.isBefore(ahora)) {
                    inicio = ahora;
                }
                libres.addAll(restar(inicio, fin, ocupados));
            }
        }
        r.put("libres", libres.stream().map(i -> Map.of("inicio", i[0], "fin", i[1])).toList());
        return r;
    }

    /**
     * Primer inicio que se puede ofrecer hoy: al menos 5 minutos después de ahora, redondeado al siguiente
     * cuarto de hora (20:28 → 20:45). Así la hora sugerida sigue siendo futura cuando el usuario guarda la cita.
     *
     * @param ahora fecha y hora actual de Bogotá.
     * @return inicio redondeado.
     */
    static LocalDateTime proximoInicio(LocalDateTime ahora) {
        LocalDateTime base = ahora.plusMinutes(5).withSecond(0).withNano(0);
        return base.plusMinutes((15 - base.getMinute() % 15) % 15);
    }

    /** Resta intervalos ocupados de un intervalo libre (algoritmo de barrido). */
    static List<LocalDateTime[]> restar(LocalDateTime inicio, LocalDateTime fin, List<LocalDateTime[]> ocupados) {
        List<LocalDateTime[]> resultado = new ArrayList<>();
        List<LocalDateTime[]> orden = new ArrayList<>(ocupados);
        orden.sort((a, b) -> a[0].compareTo(b[0]));
        LocalDateTime cursor = inicio;
        for (LocalDateTime[] o : orden) {
            if (!o[1].isAfter(cursor) || !o[0].isBefore(fin)) {
                continue;
            }
            if (o[0].isAfter(cursor)) {
                resultado.add(new LocalDateTime[]{cursor, o[0]});
            }
            if (o[1].isAfter(cursor)) {
                cursor = o[1];
            }
        }
        if (cursor.isBefore(fin)) {
            resultado.add(new LocalDateTime[]{cursor, fin});
        }
        resultado.removeIf(i -> Duration.between(i[0], i[1]).toMinutes() < 10);
        return resultado;
    }

    /**
     * Horarios semanales y ausencias del médico.
     *
     * @param medico identificador del médico.
     * @return configuración de agenda.
     */
    public Map<String, Object> configuracion(long medico) {
        Map<String, Object> m = repositorio.obtenerMedico(Db.p("id", medico), "El médico no existe.");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("activo", Validar.bool(m, "activo"));
        r.put("horarios", repositorio.listarMedicoHorario(Db.p("m", medico)));
        r.put("ausencias", repositorio.listarMedicoAusencia(Db.p("m", medico, "ahora", Tiempo.ahora())));
        return r;
    }

    /**
     * Reemplaza el horario semanal del médico validando que las franjas no se crucen.
     *
     * @param medico identificador del médico.
     * @param franjas franjas por día de la semana.
     * @return configuración actualizada.
     */
    @Transactional
    public Map<String, Object> guardarHorarios(long medico, List<CitaApi.Franja> franjas) {
        repositorio.obtenerMedicoEnGuardarHorarios(Db.p("id", medico), "El médico no existe.");
        if (franjas == null) {
            throw new BadRequestException("Debes enviar las franjas del horario.");
        }
        List<LocalTime[]> porDia = new ArrayList<>();
        List<Integer> dias = new ArrayList<>();
        for (CitaApi.Franja f : franjas) {
            if (f == null || f.dia() == null || f.dia() < 1 || f.dia() > 7) {
                throw new BadRequestException("Cada franja debe tener un día entre 1 (lunes) y 7 (domingo).");
            }
            LocalTime inicio = hora(f.inicio());
            LocalTime fin = hora(f.fin());
            if (!fin.isAfter(inicio)) {
                throw new BadRequestException("La hora final de cada franja debe ser posterior a la inicial.");
            }
            for (int i = 0; i < porDia.size(); i++) {
                if (dias.get(i).equals(f.dia()) && inicio.isBefore(porDia.get(i)[1]) && fin.isAfter(porDia.get(i)[0])) {
                    throw new BadRequestException("Hay franjas superpuestas el mismo día.");
                }
            }
            porDia.add(new LocalTime[]{inicio, fin});
            dias.add(f.dia());
        }
        repositorio.eliminarMedicoHorario(Db.p("m", medico));
        for (int i = 0; i < porDia.size(); i++) {
            repositorio.insertarMedicoHorario(Db.p("m", medico, "d", dias.get(i), "i", porDia.get(i)[0], "f", porDia.get(i)[1]));
        }
        return configuracion(medico);
    }

    /**
     * Registra una ausencia (vacaciones, incapacidad) del médico.
     *
     * @param medico identificador del médico.
     * @param a fechas y motivo.
     * @return configuración actualizada.
     */
    @Transactional
    public Map<String, Object> registrarAusencia(long medico, CitaApi.AusenciaRq a) {
        repositorio.obtenerMedicoEnRegistrarAusencia(Db.p("id", medico), "El médico no existe.");
        if (a == null) {
            throw new BadRequestException("Los datos de la ausencia son obligatorios.");
        }
        LocalDateTime inicio = Validar.fechaHora(a.inicio(), "inicio");
        LocalDateTime fin = Validar.fechaHora(a.fin(), "fin");
        String motivo = Validar.texto(a.motivo(), "motivo", 255);
        if (!fin.isAfter(inicio)) {
            throw new BadRequestException("El fin de la ausencia debe ser posterior al inicio.");
        }
        if (fin.isBefore(Tiempo.ahora())) {
            throw new BadRequestException("No se registran ausencias en el pasado.");
        }
        long conflictos = repositorio.contarCita(Db.p("m", medico, "i", inicio, "f", fin));
        if (conflictos > 0) {
            throw new ConflictException("Hay " + conflictos + " cita(s) reservada(s) en ese periodo. "
                    + "Reprográmalas o cancélalas antes de registrar la ausencia.");
        }
        if (repositorio.existeMedicoAusencia(Db.p("m", medico, "i", inicio, "f", fin))) {
            throw new ConflictException("Ya existe una ausencia registrada que se cruza con ese periodo.");
        }
        repositorio.insertarMedicoAusencia(Db.p("m", medico, "i", inicio, "f", fin, "motivo", motivo, "u", SesionActual.usuarioId(),
                "ahora", Tiempo.ahora()));
        return configuracion(medico);
    }

    /**
     * Elimina una ausencia que aún no ha terminado (por ejemplo, vacaciones canceladas).
     * Las ausencias ya cumplidas no se eliminan: forman parte del historial de la agenda.
     *
     * @param medico identificador del médico dueño de la ausencia.
     * @param ausencia identificador de la ausencia.
     * @return configuración actualizada.
     */
    @Transactional
    public Map<String, Object> eliminarAusencia(long medico, long ausencia) {
        Map<String, Object> a = repositorio.obtenerMedicoAusencia(Db.p("id", ausencia, "m", medico), "La ausencia no existe para este médico.");
        LocalDateTime fin = (LocalDateTime) a.get("fin");
        if (fin != null && !fin.isAfter(Tiempo.ahora())) {
            throw new ConflictException("La ausencia ya terminó; se conserva en el historial.");
        }
        repositorio.eliminarMedicoAusencia(Db.p("id", ausencia));
        return configuracion(medico);
    }

    /**
     * Ausencias de un médico que se cruzan con un rango (para pintarlas en el calendario).
     *
     * @param medico identificador del médico.
     * @param desde fecha inicial AAAA-MM-DD.
     * @param hasta fecha final AAAA-MM-DD.
     * @return ausencias con inicio, fin y motivo.
     */
    public List<Map<String, Object>> ausenciasEnRango(long medico, String desde, String hasta) {
        LocalDate d = Validar.fecha(desde, "desde", true);
        LocalDate h = Validar.fecha(hasta, "hasta", true);
        return repositorio.listarMedicoAusenciaEnAusenciasEnRango(Db.p("m", medico, "d", d.atStartOfDay(), "h", h.plusDays(1).atStartOfDay()));
    }

    /**
     * Activa o inactiva al médico para recibir citas.
     *
     * @param medico identificador del médico.
     * @param activo nuevo estado.
     */
    @Transactional
    public void actividad(long medico, Boolean activo) {
        Validar.requerido(activo, "activo");
        repositorio.obtenerMedicoEnActividad(Db.p("id", medico), "El médico no existe.");
        repositorio.actualizarMedico(Db.p("a", activo, "id", medico));
    }

    /**
     * Cambia el estado de una cita validando la transición permitida.
     *
     * @param id identificador de la cita.
     * @param cambio nuevo estado y motivo.
     * @return cita actualizada.
     */
    @Transactional
    public Map<String, Object> cambiar(long id, CitaApi.CambioRq cambio) {
        Map<String, Object> actual = bloquear(id);
        SesionUsuario s = SesionActual.obtener();
        verificarAcceso(actual, s);
        if (cambio == null || (cambio.estado() == null && cambio.inicio() == null && cambio.fin() == null)) {
            throw new BadRequestException("Indica el nuevo estado o el nuevo horario.");
        }
        String estadoActual = Validar.cadena(actual, "estado");
        String estado = cambio.estado() == null ? estadoActual : cambio.estado().trim().toLowerCase();
        if (!ESTADOS.contains(estado)) {
            throw new BadRequestException("Estado de cita no válido.");
        }
        validarTransicion(estadoActual, estado, s);
        LocalDateTime inicio = (LocalDateTime) actual.get("fecha_hora");
        LocalDateTime fin = (LocalDateTime) actual.get("fecha_fin");
        validarAtendida(estadoActual, estado, inicio);
        if (cambio.inicio() != null || cambio.fin() != null) {
            if (!RESERVADAS.contains(estadoActual) || "cancelada".equals(estado)) {
                throw new ConflictException("Solo se reprograman citas pendientes o confirmadas.");
            }
            LocalDateTime nuevoInicio = cambio.inicio() == null ? inicio : Validar.fechaHora(cambio.inicio(), "inicio");
            LocalDateTime nuevoFin = cambio.fin() == null ? fin : Validar.fechaHora(cambio.fin(), "fin");
            validarProgramacion(Validar.largo(actual, "medico_id"), Validar.largo(actual, "mascota_id"),
                    nuevoInicio, nuevoFin, id);
            inicio = nuevoInicio;
            fin = nuevoFin;
        }
        repositorio.actualizarCitaEnCambiar(Db.p("e", estado, "i", inicio, "f", fin, "ahora", Tiempo.ahora(), "id", id));
        registrarRevision(id, actual, estado, inicio, fin);
        return repositorio.buscarCita(Db.p("id", id));
    }

    /**
     * Historial de cambios (revisiones) de una cita.
     *
     * @param id identificador de la cita.
     * @return revisiones en orden cronológico.
     */
    public List<Map<String, Object>> historial(long id) {
        Map<String, Object> cita = repositorio.obtenerCitaEnHistorial(Db.p("id", id), "La cita no existe.");
        verificarAcceso(cita, SesionActual.obtener());
        List<Map<String, Object>> filas = repositorio.listarCitaRevision(Db.p("id", id));
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> f : filas) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", f.get("id"));
            r.put("usuarioId", f.get("usuario_id"));
            r.put("fecha", f.get("fecha"));
            if (f.get("estado_anterior") == null) {
                r.put("anterior", null);
            } else {
                Map<String, Object> anterior = new LinkedHashMap<>();
                anterior.put("estado", f.get("estado_anterior"));
                anterior.put("inicio", f.get("inicio_anterior"));
                anterior.put("fin", f.get("fin_anterior"));
                r.put("anterior", anterior);
            }
            Map<String, Object> actualMapa = new LinkedHashMap<>();
            actualMapa.put("estado", f.get("estado_nuevo"));
            actualMapa.put("inicio", f.get("inicio_nuevo"));
            actualMapa.put("fin", f.get("fin_nuevo"));
            r.put("actual", actualMapa);
            resultado.add(r);
        }
        return resultado;
    }

    /**
     * Historial paginado de citas de una mascota.
     *
     * @param mascota identificador de la mascota.
     * @param paginaRq página solicitada.
     * @return página de citas y total.
     */
    public Map<String, Object> historialMascota(int mascota, Integer paginaRq) {
        repositorio.obtenerMascota(Db.p("m", mascota), "La mascota no existe.");
        int pagina = Validar.pagina(paginaRq);
        int tamano = 10;
        List<Map<String, Object>> citas = repositorio.listarCitaEnHistorialMascota(Db.p("m", mascota, "limite", tamano + 1, "desde", pagina * tamano));
        boolean hayMas = citas.size() > tamano;
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("citas", hayMas ? citas.subList(0, tamano) : citas);
        r.put("hayMas", hayMas);
        return r;
    }

    // ------------------------------------------------------------------ REGLAS

    /**
     * Valida y bloquea (SELECT ... FOR UPDATE) médico y mascota para serializar reservas concurrentes,
     * de modo que dos solicitudes simultáneas no puedan reservar el mismo horario.
     */
    void validarProgramacion(long medico, long mascota, LocalDateTime inicio, LocalDateTime fin, Long excluir) {
        if (!fin.isAfter(inicio)) {
            throw new BadRequestException("La hora de finalización debe ser posterior a la de inicio.");
        }
        if (Duration.between(inicio, fin).toMinutes() > MINUTOS_MAXIMOS) {
            throw new BadRequestException("Una cita no puede durar más de 4 horas.");
        }
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new BadRequestException("La cita debe iniciar y terminar el mismo día.");
        }
        if (inicio.isBefore(Tiempo.ahora())) {
            throw new BadRequestException("No se pueden programar citas en una fecha u hora pasada.");
        }
        Map<String, Object> m = repositorio.obtenerMedicoEnValidarProgramacion(Db.p("id", medico), "El médico no existe.");
        if (!Validar.bool(m, "activo")) {
            throw new ConflictException("No se pueden asignar citas a un médico inactivo.");
        }
        repositorio.obtenerMascotaEnValidarProgramacion(Db.p("id", mascota), "La mascota no existe.");
        boolean enHorario = franjas(medico, inicio.toLocalDate()).stream().anyMatch(f ->
                !inicio.toLocalTime().isBefore(LocalTime.parse(Validar.cadena(f, "inicio")))
                        && !fin.toLocalTime().isAfter(LocalTime.parse(Validar.cadena(f, "fin"))));
        if (!enHorario) {
            throw new ConflictException("El horario solicitado está fuera de la disponibilidad del veterinario.");
        }
        if (repositorio.existeMedicoAusencia(Db.p("m", medico, "i", inicio, "f", fin))) {
            throw new ConflictException("El veterinario tiene una ausencia registrada en ese horario.");
        }
        // Regla: el veterinario no puede tener otra cita a menos de 15 minutos antes o después.
        if (repositorio.existeCita(Db.p("m", medico, "i", inicio.minusMinutes(MARGEN_MINUTOS), "f", fin.plusMinutes(MARGEN_MINUTOS),
                        "excluir", excluir))) {
            throw new ConflictException("El veterinario ya tiene una cita en ese horario o a menos de "
                    + MARGEN_MINUTOS + " minutos. Elige otra hora.");
        }
        if (repositorio.existeCitaEnValidarProgramacion(Db.p("m", mascota, "i", inicio, "f", fin, "excluir", excluir))) {
            throw new ConflictException("La mascota ya tiene otra cita en ese horario.");
        }
    }

    /** Transiciones permitidas entre estados de la cita. */
    static void validarTransicion(String actual, String nuevo, SesionUsuario s) {
        if (actual.equals(nuevo)) {
            return;
        }
        boolean valida = switch (actual) {
            case "pendiente", "programada" -> Set.of("pendiente", "programada", "confirmada", "cancelada").contains(nuevo)
                    || ("atendida".equals(nuevo) && s.esAdmin());
            case "confirmada" -> Set.of("pendiente", "programada", "cancelada").contains(nuevo)
                    || ("atendida".equals(nuevo) && s.esAdmin());
            // Una cita cancelada solo vuelve a estar activa con la operación explícita "reactivar".
            case "atendida" -> "finalizada".equals(nuevo) && s.es(Perfiles.ADMINISTRADOR, Perfiles.VETERINARIO, Perfiles.SECRETARIA);
            default -> false;
        };
        if (!valida) {
            throw new ConflictException("No se permite cambiar una cita de " + actual + " a " + nuevo + ".");
        }
    }

    /**
     * Una cita solo puede marcarse como atendida cuando ya ocurrió (su hora de inicio no es futura).
     *
     * @param actual estado actual.
     * @param nuevo  estado solicitado.
     * @param inicio hora de inicio de la cita.
     */
    private static void validarAtendida(String actual, String nuevo, LocalDateTime inicio) {
        if ("atendida".equals(nuevo) && !"atendida".equals(actual) && inicio.isAfter(Tiempo.ahora())) {
            throw new ConflictException("No se puede marcar como atendida una cita que aún no ha ocurrido.");
        }
    }

    /**
     * Verifica que un médico solo acceda a sus propias citas.
     *
     * @param cita fila de la cita.
     * @param s sesión actual.
     */
    /**
     * Si quien agenda es un médico y no indica médico, la cita queda a su propio nombre.
     *
     * @param d datos recibidos.
     * @return datos con el médico completado cuando corresponde.
     */
    private CitaApi.CitaRq conMedicoPropio(CitaApi.CitaRq d) {
        if (d != null && d.medicoId() == null && SesionActual.obtener().es(Perfiles.VETERINARIO)) {
            return new CitaApi.CitaRq(d.clienteId(), d.mascotaId(), SesionActual.medicoId(), d.fechaHora(), d.fechaFin(),
                    d.estado(), d.motivo());
        }
        return d;
    }

    /**
     * Un médico solo registra o edita citas asignadas a sí mismo.
     *
     * @param medico médico de la cita.
     * @param s sesión actual.
     */
    private void verificarMedicoPropio(long medico, SesionUsuario s) {
        if (s.es(Perfiles.VETERINARIO) && medico != SesionActual.medicoId()) {
            throw new ForbiddenException("Como médico solo puedes agendar o editar citas a tu propio nombre.");
        }
    }

        private void verificarAcceso(Map<String, Object> cita, SesionUsuario s) {
        if (s.es(Perfiles.VETERINARIO) && Validar.largo(cita, "medico_id") != SesionActual.medicoId()) {
            throw new ForbiddenException("Solo puedes gestionar tus propias citas.");
        }
    }

    /**
     * Obtiene la cita con bloqueo de fila (SELECT ... FOR UPDATE).
     *
     * @param id identificador de la cita.
     * @return fila de la cita.
     */
    private Map<String, Object> bloquear(long id) {
        return repositorio.obtenerCitaEnBloquear(Db.p("id", id), "La cita no existe.");
    }

    /**
     * Franjas de horario del médico para el día de la semana de la fecha.
     *
     * @param medico identificador del médico.
     * @param fecha día consultado.
     * @return franjas de atención.
     */
    private List<Map<String, Object>> franjas(long medico, LocalDate fecha) {
        return repositorio.listarMedicoHorarioEnFranjas(Db.p("m", medico, "d", fecha.getDayOfWeek().getValue()));
    }

    /**
     * Guarda una revisión con los valores anteriores de la cita.
     *
     * @param cita identificador de la cita.
     * @param anterior fila antes del cambio.
     * @param estado nuevo estado.
     * @param inicio nuevo inicio.
     * @param fin nuevo fin.
     */
    void registrarRevision(long cita, Map<String, Object> anterior, String estado, LocalDateTime inicio, LocalDateTime fin) {
        repositorio.insertarCitaRevision(Db.p("cita", cita, "usuario", SesionActual.usuarioId(), "ahora", Tiempo.ahora(),
                "ea", anterior == null ? null : anterior.get("estado"),
                "ia", anterior == null ? null : anterior.get("fecha_hora"),
                "fa", anterior == null ? null : anterior.get("fecha_fin"),
                "en", estado, "in", inicio, "fn", fin));
    }

    /**
     * Convierte un texto HH:MM en hora.
     *
     * @param valor texto recibido.
     * @return hora.
     */
    private static LocalTime hora(String valor) {
        try {
            return LocalTime.parse(valor.trim().length() == 5 ? valor.trim() + ":00" : valor.trim());
        } catch (Exception e) {
            throw new BadRequestException("Las horas deben tener formato HH:MM.");
        }
    }

    /**
     * Valida los datos de la cita en orden cliente → mascota → médico → horario.
     *
     * @param d datos recibidos.
     * @return datos validados.
     */
    private Datos validarDatos(CitaApi.CitaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la cita son obligatorios.");
        }
        Validar.requerido(d.clienteId(), "propietario");
        Validar.requerido(d.mascotaId(), "mascota");
        Validar.requerido(d.medicoId(), "médico");
        // Orden de validación: cliente, mascota, pertenencia y veterinario (mensajes claros para cada caso).
        Map<String, Object> cliente = repositorio.obtenerCliente(Db.p("c", d.clienteId()), "El cliente no existe.");
        if (!Validar.bool(cliente, "activo")) {
            throw new BadRequestException("El propietario está archivado.");
        }
        Map<String, Object> mascota = repositorio.obtenerMascotaEnValidarDatos(Db.p("m", d.mascotaId()), "La mascota no existe.");
        if (Validar.largo(mascota, "cliente_id") != d.clienteId()) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado.");
        }
        if (!Validar.bool(mascota, "activo")) {
            throw new BadRequestException("La mascota está inactiva.");
        }
        repositorio.obtenerMedicoEnValidarDatos(Db.p("m", d.medicoId()), "El veterinario no existe.");
        String estado = d.estado() == null || d.estado().isBlank() ? "pendiente" : d.estado().trim().toLowerCase();
        if (!ESTADOS.contains(estado)) {
            throw new BadRequestException("Estado de cita no válido.");
        }
        LocalDateTime inicio = Validar.fechaHora(d.fechaHora(), "fecha y hora de inicio");
        LocalDateTime fin = d.fechaFin() == null || d.fechaFin().isBlank() ? inicio.plusMinutes(30)
                : Validar.fechaHora(d.fechaFin(), "fecha y hora de finalización");
        return new Datos(d.clienteId(), d.mascotaId(), d.medicoId(), inicio, fin, estado,
                Validar.texto(d.motivo(), "motivo", 1000));
    }

    /** Datos de una cita ya validados. */
    private record Datos(long cliente, long mascota, long medico, LocalDateTime inicio, LocalDateTime fin,
                         String estado, String motivo) {
    }
}
