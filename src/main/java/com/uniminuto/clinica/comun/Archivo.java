package com.uniminuto.clinica.comun;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** Respuestas HTTP de descarga de archivos (PDF y Excel). */
public final class Archivo {

    /** Tipo MIME de los libros de Excel (.xlsx). */
    public static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    /** Clase de utilidades: no se instancia. */
    private Archivo() {
    }

    /**
     * Respuesta de descarga de un PDF.
     *
     * @param contenido bytes del archivo.
     * @param nombre nombre sugerido del archivo.
     * @return respuesta HTTP con Content-Disposition attachment.
     */
    public static ResponseEntity<byte[]> pdf(byte[] contenido, String nombre) {
        return descarga(contenido, nombre, MediaType.APPLICATION_PDF);
    }

    /**
     * Respuesta de descarga de un Excel.
     *
     * @param contenido bytes del archivo.
     * @param nombre nombre sugerido del archivo.
     * @return respuesta HTTP con Content-Disposition attachment.
     */
    public static ResponseEntity<byte[]> excel(byte[] contenido, String nombre) {
        return descarga(contenido, nombre, XLSX);
    }

    /**
     * Construye la respuesta de descarga.
     *
     * @param contenido bytes del archivo.
     * @param nombre nombre del archivo (UTF-8).
     * @param tipo tipo MIME.
     * @return respuesta HTTP.
     */
    private static ResponseEntity<byte[]> descarga(byte[] contenido, String nombre, MediaType tipo) {
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nombre, StandardCharsets.UTF_8).build().toString())
                .body(contenido);
    }
}
