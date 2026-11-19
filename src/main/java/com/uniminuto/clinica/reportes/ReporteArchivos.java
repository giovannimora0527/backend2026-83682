package com.uniminuto.clinica.reportes;

import static com.uniminuto.clinica.reportes.ReporteDatos.Formato.*;
import static com.uniminuto.clinica.reportes.ReporteDatos.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
class ReporteArchivos {
    private static final String CLINICA="Clínica Veterinaria UNIMINUTO";
    private static final DateTimeFormatter MOMENTO=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DIA=DateTimeFormatter.ofPattern("dd/MM/yyyy");

    byte[] excel(Resultado r) throws IOException {
        try(var libro=new XSSFWorkbook();var bytes=new ByteArrayOutputStream()) {
            var resumen=libro.createSheet("Resumen");
            var detalle=libro.createSheet("Detalle");
            var moneda=estilo(libro,"\"COP\" #,##0.00;-\"COP\" #,##0.00");
            var fecha=estilo(libro,"dd/mm/yyyy");
            var instante=estilo(libro,"dd/mm/yyyy hh:mm:ss");
            var encabezado=libro.createCellStyle();
            var fuente=libro.createFont();
            fuente.setBold(true);
            fuente.setColor(IndexedColors.WHITE.getIndex());
            encabezado.setFont(fuente);
            encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            par(resumen,0,CLINICA,r.titulo());
            par(resumen,1,"Generado (Bogotá)",r.generado());
            resumen.getRow(1).getCell(1).setCellStyle(instante);
            int indice=2;
            for(var f:r.filtros().entrySet()) par(resumen,indice++,f.getKey(),f.getValue());
            if(r.filas().isEmpty()) par(resumen,indice++,"Resultado","No hubo resultados para los filtros seleccionados.");
            for(var t:r.totales().entrySet()) {
                par(resumen,indice,nombreTotal(r,t.getKey()),t.getValue());
                if(esMoneda(r,t.getKey())) resumen.getRow(indice).getCell(1).setCellStyle(moneda);
                indice++;
            }
            resumen.setColumnWidth(0,31*256);
            resumen.setColumnWidth(1,52*256);
            var cabecera=detalle.createRow(0);
            for(int i=0;i<r.columnas().size();i++) {
                var c=r.columnas().get(i);
                var celda=cabecera.createCell(i);
                celda.setCellValue(c.titulo());
                celda.setCellStyle(encabezado);
                detalle.setColumnWidth(i,Math.min(39,Math.max(16,c.titulo().length()+5))*256);
            }
            int pos=1;
            for(var f:r.filas()) {
                var fila=detalle.createRow(pos++);
                for(int i=0;i<r.columnas().size();i++) {
                    var c=r.columnas().get(i);
                    var celda=fila.createCell(i);
                    poner(celda,f.get(c.clave()));
                    switch(c.formato()) {
                        case COP->celda.setCellStyle(moneda);
                        case FECHA->celda.setCellStyle(fecha);
                        case FECHAHORA->celda.setCellStyle(instante);
                        default->{}
                    }
                }
            }
            detalle.createFreezePane(0,1);
            detalle.setAutoFilter(new CellRangeAddress(0,Math.max(0,pos-1),0,r.columnas().size()-1));
            libro.write(bytes);
            return bytes.toByteArray();
        }
    }

    private static CellStyle estilo(XSSFWorkbook libro,String patron) {
        var estilo=libro.createCellStyle();
        estilo.setDataFormat(libro.createDataFormat().getFormat(patron));
        return estilo;
    }

    private static void par(Sheet hoja,int i,String etiqueta,Object valor) {
        var fila=hoja.createRow(i);
        fila.createCell(0).setCellValue(etiqueta);
        poner(fila.createCell(1),valor);
    }

    private static void poner(Cell c,Object valor) {
        switch(valor) {
            case LocalDate d->c.setCellValue(d);
            case LocalDateTime f->c.setCellValue(f);
            case Number n->{
                BigDecimal exacto=new BigDecimal(n.toString()).stripTrailingZeros();
                if(exacto.precision()>15 || !Double.isFinite(n.doubleValue())) {
                    throw com.uniminuto.clinica.calendario.AgendaServicio.invalido(
                            "Un valor supera la precisión numérica de Excel. Usa PDF o acota la consulta.");
                }
                c.setCellValue(n.doubleValue());
            }
            case null->c.setBlank();
            default->c.setCellValue(valor.toString());
        }
    }

    byte[] pdf(Resultado r) throws IOException {
        var regular=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        var negrita=new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        try(var doc=new PDDocument();var bytes=new ByteArrayOutputStream()) {
            doc.getDocumentInformation().setTitle(r.titulo());
            doc.getDocumentInformation().setAuthor(CLINICA);
            paginaResumen(doc,r,regular,negrita);
            if(!r.filas().isEmpty()) paginasDetalle(doc,r,regular,negrita);
            doc.save(bytes);
            return bytes.toByteArray();
        }
    }

    private static PDPage pagina(PDDocument doc) {
        var a4=new PDRectangle(PDRectangle.A4.getHeight(),PDRectangle.A4.getWidth());
        var p=new PDPage(a4);
        doc.addPage(p);
        return p;
    }

