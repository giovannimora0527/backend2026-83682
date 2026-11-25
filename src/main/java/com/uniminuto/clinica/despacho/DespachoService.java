package com.uniminuto.clinica.despacho;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.PdfDocumento;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.inventario.InventarioService;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Requerimiento 5: despacho y dispensación de medicamentos (perfil DESPACHO_MEDICO).
 *
 * <p>Flujo: fórmula CONFIRMADA → PENDIENTE_DESPACHO → (preparación: selección de lotes FEFO) EN_PREPARACION →
 * confirmación de entrega → DESPACHO_PARCIAL o DESPACHADO. También PENDIENTE_DISPONIBILIDAD y CANCELADO.</p>
 *
 * <p>Reglas: el inventario solo se descuenta al confirmar la entrega, dentro de una transacción; no se
 * dispensan lotes vencidos ni cantidades superiores a las existencias o a lo pendiente; cada salida queda
 * relacionada con el despacho y la fórmula; una preparación no puede confirmarse dos veces (restricción
 * UNIQUE); las entregas confirmadas no se eliminan: se revierten con un procedimiento trazable.</p>
 */
@Service
public class DespachoService {

    /** Estados de despacho de una fórmula. */
    static final Set<String> ESTADOS = Set.of("PENDIENTE_DESPACHO", "EN_PREPARACION", "DESPACHO_PARCIAL", "DESPACHADO",
            "PENDIENTE_DISPONIBILIDAD", "CANCELADO");
    /** Registros por página. */
    private static final int TAMANO = 50;

    /** Consulta base de fórmulas para despacho. */
    private static final String FORMULA = """
            SELECT f.id, COALESCE(f.fecha_confirmacion, f.fecha_creacion) AS fecha,
                   CONCAT(md.nombres, ' ', md.apellidos) AS veterinario,
                   CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, m.nombre_mascota AS mascota,
                   f.estado_despacho AS estado, f.cita_id AS cita
            FROM formula_clinica f
            JOIN medico md ON md.id = f.medico_id
            JOIN mascota m ON m.mascota_id = f.mascota_id
            JOIN cita c ON c.id = f.cita_id
            JOIN cliente cl ON cl.cliente_id = c.cliente_id
            """;

