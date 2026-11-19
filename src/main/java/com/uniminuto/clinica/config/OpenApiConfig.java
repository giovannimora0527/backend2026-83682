package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Describe los contratos HTTP bajo el mismo prefijo para acceso directo y por Nginx. */
@Configuration
public class OpenApiConfig {
    private static final String BEARER = "jwtBearer";

    @Bean
    OpenAPI clinicaOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Clínica Veterinaria UNIMINUTO").version("v1")
                        .description("Usa POST /auth/login y luego autoriza con el JWT recibido. "
                                + "Los permisos y el acceso a recursos propios se verifican en el servidor."))
                .servers(List.of(new Server().url("/clinica/v1")))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSchemas("ErrorApi", new ObjectSchema()
                                .addProperty("status", new IntegerSchema())
                                .addProperty("error", new StringSchema())
                                .addProperty("mensaje", new StringSchema())));
    }

    @Bean
    OpenApiCustomizer operacionesClinica() {
        return api -> api.getPaths().forEach((ruta, item) ->
                item.readOperationsMap().forEach((metodo, operacion) -> documentar(ruta, metodo, operacion)));
    }

    private void documentar(String ruta, PathItem.HttpMethod metodo, Operation operacion) {
        String modulo = ruta.split("/", 3)[1];
        operacion.setTags(List.of(etiqueta(modulo)));
        boolean login = ruta.equals("/auth/login") && metodo == PathItem.HttpMethod.POST;
        if (ruta.equals("/clinica/test")) {
            return;
        }
        error(operacion, "401", login ? "Credenciales inválidas." : "Sesión ausente, inválida o vencida.");
        error(operacion, "403", "El perfil o la identidad carece de permiso.");
        if (!login) {
            operacion.addSecurityItem(new SecurityRequirement().addList(BEARER));
        }

        boolean escritura = metodo == PathItem.HttpMethod.POST || metodo == PathItem.HttpMethod.PUT
                || metodo == PathItem.HttpMethod.PATCH || metodo == PathItem.HttpMethod.DELETE;
        if (escritura || operacion.getParameters() != null) {
            error(operacion, "400", "La solicitud contiene datos inválidos.");
        }
        if (ruta.contains("{") && !modulo.equals("reportes")) {
            error(operacion, "404", "El recurso solicitado no existe.");
        }
        if (escritura && List.of("inventario", "cita", "atencion", "formula-medica",
                "tarifario", "despacho", "facturacion").contains(modulo)) {
            error(operacion, "409", "La operación entra en conflicto con el estado del recurso.");
        }
        if (operacion.getDescription() == null) {
            operacion.setDescription(regla(modulo));
        }
    }

    private void error(Operation operacion, String estado, String descripcion) {
        operacion.getResponses().putIfAbsent(estado, new ApiResponse().description(descripcion)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new ObjectSchema().$ref("#/components/schemas/ErrorApi")))));
    }

    private String etiqueta(String modulo) {
        return switch (modulo) {
            case "auth", "menu", "usuario", "perfil" -> "Seguridad y permisos";
            case "inventario", "medicamento" -> "Inventario";
            case "cita" -> "Citas y calendario";
            case "atencion", "historia-medica", "anotacion-historia", "formula-medica", "tarifario" ->
                    "Atención clínica";
            case "despacho" -> "Despacho";
            case "facturacion" -> "Facturación y pagos";
            case "reportes" -> "Reportes";
            default -> "Catálogos y registros";
        };
    }

    private String regla(String modulo) {
        return switch (modulo) {
            case "inventario" -> "Los movimientos registran el cambio de existencias por lote.";
            case "cita" -> "La agenda aplica disponibilidad y transiciones de estado por médico.";
            case "atencion", "formula-medica" -> "Las operaciones clínicas aplican la identidad del médico.";
            case "despacho" -> "Confirma cantidades pendientes contra lotes elegibles y registra sus movimientos.";
            case "facturacion" -> "Calcula importes y saldos en el servidor y conserva la trazabilidad de pagos.";
            case "reportes" -> "Aplica filtros por tipo y permisos por perfil para consultas y exportaciones.";
            default -> null;
        };
    }
}
