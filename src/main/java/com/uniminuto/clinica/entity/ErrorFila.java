package com.uniminuto.clinica.entity;

/** Error de una fila de la carga masiva. */
public record ErrorFila(String campo, String mensaje) {
}
