package com.uniminuto.clinica.comun;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador sencillo de documentos PDF con el encabezado institucional de la clínica.
 * Se usa para fórmulas, prefacturas, facturas, comprobantes de despacho y reportes.
 */
public class PdfDocumento {

    /** Nombre de la clínica para los encabezados. */
    public static final String CLINICA = "Clínica Veterinaria Uniminuto";
    /** NIT de la clínica. */
    public static final String NIT = "NIT 900.123.456-7";
    /** Dirección y teléfono de la clínica. */
    public static final String DIRECCION = "Calle 80 # 73-49, Bogotá D.C. · Tel. (601) 291 6520";

    /** Color institucional de títulos y cabeceras. */
    private static final Color PRIMARIO = new Color(4, 169, 245);
    /** Color de fondo de etiquetas. */
    private static final Color GRIS = new Color(240, 243, 246);
    /** Fuente del título del documento. */
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(33, 37, 41));
    /** Fuente de los títulos de sección. */
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(33, 37, 41));
    /** Fuente del texto normal. */
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    /** Fuente de etiquetas y totales. */
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    /** Fuente de las cabeceras de tabla. */
    private static final Font CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
    /** Fuente de las celdas de tabla. */
    private static final Font CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8);
    /** Fuente de notas y datos secundarios. */
    private static final Font PEQUENA = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY);

    /** Contenido del PDF en memoria. */
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    /** Documento OpenPDF en construcción. */
    private final Document documento;

    /**
     * Crea el documento con el encabezado institucional.
     *
     * @param titulo título del documento.
     * @param horizontal true para orientación horizontal.
     */
    public PdfDocumento(String titulo, boolean horizontal) {
        documento = new Document(horizontal ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(documento, salida);
        documento.open();
        encabezado(titulo);
    }

    /**
     * Crea un documento vertical con el encabezado institucional.
     *
     * @param titulo título del documento.
     */
    public PdfDocumento(String titulo) {
        this(titulo, false);
    }

    /**
     * Agrega el encabezado con los datos de la clínica, el título y la fecha de generación.
     *
     * @param titulo título del documento.
     */
    private void encabezado(String titulo) {
        PdfPTable tabla = new PdfPTable(new float[]{3, 2});
        tabla.setWidthPercentage(100);
        PdfPCell izquierda = new PdfPCell();
        izquierda.setBorder(Rectangle.NO_BORDER);
        izquierda.addElement(new Paragraph(CLINICA, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, PRIMARIO)));
        izquierda.addElement(new Paragraph(NIT + "\n" + DIRECCION, PEQUENA));
        PdfPCell derecha = new PdfPCell();
        derecha.setBorder(Rectangle.NO_BORDER);
        Paragraph t = new Paragraph(titulo, TITULO);
        t.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(t);
        Paragraph fecha = new Paragraph("Generado: "
                + Tiempo.ahora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), PEQUENA);
        fecha.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(fecha);
        tabla.addCell(izquierda);
        tabla.addCell(derecha);
        documento.add(tabla);
        Paragraph linea = new Paragraph(new Chunk(" "));
        linea.setSpacingAfter(4);
        documento.add(linea);
    }

    /**
     * Agrega un título de sección.
     *
     * @param texto título.
     * @return el mismo documento (encadenable).
     */
    public PdfDocumento seccion(String texto) {
        Paragraph p = new Paragraph(texto, SUBTITULO);
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        documento.add(p);
        return this;
    }

    /**
     * Agrega un párrafo de texto.
     *
     * @param texto contenido.
     * @return el mismo documento (encadenable).
     */
    public PdfDocumento parrafo(String texto) {
        documento.add(new Paragraph(texto == null ? "" : texto, NORMAL));
        return this;
    }

    /** Pares etiqueta/valor en dos columnas. */
    public PdfDocumento campos(String[][] pares) {
        PdfPTable tabla = new PdfPTable(new float[]{1.3f, 3, 1.3f, 3});
        tabla.setWidthPercentage(100);
        for (String[] par : pares) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(par[0], NEGRITA));
            etiqueta.setBackgroundColor(GRIS);
            etiqueta.setBorderColor(Color.LIGHT_GRAY);
            PdfPCell valor = new PdfPCell(new Phrase(par.length > 1 && par[1] != null ? par[1] : "—", NORMAL));
            valor.setBorderColor(Color.LIGHT_GRAY);
            tabla.addCell(etiqueta);
            tabla.addCell(valor);
        }
        if (pares.length % 2 == 1) {
            PdfPCell vacia = new PdfPCell(new Phrase(""));
            vacia.setColspan(2);
            vacia.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(vacia);
        }
        documento.add(tabla);
        return this;
    }

    /** Tabla con cabecera; las columnas numéricas indicadas se alinean a la derecha. */
    public PdfDocumento tabla(String[] cabeceras, List<String[]> filas, float[] anchos, boolean[] derecha) {
        PdfPTable tabla = new PdfPTable(anchos == null ? uniforme(cabeceras.length) : anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String c : cabeceras) {
            PdfPCell celda = new PdfPCell(new Phrase(c, CABECERA));
            celda.setBackgroundColor(PRIMARIO);
            celda.setBorderColor(Color.WHITE);
            celda.setPadding(4);
            tabla.addCell(celda);
        }
        if (filas.isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("Sin registros.", CELDA));
            vacia.setColspan(cabeceras.length);
            tabla.addCell(vacia);
        }
        for (String[] fila : filas) {
            for (int i = 0; i < cabeceras.length; i++) {
                PdfPCell celda = new PdfPCell(new Phrase(i < fila.length && fila[i] != null ? fila[i] : "", CELDA));
                celda.setBorderColor(Color.LIGHT_GRAY);
                celda.setPadding(3);
                if (derecha != null && i < derecha.length && derecha[i]) {
                    celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                tabla.addCell(celda);
            }
        }
        documento.add(tabla);
        return this;
    }

    /** Bloque de totales alineado a la derecha. */
    public PdfDocumento totales(String[][] pares) {
        PdfPTable tabla = new PdfPTable(new float[]{4, 1.6f, 1.4f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(6);
        for (String[] par : pares) {
            PdfPCell vacia = new PdfPCell(new Phrase(""));
            vacia.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(vacia);
            PdfPCell etiqueta = new PdfPCell(new Phrase(par[0], NEGRITA));
            etiqueta.setBackgroundColor(GRIS);
            etiqueta.setBorderColor(Color.LIGHT_GRAY);
            tabla.addCell(etiqueta);
            PdfPCell valor = new PdfPCell(new Phrase(par[1], NEGRITA));
            valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            valor.setBorderColor(Color.LIGHT_GRAY);
            tabla.addCell(valor);
        }
        documento.add(tabla);
        return this;
    }

    /**
     * Agrega una nota al pie en letra pequeña.
     *
     * @param texto contenido.
     * @return el mismo documento (encadenable).
     */
    public PdfDocumento nota(String texto) {
        Paragraph p = new Paragraph(texto, PEQUENA);
        p.setSpacingBefore(10);
        documento.add(p);
        return this;
    }

    /**
     * Cierra el documento.
     *
     * @return bytes del PDF generado.
     */
    public byte[] cerrar() {
        documento.close();
        return salida.toByteArray();
    }

    /**
     * @param n número de columnas.
     * @return anchos iguales para n columnas.
     */
    private static float[] uniforme(int n) {
        float[] a = new float[n];
        java.util.Arrays.fill(a, 1f);
        return a;
    }
}
