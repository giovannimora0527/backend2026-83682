package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.entity.enums.EstadoFactura;
import com.uniminuto.clinica.util.RangoFechas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Facturado: facturas no anuladas por fecha de generacion. Recaudado: pagos por fecha de pago; en los reportes por
 * renglon se reparte cada pago en proporcion al valor del renglon en su factura.
 */
@Configuration
public class ReportesFinancieros {

    private static final String GRUPO = "Financieros";
    private static final String VIGENTE = "f.estado <> com.uniminuto.clinica.entity.enums.EstadoFactura.ANULADA";
    private static final String EN_RANGO = " AND f.fechaGeneracion >= :desde AND f.fechaGeneracion < :hasta ";
    private static final String PAGOS = " WHERE p.anulado = false AND p.fecha >= :desde AND p.fecha < :hasta ";

    private final Consulta consulta;

    public ReportesFinancieros(Consulta consulta) {
        this.consulta = consulta;
    }

    @Bean
    GeneradorReporte ingresosPeriodo() {
        return GeneradorReporte.de("ingresos-periodo", "Ingresos por período", GRUPO,
                List.of(Filtro.DESDE, Filtro.HASTA, Filtro.AGRUPACION), f -> {
            String agrupacion = f.texto("agrupacion", "MES");
            String porFactura = Consulta.periodo("f.fechaGeneracion", agrupacion);
            String porPago = Consulta.periodo("p.fecha", agrupacion);
            Map<String, Object[]> periodos = new TreeMap<>();
            rango("SELECT " + porFactura + ", COUNT(f), SUM(f.subtotal), SUM(f.totalDescuento), "
                    + "SUM(f.totalImpuestos), SUM(f.total) FROM Factura f WHERE " + VIGENTE + EN_RANGO
                    + "GROUP BY " + porFactura, f).forEach(r -> periodos.put(Consulta.nombrePeriodo(r, agrupacion),
                    new Object[]{r[3], r[4], r[5], r[6], r[7], BigDecimal.ZERO}));
            rango("SELECT " + porPago + ", SUM(p.valor) FROM Pago p" + PAGOS + "GROUP BY " + porPago, f)
                    .forEach(r -> periodos.computeIfAbsent(Consulta.nombrePeriodo(r, agrupacion),
                            k -> new Object[]{0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                    BigDecimal.ZERO})[5] = r[3]);
            List<Columna> c = List.of(Columna.texto("periodo", "Período"), Columna.numero("facturas", "Facturas"),
                    Columna.moneda("subtotal", "Subtotal"), Columna.moneda("descuentos", "Descuentos"),
                    Columna.moneda("iva", "IVA"), Columna.moneda("facturado", "Facturado"),
                    Columna.moneda("recaudado", "Recaudado"));
            return Tabla.de(c, periodos.entrySet().stream().map(e -> Tabla.fila(c, e.getKey(), e.getValue()[0],
                    e.getValue()[1], e.getValue()[2], e.getValue()[3], e.getValue()[4], e.getValue()[5])).toList(),
                    "facturas", "subtotal", "descuentos", "iva", "facturado", "recaudado");
        });
    }

    @Bean
    GeneradorReporte ingresosConsultas() {
        return porRenglon("ingresos-consultas", "Ingresos por consultas", "Servicio", "d.servicio.id", "d.servicio.nombre",
                "d.servicio.tipoServicio = com.uniminuto.clinica.entity.enums.TipoServicio.CONSULTA");
    }

    @Bean
    GeneradorReporte ingresosMedicamentos() {
        return porRenglon("ingresos-medicamentos", "Ingresos por venta de medicamentos", "Medicamento", "d.medicamento.id",
                "d.medicamento.nombre", "d.tipoItem = com.uniminuto.clinica.entity.enums.TipoItem.MEDICAMENTO");
    }

    @Bean
    GeneradorReporte ingresosEspecialidad() {
        return porFactura("ingresos-especialidad", "Ingresos por especialidad", "Especialidad", "f.especialidadNombre",
                "f.especialidadNombre");
    }

    @Bean
    GeneradorReporte ingresosVeterinario() {
        return porFactura("ingresos-veterinario", "Ingresos por veterinario", "Veterinario", "f.medico.id",
                "MAX(f.medicoNombre)");
    }