    /** Acceso a datos. */
    private final Db db;
    /** Movimientos de inventario. */
    private final InventarioService inventario;

    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /**
     * @param db acceso a datos.
     * @param inventario servicio de inventario.
     * @param auditoria log de operaciones sensibles.
     */
    public DespachoService(Db db, InventarioService inventario, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.db = db;
        this.inventario = inventario;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ Consultas

    /**
     * Busca fórmulas para despacho con filtros.
     *
     * @param id número de fórmula.
     * @param propietario nombre o documento del propietario.
     * @param mascota nombre de la mascota.
     * @param fecha fecha de emisión.
     * @param estado estado de despacho.
     * @param pagina página.
     * @return fórmulas.
     */
    public List<Map<String, Object>> formulas(String id, String propietario, String mascota, String fecha, String estado,
                                              Integer pagina) {
        Long formulaId = Validar.id(id, "número de fórmula");
        LocalDate dia = Validar.fecha(fecha, "fecha", false);
        String est = estado == null || estado.isBlank() ? null : estado.trim().toUpperCase();
        if (est != null && !ESTADOS.contains(est)) {
            throw new BadRequestException("Estado de despacho no válido.");
        }
        return db.lista(FORMULA + """
                WHERE f.estado = 'CONFIRMADA'
                  AND (:id IS NULL OR f.id = :id)
                  AND (:propietario IS NULL OR CONCAT(cl.nombres, ' ', cl.apellidos) LIKE :propietario
                       OR cl.numero_documento = :documento)
                  AND (:mascota IS NULL OR m.nombre_mascota LIKE :mascota)
                  AND (:desde IS NULL OR (COALESCE(f.fecha_confirmacion, f.fecha_creacion) >= :desde
                       AND COALESCE(f.fecha_confirmacion, f.fecha_creacion) < :hasta))
                  AND (:estado IS NULL OR f.estado_despacho = :estado)
                ORDER BY FIELD(f.estado_despacho, 'EN_PREPARACION', 'PENDIENTE_DESPACHO', 'DESPACHO_PARCIAL',
                               'PENDIENTE_DISPONIBILIDAD', 'DESPACHADO', 'CANCELADO'), fecha DESC, f.id DESC
                LIMIT :limite OFFSET :desplazamiento
                """, Db.p("id", formulaId, "propietario", like(propietario),
                "documento", propietario == null ? null : propietario.trim(), "mascota", like(mascota),
                "desde", dia == null ? null : dia.atStartOfDay(), "hasta", dia == null ? null : dia.plusDays(1).atStartOfDay(),
                "estado", est, "limite", TAMANO, "desplazamiento", Validar.pagina(pagina) * TAMANO));
    }

    /**
     * Detalle de la fórmula con líneas, lotes vigentes (FEFO) y entregas.
     *
     * @param formulaId identificador de la fórmula.
     * @return detalle.
     */
    public Map<String, Object> detalle(long formulaId) {
        Map<String, Object> formula = db.requerido(FORMULA + " WHERE f.id = :id AND f.estado = 'CONFIRMADA'",
                Db.p("id", formulaId), "La fórmula no existe o no está confirmada.");
        LocalDate hoy = Tiempo.hoy();
        List<Map<String, Object>> medicamentos = new ArrayList<>();
        for (Map<String, Object> d : lineas(formulaId)) {
            int prescrita = Validar.numero(d, "prescrita");
            int entregada = Validar.numero(d, "entregada");
            Map<String, Object> prescripcion = new LinkedHashMap<>();
            prescripcion.put("detalle", d.get("detalle"));
            prescripcion.put("medicamento", d.get("medicamento"));
            prescripcion.put("nombre", d.get("nombre"));
            prescripcion.put("presentacion", d.get("presentacion"));
            prescripcion.put("dosis", d.get("dosis"));
            prescripcion.put("frecuencia", d.get("frecuencia"));
            prescripcion.put("duracion", d.get("duracion"));
            prescripcion.put("via", d.get("via"));
            prescripcion.put("indicaciones", d.get("indicaciones"));
            prescripcion.put("prescrita", prescrita);
            prescripcion.put("entregada", entregada);
            prescripcion.put("pendiente", Math.max(0, prescrita - entregada));
            List<Map<String, Object>> lotes = lotesVigentes(Validar.largo(d, "medicamento"), hoy);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("prescripcion", prescripcion);
            m.put("lotes", lotes);
            m.put("disponible", lotes.stream().mapToInt(l -> Validar.numero(l, "cantidad")).sum());
            m.put("vencidas", db.contar("""
                    SELECT COALESCE(SUM(cantidad), 0) FROM medicamento_lote
                    WHERE medicamento_id = :m AND cantidad > 0 AND fecha_vence < :hoy
                    """, Db.p("m", d.get("medicamento"), "hoy", hoy)));
            medicamentos.add(m);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("formula", formula);
        r.put("medicamentos", medicamentos);
        r.put("preparacion", preparacionVigente(formulaId));
        r.put("entregas", entregas("WHERE d.formula_id = :f", Db.p("f", formulaId)));
        r.put("historial", db.lista("""
                SELECT h.estado_anterior AS anterior, h.estado_nuevo AS nuevo, u.username AS responsable, h.fecha, h.motivo
                FROM despacho_estado_historial h JOIN usuario u ON u.id = h.usuario_id
                WHERE h.formula_id = :f ORDER BY h.fecha DESC, h.id DESC
                """, Db.p("f", formulaId)));
        return r;
    }

    /**
     * Historial de entregas.
     *
     * @param desde fecha inicial.
     * @param hasta fecha final.
     * @param medicamento texto del medicamento.
     * @param pagina página.
     * @return entregas.
     */
    public List<Map<String, Object>> historial(String desde, String hasta, String medicamento, Integer pagina) {
        LocalDate d = Validar.fecha(desde, "desde", false);
        LocalDate h = Validar.fecha(hasta, "hasta", false);
        Long med = Validar.id(medicamento, "medicamento");
        Map<String, Object> p = Db.p("desde", d == null ? null : d.atStartOfDay(),
                "hasta", h == null ? null : h.plusDays(1).atStartOfDay(), "med", med,
                "limite", TAMANO, "desplazamiento", Validar.pagina(pagina) * TAMANO);
        return entregas("""
                WHERE (:desde IS NULL OR d.fecha >= :desde) AND (:hasta IS NULL OR d.fecha < :hasta)
                  AND (:med IS NULL OR EXISTS (SELECT 1 FROM despacho_detalle x WHERE x.despacho_id = d.id AND x.medicamento_id = :med))
                """, p, "LIMIT :limite OFFSET :desplazamiento");
    }

    // ------------------------------------------------------------------ Preparación

    /**
     * Prepara un despacho validando lotes, vencimientos y existencias.
     *
     * @param formulaId identificador de la fórmula.
     * @param rq selección de lotes.
     * @return plan de preparación.
     */
    @Transactional
    public Map<String, Object> preparar(long formulaId, DespachoApi.PrepararRq rq) {
        if (rq == null || rq.clave() == null || rq.clave().isBlank() || rq.clave().length() > 64) {
            throw new BadRequestException("La solicitud de preparación no tiene una clave válida.");
        }
        Map<String, Object> existente = db.uno("SELECT id, formula_id FROM despacho_preparacion WHERE clave = :c",
                Db.p("c", rq.clave()));
        if (existente != null) {
            if (Validar.largo(existente, "formula_id") != formulaId) {
                throw new ConflictException("La clave de la solicitud ya fue usada para otra fórmula.");
            }
            return plan(Validar.largo(existente, "id"));
        }
        Map<String, Object> formula = bloquearFormula(formulaId);
        String estado = Validar.cadena(formula, "estado_despacho");
        if (Set.of("DESPACHADO", "CANCELADO").contains(estado)) {
            throw new ConflictException("La fórmula está " + estado.toLowerCase().replace('_', ' ') + ".");
        }
        if (rq.lotes() == null || rq.lotes().isEmpty()) {
            throw new BadRequestException("Selecciona al menos un lote con cantidad a entregar.");
        }
        LocalDate hoy = Tiempo.hoy();
        Map<Long, Map<String, Object>> lineas = new HashMap<>();
        for (Map<String, Object> l : lineas(formulaId)) {
            lineas.put(Validar.largo(l, "medicamento"), l);
        }
        Map<Long, Map<Long, Integer>> seleccion = new LinkedHashMap<>();
        Set<Long> lotesVistos = new HashSet<>();
        for (DespachoApi.SeleccionLote s : rq.lotes()) {
            if (s == null || s.medicamento() == null || s.lote() == null || s.cantidad() == null || s.cantidad() <= 0) {
                throw new BadRequestException("Cada lote seleccionado debe tener medicamento, lote y cantidad mayor que cero.");
            }
            if (!lineas.containsKey(s.medicamento())) {
                throw new BadRequestException("El medicamento " + s.medicamento() + " no está en la fórmula.");
            }
            if (!lotesVistos.add(s.lote())) {
                throw new BadRequestException("Un lote no puede seleccionarse dos veces.");
            }
            seleccion.computeIfAbsent(s.medicamento(), k -> new LinkedHashMap<>()).put(s.lote(), s.cantidad());
        }
        for (Map.Entry<Long, Map<Long, Integer>> e : seleccion.entrySet()) {
            Map<String, Object> linea = lineas.get(e.getKey());
            validarSeleccion(Validar.cadena(linea, "nombre"),
                    Validar.numero(linea, "prescrita") - Validar.numero(linea, "entregada"),
                    lotesVigentes(e.getKey(), hoy), e.getValue(), e.getKey());
        }
        db.ejecutar("UPDATE despacho_preparacion SET estado = 'DESCARTADA' WHERE formula_id = :f AND estado = 'VIGENTE'",
                Db.p("f", formulaId));
        long plan = db.insertar("""
                INSERT INTO despacho_preparacion (formula_id, clave, usuario_id, fecha, estado)
                VALUES (:f, :c, :u, :ahora, 'VIGENTE')
                """, Db.p("f", formulaId, "c", rq.clave(), "u", SesionActual.usuarioId(), "ahora", Tiempo.ahora()));
        for (Map.Entry<Long, Map<Long, Integer>> e : seleccion.entrySet()) {
            for (Map.Entry<Long, Integer> l : e.getValue().entrySet()) {
                db.insertar("""
                        INSERT INTO despacho_preparacion_lote (preparacion_id, detalle_id, lote_id, cantidad)
                        VALUES (:p, :d, :l, :c)
                        """, Db.p("p", plan, "d", lineas.get(e.getKey()).get("detalle"), "l", l.getKey(), "c", l.getValue()));
            }
        }
        cambiarEstado(formulaId, estado, "EN_PREPARACION", "Selección de lotes para la entrega");
        return plan(plan);
    }

    /**
     * Valida la selección de lotes de un medicamento: lotes vigentes del medicamento, cantidades dentro de
     * las existencias, total no superior a lo pendiente y prioridad FEFO (no se puede tomar de un lote si
     * queda saldo sin seleccionar en un lote que vence antes).
     */
    static void validarSeleccion(String nombre, int pendiente, List<Map<String, Object>> vigentes,
                                 Map<Long, Integer> seleccion, long medicamento) {
        int total = 0;
        Map<Long, Map<String, Object>> porId = new HashMap<>();
        for (Map<String, Object> l : vigentes) {
            porId.put(Validar.largo(l, "id"), l);
        }
        for (Map.Entry<Long, Integer> s : seleccion.entrySet()) {
            Map<String, Object> lote = porId.get(s.getKey());
            if (lote == null) {
                throw new ConflictException("El lote " + s.getKey() + " no está disponible para " + nombre
                        + " (no existe, está vencido o agotado).");
            }
            if (s.getValue() > Validar.numero(lote, "cantidad")) {
                throw new ConflictException("El lote " + lote.get("numero") + " de " + nombre + " solo tiene "
                        + lote.get("cantidad") + " unidades.");
            }
            total += s.getValue();
        }
        if (total > pendiente) {
            throw new ConflictException("Para " + nombre + " se seleccionaron " + total + " unidades y solo hay "
                    + pendiente + " pendientes por entregar.");
        }
        boolean posterior = false;
        for (int i = vigentes.size() - 1; i >= 0; i--) {
            Map<String, Object> l = vigentes.get(i);
            int tomado = seleccion.getOrDefault(Validar.largo(l, "id"), 0);
            if (posterior && tomado < Validar.numero(l, "cantidad")) {
                throw new ConflictException("Prioridad FEFO: para " + nombre + " primero debes entregar las unidades del lote "
                        + l.get("numero") + " (vence " + (l.get("fechaVence") == null ? "sin fecha" : l.get("fechaVence")) + ").");
            }
            if (tomado > 0) {
                posterior = true;
            }
        }
    }

    // ------------------------------------------------------------------ Confirmación

    /**
     * Confirma el plan, descuenta inventario y actualiza el estado de la fórmula.
     *
     * @param planId identificador del plan.
     * @return entrega.
     */
    @Transactional
    public Map<String, Object> confirmar(long planId) {
        Map<String, Object> plan = db.requerido("SELECT * FROM despacho_preparacion WHERE id = :id FOR UPDATE",
                Db.p("id", planId), "La preparación no existe.");
        if (!"VIGENTE".equals(plan.get("estado"))) {
            throw new ConflictException("CONFIRMADA".equals(plan.get("estado"))
                    ? "Esta preparación ya fue confirmada; no se descuenta dos veces el inventario."
                    : "La preparación fue reemplazada o descartada. Actualiza el detalle.");
        }
        long formulaId = Validar.largo(plan, "formula_id");
        Map<String, Object> formula = bloquearFormula(formulaId);
        String estado = Validar.cadena(formula, "estado_despacho");
        if (!"EN_PREPARACION".equals(estado)) {
            throw new ConflictException("La fórmula no está en preparación (estado actual: " + estado + ").");
        }
        LocalDate hoy = Tiempo.hoy();
        LocalDateTime ahora = Tiempo.ahora();
        long despacho = db.insertar("""
                INSERT INTO despacho (formula_id, preparacion_id, usuario_id, fecha, estado)
                VALUES (:f, :p, :u, :ahora, 'EN_PREPARACION')
                """, Db.p("f", formulaId, "p", planId, "u", SesionActual.usuarioId(), "ahora", ahora));
        Map<Long, Integer> pendientes = new HashMap<>();
        for (Map<String, Object> l : lineas(formulaId)) {
            pendientes.put(Validar.largo(l, "detalle"), Validar.numero(l, "prescrita") - Validar.numero(l, "entregada"));
        }
        for (Map<String, Object> s : db.lista("""
                SELECT pl.detalle_id, pl.lote_id, pl.cantidad, l.numero, l.fecha_vence, l.medicamento_id, m.nombre
                FROM despacho_preparacion_lote pl
                JOIN medicamento_lote l ON l.id = pl.lote_id
                JOIN medicamento m ON m.id = l.medicamento_id
                WHERE pl.preparacion_id = :p ORDER BY l.fecha_vence IS NULL, l.fecha_vence, pl.id
                """, Db.p("p", planId))) {
            LocalDate vence = (LocalDate) s.get("fecha_vence");
            if (vence != null && vence.isBefore(hoy)) {
                throw new ConflictException("El lote " + s.get("numero") + " de " + s.get("nombre")
                        + " venció; no se dispensan medicamentos vencidos. Prepara nuevamente.");
            }
            long detalle = Validar.largo(s, "detalle_id");
            int cantidad = Validar.numero(s, "cantidad");
            int pendiente = pendientes.getOrDefault(detalle, 0);
            if (cantidad > pendiente) {
                throw new ConflictException("La cantidad preparada de " + s.get("nombre") + " supera lo pendiente.");
            }
            pendientes.put(detalle, pendiente - cantidad);
            long movimiento = inventario.mover(Validar.largo(s, "lote_id"), "SALIDA", -cantidad,
                    "Dispensación de fórmula #" + formulaId, "Fórmula " + formulaId + " · Despacho " + despacho,
                    formulaId, despacho, null);
            db.insertar("""
                    INSERT INTO despacho_detalle (despacho_id, detalle_id, medicamento_id, lote_id, cantidad, movimiento_id)
                    VALUES (:d, :det, :m, :l, :c, :mov)
                    """, Db.p("d", despacho, "det", detalle, "m", s.get("medicamento_id"), "l", s.get("lote_id"),
                    "c", cantidad, "mov", movimiento));
        }
        List<String> faltantes = faltantes(formulaId);
        String nuevo = faltantes.isEmpty() ? "DESPACHADO" : "DESPACHO_PARCIAL";
        db.ejecutar("UPDATE despacho SET estado = :e, pendiente = :p WHERE id = :id",
                Db.p("e", nuevo, "p", faltantes.isEmpty() ? null : String.join("\n", faltantes), "id", despacho));
        db.ejecutar("UPDATE despacho_preparacion SET estado = 'CONFIRMADA' WHERE id = :id", Db.p("id", planId));
        cambiarEstado(formulaId, estado, nuevo, "Entrega #" + despacho + " confirmada");
        auditoria.registrar("DESPACHO_CONFIRMADO", "despacho_preparacion", planId, "Entrega confirmada del plan de despacho " + planId + " (descuento de inventario)");
        return entrega(despacho);
    }

    /**
     * Retiene, libera o cancela una fórmula.
     *
     * @param formulaId identificador de la fórmula.
     * @param accion acción solicitada.
     * @param motivoRq motivo.
     * @return fórmula actualizada.
     */
    @Transactional
    public Map<String, Object> cambiar(long formulaId, String accion, String motivoRq) {
        String motivo = Validar.texto(motivoRq, "motivo", 255);
        Map<String, Object> formula = bloquearFormula(formulaId);
        String estado = Validar.cadena(formula, "estado_despacho");
        String nuevo;
        if ("cancelar".equals(accion)) {
            if (Set.of("DESPACHADO", "CANCELADO").contains(estado)) {
                throw new ConflictException("No se puede cancelar un despacho " + estado.toLowerCase() + ".");
            }
            nuevo = "CANCELADO";
        } else if ("disponibilidad".equals(accion)) {
            if (!Set.of("PENDIENTE_DESPACHO", "EN_PREPARACION", "DESPACHO_PARCIAL").contains(estado)) {
                throw new ConflictException("No se puede marcar como pendiente por disponibilidad desde " + estado + ".");
            }
            nuevo = "PENDIENTE_DISPONIBILIDAD";
        } else {
            throw new BadRequestException("Acción no válida.");
        }
        db.ejecutar("UPDATE despacho_preparacion SET estado = 'DESCARTADA' WHERE formula_id = :f AND estado = 'VIGENTE'",
                Db.p("f", formulaId));
        cambiarEstado(formulaId, estado, nuevo, motivo);
        auditoria.registrar("DESPACHO_" + String.valueOf(accion).toUpperCase(), "formula_clinica", formulaId, "Fórmula " + formulaId + ": " + accion + ". Motivo: " + motivoRq);
        return detalle(formulaId);
    }

    /**
     * Reversión controlada: devuelve las unidades a los lotes originales mediante movimientos de ENTRADA
     * enlazados a cada salida. No se eliminan registros. No procede si la entrega ya fue facturada.
     */
    @Transactional
    public Map<String, Object> revertir(long despachoId, String motivoRq) {
        String motivo = Validar.texto(motivoRq, "motivo", 255);
        Map<String, Object> d = db.requerido("SELECT * FROM despacho WHERE id = :id FOR UPDATE", Db.p("id", despachoId),
                "La entrega no existe.");
        if (d.get("fecha_reversion") != null) {
            throw new ConflictException("La entrega ya fue revertida.");
        }
        if (db.existe("""
                SELECT 1 FROM factura_linea fl JOIN factura f ON f.id = fl.factura_id
                JOIN despacho_detalle dd ON dd.movimiento_id = fl.movimiento_id
                WHERE dd.despacho_id = :d AND f.estado <> 'ANULADA'
                """, Db.p("d", despachoId))) {
            throw new ConflictException("La entrega ya fue facturada. Anula la factura antes de revertir la dispensación.");
        }
        long formulaId = Validar.largo(d, "formula_id");
        Map<String, Object> formula = bloquearFormula(formulaId);
        for (Map<String, Object> det : db.lista("SELECT * FROM despacho_detalle WHERE despacho_id = :d", Db.p("d", despachoId))) {
            long mov = inventario.mover(Validar.largo(det, "lote_id"), "ENTRADA", Validar.numero(det, "cantidad"),
                    "Reversión de despacho #" + despachoId + ": " + motivo, "Fórmula " + formulaId + " · Despacho " + despachoId,
                    formulaId, despachoId, null);
            db.ejecutar("UPDATE despacho_detalle SET movimiento_reversion_id = :m WHERE id = :id",
                    Db.p("m", mov, "id", det.get("id")));
        }
        db.ejecutar("""
                UPDATE despacho SET fecha_reversion = :f, usuario_reversion_id = :u, motivo_reversion = :m WHERE id = :id
                """, Db.p("f", Tiempo.ahora(), "u", SesionActual.usuarioId(), "m", motivo, "id", despachoId));
        String estado = Validar.cadena(formula, "estado_despacho");
        if (!"CANCELADO".equals(estado)) {
            long entregado = db.contar("""
                    SELECT COALESCE(SUM(dd.cantidad), 0) FROM despacho_detalle dd JOIN despacho x ON x.id = dd.despacho_id
                    WHERE x.formula_id = :f AND dd.movimiento_reversion_id IS NULL
                    """, Db.p("f", formulaId));
            db.ejecutar("UPDATE despacho_preparacion SET estado = 'DESCARTADA' WHERE formula_id = :f AND estado = 'VIGENTE'",
                    Db.p("f", formulaId));
            cambiarEstado(formulaId, estado, entregado == 0 ? "PENDIENTE_DESPACHO" : "DESPACHO_PARCIAL",
                    "Reversión de la entrega #" + despachoId + ": " + motivo);
        }
        auditoria.registrar("DESPACHO_REVERTIDO", "despacho", despachoId, "Reversión de la entrega " + despachoId + ". Motivo: " + motivoRq);
        return entrega(despachoId);
    }

    /**
     * Comprobante de despacho en PDF.
     *
     * @param despachoId identificador de la entrega.
     * @return PDF.
     */
    public byte[] pdf(long despachoId) {
        Map<String, Object> e = entrega(despachoId);
        Map<String, Object> f = db.requerido(FORMULA + " WHERE f.id = :id", Db.p("id", e.get("formula")),
                "La fórmula no existe.");
        PdfDocumento pdf = new PdfDocumento("Comprobante de despacho N.º " + despachoId);
        pdf.campos(new String[][]{{"Fórmula", "#" + e.get("formula")}, {"Fecha de entrega", texto(e.get("fecha"))},
                {"Propietario", texto(f.get("propietario"))}, {"Mascota", texto(f.get("mascota"))},
                {"Veterinario", texto(f.get("veterinario"))}, {"Responsable", texto(e.get("responsable"))},
                {"Estado", texto(e.get("estado"))}, {"Revertida", e.get("revertida") == null ? "No" : texto(e.get("revertida"))}});
        pdf.seccion("Medicamentos entregados");
        List<String[]> filas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineas = (List<Map<String, Object>>) e.get("lineas");
        for (Map<String, Object> l : lineas) {
            filas.add(new String[]{texto(l.get("nombre")), texto(l.get("numero")), texto(l.get("fechaVence")),
                    texto(l.get("cantidad")), "#" + l.get("movimiento")});
        }
        pdf.tabla(new String[]{"Medicamento", "Lote", "Vence", "Cantidad", "Movimiento"}, filas,
                new float[]{3, 1.5f, 1.3f, 1, 1.2f}, new boolean[]{false, false, false, true, false});
        @SuppressWarnings("unchecked")
        List<String> pendiente = (List<String>) e.get("pendiente");
        if (!pendiente.isEmpty()) {
            pdf.seccion("Pendiente por entregar");
            pdf.parrafo(String.join("\n", pendiente));
        }
        if (e.get("revertida") != null) {
            pdf.nota("Entrega revertida el " + texto(e.get("revertida")) + " por " + e.get("reversor") + ". Motivo: " + e.get("motivo"));
        }
        pdf.parrafo("\n\n\n_______________________________          _______________________________\n"
                + "Entregó (despacho)                                          Recibió (propietario)");
        return pdf.cerrar();
    }

    // ------------------------------------------------------------------ Utilidades

    /**
     * @param formulaId identificador de la fórmula.
     * @return líneas con cantidades prescritas, despachadas y pendientes.
     */
    private List<Map<String, Object>> lineas(long formulaId) {
        return db.lista("""
                SELECT d.id AS detalle, d.medicamento_id AS medicamento, m.nombre, m.presentacion, d.dosis, d.frecuencia,
                       d.duracion, d.via, d.indicaciones, d.cantidad AS prescrita,
                       COALESCE((SELECT SUM(dd.cantidad) FROM despacho_detalle dd
                                 WHERE dd.detalle_id = d.id AND dd.movimiento_reversion_id IS NULL), 0) AS entregada
                FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                WHERE d.formula_id = :f ORDER BY d.id
                """, Db.p("f", formulaId));
    }

    /** Lotes con existencias y no vencidos, en orden FEFO (primero el que vence antes; sin fecha al final). */
    private List<Map<String, Object>> lotesVigentes(long medicamento, LocalDate hoy) {
        return db.lista("""
                SELECT id, numero, fecha_vence AS fechaVence, cantidad FROM medicamento_lote
                WHERE medicamento_id = :m AND cantidad > 0 AND (fecha_vence IS NULL OR fecha_vence >= :hoy)
                ORDER BY fecha_vence IS NULL, fecha_vence, id
                """, Db.p("m", medicamento, "hoy", hoy));
    }

    /**
     * @param formulaId identificador de la fórmula.
     * @return medicamentos con cantidad pendiente.
     */
    private List<String> faltantes(long formulaId) {
        List<String> faltantes = new ArrayList<>();
        for (Map<String, Object> l : lineas(formulaId)) {
            int pendiente = Validar.numero(l, "prescrita") - Validar.numero(l, "entregada");
            if (pendiente > 0) {
                faltantes.add(l.get("nombre") + ": " + pendiente + " unidad(es)");
            }
        }
        return faltantes;
    }

    /**
     * @param formulaId identificador de la fórmula.
     * @return plan de preparación abierto o null.
     */
    private Map<String, Object> preparacionVigente(long formulaId) {
        Long id = db.valor("SELECT id FROM despacho_preparacion WHERE formula_id = :f AND estado = 'VIGENTE'",
                Db.p("f", formulaId), Long.class);
        return id == null ? null : plan(id);
    }

    /**
     * @param id identificador del plan.
     * @return plan con sus líneas.
     */
    private Map<String, Object> plan(long id) {
        Map<String, Object> p = db.requerido("SELECT id, formula_id AS formula, estado, fecha FROM despacho_preparacion WHERE id = :id",
                Db.p("id", id), "La preparación no existe.");
        p.put("lotes", db.lista("""
                SELECT d.medicamento_id AS medicamento, pl.lote_id AS lote, pl.cantidad
                FROM despacho_preparacion_lote pl JOIN formula_clinica_detalle d ON d.id = pl.detalle_id
                WHERE pl.preparacion_id = :p ORDER BY pl.id
                """, Db.p("p", id)));
        return p;
    }

    /**
     * @param id identificador de la entrega.
     * @return entrega.
     */
    private Map<String, Object> entrega(long id) {
        List<Map<String, Object>> lista = entregas("WHERE d.id = :id", Db.p("id", id));
        if (lista.isEmpty()) {
            throw new NotFoundException("La entrega no existe.");
        }
        return lista.get(0);
    }

    /**
     * @param filtro cláusula WHERE.
     * @param params parámetros.
     * @return entregas.
     */
    private List<Map<String, Object>> entregas(String filtro, Map<String, Object> params) {
        return entregas(filtro, params, "");
    }

    /**
     * @param filtro cláusula WHERE.
     * @param params parámetros.
     * @param limite cláusula LIMIT.
     * @return entregas con sus líneas.
     */
    private List<Map<String, Object>> entregas(String filtro, Map<String, Object> params, String limite) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> d : db.lista("""
                SELECT d.id, d.formula_id AS formula, u.username AS responsable, d.fecha, d.estado,
                       d.fecha_reversion AS revertida, r.username AS reversor, d.motivo_reversion AS motivo, d.pendiente,
                       m.nombre_mascota AS mascota, CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario,
                       CONCAT(md.nombres, ' ', md.apellidos) AS veterinario
                FROM despacho d
                JOIN usuario u ON u.id = d.usuario_id
                LEFT JOIN usuario r ON r.id = d.usuario_reversion_id
                JOIN formula_clinica f ON f.id = d.formula_id
                JOIN mascota m ON m.mascota_id = f.mascota_id
                JOIN cita c ON c.id = f.cita_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = f.medico_id
                """ + filtro + " ORDER BY d.fecha DESC, d.id DESC " + limite, params)) {
            String pendiente = Validar.cadena(d, "pendiente");
            d.put("pendiente", pendiente == null ? List.of() : List.of(pendiente.split("\n")));
            d.put("lineas", db.lista("""
                    SELECT m.nombre, l.numero, l.fecha_vence AS fechaVence, dd.cantidad, dd.movimiento_id AS movimiento,
                           dd.movimiento_reversion_id AS movimientoReversion
                    FROM despacho_detalle dd
                    JOIN medicamento m ON m.id = dd.medicamento_id
                    JOIN medicamento_lote l ON l.id = dd.lote_id
                    WHERE dd.despacho_id = :d ORDER BY dd.id
                    """, Db.p("d", d.get("id"))));
            resultado.add(d);
        }
        return resultado;
    }

