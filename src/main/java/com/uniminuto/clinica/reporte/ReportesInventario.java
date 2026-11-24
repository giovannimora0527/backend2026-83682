package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.util.RangoFechas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Los de existencias muestran la situacion actual: no dependen del rango de fechas. */
@Configuration
public class ReportesInventario {

    private static final String GRUPO = "Inventario";
    private static final List<Filtro> SIN_FECHAS = List.of(Filtro.DESDE_OPCIONAL, Filtro.HASTA_OPCIONAL);
    /** Existencias por producto: datos, total, vigente y valor a costo. */
    private static final String EXISTENCIAS = """
            SELECT m.codigo, m.nombre, m.tipoProducto, m.unidadMedida, m.stockMinimo, m.precioVenta,
                COALESCE(SUM(l.cantidadDisponible), 0),
                COALESCE(SUM(CASE WHEN l.fechaVencimiento IS NULL OR l.fechaVencimiento >= :hoy
                    THEN l.cantidadDisponible ELSE 0 END), 0),
                COALESCE(SUM(l.cantidadDisponible * l.precioCompra), 0)
            FROM Medicamento m LEFT JOIN LoteMedicamento l ON l.medicamento = m AND l.activo = true
            WHERE %s
            GROUP BY m.id, m.codigo, m.nombre, m.tipoProducto, m.unidadMedida, m.stockMinimo, m.precioVenta
            %s ORDER BY m.nombre
            """;
    private static final String CANTIDAD = "COALESCE(SUM(l.cantidadDisponible), 0)";

    private final Consulta consulta;
    private final MedicamentoRepository medicamentoRepository;

    public ReportesInventario(Consulta consulta, MedicamentoRepository medicamentoRepository) {
        this.consulta = consulta;
        this.medicamentoRepository = medicamentoRepository;
    }

    @Bean
    GeneradorReporte existencias() {
        return GeneradorReporte.de("existencias", "Existencias", GRUPO, SIN_FECHAS,
                f -> tablaExistencias(existencias("")));
    }

    @Bean
    GeneradorReporte agotados() {
        return GeneradorReporte.de("agotados", "Productos agotados", GRUPO, SIN_FECHAS,
                f -> tablaExistencias(existencias("HAVING " + CANTIDAD + " = 0")));
    }

    @Bean
    GeneradorReporte stockMinimo() {
        return GeneradorReporte.de("stock-minimo", "Productos en stock mínimo", GRUPO, SIN_FECHAS,
                f -> tablaExistencias(existencias("HAVING " + CANTIDAD + " > 0 AND " + CANTIDAD + " <= m.stockMinimo")));
    }

    @Bean
    GeneradorReporte valorInventario() {
        return GeneradorReporte.de("valor-inventario", "Valor del inventario", GRUPO, SIN_FECHAS, f -> {
            List<Columna> c = List.of(Columna.texto("codigo", "Código"), Columna.texto("nombre", "Producto"),
                    Columna.numero("cantidad", "Cantidad"), Columna.moneda("precioVenta", "Precio de venta"),
                    Columna.moneda("valorCosto", "Valor a costo"), Columna.moneda("valorVenta", "Valor de venta"),
                    Columna.moneda("utilidad", "Utilidad potencial"));
            // Un producto inactivo con existencias tambien vale.
            return Tabla.de(c, consulta.filas(EXISTENCIAS.formatted("1 = 1", "HAVING " + CANTIDAD + " > 0"), "hoy",
                    LocalDate.now()).stream().map(x -> {
                BigDecimal costo = decimal(x[8]);
                BigDecimal venta = decimal(x[5]).multiply(decimal(x[6]));
                return Tabla.fila(c, x[0], x[1], x[6], x[5], costo, venta, venta.subtract(costo));
            }).toList(), "cantidad", "valorCosto", "valorVenta", "utilidad");
        });
    }

    @Bean
    GeneradorReporte proximosVencer(@Value("${inventario.dias-proximo-vencer:30}") int dias) {
        return GeneradorReporte.de("proximos-vencer", "Próximos a vencer", GRUPO, List.of(Filtro.DESDE_OPCIONAL,
                Filtro.HASTA_OPCIONAL, Filtro.opcional("dias")), f -> {
            LocalDate hoy = LocalDate.now();
            return lotes("l.fechaVencimiento >= :hoy AND l.fechaVencimiento <= :limite", "hoy", hoy, "limite",
                    hoy.plusDays(f.entero("dias", dias, 1, 3650)));
        });
    }

