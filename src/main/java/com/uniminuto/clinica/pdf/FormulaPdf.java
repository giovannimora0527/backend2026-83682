package com.uniminuto.clinica.pdf;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.Descarga;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FormulaPdf {

    private FormulaPdf() {
    }

    public static Descarga generar(ConfiguracionRs clinica, FormulaMedica f, List<FormulaMedicaDetalle> detalles) {
        Medico medico = f.getMedico();
        Cliente cliente = f.getCliente();
        Mascota mascota = f.getMascota();
        Raza raza = mascota.getRaza();
        String numero = f.getNumero() == null ? "BORRADOR" : f.getNumero();
        String marca = f.getEstadoFormula() == EstadoFormula.CONFIRMADA ? null : f.getEstadoFormula().name();
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Fecha", DocumentoPdf.FECHA_HORA.format(f.getFechaConfirmacion() == null
                ? f.getFechaGeneracion() : f.getFechaConfirmacion()));
        datos.put("Estado", f.getEstadoFormula().name());
        datos.put("Veterinario", medico.getNombres() + " " + medico.getApellidos());
        datos.put("Especialidad", medico.getEspecializacion().getNombre());
        datos.put("Documento vet.", medico.getTipoDocumento() + " " + medico.getNumeroDocumento());
        datos.put("Registro prof.", medico.getRegistroProfesional());
        datos.put("Propietario", cliente.getNombres() + " " + cliente.getApellidos());
        datos.put("Doc. propietario", cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento());
        datos.put("Mascota", mascota.getNombreMascota());
        datos.put("Especie / raza", raza == null ? "" : raza.getEspecie() + " / " + raza.getNombre());
        datos.put("Edad", mascota.getEdad() == null ? "" : mascota.getEdad() + " años");
        datos.put("Teléfono", cliente.getTelefono());
        DocumentoPdf pdf = new DocumentoPdf(clinica, "FÓRMULA MÉDICA " + numero, null, marca)
                .datos(datos)
                .seccion("Medicamentos")
                .tabla(List.of("Medicamento", "Presentación", "Dosis", "Frecuencia", "Duración", "Vía", "Cantidad",
                        "Indicaciones"), new float[]{2.4f, 1.8f, 1.3f, 1.4f, 1.1f, 1.4f, 1.1f, 2.6f},
                        detalles.stream().map(FormulaPdf::fila).toList(), Set.of(6));
        if (f.getIndicacionesGenerales() != null && !f.getIndicacionesGenerales().isBlank()) {
            pdf.seccion("Indicaciones generales").texto(f.getIndicacionesGenerales());
        }
        if (f.getMotivoAnulacion() != null) {
            pdf.seccion("Motivo de anulación").texto(f.getMotivoAnulacion());
        }
        return pdf.firma(medico.getNombres() + " " + medico.getApellidos() + " - " + medico.getRegistroProfesional())
                .terminar("formula_" + (f.getNumero() == null ? f.getId() : f.getNumero()));
    }

    private static List<String> fila(FormulaMedicaDetalle d) {
        Medicamento m = d.getMedicamento();
        return List.of(m.getNombre(), m.getPresentacion() == null ? "" : m.getPresentacion(),
                DocumentoPdf.numero(d.getDosis()) + " " + d.getUnidadDosis(),
                "Cada " + d.getFrecuenciaHoras() + " horas",
                d.getDuracionDias() + (d.getDuracionDias() == 1 ? " día" : " días"), d.getViaAdministracion().name(),
                String.valueOf(d.getCantidadPrescrita()), d.getIndicaciones() == null ? "" : d.getIndicaciones());
    }
}