    private static void cabecera(PDPageContentStream flujo,Resultado r,PDType1Font regular,PDType1Font negrita,
                                 int numero) throws IOException {
        escribir(flujo,negrita,15,32,560,CLINICA);
        escribir(flujo,negrita,12,32,539,r.titulo());
        escribir(flujo,regular,8,32,522,"Generado (Bogotá): "+MOMENTO.format(r.generado()));
        escribir(flujo,regular,8,730,24,"Página "+numero);
    }

    private static void paginaResumen(PDDocument doc,Resultado r,PDType1Font regular,PDType1Font negrita) throws IOException {
        try(var flujo=new PDPageContentStream(doc,pagina(doc))) {
            cabecera(flujo,r,regular,negrita,doc.getNumberOfPages());
            float y=496;
            for(var f:r.filtros().entrySet()) {
                escribir(flujo,regular,9,32,y,f.getKey()+": "+f.getValue());
                y-=14;
            }
            if(r.filas().isEmpty()) {
                escribir(flujo,negrita,10,32,y,"No hubo resultados para los filtros seleccionados.");
                y-=17;
            }
            escribir(flujo,negrita,11,32,y,"Totales y resumen");
            y-=17;
            for(var t:r.totales().entrySet()) {
                escribir(flujo,regular,9,32,y,nombreTotal(r,t.getKey())+": "+
                        mostrar(t.getValue(),esMoneda(r,t.getKey())?COP:TEXTO));
                y-=14;
            }
        }
    }

    private static void paginasDetalle(PDDocument doc,Resultado r,PDType1Font regular,PDType1Font negrita) throws IOException {
        float ancho=776f/r.columnas().size();
        var titulos=new ArrayList<List<String>>();
        for(var c:r.columnas()) titulos.add(partir(c.titulo(),negrita,ancho-5));
        int i=0;
        while(i<r.filas().size()) {
            try(var flujo=new PDPageContentStream(doc,pagina(doc))) {
                cabecera(flujo,r,regular,negrita,doc.getNumberOfPages());
                float y=488;
                y-=dibujarFila(flujo,negrita,titulos,ancho,y);
                while(i<r.filas().size()) {
                    var celdas=new ArrayList<List<String>>();
                    Map<String,Object> fila=r.filas().get(i);
                    for(var c:r.columnas()) celdas.add(partir(mostrar(fila.get(c.clave()),c.formato()),regular,ancho-5));
                    if(y-alto(celdas)<44) break;
                    y-=dibujarFila(flujo,regular,celdas,ancho,y);
                    i++;
                }
            }
        }
    }

    private static float alto(List<List<String>> celdas) {
        return celdas.stream().mapToInt(List::size).max().orElse(1)*11+4;
    }

    private static float dibujarFila(PDPageContentStream f,PDType1Font fuente,List<List<String>> celdas,
                                     float ancho,float y) throws IOException {
        float x=32;
        for(var celda:celdas) {
            float linea=y;
            for(var trozo:celda) {
                escribir(f,fuente,8,x,linea,trozo);
                linea-=11;
            }
            x+=ancho;
        }
        float alto=alto(celdas);
        f.setLineWidth(.25f);
        f.moveTo(32,y-alto+9);
        f.lineTo(808,y-alto+9);
        f.stroke();
        return alto;
    }

    private static List<String> partir(String texto,PDType1Font fuente,float ancho) throws IOException {
        var salida=new ArrayList<String>();
        var linea=new StringBuilder();
        for(String palabra:seguro(texto,fuente).split("\\s+")) {
            if(!linea.isEmpty() && ancho(fuente,linea+" "+palabra)>ancho) {
                salida.add(linea.toString());
                linea.setLength(0);
            }
            if(!linea.isEmpty()) linea.append(' ');
            for(int i=0;i<palabra.length();i++) {
                char letra=palabra.charAt(i);
                if(!linea.isEmpty() && ancho(fuente,linea.toString()+letra)>ancho) {
                    salida.add(linea.toString());
                    linea.setLength(0);
                }
                linea.append(letra);
            }
        }
        if(!linea.isEmpty()) salida.add(linea.toString());
        return salida;
    }

    private static float ancho(PDType1Font fuente,String texto) throws IOException {
        return fuente.getStringWidth(texto)*.008f;
    }

    private static void escribir(PDPageContentStream out,PDType1Font font,int tam,float x,float y,String texto) throws IOException {
        out.beginText();
        out.setFont(font,tam);
        out.newLineAtOffset(x,y);
        out.showText(seguro(texto,font));
        out.endText();
    }

    private static String seguro(String texto,PDType1Font fuente) {
        var salida=new StringBuilder();
        texto.codePoints().forEach(cp->{
            String letra=Character.isWhitespace(cp)?" ":new String(Character.toChars(cp));
            try { fuente.encode(letra); salida.append(letra); }
            catch(IOException | IllegalArgumentException e) { salida.append('?'); }
        });
        return salida.toString();
    }

    private static String mostrar(Object v,Formato tipo) {
        if(v==null) return "—";
        if(tipo==COP) return "COP "+new BigDecimal(v.toString()).setScale(2).toPlainString();
        if(v instanceof LocalDate d) return DIA.format(d);
        if(v instanceof LocalDateTime f) return MOMENTO.format(f);
        return v.toString();
    }

    private static boolean esMoneda(Resultado r,String campo) {
        return r.columnas().stream().anyMatch(c->c.clave().equals(campo) && c.formato()==COP);
    }

    static String nombreTotal(Resultado r,String campo) {
        return r.columnas().stream().filter(c->c.clave().equals(campo)).map(Columna::titulo).findFirst()
                .orElse(campo.replace('_',' '));
    }
}
