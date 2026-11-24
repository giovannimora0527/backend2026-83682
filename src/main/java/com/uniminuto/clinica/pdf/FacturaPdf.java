package com.uniminuto.clinica.pdf;

import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.entity.enums.EstadoFactura;
import com.uniminuto.clinica.entity.enums.TipoItem;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.FacturaDetalleRs;
import com.uniminuto.clinica.models.PagoRs;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Usa los datos congelados en la factura. */
public final class FacturaPdf {

    private FacturaPdf() {
    }

    public static Descarga generar(ConfiguracionRs clinica, Factura f, List<FacturaDetalleRs> detalles,
                                   List<PagoRs> pagos) {
        boolean anulada = f.getEstado() == EstadoFactura.ANULADA;
        Raza raza = f.getMascota().getRaza();
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Fecha", DocumentoPdf.FECHA_HORA.format(f.getFechaGeneracion()));
        datos.put("Estado de pago", f.getEstado().name().replace('_', ' '));
        datos.put("Propietario", f.getClienteNombre());
        datos.put("Documento", f.getClienteDocumento());
        datos.put("Mascota", f.getMascotaNombre());
        datos.put("Especie / raza", raza == null ? "" : raza.getEspecie() + " / " + raza.getNombre());
        datos.put("Veterinario", f.getMedicoNombre());
        datos.put("Especialidad", f.getEspecialidadNombre());
        DocumentoPdf pdf = new DocumentoPdf(clinica, "FACTURA " + f.getNumero(), null, anulada ? "ANULADA" : null)
                .datos(datos);
        seccion(pdf, "Servicios prestados", detalles, TipoItem.SERVICIO);
        seccion(pdf, "Medicamentos suministrados", detalles, TipoItem.MEDICAMENTO);
        Map<String, String> totales = new LinkedHashMap<>();
        totales.put("Subtotal", DocumentoPdf.dinero(f.getSubtotal()));
        totales.put("Descuentos", DocumentoPdf.dinero(f.getTotalDescuento()));
        detalles.stream().collect(Collectors.groupingBy(d -> d.porcentajeIva().stripTrailingZeros(),
                        java.util.TreeMap::new, Collectors.reducing(BigDecimal.ZERO, FacturaDetalleRs::valorIva, BigDecimal::add)))
                .forEach((p, v) -> totales.put("IVA " + DocumentoPdf.numero(p) + " %", DocumentoPdf.dinero(v)));
        totales.put("Total impuestos", DocumentoPdf.dinero(f.getTotalImpuestos()));
        totales.put("TOTAL", DocumentoPdf.dinero(f.getTotal()));
        pdf.totales(totales);
        if (!pagos.isEmpty()) {
            pdf.seccion("Pagos registrados").tabla(List.of("Fecha", "Medio", "Referencia", "Recibido por", "Valor"),
                    new float[]{1.6f, 1.8f, 2, 1.6f, 1.6f}, pagos.stream().map(p -> List.of(
                            DocumentoPdf.FECHA_HORA.format(p.fecha()), p.medioPago().name().replace('_', ' '),
                            p.referencia() == null ? "" : p.referencia(), p.usuario(),
                            DocumentoPdf.dinero(p.valor()))).toList(), Set.of(4));
        }
        Map<String, String> saldo = new LinkedHashMap<>();
        saldo.put("Pagado", DocumentoPdf.dinero(f.getTotalPagado()));
        saldo.put("Saldo", DocumentoPdf.dinero(f.getSaldo()));
        pdf.totales(saldo);
        if (f.getObservacion() != null && !f.getObservacion().isBlank()) {
            pdf.seccion("Observación").texto(f.getObservacion());
        }
        if (anulada) {
            pdf.seccion("Anulación").texto(DocumentoPdf.FECHA_HORA.format(f.getFechaAnulacion()) + " por "
                    + f.getUsuarioAnulacion().getUsername() + ": " + f.getMotivoAnulacion());
        }
        return pdf.terminar("factura_" + f.getNumero());
    }

    private static void seccion(DocumentoPdf pdf, String titulo, List<FacturaDetalleRs> detalles, TipoItem tipo) {
        List<List<String>> filas = detalles.stream().filter(d -> d.tipoItem() == tipo)
                .map(d -> List.of(d.descripcion(), String.valueOf(d.cantidad()),
                        DocumentoPdf.dinero(d.precioUnitario()), DocumentoPdf.dinero(d.subtotal()),
                        d.valorDescuento().signum() == 0 ? "-" : DocumentoPdf.dinero(d.valorDescuento()) + " ("
                                + DocumentoPdf.numero(d.porcentajeDescuento()) + " %)",
                        DocumentoPdf.numero(d.porcentajeIva()) + " %", DocumentoPdf.dinero(d.valorIva()),
                        DocumentoPdf.dinero(d.total()))).toList();
        if (!filas.isEmpty()) {
            pdf.seccion(titulo).tabla(List.of("Descripción", "Cant.", "Valor unitario", "Subtotal", "Descuento",
                            "IVA %", "IVA", "Total"), new float[]{2.8f, 0.7f, 1.5f, 1.5f, 1.6f, 0.8f, 1.3f, 1.5f}, filas,
                    Set.of(1, 2, 3, 4, 5, 6, 7));
        }
    }
}
