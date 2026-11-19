package com.uniminuto.clinica.atencion;

import static com.uniminuto.clinica.atencion.ClinicaDatos.*;
import static com.uniminuto.clinica.calendario.AgendaServicio.conflicto;

import com.uniminuto.clinica.entity.FormulaMedica;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class DocumentosClinicos {
    private static final String CLINICA="Clínica Veterinaria UNIMINUTO";
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale CO=Locale.forLanguageTag("es-CO");

    byte[] formula(Formula f,Paciente p) throws IOException {
        if(!f.estado().equals("CONFIRMADA")) throw conflicto("Confirma la fórmula antes de descargar el PDF.");
        var lineas=base(p,f.confirmada());
        for(Detalle d:f.medicamentos()) {
            lineas.add(new Linea(d.nombre()+" · "+d.cantidad()+" unidades",true));
            lineas.add(new Linea("Dosis: "+d.dosis()+" · Frecuencia: "+d.frecuencia(),false));
            lineas.add(new Linea("Duración: "+d.duracion()+" · Vía: "+d.via(),false));
            lineas.add(new Linea("Indicaciones: "+valor(d.indicaciones()),false));
        }
        return pdf("Fórmula médica n.º "+f.id(),lineas);
    }

    byte[] legada(FormulaMedica f,Paciente p,String medicamento) throws IOException {
        var lineas=base(p,f.getFechaEmision()==null?f.getFechaCreacionRegistro():f.getFechaEmision());
        lineas.add(new Linea(medicamento,true));
        lineas.add(new Linea("Dosis: "+valor(f.getDosis())+" · Frecuencia: "+valor(f.getFrecuencia()),false));
        lineas.add(new Linea("Duración: "+valor(f.getDuracionTratamiento()),false));
        lineas.add(new Linea("Cantidad y vía: datos sin registro en la fórmula histórica.",false));
        lineas.add(new Linea("Indicaciones: "+valor(f.getIndicaciones()),false));
        return pdf("Fórmula médica histórica n.º "+f.getId(),lineas);
    }

    byte[] prefactura(Atencion a) throws IOException {
        var c=a.atencion();
        if(c==null || c.finalizada()==null) throw conflicto("Finaliza la atención para generar la prefactura.");
        var lineas=base(a.cita(),c.finalizada());
        lineas.add(new Linea("Especialidad: "+c.especialidad(),false));
        lineas.add(new Linea("Servicios prestados",true));
        for(var s:a.servicios()) {
            lineas.add(new Linea(s.cantidad()+" × "+s.nombre()+" · "+dinero(s.valor())+" por unidad",false));
            lineas.add(new Linea("IVA "+s.iva().toPlainString()+" % · Otros impuestos "+s.impuesto().toPlainString()+" %",false));
        }
        lineas.add(new Linea("Medicamentos formulados · estimación",true));
        boolean medicamentos=false;
        for(var f:a.formulas()) if(f.estado().equals("CONFIRMADA")) {
            for(var d:f.medicamentos()) {
                medicamentos=true;
                lineas.add(new Linea(d.cantidad()+" × "+d.nombre()+" · "+dinero(d.precioEstimado())+" por unidad",false));
            }
        }
        if(!medicamentos) lineas.add(new Linea("Sin medicamentos formulados en esta atención.",false));
        lineas.add(new Linea("Medicamentos entregados: sin registro de dispensación en esta prefactura.",false));
        var t=AtencionServicio.totales(a);
        lineas.add(new Linea("Subtotal estimado: "+dinero(t.subtotal()),true));
        lineas.add(new Linea("Impuestos estimados de servicios: "+dinero(t.impuestos()),false));
        lineas.add(new Linea("TOTAL ESTIMADO: "+dinero(t.total()),true));
        lineas.add(new Linea("Documento informativo de cotización. Sin factura ni registro de pago.",false));
        return pdf("Prefactura · Cita n.º "+a.cita().citaId(),lineas);
    }

    private List<Linea> base(Paciente p,LocalDateTime fecha) {
        var lineas=new ArrayList<Linea>();
        lineas.add(new Linea("Fecha (Bogotá): "+(fecha==null?"Sin registro":FECHA.format(fecha))+" · Cita "+p.citaId(),false));
        lineas.add(new Linea("Veterinario: "+p.medico()+" · Registro: "+p.registro(),false));
        lineas.add(new Linea("Mascota: "+p.mascota()+" · Propietario: "+p.propietario(),false));
        return lineas;
    }

    private String valor(String v) { return v==null || v.isBlank()?"Sin registro":v; }
    private String dinero(BigDecimal n) { return "COP "+NumberFormat.getNumberInstance(CO).format(n.setScale(2)); }
    public byte[] comprobante(String titulo,List<String> datos) throws IOException {
        return pdf(titulo,datos.stream().map(texto->new Linea(texto,false)).toList());
    }

    private record Linea(String texto,boolean destacado) {}

    private byte[] pdf(String titulo,List<Linea> lineas) throws IOException {
        var regular=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        var negrita=new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        try(var doc=new PDDocument();var bytes=new ByteArrayOutputStream()) {
            doc.getDocumentInformation().setTitle(titulo);
            doc.getDocumentInformation().setAuthor(CLINICA);
            var preparada=new ArrayList<Linea>();
            for(var linea:lineas) {
                var font=linea.destacado()?negrita:regular;
                for(String parte:partir(linea.texto(),font)) preparada.add(new Linea(parte,linea.destacado()));
            }
            final int capacidad=42;
            for(int offset=0;offset<preparada.size();offset+=capacidad) {
                var pagina=new PDPage(PDRectangle.A4);
                doc.addPage(pagina);
                try(var flujo=new PDPageContentStream(doc,pagina)) {
                    escribir(flujo,negrita,16,785,CLINICA);
                    escribir(flujo,negrita,14,760,titulo);
                    flujo.setLineWidth(.5f); flujo.moveTo(48,745);flujo.lineTo(545,745);flujo.stroke();
                    float y=721;
                    for(var linea:preparada.subList(offset,Math.min(offset+capacidad,preparada.size()))) {
                        escribir(flujo,linea.destacado()?negrita:regular,10,y,linea.texto()); y-=15;
                    }
                    escribir(flujo,regular,9,47,"Página "+doc.getNumberOfPages());
                }
            }
            doc.save(bytes);
            return bytes.toByteArray();
        }
    }

    private List<String> partir(String texto,PDType1Font font) throws IOException {
        var normal=new StringBuilder();
        texto.codePoints().forEach(cp->{
            String letra=new String(Character.toChars(cp));
            try { if(Character.isWhitespace(cp)) normal.append(' '); else {font.encode(letra); normal.append(letra);} }
            catch (IOException | IllegalArgumentException e){ normal.append('?'); }
        });
        var partes=new ArrayList<String>();
        var linea=new StringBuilder();
        for(String palabra:normal.toString().strip().split("\\s+")) {
            if(font.getStringWidth(palabra)*.01f>485) {
                if(!linea.isEmpty()){partes.add(linea.toString());linea.setLength(0);}
                for(int i=0;i<palabra.length();i+=45) partes.add(palabra.substring(i,Math.min(i+45,palabra.length())));
                continue;
            }
            String propuesta=linea.isEmpty()?palabra:linea+" "+palabra;
            if(font.getStringWidth(propuesta)*.01f>485) {
                partes.add(linea.toString()); linea.setLength(0); linea.append(palabra);
            } else { linea.setLength(0); linea.append(propuesta); }
        }
        if(!linea.isEmpty()) partes.add(linea.toString());
        return partes;
    }

    private void escribir(PDPageContentStream out,PDType1Font font,int tam,float y,String texto) throws IOException {
        out.beginText();out.setFont(font,tam);out.newLineAtOffset(48,y);out.showText(texto);out.endText();
    }
}