    /**
     * @param id identificador de la fórmula.
     * @return fórmula bloqueada para actualización.
     */
    private Map<String, Object> bloquearFormula(long id) {
        Map<String, Object> f = db.requerido("SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE", Db.p("id", id),
                "La fórmula no existe.");
        if (!"CONFIRMADA".equals(f.get("estado"))) {
            throw new ConflictException("La fórmula no está confirmada por el médico.");
        }
        return f;
    }

    /**
     * Cambia el estado de despacho y lo registra en el historial.
     *
     * @param formulaId identificador de la fórmula.
     * @param anterior estado anterior.
     * @param nuevo estado nuevo.
     * @param motivo motivo.
     */
    private void cambiarEstado(long formulaId, String anterior, String nuevo, String motivo) {
        if (nuevo.equals(anterior)) {
            return;
        }
        db.ejecutar("UPDATE formula_clinica SET estado_despacho = :e WHERE id = :id", Db.p("e", nuevo, "id", formulaId));
        db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, :a, :n, :u, :ahora, :m)
                """, Db.p("f", formulaId, "a", anterior, "n", nuevo, "u", SesionActual.usuarioId(), "ahora", Tiempo.ahora(),
                "m", motivo.length() > 255 ? motivo.substring(0, 255) : motivo));
    }

    /**
     * @param valor texto de búsqueda.
     * @return patrón LIKE o null.
     */
    private static String like(String valor) {
        return valor == null || valor.isBlank() ? null : "%" + valor.trim().replace("%", "\\%").replace("_", "\\_") + "%";
    }

    /**
     * @param v valor.
     * @return texto o cadena vacía.
     */
    private static String texto(Object v) {
        return v == null ? "—" : v.toString().replace('T', ' ');
    }
}
