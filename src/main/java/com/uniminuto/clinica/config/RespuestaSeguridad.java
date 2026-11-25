package com.uniminuto.clinica.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

/** Respuestas JSON de Spring Security (401 y 403) con el formato común {status, error, mensaje}. */
final class RespuestaSeguridad {

    /** Clase de utilidades: no se instancia. */
    private RespuestaSeguridad() {
    }

    /**
     * Escribe la respuesta de error.
     *
     * @param res respuesta HTTP.
     * @param status 401 o 403.
     * @param mensaje mensaje para el usuario.
     * @throws IOException error de escritura.
     */
    static void escribir(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"error\":\"" + (status == 401 ? "UNAUTHORIZED" : "FORBIDDEN")
                + "\",\"mensaje\":\"" + mensaje.replace("\"", "\\\"") + "\"}");
    }
}
