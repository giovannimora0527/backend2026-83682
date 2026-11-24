package com.uniminuto.clinica.reporte;

public record Columna(String clave, String titulo, TipoColumna tipo) {

    public static Columna texto(String clave, String titulo) {
        return new Columna(clave, titulo, TipoColumna.TEXTO);
    }

    public static Columna numero(String clave, String titulo) {
        return new Columna(clave, titulo, TipoColumna.NUMERO);
    }

    public static Columna moneda(String clave, String titulo) {
        return new Columna(clave, titulo, TipoColumna.MONEDA);
    }

    public static Columna fecha(String clave, String titulo) {
        return new Columna(clave, titulo, TipoColumna.FECHA);
    }

    public static Columna fechaHora(String clave, String titulo) {
        return new Columna(clave, titulo, TipoColumna.FECHA_HORA);
    }

    public static Columna porcentaje(String clave, String titulo) {
        return new Columna(clave, titulo, TipoColumna.PORCENTAJE);
    }
}
