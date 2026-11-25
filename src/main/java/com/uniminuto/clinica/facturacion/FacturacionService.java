package com.uniminuto.clinica.facturacion;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.PdfDocumento;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.seguridad.ClaveUtil;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Requerimiento 3.4 y 3.5: facturación de servicios veterinarios y registro de pagos.
 *
 * <ul>
 *   <li>Solo una atención finalizada puede facturarse y solo puede tener una factura no anulada
 *   (restricción única en la base de datos): no se cobra dos veces el mismo servicio.</li>
 *   <li>Los servicios usan la tarifa congelada al finalizar la atención; los medicamentos solo se facturan
 *   si fueron efectivamente dispensados (salidas de inventario no revertidas ni facturadas).</li>
 *   <li>Los valores (precio, impuestos, nombres) se copian a la factura y no cambian con tarifas futuras.</li>
 *   <li>La emisión exige la huella de la vista previa: si los importes cambiaron, responde 409.</li>
 *   <li>Pagos parciales con control de saldo esperado e idempotencia por clave; estados PENDIENTE_PAGO,
 *   PAGO_PARCIAL, PAGADA y ANULADA. La anulación es controlada (ADMINISTRADOR, con motivo) y no borra datos.</li>
 * </ul>
 */
@Service
public class FacturacionService {

