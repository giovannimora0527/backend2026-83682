package com.uniminuto.clinica.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.Descarga;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Base comun de los PDF: encabezado de la clinica, tablas, numero de pagina y marca de agua. */
public class DocumentoPdf {

    public static final String TIPO = "application/pdf";
    static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final Color AZUL = new Color(31, 78, 121);
    private static final Color GRIS = new Color(225, 230, 236);

    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private final Document documento = new Document(PageSize.LETTER, 40, 40, 40, 45);

    /** @param marcaAgua texto en diagonal, o nulo. */
    public DocumentoPdf(ConfiguracionRs clinica, String titulo, String subtitulo, String marcaAgua) {
        PdfWriter.getInstance(documento, salida).setPageEvent(new Pie(marcaAgua));
        documento.addTitle(titulo);
        documento.open();
        parrafo(clinica.nombre(), 16, true, AZUL, Element.ALIGN_LEFT);
        parrafo(Stream.of("NIT " + clinica.nit(), clinica.direccion(), clinica.telefono(), clinica.correo())
                .filter(t -> t != null && !t.isBlank()).collect(Collectors.joining("  |  ")), 8, false, Color.DARK_GRAY,
                Element.ALIGN_LEFT);
        espacio();
        parrafo(titulo, 14, true, Color.BLACK, Element.ALIGN_CENTER);
        if (subtitulo != null) {
            parrafo(subtitulo, 9, false, Color.DARK_GRAY, Element.ALIGN_CENTER);
        }
        espacio();
    }

    /** Pares etiqueta-valor en dos columnas. */
    public DocumentoPdf datos(Map<String, String> datos) {
        PdfPTable tabla = new PdfPTable(new float[]{1.2f, 2.6f, 1.2f, 2.6f});
        tabla.setWidthPercentage(100);
        datos.forEach((etiqueta, valor) -> {
            tabla.addCell(celda(etiqueta, true, GRIS, Element.ALIGN_LEFT));
            tabla.addCell(celda(valor == null ? "" : valor, false, null, Element.ALIGN_LEFT));
        });
        tabla.completeRow();
        documento.add(tabla);
        return espacio();
    }

    /** @param derecha indices de las columnas numericas. El encabezado se repite en cada pagina. */
    public DocumentoPdf tabla(List<String> encabezados, float[] anchos, List<List<String>> filas, Set<Integer> derecha) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        encabezados.forEach(e -> tabla.addCell(celda(e, true, GRIS, Element.ALIGN_CENTER)));
        for (List<String> fila : filas) {
            for (int i = 0; i < fila.size(); i++) {
                tabla.addCell(celda(fila.get(i), false, null,
                        derecha.contains(i) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT));
            }
        }
        documento.add(tabla);
        return espacio();
    }

    /** Totales alineados a la derecha; el ultimo va en negrita. */
    public DocumentoPdf totales(Map<String, String> totales) {
        PdfPTable tabla = new PdfPTable(new float[]{3, 2});
        tabla.setWidthPercentage(40);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        int i = 0;
        for (Map.Entry<String, String> e : totales.entrySet()) {
            boolean ultimo = ++i == totales.size();
            tabla.addCell(celda(e.getKey(), true, ultimo ? GRIS : null, Element.ALIGN_LEFT));
            tabla.addCell(celda(e.getValue(), ultimo, ultimo ? GRIS : null, Element.ALIGN_RIGHT));
        }
        documento.add(tabla);
        return espacio();
    }

    public DocumentoPdf seccion(String titulo) {
        parrafo(titulo, 11, true, AZUL, Element.ALIGN_LEFT);
        return espacio();
    }

    public DocumentoPdf texto(String texto) {
        parrafo(texto, 9, false, Color.BLACK, Element.ALIGN_LEFT);
        return espacio();
    }

    public DocumentoPdf firma(String texto) {
        espacio();
        espacio();
        parrafo("______________________________", 10, false, Color.BLACK, Element.ALIGN_LEFT);
        parrafo(texto, 9, false, Color.BLACK, Element.ALIGN_LEFT);
        return this;
    }

    public Descarga terminar(String nombre) {
        documento.close();
        return new Descarga(nombre + ".pdf", TIPO, salida.toByteArray());
    }

    public static String dinero(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0.00", simbolos).format(valor);
    }

    public static String numero(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private DocumentoPdf espacio() {
        parrafo(" ", 5, false, Color.BLACK, Element.ALIGN_LEFT);
        return this;
    }

    private void parrafo(String texto, float puntos, boolean negrita, Color color, int alineacion) {
        Paragraph p = new Paragraph(texto, fuente(puntos, negrita, color));
        p.setAlignment(alineacion);
        documento.add(p);
    }

    private static PdfPCell celda(String texto, boolean negrita, Color fondo, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente(8, negrita, Color.BLACK)));
        celda.setPadding(4);
        celda.setBorderColor(Color.LIGHT_GRAY);
        celda.setHorizontalAlignment(alineacion);
        if (fondo != null) {
            celda.setBackgroundColor(fondo);
        }
        return celda;
    }

    private static com.lowagie.text.Font fuente(float puntos, boolean negrita, Color color) {
        return FontFactory.getFont(negrita ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, puntos, com.lowagie.text.Font.NORMAL, color);
    }

    private static final class Pie extends PdfPageEventHelper {

        private final String marcaAgua;
        private final String generado = FECHA_HORA.format(LocalDateTime.now());

        private Pie(String marcaAgua) {
            this.marcaAgua = marcaAgua;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte lienzo = writer.getDirectContent();
            ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT, new Phrase("Generado: " + generado,
                    fuente(7, false, Color.GRAY)), documento.left(), 25, 0);
            ColumnText.showTextAligned(lienzo, Element.ALIGN_RIGHT, new Phrase("Página " + writer.getPageNumber(),
                    fuente(7, false, Color.GRAY)), documento.right(), 25, 0);
            if (marcaAgua != null) {
                PdfContentByte fondo = writer.getDirectContentUnder();
                ColumnText.showTextAligned(fondo, Element.ALIGN_CENTER, new Phrase(marcaAgua,
                                fuente(90, true, new Color(235, 215, 215))),
                        documento.getPageSize().getWidth() / 2, documento.getPageSize().getHeight() / 2, 45);
            }
        }
    }
}
