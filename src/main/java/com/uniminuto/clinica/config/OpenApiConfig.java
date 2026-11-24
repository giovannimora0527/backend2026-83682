package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación Swagger/OpenAPI disponible en /clinica/v1/swagger-ui.html.
 * Use el botón "Authorize" con el token obtenido en POST /auth/login.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clinicaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Clínica Veterinaria Uniminuto")
                        .version("1.0.0")
                        .description("Servicios REST de inventario, citas, atención médica, fórmulas, despacho, "
                                + "facturación y reportes. Autenticación JWT (Bearer)."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