    @Bean
    GeneradorReporte vencidos() {
        return GeneradorReporte.de("vencidos", "Vencidos", GRUPO, SIN_FECHAS,
                f -> lotes("l.fechaVencimiento < :hoy", "hoy", LocalDate.now()));
    }

    @Bean
    GeneradorReporte movimientos() {
        return GeneradorReporte.de("movimientos", "Entradas y salidas", GRUPO, List.of(Filtro.DESDE, Filtro.HASTA,
                Filtro.opciones("tipo", Arrays.stream(TipoMovimiento.values()).map(Enum::name).toList()),
                Filtro.opcional("medicamentoId")), f -> {
            String tipo = f.texto("tipo", null);
            List<Columna> c = columnasMovimiento();
            return Tabla.de(c, movimientos(f, f.id("medicamentoId"), tipo == null ? null : TipoMovimiento.valueOf(tipo))
                    .stream().map(x -> filaMovimiento(c, x)).toList(), "entrada", "salida", "valorEntrada", "valorSalida");
        });
    }

    @Bean
    GeneradorReporte kardex() {
        return GeneradorReporte.de("kardex", "Kardex por medicamento", GRUPO, List.of(Filtro.DESDE, Filtro.HASTA,
                Filtro.obligatorio("medicamentoId")), f -> {
            Medicamento m = medicamentoRepository.findById(f.id("medicamentoId"))
                    .orElseThrow(() -> new NotFoundException("El producto no existe."));
            f.aplicados().put("medicamento", m.getCodigo() + " - " + m.getNombre());
            List<Object[]> datos = movimientos(f, m.getId(), null);
            List<Columna> c = columnasMovimiento();
            List<Columna> visibles = c.stream().filter(x -> !Set.of("codigo", "nombre").contains(x.clave())).toList();
            List<Map<String, Object>> filas = new ArrayList<>();
            BigDecimal inicial = saldoAntes(m.getId(), f.rango().inicio());
            filas.add(Tabla.fila(c, null, "SALDO INICIAL", m.getCodigo(), m.getNombre(), null, null, null, inicial, null,
                    null, null, null, null));
            datos.forEach(x -> filas.add(filaMovimiento(c, x)));
            Map<String, Object> totales = new LinkedHashMap<>(Tabla.de(c, filas, "entrada", "salida", "valorEntrada",
                    "valorSalida").totales());
            totales.put("saldo", inicial.add((BigDecimal) totales.get("entrada")).subtract((BigDecimal) totales.get("salida")));
            filas.forEach(x -> x.keySet().removeAll(Set.of("codigo", "nombre")));
            return new Tabla(visibles, filas, totales);
        });
    }

    private Tabla tablaExistencias(List<Object[]> datos) {
        List<Columna> c = List.of(Columna.texto("codigo", "Código"), Columna.texto("nombre", "Producto"),
                Columna.texto("tipo", "Tipo"), Columna.texto("unidad", "Unidad"), Columna.numero("cantidad", "Cantidad"),
                Columna.numero("vigente", "Vigente"), Columna.numero("vencida", "Vencida"),
                Columna.numero("stockMinimo", "Stock mínimo"));
        return Tabla.de(c, datos.stream().map(x -> Tabla.fila(c, x[0], x[1], x[2], x[3], x[6], x[7],
                decimal(x[6]).subtract(decimal(x[7])), x[4])).toList(), "cantidad", "vigente", "vencida");
    }

    private List<Object[]> existencias(String condicionGrupo) {
        return consulta.filas(EXISTENCIAS.formatted("m.activo = true", condicionGrupo), "hoy", LocalDate.now());
    }

