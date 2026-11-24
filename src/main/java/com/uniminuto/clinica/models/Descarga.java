package com.uniminuto.clinica.models;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

/** Archivo para descargar. */
public record Descarga(String nombre, String tipoContenido, byte[] contenido) {

    public ResponseEntity<byte[]> respuesta() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .header(HttpHeaders.CONTENT_TYPE, tipoContenido)
                .body(contenido);
    }
}
