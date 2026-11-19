package com.uniminuto.clinica.despacho;

import java.time.LocalDateTime;
import java.util.List;

import com.uniminuto.clinica.inventario.InventarioDatos.Lote;

public final class DespachoDatos {
    private DespachoDatos() {}

    public enum Estado {
        PENDIENTE_DESPACHO, EN_PREPARACION, DESPACHO_PARCIAL, DESPACHADO, PENDIENTE_DISPONIBILIDAD, CANCELADO
    }

    public record Formula(long id, int revision, LocalDateTime fecha, String veterinario, String mascota,
            String propietario, Estado estado) {}
    public record Prescripcion(long medicamento, String nombre, String dosis, String frecuencia, String duracion,
            String via, String indicaciones, int prescrita, int entregada, int pendiente) {}
    public record Medicamento(Prescripcion prescripcion, List<LoteDisponible> lotes, long disponible) {}
    public record LoteDisponible(long id, String numero, java.time.LocalDate fechaVence, int cantidad) {
        static LoteDisponible de(Lote l) { return new LoteDisponible(l.id(),l.numero(),l.fechaVence(),l.cantidad()); }
    }
    public record Seleccion(long medicamento, long lote, int cantidad) {}
    public record Preparar(java.util.UUID clave, List<Seleccion> lotes) {}
    public record Motivo(String motivo) {}
    public record Plan(long id, List<Seleccion> lotes, Long confirmadaPor) {}
    public record Linea(long medicamento, String nombre, long lote, String numero, int cantidad,
            long movimiento, Long movimientoReversion) {}
    public record Entrega(long id, long formula, String responsable, LocalDateTime fecha, Estado estado,
            LocalDateTime revertida, String reversor, String motivo, List<Linea> lineas, List<String> pendiente) {}
    public record Cambio(Estado anterior, Estado nuevo, String responsable, LocalDateTime fecha, String motivo) {}
    public record Detalle(Formula formula, List<Medicamento> medicamentos, Plan preparacion,
            List<Entrega> entregas, List<Cambio> historial) {}
}