    private Tabla lotes(String condicion, Object... parametros) {
        LocalDate hoy = LocalDate.now();
        List<Columna> c = List.of(Columna.texto("codigo", "Código"), Columna.texto("nombre", "Producto"),
                Columna.texto("lote", "Lote"), Columna.fecha("vencimiento", "Vencimiento"), Columna.numero("dias", "Días"),
                Columna.numero("cantidad", "Cantidad"), Columna.moneda("costo", "Costo unitario"),
                Columna.moneda("valorCosto", "Valor a costo"));
        return Tabla.de(c, consulta.filas("SELECT m.codigo, m.nombre, l.numeroLote, l.fechaVencimiento, "
                + "l.cantidadDisponible, l.precioCompra, l.cantidadDisponible * l.precioCompra FROM LoteMedicamento l "
                + "JOIN l.medicamento m WHERE l.activo = true AND l.cantidadDisponible > 0 AND " + condicion
                + " ORDER BY l.fechaVencimiento, m.nombre", parametros).stream().map(x -> Tabla.fila(c, x[0], x[1], x[2],
                x[3], ChronoUnit.DAYS.between(hoy, (LocalDate) x[3]), x[4], x[5], x[6])).toList(), "cantidad", "valorCosto");
    }

    private List<Object[]> movimientos(Filtros f, Long medicamentoId, TipoMovimiento tipo) {
        RangoFechas r = f.rango();
        return consulta.filas("SELECT x.fecha, x.tipo, m.codigo, m.nombre, l.numeroLote, x.cantidad, "
                + "x.saldoProductoResultante, x.costoUnitario, u.username, f.numero, d.numero, c.id, x.observacion "
                + "FROM MovimientoInventario x JOIN x.medicamento m JOIN x.lote l JOIN x.usuario u LEFT JOIN x.formula f "
                + "LEFT JOIN x.despacho d LEFT JOIN x.carga c WHERE x.fecha >= :desde AND x.fecha < :hasta "
                + "AND (:medicamentoId IS NULL OR m.id = :medicamentoId) AND (:tipo IS NULL OR x.tipo = :tipo) "
                + "ORDER BY x.fecha, x.id", "desde", r.inicio(), "hasta", r.finExclusivo(), "medicamentoId", medicamentoId,
                "tipo", tipo);
    }

    private static List<Columna> columnasMovimiento() {
        return List.of(Columna.fechaHora("fecha", "Fecha"), Columna.texto("tipo", "Tipo"), Columna.texto("codigo", "Código"),
                Columna.texto("nombre", "Producto"), Columna.texto("lote", "Lote"), Columna.numero("entrada", "Entrada"),
                Columna.numero("salida", "Salida"), Columna.numero("saldo", "Saldo"), Columna.moneda("costo", "Costo unitario"),
                Columna.moneda("valorEntrada", "Valor entrada"), Columna.moneda("valorSalida", "Valor salida"),
                Columna.texto("usuario", "Usuario"), Columna.texto("referencia", "Referencia"));
    }

    private static Map<String, Object> filaMovimiento(List<Columna> c, Object[] x) {
        boolean salida = x[1] == TipoMovimiento.SALIDA_DESPACHO || x[1] == TipoMovimiento.AJUSTE_NEGATIVO;
        BigDecimal valor = decimal(x[5]).multiply(decimal(x[7]));
        String referencia = x[10] != null ? x[10] + " / " + x[9] : x[11] != null ? "Carga " + x[11] : (String) x[12];
        return Tabla.fila(c, x[0], x[1], x[2], x[3], x[4], salida ? null : x[5], salida ? x[5] : null, x[6], x[7],
                salida ? null : valor, salida ? valor : null, x[8], referencia);
    }

    private BigDecimal saldoAntes(Long medicamentoId, LocalDateTime fecha) {
        return consulta.filas("SELECT COALESCE(SUM(CASE WHEN x.tipo IN (com.uniminuto.clinica.entity.enums.TipoMovimiento."
                + "SALIDA_DESPACHO, com.uniminuto.clinica.entity.enums.TipoMovimiento.AJUSTE_NEGATIVO) THEN -x.cantidad "
                + "ELSE x.cantidad END), 0), COUNT(x) FROM MovimientoInventario x WHERE x.medicamento.id = :id "
                + "AND x.fecha < :fecha", "id", medicamentoId, "fecha", fecha).stream().map(r -> decimal(r[0]))
                .findFirst().orElse(BigDecimal.ZERO);
    }

    private static BigDecimal decimal(Object valor) {
        return valor instanceof BigDecimal b ? b : new BigDecimal(valor.toString());
    }
}
