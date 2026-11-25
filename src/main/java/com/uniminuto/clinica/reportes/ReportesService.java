package com.uniminuto.clinica.reportes;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.ExcelLibro;
import com.uniminuto.clinica.comun.ExcelLibro.Columna;
import com.uniminuto.clinica.comun.ExcelLibro.Formato;
import com.uniminuto.clinica.comun.PdfDocumento;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Requerimiento 4: reportes administrativos, contables, de inventario y de gestión médica.
 *
 * <p>Cada reporte recibe un rango de fechas y filtros según su tipo, devuelve encabezados, fecha de
 * generación, totales y filas paginadas (50 por página) para visualizar antes de exportar, y se descarga en
 * PDF o Excel. Los datos financieros se calculan desde facturas, líneas, pagos y movimientos registrados.
 * El acceso se restringe por perfil: ADMINISTRADOR consulta todos; RECEPCION y MEDICO solo el resumen
 * operativo de citas (el médico, únicamente sus citas).</p>
 */
@Service
public class ReportesService {

    /** Registros por página. */
    static final int TAMANO = 50;
    /** Máximo de filas por reporte. */
    private static final int MAXIMO = 20_000;
    /** Reportes operativos. */
    private static final Set<String> OPERATIVOS = Set.of("CITAS_OPERATIVAS");
    /** Reportes de inventario. */
    private static final Set<String> INVENTARIO = Set.of("EXISTENCIAS", "LOTES", "MOVIMIENTOS", "DISPENSACIONES");
    /** Estados de cita para filtrar. */
    private static final Set<String> ESTADOS_CITA = Set.of("pendiente", "confirmada", "cancelada", "atendida", "finalizada");
    /** Formato de moneda colombiana. */
    private static final NumberFormat COP = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

    /** Definición ejecutada de un reporte. */
    record Reporte(String titulo, List<Columna> columnas, List<Map<String, Object>> filas, Map<String, Object> totales,
                   Map<String, String> filtros) {
    }


    /** Acceso a datos del módulo (SQL). */
    private final ReportesRepository repositorio;

