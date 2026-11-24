package com.uniminuto.clinica.pdf;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.util.RenglonCobro;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PrefacturaPdf {

    private PrefacturaPdf() {
    }

    public static Descarga generar(ConfiguracionRs clinica, AtencionMedica a, List<RenglonCobro> renglones) {
        Medico medico = a.getMedico();
        Cliente cliente = a.getCita().getCliente();
        Mascota mascota = a.getMascota();
        Raza raza = mascota.getRaza();
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Fecha atención", DocumentoPdf.FECHA_HORA.format(a.getFechaInicio()));
        datos.put("Estado", Boolean.TRUE.equals(a.getFinalizada()) ? "Finalizada" : "En curso");
        datos.put("Propietario", cliente.getNombres() + " " + cliente.getApellidos());
        datos.put("Documento", cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento());
        datos.put("Mascota", mascota.getNombreMascota());
        datos.put("Especie / raza", raza == null ? "" : raza.getEspecie() + " / " + raza.getNombre());
        datos.put("Veterinario", medico.getNombres() + " " + medico.getApellidos());
        datos.put("Especialidad", medico.getEspecializacion().getNombre());
        DocumentoPdf pdf = new DocumentoPdf(clinica, "PREFACTURA", "Documento no válido como factura", null).datos(datos);
        seccion(pdf, "Servicios prestados", renglones, "Servicio");
        seccion(pdf, "Medicamentos formulados", renglones, "Medicamento");
        Map<String, String> totales = new LinkedHashMap<>();
        totales.put("Subtotal", DocumentoPdf.dinero(RenglonCobro.sumar(renglones, RenglonCobro::subtotal)));
        totales.put("IVA", DocumentoPdf.dinero(RenglonCobro.sumar(renglones, RenglonCobro::iva)));
        totales.put("Total estimado", DocumentoPdf.dinero(RenglonCobro.sumar(renglones, RenglonCobro::total)));
        return pdf.totales(totales)
                .texto("Solo se facturan los medicamentos efectivamente dispensados; el total puede variar.")
                .terminar("prefactura_" + a.getId());
    }

    private static void seccion(DocumentoPdf pdf, String titulo, List<RenglonCobro> renglones, String tipo) {
        List<List<String>> filas = renglones.stream().filter(r -> r.tipo().equals(tipo))
                .map(r -> List.of(r.descripcion(), String.valueOf(r.cantidad()),
                        DocumentoPdf.dinero(r.precioUnitario()), DocumentoPdf.dinero(r.subtotal()),
                        DocumentoPdf.numero(r.porcentajeIva()) + " %", DocumentoPdf.dinero(r.iva()),
                        DocumentoPdf.dinero(r.total()))).toList();
        pdf.seccion(titulo);
        if (filas.isEmpty()) {
            pdf.texto("Ninguno.");
        } else {
            pdf.tabla(List.of("Descripción", "Cant.", "Valor unitario", "Subtotal", "IVA %", "IVA", "Total"),
                    new float[]{3.4f, 0.8f, 1.6f, 1.6f, 0.9f, 1.4f, 1.7f}, filas, Set.of(1, 2, 3, 4, 5, 6));
        }
    }
}
