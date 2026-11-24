package com.uniminuto.clinica.pdf;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.Descarga;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ComprobanteDespachoPdf {

    private ComprobanteDespachoPdf() {
    }

    /** @param pendientes cantidad pendiente por detalle de la formula, despues de este despacho. */
    public static Descarga generar(ConfiguracionRs clinica, Despacho d, List<FormulaMedicaDetalle> formulados,
                                   List<DespachoDetalle> entregas, Map<Long, Long> pendientes) {
        FormulaMedica f = d.getFormula();
        Cliente cliente = f.getCliente();
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Fórmula", f.getNumero());
        datos.put("Fecha y hora", DocumentoPdf.FECHA_HORA.format(d.getFechaConfirmacion()));
        datos.put("Despachado por", d.getUsuario().getUsername());
        datos.put("Veterinario", f.getMedico().getNombres() + " " + f.getMedico().getApellidos());
        datos.put("Propietario", cliente.getNombres() + " " + cliente.getApellidos());
        datos.put("Documento", cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento());
        datos.put("Mascota", f.getMascota().getNombreMascota());
        datos.put("Estado", d.getEstado().name());
        List<List<String>> filas = new ArrayList<>();
        for (FormulaMedicaDetalle detalle : formulados) {
            List<DespachoDetalle> propias = entregas.stream()
                    .filter(x -> x.getFormulaDetalle().getId().equals(detalle.getId())).toList();
            String pendiente = String.valueOf(pendientes.get(detalle.getId()));
            if (propias.isEmpty()) {
                filas.add(List.of(detalle.getMedicamento().getNombre(), "-", "-",
                        String.valueOf(detalle.getCantidadPrescrita()), "0", pendiente));
            }
            for (DespachoDetalle x : propias) {
                filas.add(List.of(detalle.getMedicamento().getNombre(), x.getLote().getNumeroLote(),
                        x.getLote().getFechaVencimiento() == null ? "-" : x.getLote().getFechaVencimiento().toString(),
                        String.valueOf(detalle.getCantidadPrescrita()), String.valueOf(x.getCantidadEntregada()),
                        pendiente));
            }
        }
        DocumentoPdf pdf = new DocumentoPdf(clinica, "COMPROBANTE DE DESPACHO " + d.getNumero(), null,
                d.getEstado() == EstadoPreparacion.REVERTIDO ? "REVERTIDO" : null)
                .datos(datos)
                .seccion("Medicamentos")
                .tabla(List.of("Medicamento", "Lote", "Vencimiento", "Prescrita", "Entregada", "Pendiente"),
                        new float[]{3.2f, 1.6f, 1.5f, 1.2f, 1.2f, 1.2f}, filas, Set.of(3, 4, 5));
        if (d.getObservacion() != null && !d.getObservacion().isBlank()) {
            pdf.seccion("Observación").texto(d.getObservacion());
        }
        if (d.getMotivoReversion() != null) {
            pdf.seccion("Reversión").texto(DocumentoPdf.FECHA_HORA.format(d.getFechaReversion()) + " por "
                    + d.getUsuarioReversion().getUsername() + ": " + d.getMotivoReversion());
        }
        return pdf.firma("Recibe: nombre, documento y firma").terminar("despacho_" + d.getNumero());
    }
}