    /**
     * @param repositorio acceso a datos del módulo.
     */
    public ReportesService(ReportesRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Ejecuta un reporte y devuelve una página.
     *
     * @param tipo tipo de reporte.
     * @param parametros filtros.
     * @param paginaRq página.
     * @return filas, columnas y totales.
     */
    public Map<String, Object> consultar(String tipo, Map<String, String> parametros, Integer paginaRq) {
        Reporte r = ejecutar(tipo, parametros);
        int pagina = Validar.pagina(paginaRq);
        int desde = Math.min(pagina * TAMANO, r.filas().size());
        int hasta = Math.min(desde + TAMANO, r.filas().size());
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("titulo", r.titulo());
        resultado.put("generado", Tiempo.ahora());
        resultado.put("filtros", r.filtros());
        resultado.put("columnas", r.columnas().stream().map(c -> Map.of("clave", c.clave(), "titulo", c.titulo(),
                "formato", c.formato().name())).toList());
        resultado.put("totales", r.totales());
        resultado.put("filas", r.filas().subList(desde, hasta));
        resultado.put("registros", r.filas().size());
        resultado.put("pagina", pagina);
        return resultado;
    }

    /**
     * @param tipo tipo de reporte.
     * @param parametros filtros.
     * @return reporte en Excel.
     */
    public byte[] excel(String tipo, Map<String, String> parametros) {
        Reporte r = ejecutar(tipo, parametros);
        return ExcelLibro.generar(r.titulo(), r.filtros(), r.columnas(), r.filas(), r.totales());
    }

    /**
     * @param tipo tipo de reporte.
     * @param parametros filtros.
     * @return reporte en PDF.
     */
    public byte[] pdf(String tipo, Map<String, String> parametros) {
        Reporte r = ejecutar(tipo, parametros);
        PdfDocumento pdf = new PdfDocumento(r.titulo(), r.columnas().size() > 6);
        String[][] filtros = r.filtros().entrySet().stream().map(e -> new String[]{e.getKey(), e.getValue()})
                .toArray(String[][]::new);
        if (filtros.length > 0) {
            pdf.campos(filtros);
        }
        pdf.seccion("Resultados (" + r.filas().size() + " registros)");
        String[] cabeceras = r.columnas().stream().map(Columna::titulo).toArray(String[]::new);
        boolean[] derecha = new boolean[cabeceras.length];
        for (int i = 0; i < derecha.length; i++) {
            Formato f = r.columnas().get(i).formato();
            derecha[i] = f == Formato.COP || f == Formato.ENTERO;
        }
        List<String[]> filas = new ArrayList<>();
        for (Map<String, Object> fila : r.filas()) {
            filas.add(r.columnas().stream().map(c -> formatear(fila.get(c.clave()), c.formato())).toArray(String[]::new));
        }
        pdf.tabla(cabeceras, filas, null, derecha);
        if (!r.totales().isEmpty()) {
            List<String[]> totales = new ArrayList<>();
            for (Map.Entry<String, Object> t : r.totales().entrySet()) {
                Columna c = r.columnas().stream().filter(x -> x.clave().equals(t.getKey())).findFirst().orElse(null);
                totales.add(new String[]{c == null ? t.getKey().replace('_', ' ') : c.titulo(),
                        formatear(t.getValue(), c == null ? Formato.ENTERO : c.formato())});
            }
            pdf.totales(totales.toArray(String[][]::new));
        }
        return pdf.cerrar();
    }

    // ------------------------------------------------------------------ Ejecución

    /**
     * Permisos que habilitan cada tipo de reporte (matriz RBAC): financieros, inventario, citas, gestión médica
     * y despacho. El veterinario solo ve su propia actividad (los filtros se restringen a su médico).
     *
     * @param tipo tipo de reporte en mayúsculas.
     * @return permisos aceptados (cualquiera de ellos).
     */
    static String[] permisosDelTipo(String tipo) {
        return switch (tipo) {
            case "FINANCIERO", "FACTURAS", "PAGOS", "POR_MEDICO", "POR_ESPECIALIDAD", "CONCEPTOS" -> new String[]{"REPORTE_FINANCIERO"};
            case "EXISTENCIAS", "LOTES", "MOVIMIENTOS" -> new String[]{"REPORTE_INVENTARIO"};
            case "DISPENSACIONES" -> new String[]{"REPORTE_INVENTARIO", "REPORTE_DESPACHO"};
            case "CITAS", "CITAS_OPERATIVAS" -> new String[]{"REPORTE_CITAS"};
            case "CONSULTAS", "ATENCIONES" -> new String[]{"REPORTE_MEDICO"};
            default -> new String[]{"REPORTE_FINANCIERO", "REPORTE_INVENTARIO", "REPORTE_CITAS", "REPORTE_MEDICO"};
        };
    }

    /**
     * Valida permisos y filtros y ejecuta el reporte.
     *
     * @param tipoRq tipo de reporte.
     * @param p filtros.
     * @return reporte completo.
     */
    Reporte ejecutar(String tipoRq, Map<String, String> p) {
        String tipo = tipoRq == null ? "" : tipoRq.trim().toUpperCase();
        SesionUsuario s = SesionActual.obtener();
        if (!s.tieneAlguno(permisosDelTipo(tipo))) {
            throw new ForbiddenException("Tu perfil no tiene acceso a este reporte.");
        }
        Filtros f = new Filtros(p, s, tipo);
        return switch (tipo) {
            case "FINANCIERO" -> financiero(f);
            case "FACTURAS" -> facturas(f);
            case "PAGOS" -> pagos(f);
            case "POR_MEDICO" -> agrupado(f, "veterinario", "Facturación por veterinario");
            case "POR_ESPECIALIDAD" -> agrupado(f, "especialidad", "Facturación por especialidad");
            case "CONCEPTOS" -> conceptos(f);
            case "EXISTENCIAS" -> f.desde == null ? existencias(f) : existenciasHistoricas(f);
            case "LOTES" -> lotes(f);
            case "MOVIMIENTOS" -> movimientos(f);
            case "DISPENSACIONES" -> dispensaciones(f);
            case "CITAS" -> citas(f);
            case "CONSULTAS" -> consultas(f);
            case "ATENCIONES" -> atenciones(f);
            case "CITAS_OPERATIVAS" -> operativas(f);
            default -> throw new BadRequestException("Tipo de reporte no válido.");
        };
    }

    /** Filtros normalizados y validados. */
    final class Filtros {
        final LocalDate desde;
        final LocalDate hasta;
        final Long medico;
        final Long especialidad;
        final Long medicamento;
        final Long lote;
        final Long mascota;
        final Long propietario;
        final String estado;
        final String agrupacion;
        final Map<String, String> descripcion = new LinkedHashMap<>();

        Filtros(Map<String, String> p, SesionUsuario s, String tipo) {
            LocalDate d = Validar.fecha(p.get("desde"), "fecha inicial", false);
            LocalDate h = Validar.fecha(p.get("hasta"), "fecha final", false);
            boolean fechasObligatorias = !INVENTARIO.contains(tipo) || Set.of("MOVIMIENTOS", "DISPENSACIONES").contains(tipo);
            if (fechasObligatorias && (d == null || h == null)) {
                throw new BadRequestException("Selecciona la fecha inicial y la fecha final.");
            }
            if ((d == null) != (h == null)) {
                throw new BadRequestException("Indica ambas fechas del rango o ninguna.");
            }
            if (d != null && h.isBefore(d)) {
                throw new BadRequestException("La fecha final no puede ser anterior a la inicial.");
            }
            if (d != null && d.plusYears(3).isBefore(h)) {
                throw new BadRequestException("El rango máximo de un reporte es de tres años.");
            }
            desde = d;
            hasta = h;
            medicamento = Validar.id(p.get("medicamento"), "medicamento");
            lote = Validar.id(p.get("lote"), "lote");
            mascota = Validar.id(p.get("mascota"), "mascota");
            propietario = Validar.id(p.get("propietario"), "propietario");
            especialidad = Validar.id(p.get("especialidad"), "especialidad");
            Long med = Validar.id(p.get("medico"), "médico");
            if (s.es(Perfiles.VETERINARIO)) {
                med = SesionActual.medicoId();
            }
            medico = med;
            String e = p.get("estado") == null || p.get("estado").isBlank() ? null : p.get("estado").trim();
            estado = e;
            String a = p.get("agrupacion") == null ? "DIA" : p.get("agrupacion").trim().toUpperCase();
            if (!Set.of("DIA", "MES", "ANIO").contains(a)) {
                throw new BadRequestException("La agrupación debe ser DIA, MES o ANIO.");
            }
            agrupacion = a;
            if (d != null) {
                descripcion.put(tipo.equals("LOTES") ? "Vencimiento" : "Período", d + " a " + h);
            }
            describir("Médico", medico, ReportesRepository.Entidad.MEDICO);
            describir("Especialidad", especialidad, ReportesRepository.Entidad.ESPECIALIDAD);
            describir("Medicamento", medicamento, ReportesRepository.Entidad.MEDICAMENTO);
            describir("Lote", lote, ReportesRepository.Entidad.LOTE);
            describir("Mascota", mascota, ReportesRepository.Entidad.MASCOTA);
            describir("Propietario", propietario, ReportesRepository.Entidad.PROPIETARIO);
            if (estado != null) {
                descripcion.put("Estado o tipo", estado);
            }
            if ("FINANCIERO".equals(tipo)) {
                descripcion.put("Agrupación", agrupacion);
            }
            descripcion.put("Generado por", s.username());
        }

        private void describir(String etiqueta, Long id, ReportesRepository.Entidad entidad) {
            if (id != null) {
                String nombre = repositorio.nombreDe(entidad, id);
                descripcion.put(etiqueta, nombre == null ? "#" + id + " (no existe)" : nombre);
            }
        }

        Map<String, Object> params() {
            return Db.p("desde", desde == null ? null : desde.atStartOfDay(),
                    "hasta", hasta == null ? null : hasta.plusDays(1).atStartOfDay(),
                    "fdesde", desde, "fhasta", hasta, "medico", medico, "especialidad", especialidad,
                    "medicamento", medicamento, "lote", lote, "mascota", mascota, "propietario", propietario,
                    "estado", estado, "hoy", Tiempo.hoy(), "limite", Tiempo.limiteProximoVencer(),
                    "maximo", MAXIMO);
        }

        void estadoEn(Set<String> validos) {
            if (estado != null && !validos.contains(estado)) {
                throw new BadRequestException("Estado no válido para este reporte.");
            }
        }
    }

    // ------------------------------------------------------------------ Financieros

    /** Filtro común de facturas por fecha y estado. */
    private static final String FILTRO_FACTURA = """
            f.fecha_emision >= :desde AND f.fecha_emision < :hasta
              AND (:medico IS NULL OR f.medico_id = :medico)
              AND (:especialidad IS NULL OR f.especializacion_id = :especialidad)
              AND (:mascota IS NULL OR f.mascota_id = :mascota)
              AND (:propietario IS NULL OR f.cliente_id = :propietario)
            """;

    /** Subconsulta de líneas por factura. */
    private static final String LINEAS_POR_FACTURA = """
            (SELECT factura_id,
                    SUM(CASE WHEN servicio_id IS NOT NULL THEN total ELSE 0 END) AS servicios,
                    SUM(CASE WHEN movimiento_id IS NOT NULL THEN total ELSE 0 END) AS medicamentos
             FROM factura_linea GROUP BY factura_id) l
            """;

    /**
     * @param f filtros.
     * @return resumen financiero por día.
     */
    private Reporte financiero(Filtros f) {
        String formato = switch (f.agrupacion) {
            case "MES" -> "%Y-%m";
            case "ANIO" -> "%Y";
            default -> "%Y-%m-%d";
        };
        Map<String, Object> params = f.params();
        params.put("formato", formato);
        List<Map<String, Object>> filas = repositorio.listarFactura(params);
        List<Columna> c = List.of(col("periodo", "Período", Formato.TEXTO), col("facturas", "Facturas", Formato.ENTERO),
                col("pagadas", "Pagadas", Formato.ENTERO), col("pendientes", "Pendientes", Formato.ENTERO),
                col("parciales", "Pago parcial", Formato.ENTERO), col("anuladas", "Anuladas", Formato.ENTERO),
                col("ingresos_consultas", "Ingresos consultas", Formato.COP),
                col("ingresos_medicamentos", "Ingresos medicamentos", Formato.COP),
                col("descuentos", "Descuentos", Formato.COP), col("iva", "IVA", Formato.COP),
                col("otros_impuestos", "Otros impuestos", Formato.COP), col("total_facturado", "Total facturado", Formato.COP),
                col("recaudado", "Recaudado", Formato.COP), col("saldo", "Saldo por cobrar", Formato.COP));
        return new Reporte("Resumen financiero de ingresos", c, filas, totales(filas, c, null), f.descripcion);
    }

    /**
     * @param f filtros.
     * @return listado de facturas.
     */
    private Reporte facturas(Filtros f) {
        f.estadoEn(Set.of("PENDIENTE_PAGO", "PAGO_PARCIAL", "PAGADA", "ANULADA"));
        List<Map<String, Object>> filas = repositorio.listarFacturaEnFacturas(f.params());
        List<Columna> c = List.of(col("numero", "Factura", Formato.TEXTO), col("emitida", "Emitida", Formato.FECHAHORA),
                col("propietario", "Propietario", Formato.TEXTO), col("mascota", "Mascota", Formato.TEXTO),
                col("veterinario", "Veterinario", Formato.TEXTO), col("especialidad", "Especialidad", Formato.TEXTO),
                col("estado", "Estado", Formato.TEXTO), col("subtotal", "Subtotal", Formato.COP),
                col("descuento", "Descuento", Formato.COP), col("iva", "IVA", Formato.COP),
                col("otros_impuestos", "Otros impuestos", Formato.COP), col("total", "Total", Formato.COP),
                col("pagado", "Pagado", Formato.COP), col("saldo", "Saldo", Formato.COP));
        Map<String, Object> t = totales(filas, c, x -> !"ANULADA".equals(x.get("estado")));
        for (String e : List.of("PENDIENTE_PAGO", "PAGO_PARCIAL", "PAGADA", "ANULADA")) {
            t.put("facturas_" + e.toLowerCase(), filas.stream().filter(x -> e.equals(x.get("estado"))).count());
        }
        return new Reporte("Facturas y saldos al cierre (totales sin facturas anuladas)", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @return pagos recibidos.
     */
    private Reporte pagos(Filtros f) {
        f.estadoEn(Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "OTRO"));
        List<Map<String, Object>> filas = repositorio.listarPago(f.params());
        List<Columna> c = List.of(col("fecha", "Fecha", Formato.FECHAHORA), col("factura", "Factura", Formato.TEXTO),
                col("propietario", "Propietario", Formato.TEXTO), col("mascota", "Mascota", Formato.TEXTO),
                col("metodo", "Método", Formato.TEXTO), col("referencia", "Referencia", Formato.TEXTO),
                col("importe", "Importe", Formato.COP), col("responsable", "Registró", Formato.TEXTO),
                col("estado_factura", "Estado factura", Formato.TEXTO));
        Map<String, Object> t = totales(filas, c, null);
        t.put("pagos_registrados", filas.size());
        return new Reporte("Pagos recibidos", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @param campo campo de agrupación.
     * @param titulo título del reporte.
     * @return ventas agrupadas.
     */
    private Reporte agrupado(Filtros f, String campo, String titulo) {
        List<Map<String, Object>> filas = repositorio.ventasAgrupadas(campo, f.params());
        List<Columna> c = List.of(col("grupo", "veterinario".equals(campo) ? "Veterinario" : "Especialidad", Formato.TEXTO),
                col("facturas", "Facturas", Formato.ENTERO), col("ingresos_consultas", "Ingresos consultas", Formato.COP),
                col("ingresos_medicamentos", "Ingresos medicamentos", Formato.COP),
                col("descuentos", "Descuentos", Formato.COP), col("iva", "IVA", Formato.COP),
                col("otros_impuestos", "Otros impuestos", Formato.COP), col("total_facturado", "Total facturado", Formato.COP),
                col("recaudado", "Recaudado", Formato.COP), col("saldo", "Saldo", Formato.COP));
        return new Reporte(titulo, c, filas, totales(filas, c, null), f.descripcion);
    }

    /**
     * @param f filtros.
     * @return ventas por concepto.
     */
    private Reporte conceptos(Filtros f) {
        f.estadoEn(Set.of("SERVICIO", "MEDICAMENTO"));
        List<Map<String, Object>> filas = repositorio.listarFacturaLinea(f.params());
        List<Columna> c = List.of(col("tipo", "Tipo", Formato.TEXTO), col("concepto", "Servicio o medicamento", Formato.TEXTO),
                col("cantidad", "Cantidad", Formato.ENTERO), col("subtotal", "Subtotal", Formato.COP),
                col("descuento", "Descuento", Formato.COP), col("iva", "IVA", Formato.COP),
                col("otros_impuestos", "Otros impuestos", Formato.COP), col("total", "Total", Formato.COP));
        Map<String, Object> t = totales(filas, c, null);
        t.put("ingresos_servicios", suma(filas, "total", x -> "SERVICIO".equals(x.get("tipo"))));
        t.put("ingresos_medicamentos", suma(filas, "total", x -> "MEDICAMENTO".equals(x.get("tipo"))));
        return new Reporte("Facturación por servicio y medicamento", c, filas, t, f.descripcion);
    }

    // ------------------------------------------------------------------ Inventario

    /**
     * @param f filtros.
     * @return existencias actuales.
     */
    private Reporte existencias(Filtros f) {
        f.estadoEn(Set.of("agotado", "minimo", "vencido", "proximo"));
        List<Map<String, Object>> filas = repositorio.listarMedicamento(f.params());
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> x : filas) {
            int disponible = Validar.numero(x, "disponible");
            String alerta = disponible == 0 ? "agotado" : Validar.numero(x, "vencidas") > 0 ? "vencido"
                    : disponible < Validar.numero(x, "stock_minimo") ? "minimo"
                    : Validar.numero(x, "proximas") > 0 ? "proximo" : "normal";
            x.put("alerta", alerta);
            boolean incluye = f.estado == null
                    || ("agotado".equals(f.estado) && disponible == 0)
                    || ("minimo".equals(f.estado) && disponible < Validar.numero(x, "stock_minimo"))
                    || ("vencido".equals(f.estado) && Validar.numero(x, "vencidas") > 0)
                    || ("proximo".equals(f.estado) && Validar.numero(x, "proximas") > 0);
            if (incluye) {
                resultado.add(x);
            }
        }
        List<Columna> c = List.of(col("codigo", "Código", Formato.TEXTO), col("nombre", "Producto", Formato.TEXTO),
                col("tipo", "Tipo", Formato.TEXTO), col("stock_minimo", "Stock mínimo", Formato.ENTERO),
                col("existencia", "Existencia", Formato.ENTERO), col("disponible", "Disponible", Formato.ENTERO),
                col("vencidas", "Unidades vencidas", Formato.ENTERO), col("proximas", "Próximas a vencer", Formato.ENTERO),
                col("precio_compra", "Precio compra", Formato.COP), col("precio_venta", "Precio venta", Formato.COP),
                col("valor_costo", "Valor a costo", Formato.COP), col("valor_venta", "Valor potencial de venta", Formato.COP),
                col("alerta", "Alerta", Formato.TEXTO));
        Map<String, Object> t = totales(resultado, c, null);
        t.remove("stock_minimo");
        t.remove("precio_compra");
        t.remove("precio_venta");
        for (String a : List.of("agotado", "minimo", "vencido", "proximo")) {
            t.put("productos_" + a, resultado.stream().filter(x -> a.equals(x.get("alerta"))).count());
        }
        return new Reporte("Existencias actuales y valoración del inventario", c, resultado, t, f.descripcion);
    }

    /**
     * @param f filtros (fecha de corte).
     * @return existencias a una fecha.
     */
    private Reporte existenciasHistoricas(Filtros f) {
        List<Map<String, Object>> filas = repositorio.listarMedicamentoLote(f.params());
        List<Columna> c = List.of(col("codigo", "Código", Formato.TEXTO), col("nombre", "Producto", Formato.TEXTO),
                col("lote", "Lote", Formato.TEXTO), col("fecha_vence", "Vence", Formato.FECHA),
                col("saldo_inicial", "Saldo inicial", Formato.ENTERO), col("entradas", "Entradas", Formato.ENTERO),
                col("salidas", "Salidas", Formato.ENTERO), col("saldo_final", "Saldo final", Formato.ENTERO),
                col("valor_costo", "Valor a costo (final)", Formato.COP));
        return new Reporte("Existencias históricas por lote", c, filas, totales(filas, c, null), f.descripcion);
    }

    /**
     * @param f filtros.
     * @return lotes y vencimientos.
     */
    private Reporte lotes(Filtros f) {
        List<Map<String, Object>> filas = repositorio.listarMedicamentoLoteEnLotes(f.params());
        List<Columna> c = List.of(col("codigo", "Código", Formato.TEXTO), col("nombre", "Producto", Formato.TEXTO),
                col("lote_id", "ID lote", Formato.ENTERO), col("lote", "Lote", Formato.TEXTO),
                col("fecha_ingreso", "Ingreso", Formato.FECHA), col("fecha_vence", "Vence", Formato.FECHA),
                col("cantidad_inicial", "Cantidad inicial", Formato.ENTERO), col("cantidad", "Existencia", Formato.ENTERO),
                col("costo", "Costo unitario", Formato.COP), col("valor_costo", "Valor a costo", Formato.COP),
                col("estado", "Estado", Formato.TEXTO));
        Map<String, Object> t = totales(filas, c, null);
        t.remove("lote_id");
        t.remove("costo");
        return new Reporte("Inventario por lote", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @return movimientos de inventario.
     */
    private Reporte movimientos(Filtros f) {
        f.estadoEn(Set.of("ENTRADA", "SALIDA", "AJUSTE"));
        List<Map<String, Object>> filas = repositorio.listarMovimientoInventario(f.params());
        List<Columna> c = List.of(col("id", "Movimiento", Formato.ENTERO), col("fecha", "Fecha", Formato.FECHAHORA),
                col("tipo", "Tipo", Formato.TEXTO), col("codigo", "Código", Formato.TEXTO),
                col("nombre", "Producto", Formato.TEXTO), col("lote", "Lote", Formato.TEXTO),
                col("entrada", "Entradas", Formato.ENTERO), col("salida", "Salidas", Formato.ENTERO),
                col("saldo", "Saldo lote", Formato.ENTERO), col("motivo", "Motivo", Formato.TEXTO),
                col("referencia", "Referencia", Formato.TEXTO), col("formula", "Fórmula", Formato.ENTERO),
                col("despacho", "Despacho", Formato.ENTERO), col("usuario", "Usuario", Formato.TEXTO));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("entrada", suma(filas, "entrada", null));
        t.put("salida", suma(filas, "salida", null));
        t.put("movimientos", filas.size());
        return new Reporte("Entradas y salidas de inventario", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @return medicamentos dispensados.
     */
    private Reporte dispensaciones(Filtros f) {
        f.estadoEn(Set.of("VIGENTE", "REVERTIDA"));
        List<Map<String, Object>> filas = repositorio.listarDespachoDetalle(f.params());
        List<Columna> c = List.of(col("fecha", "Fecha", Formato.FECHAHORA), col("despacho", "Despacho", Formato.ENTERO),
                col("formula", "Fórmula", Formato.ENTERO), col("mascota", "Mascota", Formato.TEXTO),
                col("propietario", "Propietario", Formato.TEXTO), col("veterinario", "Veterinario", Formato.TEXTO),
                col("medicamento", "Medicamento", Formato.TEXTO), col("lote", "Lote", Formato.TEXTO),
                col("cantidad", "Cantidad", Formato.ENTERO), col("movimiento", "Mov. salida", Formato.ENTERO),
                col("reversion", "Mov. reversión", Formato.ENTERO), col("estado", "Estado", Formato.TEXTO),
                col("responsable", "Responsable", Formato.TEXTO));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("cantidad", suma(filas, "cantidad", x -> "VIGENTE".equals(x.get("estado"))));
        t.put("unidades_revertidas", suma(filas, "cantidad", x -> "REVERTIDA".equals(x.get("estado"))));
        return new Reporte("Dispensaciones y reversiones", c, filas, t, f.descripcion);
    }

    // ------------------------------------------------------------------ Gestión médica


    /**
     * @param f filtros.
     * @return citas.
     */
    private Reporte citas(Filtros f) {
        f.estadoEn(ESTADOS_CITA);
        List<Map<String, Object>> filas = repositorio.listarCita(f.params());
        List<Columna> c = List.of(col("id", "Cita", Formato.ENTERO), col("inicio", "Inicio", Formato.FECHAHORA),
                col("fin", "Fin", Formato.FECHAHORA), col("estado", "Estado", Formato.TEXTO),
                col("mascota", "Mascota", Formato.TEXTO), col("propietario", "Propietario", Formato.TEXTO),
                col("veterinario", "Veterinario", Formato.TEXTO), col("especialidad", "Especialidad", Formato.TEXTO),
                col("motivo", "Motivo", Formato.TEXTO));
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("citas", filas.size());
        for (String e : List.of("pendiente", "confirmada", "cancelada", "atendida", "finalizada")) {
            t.put("citas_" + e, filas.stream().filter(x -> e.equals(x.get("estado"))).count());
        }
        return new Reporte("Gestión médica de citas", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @return consultas por médico.
     */
    private Reporte consultas(Filtros f) {
        f.estadoEn(ESTADOS_CITA);
        List<Map<String, Object>> filas = repositorio.listarCitaEnConsultas(f.params());
        List<Columna> c = List.of(col("veterinario", "Veterinario", Formato.TEXTO), col("especialidad", "Especialidad", Formato.TEXTO),
                col("citas", "Citas", Formato.ENTERO), col("pendientes", "Pendientes", Formato.ENTERO),
                col("confirmadas", "Confirmadas", Formato.ENTERO), col("canceladas", "Canceladas", Formato.ENTERO),
                col("atendidas", "Atendidas", Formato.ENTERO), col("consultas_realizadas", "Consultas realizadas", Formato.ENTERO),
                col("mascotas_atendidas", "Mascotas atendidas", Formato.ENTERO));
        Map<String, Object> t = totales(filas, c, null);
        t.put("mascotas_atendidas", repositorio.contarCita(f.params()));
        Map<String, Object> porEspecialidad = new LinkedHashMap<>();
        for (Map<String, Object> x : filas) {
            String clave = "especialidad_" + x.get("especialidad");
            porEspecialidad.merge(clave, Validar.largo(x, "consultas_realizadas"), (a, b) -> (Long) a + (Long) b);
        }
        t.putAll(porEspecialidad);
        return new Reporte("Consultas por veterinario y especialidad", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @return atenciones.
     */
    private Reporte atenciones(Filtros f) {
        f.estadoEn(ESTADOS_CITA);
        List<Map<String, Object>> filas = repositorio.listarAtencionServicio(f.params());
        List<Columna> c = List.of(col("finalizada", "Finalizada", Formato.FECHAHORA), col("cita", "Cita", Formato.ENTERO),
                col("mascota", "Mascota", Formato.TEXTO), col("especie", "Especie", Formato.TEXTO),
                col("propietario", "Propietario", Formato.TEXTO), col("veterinario", "Veterinario", Formato.TEXTO),
                col("especialidad", "Especialidad", Formato.TEXTO), col("diagnostico", "Diagnóstico", Formato.TEXTO),
                col("servicios", "Servicios", Formato.ENTERO), col("formulas", "Fórmulas", Formato.ENTERO));
        Map<String, Object> t = totales(filas, c, null);
        t.put("atenciones", filas.size());
        t.put("mascotas_distintas", filas.stream().map(x -> x.get("mascota") + "|" + x.get("propietario")).distinct().count());
        return new Reporte("Atenciones finalizadas (mascotas atendidas por período)", c, filas, t, f.descripcion);
    }

    /**
     * @param f filtros.
     * @return citas operativas del día.
     */
    private Reporte operativas(Filtros f) {
        f.estadoEn(ESTADOS_CITA);
        List<Map<String, Object>> filas = repositorio.listarCitaEnOperativas(f.params());
        List<Columna> c = List.of(col("fecha", "Fecha", Formato.FECHA), col("citas", "Citas", Formato.ENTERO),
                col("pendientes", "Pendientes", Formato.ENTERO), col("confirmadas", "Confirmadas", Formato.ENTERO),
                col("canceladas", "Canceladas", Formato.ENTERO), col("atendidas", "Atendidas", Formato.ENTERO),
                col("finalizadas", "Finalizadas", Formato.ENTERO), col("mascotas", "Mascotas", Formato.ENTERO),
                col("veterinarios", "Veterinarios", Formato.ENTERO));
        Map<String, Object> t = totales(filas, c, null);
        t.remove("mascotas");
        t.remove("veterinarios");
        return new Reporte("Resumen operativo de citas", c, filas, t, f.descripcion);
    }

    // ------------------------------------------------------------------ Utilidades

    /**
     * @param clave clave del dato.
     * @param titulo título.
     * @param formato formato.
     * @return definición de columna.
     */
    private static Columna col(String clave, String titulo, Formato formato) {
        return new Columna(clave, titulo, formato);
    }

    /** Suma las columnas numéricas (ENTERO y COP) de las filas que cumplen el predicado. */
    static Map<String, Object> totales(List<Map<String, Object>> filas, List<Columna> columnas,
                                       Predicate<Map<String, Object>> incluir) {
        Map<String, Object> t = new LinkedHashMap<>();
        for (Columna c : columnas) {
            if ((c.formato() == Formato.COP || c.formato() == Formato.ENTERO) && !Set.of("id", "cita", "despacho", "formula",
                    "movimiento", "reversion").contains(c.clave())) {
                BigDecimal s = suma(filas, c.clave(), incluir);
                t.put(c.clave(), c.formato() == Formato.ENTERO ? (Object) s.longValue() : s);
            }
        }
        return t;
    }

    /**
     * @param filas filas.
     * @param clave columna a sumar.
     * @param incluir filtro de filas.
     * @return suma.
     */
    static BigDecimal suma(List<Map<String, Object>> filas, String clave, Predicate<Map<String, Object>> incluir) {
        BigDecimal s = BigDecimal.ZERO;
        for (Map<String, Object> f : filas) {
            if (incluir == null || incluir.test(f)) {
                s = s.add(Validar.decimal(f, clave));
            }
        }
        return s;
    }

    /**
     * @param valor valor.
     * @param formato formato.
     * @return texto para el PDF.
     */
    private static String formatear(Object valor, Formato formato) {
        if (valor == null) {
            return "—";
        }
        if (formato == Formato.COP) {
            BigDecimal b = valor instanceof BigDecimal x ? x : new BigDecimal(valor.toString());
            synchronized (COP) {
                return COP.format(b);
            }
        }
        if (formato == Formato.FECHAHORA) {
            String t = valor.toString().replace('T', ' ');
            return t.length() > 16 ? t.substring(0, 16) : t;
        }
        return valor.toString();
    }
}