    /** Registros por página. */
    private static final int TAMANO = 50;
    /** Métodos de pago permitidos. */
    private static final Set<String> METODOS = Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "OTRO");
    /** Formato de moneda colombiana. */
    private static final NumberFormat COP = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));




    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /** Acceso a datos del módulo (SQL). */
    private final FacturacionRepository repositorio;

    /**
     * @param repositorio acceso a datos del módulo.
     * @param auditoria log de operaciones sensibles.
     */
    public FacturacionService(FacturacionRepository repositorio, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ Emisión

    /**
     * @param pagina página.
     * @return atenciones finalizadas sin factura.
     */
    public List<Map<String, Object>> elegibles(Integer pagina) {
        return repositorio.listarFormulaClinica(Db.p("limite", TAMANO, "desde", Validar.pagina(pagina) * TAMANO));
    }

    /**
     * Vista previa de la liquidación de una atención.
     *
     * @param atencionId identificador de la atención.
     * @param rq descuento opcional.
     * @return importes calculados.
     */
    public Map<String, Object> previa(long atencionId, FacturacionApi.PreviaRq rq) {
        Map<String, Object> origen = origen(atencionId);
        BigDecimal descuento = descuento(rq == null ? null : rq.descuento(), rq == null ? null : rq.motivo());
        Liquidador.Resultado r = Liquidador.liquidar(conceptos(atencionId), descuento);
        return vistaPrevia(origen, r, descuento);
    }

    /**
     * Emite la factura de una atención validando la huella de la vista previa.
     *
     * @param atencionId identificador de la atención.
     * @param rq datos de emisión.
     * @return factura emitida.
     */
    @Transactional
    public Map<String, Object> emitir(long atencionId, FacturacionApi.EmitirRq rq) {
        if (rq == null || rq.clave() == null || rq.clave().isBlank() || rq.clave().length() > 64) {
            throw new BadRequestException("La solicitud de emisión no tiene una clave válida.");
        }
        Map<String, Object> repetida = repositorio.buscarFactura(Db.p("c", rq.clave()));
        if (repetida != null) {
            if (Validar.largo(repetida, "atencion_id") != atencionId) {
                throw new ConflictException("La clave de la solicitud ya fue usada en otra factura.");
            }
            return detalle(Validar.largo(repetida, "id"));
        }
        repositorio.obtenerAtencionMedica(Db.p("id", atencionId), "La atención no existe.");
        Map<String, Object> origen = origen(atencionId);
        if (repositorio.existeFactura(Db.p("a", atencionId))) {
            throw new ConflictException("La atención ya tiene una factura vigente.");
        }
        long sinDespachar = formulasSinDespachar(atencionId);
        if (sinDespachar > 0) {
            throw new ConflictException("La atención tiene " + sinDespachar + " fórmula(s) que despacho aún no ha "
                    + "procesado. Solo se facturan medicamentos entregados: espera la dispensación o que despacho la cancele.");
        }
        BigDecimal descuento = descuento(rq.descuento(), rq.motivo());
        Liquidador.Resultado r = Liquidador.liquidar(conceptos(atencionId), descuento);
        if (r.lineas().isEmpty()) {
            throw new ConflictException("La atención no tiene conceptos por facturar.");
        }
        if (!huella(atencionId, r).equals(rq.previa())) {
            throw new ConflictException("Los conceptos o importes cambiaron desde la vista previa. Revisa nuevamente.");
        }
        Map<String, Object> datos = repositorio.buscarAtencionMedica(Db.p("a", atencionId));
        LocalDateTime ahora = Tiempo.ahora();
        long id = repositorio.insertarFactura(Db.p("clave", rq.clave(), "a", atencionId, "cita", datos.get("cita"), "cliente", datos.get("cliente_id"),
                "mascota", datos.get("mascota_id"), "medico", datos.get("medico_id"), "esp", datos.get("especializacion_id"),
                "propietario", origen.get("propietario"), "documento", datos.get("documento"),
                "nombreMascota", origen.get("mascota"), "veterinario", origen.get("veterinario"),
                "especialidad", origen.get("especialidad"), "subtotal", r.subtotal(), "descuento", r.descuento(),
                "motivo", r.descuento().signum() > 0 ? rq.motivo().trim() : null, "iva", r.iva(), "otros", r.otros(),
                "total", r.total(), "u", SesionActual.usuarioId(), "ahora", ahora));
        repositorio.actualizarFactura(Db.p("n", String.format("FV-%06d", id), "id", id));
        for (Liquidador.Linea l : r.lineas()) {
            Liquidador.Concepto c = l.concepto();
            repositorio.insertarFacturaLinea(Db.p("f", id, "s", c.servicioId(), "t", c.tarifaId(), "m", c.movimientoId(), "concepto", c.nombre(),
                    "cantidad", c.cantidad(), "precio", c.precio(), "subtotal", l.subtotal(), "descuento", l.descuento(),
                    "ivap", c.ivaPorcentaje(), "iva", l.iva(), "impp", c.impuestoPorcentaje(), "otros", l.otros(),
                    "total", l.total()));
        }
        historial(id, null, "PENDIENTE_PAGO", "Factura emitida");
        auditoria.registrar("FACTURA_EMITIDA", "atencion", atencionId, "Factura emitida para la atención " + atencionId + (rq.descuento() == null || rq.descuento().isBlank() ? "" : " con descuento " + rq.descuento() + " · " + rq.motivo()));
        return detalle(id);
    }

    // ------------------------------------------------------------------ Consulta

    /**
     * Busca facturas con filtros.
     *
     * @param desde fecha inicial.
     * @param hasta fecha final.
     * @param propietario propietario.
     * @param mascota mascota.
     * @param estado estado.
     * @param numero número de factura.
     * @param pagina página.
     * @return facturas.
     */
    public List<Map<String, Object>> listar(String desde, String hasta, String propietario, String mascota, String estado,
                                            Integer pagina) {
        LocalDate d = Validar.fecha(desde, "desde", false);
        LocalDate h = Validar.fecha(hasta, "hasta", false);
        String est = estado == null || estado.isBlank() ? null : estado.trim().toUpperCase();
        if (est != null && !Set.of("PENDIENTE_PAGO", "PAGO_PARCIAL", "PAGADA", "ANULADA").contains(est)) {
            throw new BadRequestException("Estado de factura no válido.");
        }
        return repositorio.listarFactura(Db.p("desde", d == null ? null : d.atStartOfDay(), "hasta", h == null ? null : h.plusDays(1).atStartOfDay(),
                "propietario", like(propietario), "clienteId", numero(propietario), "mascota", like(mascota),
                "mascotaId", numero(mascota), "estado", est, "limite", TAMANO, "pag", Validar.pagina(pagina) * TAMANO));
    }

    /**
     * @param id identificador de la factura.
     * @return factura con líneas, pagos e historial.
     */
    public Map<String, Object> detalle(long id) {
        Map<String, Object> factura = repositorio.obtenerFactura(Db.p("id", id), "La factura no existe.");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("factura", factura);
        r.put("lineas", repositorio.listarFacturaLinea(Db.p("f", id)));
        r.put("pagos", repositorio.listarPago(Db.p("f", id)));
        r.put("historial", repositorio.listarFacturaEstadoHistorial(Db.p("f", id)));
        return r;
    }

    // ------------------------------------------------------------------ Pagos y anulación

    /**
     * Registra un pago sin superar el saldo.
     *
     * @param id identificador de la factura.
     * @param rq método y valor.
     * @return factura actualizada.
     */
    @Transactional
    public Map<String, Object> pagar(long id, FacturacionApi.PagoRq rq) {
        if (rq == null || rq.clave() == null || rq.clave().isBlank() || rq.clave().length() > 64) {
            throw new BadRequestException("La solicitud de pago no tiene una clave válida.");
        }
        Map<String, Object> f = repositorio.obtenerFacturaEnPagar(Db.p("id", id), "La factura no existe.");
        Map<String, Object> repetido = repositorio.buscarPago(Db.p("c", rq.clave()));
        if (repetido != null) {
            if (Validar.largo(repetido, "factura_id") != id) {
                throw new ConflictException("La clave del pago ya fue usada en otra factura.");
            }
            return detalle(id);
        }
        String estado = Validar.cadena(f, "estado");
        if (Set.of("PAGADA", "ANULADA").contains(estado)) {
            throw new ConflictException("La factura está " + estado.toLowerCase() + " y no admite pagos.");
        }
        BigDecimal importe = importe(rq.importe());
        if (importe.signum() <= 0) {
            throw new BadRequestException("El importe debe ser mayor que cero.");
        }
        BigDecimal saldo = Validar.decimal(f, "total").subtract(Validar.decimal(f, "pagado"));
        Validar.requerido(rq.saldoEsperado(), "saldo esperado");
        BigDecimal esperado = rq.saldoEsperado().setScale(2, java.math.RoundingMode.HALF_UP);
        if (esperado.compareTo(saldo) != 0) {
            throw new ConflictException("El saldo de la factura cambió (saldo actual " + saldo.toPlainString() + ").");
        }
        if (importe.compareTo(saldo) > 0) {
            throw new BadRequestException("El importe supera el saldo pendiente (" + saldo.toPlainString() + ").");
        }
        String metodo = Validar.texto(rq.metodo(), "método de pago", 20).toUpperCase();
        if (!METODOS.contains(metodo)) {
            throw new BadRequestException("Método de pago no válido.");
        }
        String referencia = Validar.opcional(rq.referencia(), "referencia", 100);
        if (!"EFECTIVO".equals(metodo) && referencia == null) {
            throw new BadRequestException("La referencia es obligatoria para pagos con " + metodo.toLowerCase() + ".");
        }
        repositorio.insertarPago(Db.p("f", id, "c", rq.clave(), "ahora", Tiempo.ahora(), "i", importe, "m", metodo, "r", referencia,
                "u", SesionActual.usuarioId()));
        BigDecimal pagado = Validar.decimal(f, "pagado").add(importe);
        String nuevo = pagado.compareTo(Validar.decimal(f, "total")) >= 0 ? "PAGADA" : "PAGO_PARCIAL";
        repositorio.actualizarFacturaEnPagar(Db.p("p", pagado, "e", nuevo, "id", id));
        if (!nuevo.equals(estado)) {
            historial(id, estado, nuevo, "Pago de " + importe.toPlainString() + " (" + metodo + ")");
        }
        auditoria.registrar("PAGO_REGISTRADO", "factura", id, "Pago de " + rq.importe() + " (" + rq.metodo() + ") a la factura " + id);
        return detalle(id);
    }

    /**
     * Anula una factura sin pagos.
     *
     * @param id identificador de la factura.
     * @param motivoRq motivo.
     * @return factura anulada.
     */
    @Transactional
    public Map<String, Object> anular(long id, String motivoRq) {
        if (!SesionActual.obtener().tiene("FACTURA_ANULAR")) {
            throw new ForbiddenException("Tu perfil no tiene permiso para anular facturas.");
        }
        String motivo = Validar.texto(motivoRq, "motivo de anulación", 255);
        Map<String, Object> f = repositorio.obtenerFacturaEnAnular(Db.p("id", id), "La factura no existe.");
        String estado = Validar.cadena(f, "estado");
        if ("ANULADA".equals(estado)) {
            throw new ConflictException("La factura ya está anulada.");
        }
        repositorio.actualizarFacturaEnAnular(Db.p("id", id));
        historial(id, estado, "ANULADA", motivo);
        auditoria.registrar("FACTURA_ANULADA", "factura", id, "Factura " + id + " anulada. Motivo: " + motivoRq);
        return detalle(id);
    }

    /**
     * @param id identificador de la factura.
     * @return factura en PDF.
     */
    public byte[] pdf(long id) {
        Map<String, Object> d = detalle(id);
        @SuppressWarnings("unchecked")
        Map<String, Object> f = (Map<String, Object>) d.get("factura");
        PdfDocumento pdf = new PdfDocumento("Factura de venta " + f.get("numero"));
        pdf.campos(new String[][]{{"Fecha de emisión", texto(f.get("emitida"))}, {"Estado", texto(f.get("estado"))},
                {"Propietario", texto(f.get("propietario"))}, {"Documento", texto(f.get("documento"))},
                {"Mascota", texto(f.get("mascota"))}, {"Cita / atención", "#" + f.get("cita") + " / #" + f.get("atencion")},
                {"Veterinario", texto(f.get("veterinario"))}, {"Especialidad", texto(f.get("especialidad"))}});
        pdf.seccion("Detalle");
        List<String[]> filas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineas = (List<Map<String, Object>>) d.get("lineas");
        for (Map<String, Object> l : lineas) {
            filas.add(new String[]{texto(l.get("concepto")), texto(l.get("cantidad")), cop(l.get("precio_unitario")),
                    cop(l.get("subtotal")), cop(l.get("descuento")), cop(l.get("iva")) + "\n(" + l.get("iva_porcentaje") + "%)",
                    cop(l.get("otros_impuestos")), cop(l.get("total"))});
        }
        pdf.tabla(new String[]{"Concepto", "Cant.", "V. unitario", "Subtotal", "Descuento", "IVA", "Otros imp.", "Total"},
                filas, new float[]{3.2f, 0.7f, 1.3f, 1.3f, 1.2f, 1.2f, 1.1f, 1.3f},
                new boolean[]{false, true, true, true, true, true, true, true});
        pdf.totales(new String[][]{{"Subtotal", cop(f.get("subtotal"))}, {"Descuento", cop(f.get("descuento"))},
                {"IVA", cop(f.get("iva"))}, {"Otros impuestos", cop(f.get("otros_impuestos"))},
                {"Total a pagar", cop(f.get("total"))}, {"Pagado", cop(f.get("pagado"))}, {"Saldo", cop(f.get("saldo"))}});
        if (f.get("motivoDescuento") != null) {
            pdf.nota("Descuento autorizado: " + f.get("motivoDescuento"));
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pagos = (List<Map<String, Object>>) d.get("pagos");
        if (!pagos.isEmpty()) {
            pdf.seccion("Pagos registrados");
            List<String[]> filasPago = new ArrayList<>();
            for (Map<String, Object> p : pagos) {
                filasPago.add(new String[]{texto(p.get("fecha")), texto(p.get("metodo")), texto(p.get("referencia")),
                        cop(p.get("importe")), texto(p.get("responsable"))});
            }
            pdf.tabla(new String[]{"Fecha", "Método", "Referencia", "Importe", "Registró"}, filasPago, null,
                    new boolean[]{false, false, false, true, false});
        }
        pdf.nota("Los medicamentos facturados corresponden a productos efectivamente dispensados. "
                + "Valores calculados con las tarifas e impuestos vigentes en la fecha de la atención.");
        return pdf.cerrar();
    }

    // ------------------------------------------------------------------ Cálculo

    /** Servicios de la atención y medicamentos dispensados (no revertidos ni facturados en otra factura vigente). */
    List<Liquidador.Concepto> conceptos(long atencionId) {
        List<Liquidador.Concepto> conceptos = new ArrayList<>();
        for (Map<String, Object> s : repositorio.listarAtencionServicio(Db.p("a", atencionId))) {
            conceptos.add(new Liquidador.Concepto(Validar.largo(s, "servicio_id"), Validar.largo(s, "tarifa_id"), null,
                    Validar.cadena(s, "nombre"), Validar.numero(s, "cantidad"), Validar.decimal(s, "valor_unitario"),
                    Validar.decimal(s, "iva_porcentaje"), Validar.decimal(s, "impuesto_porcentaje")));
        }
        for (Map<String, Object> m : repositorio.listarDespachoDetalle(Db.p("a", atencionId))) {
            conceptos.add(new Liquidador.Concepto(null, null, Validar.largo(m, "movimiento_id"),
                    m.get("nombre") + " (lote " + m.get("numero") + ")", Validar.numero(m, "cantidad"),
                    Validar.decimal(m, "precio_venta"), Validar.decimal(m, "iva_porcentaje"), BigDecimal.ZERO));
        }
        return conceptos;
    }

    /**
     * @param origen atención de origen.
     * @param r resultado de la liquidación.
     * @param descuento descuento global.
     * @return vista previa con huella.
     */
    private Map<String, Object> vistaPrevia(Map<String, Object> origen, Liquidador.Resultado r, BigDecimal descuento) {
        List<Map<String, Object>> lineas = new ArrayList<>();
        for (Liquidador.Linea l : r.lineas()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("servicio", l.concepto().servicioId());
            o.put("movimiento", l.concepto().movimientoId());
            o.put("nombre", l.concepto().nombre());
            o.put("cantidad", l.concepto().cantidad());
            o.put("precio", l.concepto().precio());
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("origen", o);
            linea.put("importes", importes(l.subtotal(), l.descuento(), l.iva(), l.otros(), l.total()));
            lineas.add(linea);
        }
        Map<String, Object> previa = new LinkedHashMap<>();
        previa.put("origen", origen);
        previa.put("huella", huella(Validar.largo(origen, "id"), r));
        previa.put("importes", importes(r.subtotal(), r.descuento(), r.iva(), r.otros(), r.total()));
        previa.put("lineas", lineas);
        return previa;
    }

    /** Huella SHA-256 de los conceptos e importes: garantiza que se emite exactamente lo revisado. */
    static String huella(long atencion, Liquidador.Resultado r) {
        StringBuilder sb = new StringBuilder("A").append(atencion);
        for (Liquidador.Linea l : r.lineas()) {
            Liquidador.Concepto c = l.concepto();
            sb.append('|').append(c.servicioId()).append(';').append(c.tarifaId()).append(';').append(c.movimientoId())
                    .append(';').append(c.cantidad()).append(';').append(c.precio().toPlainString()).append(';')
                    .append(l.total().toPlainString());
        }
        sb.append("|D").append(r.descuento().toPlainString()).append("|T").append(r.total().toPlainString());
        return ClaveUtil.sha256(sb.toString());
    }

    /**
     * @param subtotal subtotal.
     * @param descuento descuento.
     * @param iva IVA.
     * @param otros otros impuestos.
     * @param total total.
     * @return mapa de importes.
     */
    private static Map<String, Object> importes(BigDecimal subtotal, BigDecimal descuento, BigDecimal iva, BigDecimal otros,
                                                BigDecimal total) {
        Map<String, Object> i = new LinkedHashMap<>();
        i.put("subtotal", subtotal);
        i.put("descuento", descuento);
        i.put("iva", iva);
        i.put("otros", otros);
        i.put("total", total);
        return i;
    }

    /**
     * Fórmulas confirmadas de la atención que despacho todavía no ha procesado (pendientes o en preparación).
     * Mientras existan, no se factura: los medicamentos facturados deben corresponder a lo efectivamente entregado.
     *
     * @param atencionId identificador de la atención.
     * @return número de fórmulas sin despachar.
     */
    long formulasSinDespachar(long atencionId) {
        return repositorio.contarFormulaClinica(Db.p("a", atencionId));
    }

    /**
     * @param atencionId identificador de la atención.
     * @return atención finalizada a facturar.
     */
    private Map<String, Object> origen(long atencionId) {
        Map<String, Object> o = repositorio.obtenerFormulaClinica(Db.p("id", atencionId), "La atención no existe.");
        if (o.get("finalizada") == null) {
            throw new ConflictException("Solo se facturan atenciones finalizadas.");
        }
        return o;
    }

    /** Los descuentos solo se aplican si están autorizados: requieren el permiso DESCUENTO_AUTORIZAR y un motivo. */
    private BigDecimal descuento(String valor, String motivo) {
        BigDecimal d = valor == null || valor.isBlank() ? BigDecimal.ZERO : importe(valor);
        if (d.signum() > 0) {
            if (!SesionActual.obtener().tiene("DESCUENTO_AUTORIZAR")) {
                throw new ForbiddenException("Tu perfil no tiene permiso para autorizar descuentos.");
            }
            Validar.texto(motivo, "motivo del descuento", 255);
        }
        return d;
    }

    /**
     * @param valor texto numérico.
     * @return importe con dos decimales.
     */
    private static BigDecimal importe(String valor) {
        if (valor == null || !valor.trim().matches("\\d{1,12}(\\.\\d{1,2})?")) {
            throw new BadRequestException("El importe debe ser un número positivo con máximo dos decimales.");
        }
        return new BigDecimal(valor.trim()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Registra un cambio de estado de la factura.
     *
     * @param factura identificador.
     * @param anterior estado anterior.
     * @param nuevo estado nuevo.
     * @param motivo motivo.
     */
    private void historial(long factura, String anterior, String nuevo, String motivo) {
        repositorio.insertarFacturaEstadoHistorial(Db.p("f", factura, "a", anterior, "n", nuevo, "ahora", Tiempo.ahora(), "m", motivo,
                "u", SesionActual.usuarioId()));
    }

    /**
     * @param valor texto de búsqueda.
     * @return patrón LIKE o null.
     */
    private static String like(String valor) {
        return valor == null || valor.isBlank() ? null : "%" + valor.trim().replace("%", "\\%").replace("_", "\\_") + "%";
    }

    /**
     * @param valor texto.
     * @return número o null si no es numérico.
     */
    private static Long numero(String valor) {
        return valor != null && valor.trim().matches("\\d{1,18}") ? Long.parseLong(valor.trim()) : null;
    }

    /**
     * @param v valor.
     * @return texto o cadena vacía.
     */
    private static String texto(Object v) {
        return v == null ? "—" : v.toString().replace('T', ' ');
    }

    /**
     * @param v valor monetario.
     * @return valor en pesos colombianos.
     */
    private static String cop(Object v) {
        BigDecimal b = v == null ? BigDecimal.ZERO : (v instanceof BigDecimal x ? x : new BigDecimal(v.toString()));
        synchronized (COP) {
            return COP.format(b);
        }
    }
}
