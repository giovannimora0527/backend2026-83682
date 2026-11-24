package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Documentacion OpenAPI. Los roles salen de @PreAuthorize, asi la documentacion no se desvia del codigo. */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";
    private static final Pattern ROL = Pattern.compile("'([A-Z_]+)'");
    private static final Map<String, String> ERRORES = Map.of("400", "Datos inválidos", "401",
            "Sin token, token vencido o sesión cerrada", "403", "El rol no tiene permiso", "404", "No existe",
            "409", "Regla de negocio o duplicado");

    @Bean
    OpenAPI openApi() {
        Schema<?> detalle = new ObjectSchema().addProperty("campo", new StringSchema())
                .addProperty("mensaje", new StringSchema());
        Schema<?> error = new ObjectSchema().addProperty("status", new IntegerSchema())
                .addProperty("error", new StringSchema()).addProperty("message", new StringSchema())
                .addProperty("detalles", new ArraySchema().items(detalle));
        return new OpenAPI()
                .info(new Info().title("Clínica Veterinaria Uniminuto").version("1.0.0").description("""
                        API de la clínica veterinaria. Para probar: POST /auth/login, tomar el OTP del correo \
                        (Mailpit: http://localhost:8025), POST /auth/validate-token y pegar el token en Authorize."""))
                .components(new Components().addSchemas("Error", error).addSecuritySchemes(ESQUEMA,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    OperationCustomizer rolesYErrores() {
        return (operacion, metodo) -> {
            PreAuthorize permiso = permiso(metodo);
            boolean publico = permiso == null || permiso.value().contains("permitAll");
            String roles = publico ? "público" : permiso.value().contains("isAuthenticated") ? "cualquier usuario autenticado"
                    : String.join(", ", roles(permiso.value()));
            String descripcion = operacion.getDescription();
            operacion.setDescription((descripcion == null ? "" : descripcion + "\n\n") + "**Roles:** " + roles + ".");
            List<String> codigos = publico ? List.of("400") : List.of("400", "401", "403", "404", "409");
            codigos.forEach(codigo -> operacion.getResponses().addApiResponse(codigo, new ApiResponse()
                    .description(ERRORES.get(codigo)).content(new Content().addMediaType("application/json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/Error"))))));
            if (!publico) {
                operacion.addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
            }
            return operacion;
        };
    }

    private static PreAuthorize permiso(HandlerMethod metodo) {
        PreAuthorize propio = AnnotatedElementUtils.findMergedAnnotation(metodo.getMethod(), PreAuthorize.class);
        return propio != null ? propio
                : AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), PreAuthorize.class);
    }

    private static List<String> roles(String expresion) {
        Matcher m = ROL.matcher(expresion);
        return m.results().map(r -> r.group(1)).toList();
    }
}
