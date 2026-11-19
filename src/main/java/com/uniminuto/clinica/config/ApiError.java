package com.uniminuto.clinica.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

public record ApiError(int status, String error, String mensaje) {
    private static final ObjectMapper JSON = new ObjectMapper();

    public static ApiError de(int status, String mensaje) {
        HttpStatus codigo = HttpStatus.resolve(status);
        return new ApiError(status, codigo == null ? "Error HTTP" : codigo.getReasonPhrase(), mensaje);
    }

    public static void escribir(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        JSON.writeValue(response.getOutputStream(), de(status, mensaje));
    }
}
