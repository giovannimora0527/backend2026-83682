package com.uniminuto.clinica.comun;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** Respuestas HTTP de descarga de archivos (PDF y Excel). */
public final class Archivo {

    public static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private Archivo() {
    }

    public static ResponseEntity<byte[]> pdf(byte[] contenido, String nombre) {
        return descarga(contenido, nombre, MediaType.APPLICATION_PDF);
    }

    public static ResponseEntity<byte[]> excel(byte[] contenido, String nombre) {
        return descarga(contenido, nombre, XLSX);
    }

    private static ResponseEntity<byte[]> descarga(byte[] contenido, String nombre, MediaType tipo) {
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nombre, StandardCharsets.UTF_8).build().toString())
                .body(contenido);
    }
}
