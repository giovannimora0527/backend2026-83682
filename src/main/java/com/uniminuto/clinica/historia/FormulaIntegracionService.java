package com.uniminuto.clinica.historia;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Une los dos registros de una prescripción para que el flujo no se parta:
 * <ul>
 *   <li>{@code formula_medica}: la prescripción dentro de la historia clínica (segundo parcial).</li>
 *   <li>{@code formula_clinica}: la orden de dispensación que recibe despacho y que llega a facturación (examen final).</li>
 * </ul>
 * Una prescripción creada en “Gestión de Fórmulas” genera su orden de despacho; una fórmula confirmada en la
 * atención genera sus prescripciones en la historia clínica. Ambas quedan enlazadas por
 * {@code formula_medica.formula_clinica_id}, sin duplicar el modelo.
 */
@Service
public class FormulaIntegracionService {

    /** Vía por defecto cuando la prescripción no la indica. */
    static final String VIA_POR_DEFECTO = "Según indicación médica";
    /** Primer número de un texto (admite decimales y fracciones como 1/2). */
    private static final Pattern NUMERO = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)|(\\d+(?:[.,]\\d+)?)");

    /** Acceso a datos. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public FormulaIntegracionService(Db db) {
        this.db = db;
    }

    /**
     * Genera la orden de dispensación de una prescripción de la historia clínica. Si la cita aún no se ha atendido,
     * la orden se crea al finalizar la atención ({@link #generarPendientesDeCita(long)}).
     *
     * @param prescripcionId identificador en {@code formula_medica}.
     */
    public void generarOrden(long prescripcionId) {
        Map<String, Object> p = db.requerido("SELECT * FROM formula_medica WHERE id = :id FOR UPDATE",
                Db.p("id", prescripcionId), "La fórmula no existe.");
        if (p.get("formula_clinica_id") != null || !Validar.bool(p, "activo")) {
            return;
        }
        long citaId = Validar.largo(p, "cita_id");
        Long atencion = atencionDisponible(citaId);
        if (atencion == null) {
            return;
        }
        int cantidad = calcularCantidad(Validar.cadena(p, "dosis"), Validar.cadena(p, "frecuencia"), Validar.cadena(p, "duracion"));
        verificarExistencias(Validar.largo(p, "medicamento_id"), cantidad);
        LocalDateTime ahora = Tiempo.ahora();
        long orden = db.insertar("""
                INSERT INTO formula_clinica (cita_id, atencion_id, medico_id, mascota_id, estado, estado_despacho, revision,
                                             fecha_creacion, fecha_confirmacion)
                VALUES (:c, :a, :m, :pet, 'CONFIRMADA', 'PENDIENTE_DESPACHO', 1, :ahora, :ahora)
                """, Db.p("c", citaId, "a", atencion, "m", p.get("medico_id"), "pet", p.get("mascota_id"), "ahora", ahora));
        insertarLinea(orden, p, cantidad);
        db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, 'BORRADOR', 'PENDIENTE_DESPACHO', :u, :ahora, 'Prescripción registrada en la historia clínica')
                """, Db.p("f", orden, "u", SesionActual.usuarioId(), "ahora", ahora));
        db.ejecutar("UPDATE formula_medica SET formula_clinica_id = :o WHERE id = :id", Db.p("o", orden, "id", prescripcionId));
    }

    /**
     * Al finalizar una atención, genera las órdenes de las prescripciones de esa cita que aún no tenían.
     *
     * @param citaId identificador de la cita.
     */
    public void generarPendientesDeCita(long citaId) {
        for (Map<String, Object> p : db.lista("""
                SELECT id FROM formula_medica WHERE cita_id = :c AND activo = 1 AND formula_clinica_id IS NULL
                """, Db.p("c", citaId))) {
            generarOrden(Validar.largo(p, "id"));
        }
    }

    /**
     * Registra en la historia clínica (una prescripción por medicamento) la fórmula confirmada en la atención.
     *
     * @param ordenId identificador en {@code formula_clinica}.
     */
    public void registrarEnHistoria(long ordenId) {
        Map<String, Object> f = db.requerido("""
                SELECT f.*, a.historia_id FROM formula_clinica f JOIN atencion_medica a ON a.id = f.atencion_id WHERE f.id = :id
                """, Db.p("id", ordenId), "La fórmula no existe.");
        LocalDateTime ahora = Tiempo.ahora();
        for (Map<String, Object> l : db.lista("SELECT * FROM formula_clinica_detalle WHERE formula_id = :f", Db.p("f", ordenId))) {
            db.insertar("""
                    INSERT INTO formula_medica (cita_id, mascota_id, medico_id, historia_id, medicamento_id, dosis, frecuencia,
                           duracion, indicaciones, fecha_emision, fecha_creacion_registro, activo, formula_clinica_id)
                    VALUES (:c, :m, :medico, :h, :med, :dosis, :frec, :dur, :ind, :emision, :ahora, 1, :o)
                    """, Db.p("c", f.get("cita_id"), "m", f.get("mascota_id"), "medico", f.get("medico_id"),
                    "h", f.get("historia_id"), "med", l.get("medicamento_id"), "dosis", l.get("dosis"),
                    "frec", l.get("frecuencia"), "dur", l.get("duracion"), "ind", l.get("indicaciones"),
                    "emision", ahora.toLocalDate(), "ahora", ahora, "o", ordenId));
        }
    }

    /**
     * Valida que la prescripción todavía pueda modificarse: su orden no debe tener entregas ni estar en preparación.
     *
     * @param prescripcion fila de {@code formula_medica}.
     */
    public void verificarModificable(Map<String, Object> prescripcion) {
        Map<String, Object> orden = orden(prescripcion);
        if (orden != null && !"PENDIENTE_DESPACHO".equals(orden.get("estado_despacho"))
                && !"ANULADA".equals(orden.get("estado"))) {
            throw new ConflictException("La fórmula ya está en despacho (" + Validar.cadena(orden, "estado_despacho")
                    + "): no se puede modificar. Las correcciones se hacen con el procedimiento de reversión de despacho.");
        }
    }

    /**
     * Refleja en la orden de despacho los cambios de una prescripción editada (medicamento, posología y cantidad).
     *
     * @param prescripcionId identificador en {@code formula_medica}.
     * @param medicamentoAnterior medicamento antes de la edición.
     */
    public void sincronizarEdicion(long prescripcionId, long medicamentoAnterior) {
        Map<String, Object> p = db.requerido("SELECT * FROM formula_medica WHERE id = :id", Db.p("id", prescripcionId),
                "La fórmula no existe.");
        Map<String, Object> orden = orden(p);
        if (orden == null || "ANULADA".equals(orden.get("estado"))) {
            return;
        }
        int cantidad = calcularCantidad(Validar.cadena(p, "dosis"), Validar.cadena(p, "frecuencia"), Validar.cadena(p, "duracion"));
        verificarExistencias(Validar.largo(p, "medicamento_id"), cantidad);
        long ordenId = Validar.largo(orden, "id");
        db.ejecutar("DELETE FROM formula_clinica_detalle WHERE formula_id = :o AND medicamento_id = :m",
                Db.p("o", ordenId, "m", medicamentoAnterior));
        insertarLinea(ordenId, p, cantidad);
        db.ejecutar("UPDATE formula_clinica SET revision = revision + 1 WHERE id = :o", Db.p("o", ordenId));
    }

    /**
     * Refleja la inactivación o reactivación de una prescripción en su orden de despacho.
     *
     * @param prescripcionId identificador en {@code formula_medica}.
     * @param activo nuevo estado.
     */
    public void sincronizarEstado(long prescripcionId, boolean activo) {
        Map<String, Object> p = db.requerido("SELECT * FROM formula_medica WHERE id = :id", Db.p("id", prescripcionId),
                "La fórmula no existe.");
        Map<String, Object> orden = orden(p);
        if (activo) {
            if (orden == null || "ANULADA".equals(orden.get("estado"))) {
                db.ejecutar("UPDATE formula_medica SET formula_clinica_id = NULL WHERE id = :id", Db.p("id", prescripcionId));
                generarOrden(prescripcionId);
            }
            return;
        }
        if (orden == null || "ANULADA".equals(orden.get("estado"))) {
            return;
        }
        long ordenId = Validar.largo(orden, "id");
        long lineas = db.contar("SELECT COUNT(*) FROM formula_clinica_detalle WHERE formula_id = :o", Db.p("o", ordenId));
        if (lineas > 1) {
            db.ejecutar("DELETE FROM formula_clinica_detalle WHERE formula_id = :o AND medicamento_id = :m",
                    Db.p("o", ordenId, "m", p.get("medicamento_id")));
            db.ejecutar("UPDATE formula_medica SET formula_clinica_id = NULL WHERE id = :id", Db.p("id", prescripcionId));
            return;
        }
        db.ejecutar("UPDATE formula_clinica SET estado = 'ANULADA', estado_despacho = 'CANCELADO', revision = revision + 1 WHERE id = :o",
                Db.p("o", ordenId));
        db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, 'PENDIENTE_DESPACHO', 'CANCELADO', :u, :ahora, 'Prescripción inactivada en la historia clínica')
                """, Db.p("f", ordenId, "u", SesionActual.usuarioId(), "ahora", Tiempo.ahora()));
    }

    /**
     * Inactiva en la historia clínica las prescripciones de una orden anulada en la atención.
     *
     * @param ordenId identificador en {@code formula_clinica}.
     */
    public void inactivarPorOrden(long ordenId) {
        db.ejecutar("UPDATE formula_medica SET activo = 0, fecha_actualizacion_registro = :ahora WHERE formula_clinica_id = :o",
                Db.p("o", ordenId, "ahora", Tiempo.ahora()));
    }

    /**
     * Calcula la cantidad total requerida según la formulación: dosis × tomas por día × días de tratamiento.
     * Ejemplos: “1 tableta”, “Cada 12 horas”, “7 días” → 14; “1/2 tableta”, “Cada 8 horas”, “3 días” → 5.
     *
     * @param dosis dosis por toma (debe contener un número).
     * @param frecuencia frecuencia (“cada N horas”, “N veces al día”, “diaria”…); si no se reconoce, 1 toma diaria.
     * @param duracion duración (“N días”, “N semanas”, “N meses”).
     * @return unidades a dispensar (mínimo 1).
     */
    public static int calcularCantidad(String dosis, String frecuencia, String duracion) {
        double porToma = numero(dosis, 1);
        String f = frecuencia == null ? "" : frecuencia.toLowerCase(Locale.ROOT);
        double tomasDia = 1;
        if (f.contains("hora")) {
            tomasDia = 24 / Math.max(1, numero(f, 24));
        } else if (f.contains("vez") || f.contains("veces")) {
            tomasDia = numero(f, 1);
        }
        String d = duracion == null ? "" : duracion.toLowerCase(Locale.ROOT);
        double dias = numero(d, 1) * (d.contains("semana") ? 7 : d.contains("mes") ? 30 : 1);
        return (int) Math.max(1, Math.ceil(porToma * tomasDia * dias - 1e-9));
    }

    /**
     * @param texto texto con un número (entero, decimal o fracción).
     * @param porDefecto valor si no hay número.
     * @return primer número del texto.
     */
    private static double numero(String texto, double porDefecto) {
        if (texto == null) {
            return porDefecto;
        }
        Matcher m = NUMERO.matcher(texto);
        if (!m.find()) {
            return porDefecto;
        }
        if (m.group(1) != null) {
            double den = Double.parseDouble(m.group(2));
            return den == 0 ? porDefecto : Double.parseDouble(m.group(1)) / den;
        }
        return Double.parseDouble(m.group(3).replace(',', '.'));
    }

    /**
     * Atención de la cita a la que se asocia la orden. Si la cita ya fue atendida sin atención registrada (registro
     * del segundo parcial), se crea una atención finalizada que la representa para despacho y facturación.
     *
     * @param citaId identificador de la cita.
     * @return atención o null si la cita aún no se atiende.
     */
    private Long atencionDisponible(long citaId) {
        Long atencion = db.valor("SELECT id FROM atencion_medica WHERE cita_id = :c", Db.p("c", citaId), Long.class);
        if (atencion != null) {
            return atencion;
        }
        Map<String, Object> cita = db.requerido("""
                SELECT c.*, md.especializacion_id FROM cita c JOIN medico md ON md.id = c.medico_id WHERE c.id = :c
                """, Db.p("c", citaId), "La cita no existe.");
        if (!List.of("atendida", "finalizada").contains(Validar.cadena(cita, "estado"))) {
            return null;
        }
        Long historia = db.valor("SELECT id FROM historia_medica WHERE cita_id = :c", Db.p("c", citaId), Long.class);
        LocalDateTime ahora = Tiempo.ahora();
        if (historia == null) {
            historia = db.insertar("""
                    INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, activo, fecha_creacion)
                    VALUES (:m, :c, :medico, :fecha, :motivo, 1, :ahora)
                    """, Db.p("m", cita.get("mascota_id"), "c", citaId, "medico", cita.get("medico_id"),
                    "fecha", cita.get("fecha_hora"), "motivo", cita.get("motivo"), "ahora", ahora));
            db.ejecutar("UPDATE formula_medica SET historia_id = :h WHERE cita_id = :c AND historia_id IS NULL",
                    Db.p("h", historia, "c", citaId));
        }
        return db.insertar("""
                INSERT INTO atencion_medica (cita_id, historia_id, medico_id, especializacion_id, motivo, version,
                                             fecha_apertura, fecha_finalizacion)
                VALUES (:c, :h, :m, :e, :motivo, 0, :ahora, :ahora)
                """, Db.p("c", citaId, "h", historia, "m", cita.get("medico_id"), "e", cita.get("especializacion_id"),
                "motivo", cita.get("motivo"), "ahora", ahora));
    }

    /**
     * Valida existencias vigentes suficientes para la cantidad prescrita.
     *
     * @param medicamento identificador del medicamento.
     * @param cantidad unidades requeridas.
     */
    private void verificarExistencias(long medicamento, int cantidad) {
        Map<String, Object> m = db.requerido("""
                SELECT nombre, COALESCE((SELECT SUM(l.cantidad) FROM medicamento_lote l WHERE l.medicamento_id = m.id
                       AND (l.fecha_vence IS NULL OR l.fecha_vence >= :hoy)), 0) AS disponible
                FROM medicamento m WHERE m.id = :id
                """, Db.p("id", medicamento, "hoy", Tiempo.hoy()), "El medicamento no existe.");
        if (cantidad > Validar.numero(m, "disponible")) {
            throw new ConflictException("No hay existencias suficientes de " + m.get("nombre") + ": se requieren " + cantidad
                    + " unidades según la formulación y hay " + m.get("disponible") + " disponibles.");
        }
    }

    /**
     * Inserta la línea de la orden a partir de la prescripción.
     *
     * @param orden identificador de la orden.
     * @param p fila de {@code formula_medica}.
     * @param cantidad unidades calculadas.
     */
    private void insertarLinea(long orden, Map<String, Object> p, int cantidad) {
        db.insertar("""
                INSERT INTO formula_clinica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion, cantidad, via, indicaciones)
                VALUES (:o, :m, :dosis, :frec, :dur, :cant, :via, :ind)
                """, Db.p("o", orden, "m", p.get("medicamento_id"), "dosis", p.get("dosis"),
                "frec", p.get("frecuencia") == null ? "Según indicación" : p.get("frecuencia"),
                "dur", p.get("duracion"), "cant", cantidad, "via", VIA_POR_DEFECTO, "ind", p.get("indicaciones")));
    }

    /**
     * @param prescripcion fila de {@code formula_medica}.
     * @return orden enlazada o null.
     */
    private Map<String, Object> orden(Map<String, Object> prescripcion) {
        Object id = prescripcion.get("formula_clinica_id");
        return id == null ? null : db.uno("SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE", Db.p("id", id));
    }
}
