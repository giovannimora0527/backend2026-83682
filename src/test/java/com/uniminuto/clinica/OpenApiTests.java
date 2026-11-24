package com.uniminuto.clinica;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiTests extends PruebaIntegracion {

    @Autowired ObjectMapper json;

    @Override
    protected void limpiarDatos() {
    }

    @Test
    void todosLosEndpointsTienenResumenGrupoYRoles() throws Exception {
        JsonNode api = json.readTree(mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        int[] total = {0};
        api.get("paths").properties().forEach(ruta -> ruta.getValue().properties().forEach(op -> {
            total[0]++;
            String nombre = op.getKey() + " " + ruta.getKey();
            assertTrue(op.getValue().has("summary"), nombre + " sin resumen");
            assertTrue(op.getValue().get("tags").get(0).asString().matches("[A-ZÁÉÍÓÚ].*"), nombre + " sin grupo");
            assertTrue(op.getValue().get("description").asString().contains("**Roles:**"), nombre + " sin roles");
            boolean publico = op.getValue().get("description").asString().contains("público");
            assertEquals(!publico, op.getValue().has("security"), nombre);
        }));
        assertTrue(total[0] > 100, "endpoints: " + total[0]);
        assertTrue(api.get("components").get("securitySchemes").has("bearerAuth"));
    }

    @Test
    void swaggerYSaludSonPublicos() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }
}
