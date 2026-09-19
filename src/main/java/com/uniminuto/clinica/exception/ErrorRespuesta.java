package com.uniminuto.clinica.exception;

import java.time.LocalDateTime;

/**
 * Estructura estandar que se devuelve al cliente cada vez que ocurre un
 * error en la API, para que todos los errores tengan el mismo formato de
 * respuesta (mismo "contrato") sin importar en que parte del codigo
 * ocurrieron.
 */
public class ErrorRespuesta {

    /** Momento exacto en el que ocurrio el error. */
    private LocalDateTime fecha;

    /** Codigo de estado HTTP asociado al error (ej: 404, 400, 500). */
    private int codigo;

    /** Mensaje entendible explicando que salio mal. */
    private String mensaje;

    /** Ruta (endpoint) donde ocurrio el error. */
    private String ruta;

    /** Constructor vacio. */
    public ErrorRespuesta() {
    }

    /**
     * Crea una respuesta de error completa.
     *
     * @param codigo  codigo de estado HTTP
     * @param mensaje mensaje explicando el error
     * @param ruta    ruta del endpoint donde ocurrio el error
     */
    public ErrorRespuesta(int codigo, String mensaje, String ruta) {
        this.fecha = LocalDateTime.now();
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }

    /** @return la fecha y hora en la que ocurrio el error */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /** @param fecha nueva fecha y hora del error */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    /** @return el codigo de estado HTTP del error */
    public int getCodigo() {
        return codigo;
    }

    /** @param codigo nuevo codigo de estado HTTP del error */
    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    /** @return el mensaje explicando el error */
    public String getMensaje() {
        return mensaje;
    }

    /** @param mensaje nuevo mensaje explicando el error */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /** @return la ruta del endpoint donde ocurrio el error */
    public String getRuta() {
        return ruta;
    }

    /** @param ruta nueva ruta del endpoint donde ocurrio el error */
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