    @Bean
    GeneradorReporte facturas() {
        return GeneradorReporte.de("facturas", "Facturas", GRUPO, List.of(Filtro.DESDE, Filtro.HASTA,
                Filtro.opciones("estado", List.of("TODAS", "PENDIENTE", "PAGO_PARCIAL", "PAGADA", "ANULADA"))), f -> {
            String estado = f.texto("estado", "TODAS");
            RangoFechas r = f.rango();
            List<Object[]> filas = consulta.filas("SELECT f.numero, f.fechaGeneracion, f.clienteNombre, f.mascotaNombre, "
                    + "f.medicoNombre, f.estado, f.subtotal, f.totalDescuento, f.totalImpuestos, f.total, f.totalPagado, "
                    + "f.saldo FROM Factura f WHERE (:estado IS NULL OR f.estado = :estado)" + EN_RANGO
                    + "ORDER BY f.fechaGeneracion, f.id", "estado", "TODAS".equals(estado) ? null
                    : EstadoFactura.valueOf(estado), "desde", r.inicio(), "hasta", r.finExclusivo());
            List<Columna> c = List.of(Columna.texto("numero", "Número"), Columna.fechaHora("fecha", "Fecha"),
                    Columna.texto("cliente", "Propietario"), Columna.texto("mascota", "Mascota"),
                    Columna.texto("veterinario", "Veterinario"), Columna.texto("estado", "Estado"),
                    Columna.moneda("subtotal", "Subtotal"), Columna.moneda("descuentos", "Descuentos"),
                    Columna.moneda("iva", "IVA"), Columna.moneda("total", "Total"), Columna.moneda("pagado", "Pagado"),
                    Columna.moneda("saldo", "Saldo"));
            List<Map<String, Object>> todas = filas.stream().map(x -> Tabla.fila(c, x)).toList();
            // Las anuladas se listan, pero solo suman si se pidieron expresamente.
            Map<String, Object> totales = Tabla.de(c, todas.stream().filter(x -> "ANULADA".equals(estado)
                    || !"ANULADA".equals(x.get("estado"))).toList(), "subtotal", "descuentos", "iva", "total",
                    "pagado", "saldo").totales();
            return new Tabla(c, todas, totales);
        });
    }

    @Bean
    GeneradorReporte impuestos() {
        return GeneradorReporte.de("impuestos", "Impuestos", GRUPO, List.of(Filtro.DESDE, Filtro.HASTA), f -> {
            List<Columna> c = List.of(Columna.porcentaje("porcentaje", "IVA %"), Columna.numero("renglones", "Renglones"),
                    Columna.moneda("base", "Base gravable"), Columna.moneda("iva", "IVA"));
            return Tabla.de(c, rango("SELECT d.porcentajeIva, COUNT(d), SUM(d.subtotal - d.valorDescuento), SUM(d.valorIva) "
                    + "FROM FacturaDetalle d JOIN d.factura f WHERE " + VIGENTE + EN_RANGO
                    + "GROUP BY d.porcentajeIva ORDER BY d.porcentajeIva", f).stream().map(x -> Tabla.fila(c, x)).toList(),
                    "renglones", "base", "iva");
        });
    }

    @Bean
    GeneradorReporte descuentos() {
        return GeneradorReporte.de("descuentos", "Descuentos", GRUPO, List.of(Filtro.DESDE, Filtro.HASTA), f -> {
            List<Columna> c = List.of(Columna.fechaHora("fecha", "Fecha"), Columna.texto("factura", "Factura"),
                    Columna.texto("cliente", "Propietario"), Columna.texto("concepto", "Concepto"),
                    Columna.moneda("subtotal", "Subtotal"), Columna.porcentaje("porcentaje", "Descuento %"),
                    Columna.moneda("descuento", "Descuento"), Columna.texto("motivo", "Motivo"),
                    Columna.texto("usuario", "Autorizado por"));
            return Tabla.de(c, rango("SELECT f.fechaGeneracion, f.numero, f.clienteNombre, d.descripcion, d.subtotal, "
                    + "d.porcentajeDescuento, d.valorDescuento, d.motivoDescuento, f.usuario.username "
                    + "FROM FacturaDetalle d JOIN d.factura f WHERE d.valorDescuento > 0 AND " + VIGENTE + EN_RANGO
                    + "ORDER BY f.fechaGeneracion, d.id", f).stream().map(x -> Tabla.fila(c, x)).toList(),
                    "subtotal", "descuento");
        });
    }

    private GeneradorReporte porFactura(String codigo, String titulo, String columna, String clave, String nombre) {
        return GeneradorReporte.de(codigo, titulo, GRUPO, List.of(Filtro.DESDE, Filtro.HASTA), f -> {
            Map<Object, Object[]> grupos = new LinkedHashMap<>();
            rango("SELECT " + clave + ", " + nombre + ", COUNT(f), SUM(f.total) FROM Factura f WHERE " + VIGENTE
                    + EN_RANGO + "GROUP BY " + clave, f)
                    .forEach(r -> grupos.put(r[0], new Object[]{r[1], r[2], r[3], BigDecimal.ZERO}));
            rango("SELECT " + clave + ", " + nombre + ", SUM(p.valor) FROM Pago p JOIN p.factura f" + PAGOS
                    + "GROUP BY " + clave, f).forEach(r -> grupos.computeIfAbsent(r[0],
                    k -> new Object[]{r[1], 0L, BigDecimal.ZERO, BigDecimal.ZERO})[3] = r[2]);
            List<Columna> c = List.of(Columna.texto("nombre", columna), Columna.numero("facturas", "Facturas"),
                    Columna.moneda("facturado", "Facturado"), Columna.moneda("recaudado", "Recaudado"));
            return Tabla.de(c, ordenar(grupos).stream().map(g -> Tabla.fila(c, g)).toList(), "facturas", "facturado",
                    "recaudado");
        });
    }

    private GeneradorReporte porRenglon(String codigo, String titulo, String columna, String clave, String nombre,
                                        String condicion) {
        return GeneradorReporte.de(codigo, titulo, GRUPO, List.of(Filtro.DESDE, Filtro.HASTA), f -> {
            Map<Object, Object[]> grupos = new LinkedHashMap<>();
            rango("SELECT " + clave + ", " + nombre + ", SUM(d.cantidad), SUM(d.subtotal), SUM(d.valorDescuento), "
                    + "SUM(d.valorIva), SUM(d.total) FROM FacturaDetalle d JOIN d.factura f WHERE " + condicion + " AND "
                    + VIGENTE + EN_RANGO + "GROUP BY " + clave + ", " + nombre, f)
                    .forEach(r -> grupos.put(r[0], new Object[]{r[1], r[2], r[3], r[4], r[5], r[6], BigDecimal.ZERO}));
            rango("SELECT " + clave + ", " + nombre + ", SUM(p.valor * d.total / f.total) FROM Pago p JOIN p.factura f "
                    + "JOIN FacturaDetalle d ON d.factura = f" + PAGOS + "AND f.total > 0 AND " + condicion
                    + " GROUP BY " + clave + ", " + nombre, f).forEach(r -> grupos.computeIfAbsent(r[0],
                    k -> new Object[]{r[1], 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO})[6] = r[2]);
            List<Columna> c = List.of(Columna.texto("nombre", columna), Columna.numero("cantidad", "Cantidad"),
                    Columna.moneda("subtotal", "Subtotal"), Columna.moneda("descuentos", "Descuentos"),
                    Columna.moneda("iva", "IVA"), Columna.moneda("facturado", "Facturado"),
                    Columna.moneda("recaudado", "Recaudado"));
            return Tabla.de(c, ordenar(grupos).stream().map(g -> Tabla.fila(c, g)).toList(), "cantidad", "subtotal",
                    "descuentos", "iva", "facturado", "recaudado");
        });
    }

    private static List<Object[]> ordenar(Map<Object, Object[]> grupos) {
        return grupos.values().stream().sorted(Comparator.comparing(g -> (String) g[0],
                String.CASE_INSENSITIVE_ORDER)).toList();
    }

    private List<Object[]> rango(String jpql, Filtros f) {
        RangoFechas r = f.rango();
        return consulta.filas(jpql, "desde", r.inicio(), "hasta", r.finExclusivo());
    }
}
