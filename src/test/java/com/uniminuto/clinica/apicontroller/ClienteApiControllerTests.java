package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.GlobalExceptionHandler;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ClienteActualizarRq;
import com.uniminuto.clinica.service.ClienteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClienteApiControllerTests {

    private static final String BODY = """
            {
              "tipoDocumento": "CC",
              "numeroDocumento": "001234",
              "nombres": "Laura",
              "apellidos": "Morales",
              "fechaNacimiento": "1995-06-15",
              "genero": "F",
              "telefono": "3001234567",
              "direccion": "Calle 10"
            }
            """;

    private ClienteService service;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void preparar() {
        service = mock(ClienteService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new ClienteApiController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void cerrarValidator() {
        validator.close();
    }

    @Test
    void postRecibeIdYDtoYDevuelveClienteActualizado() throws Exception {
        Cliente actualizado = new Cliente();
        actualizado.setId(7L);
        actualizado.setUsuarioId(42);
        actualizado.setActivo(false);
        actualizado.setNumeroDocumento("001234");
        actualizado.setFechaNacimiento(LocalDate.of(1995, 6, 15));
        when(service.actualizarCliente(eq(7L), any(ClienteActualizarRq.class))).thenReturn(actualizado);

        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.usuarioId").value(42))
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.numeroDocumento").value("001234"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-06-15"));

        var captor = org.mockito.ArgumentCaptor.forClass(ClienteActualizarRq.class);
        verify(service).actualizarCliente(eq(7L), captor.capture());
        assertEquals("Laura", captor.getValue().getNombres());
        assertEquals(LocalDate.of(1995, 6, 15), captor.getValue().getFechaNacimiento());
    }

    @Test
    void inexistenteDevuelve404() throws Exception {
        when(service.actualizarCliente(eq(7L), any())).thenThrow(new NotFoundException("Cliente inexistente"));
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT FOUND"));
    }

    @Test
    void duplicadoDevuelve409() throws Exception {
        when(service.actualizarCliente(eq(7L), any())).thenThrow(new ConflictException("Documento duplicado"));
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"tipoDocumento\":\"CC\"}",
            "{\"nombres\":\" \"}",
            "{",
            "null"
    })
    void bodyInvalidoDevuelve400SinInvocarServicio(String body) throws Exception {
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "activo", "usuarioId", "otroCampo"})
    void rechazaCamposFueraDelContrato(String campo) throws Exception {
        String body = BODY.replace("\"tipoDocumento\"", "\"" + campo + "\": 1, \"tipoDocumento\"");
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"no-es-fecha", "2026-02-30", "2999-01-01"})
    void fechaInvalidaDevuelve400(String fecha) throws Exception {
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("1995-06-15", fecha)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void bodyAusenteEIdNoNumericoDevuelven400() throws Exception {
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/cliente/abc").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void idNoPositivoDevuelve400DesdeServicio() throws Exception {
        when(service.actualizarCliente(eq(0L), any())).thenThrow(new BadRequestException("ID invalido"));
        mvc.perform(post("/cliente/0").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void opcionalesOmitidosLleganComoNulos() throws Exception {
        String body = """
                {"tipoDocumento":"CC","numeroDocumento":"001234","nombres":"Laura",
                 "apellidos":"Morales","fechaNacimiento":"1995-06-15"}
                """;
        when(service.actualizarCliente(eq(7L), any())).thenReturn(new Cliente());
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        var captor = org.mockito.ArgumentCaptor.forClass(ClienteActualizarRq.class);
        verify(service).actualizarCliente(eq(7L), captor.capture());
        assertNull(captor.getValue().getGenero());
        assertNull(captor.getValue().getTelefono());
        assertNull(captor.getValue().getDireccion());
    }

    @Test
    void consultasActualesMantienenSuContrato() throws Exception {
        when(service.listarClientes()).thenReturn(List.of());
        when(service.buscarClientesPorDocumento("ausente")).thenThrow(new BadRequestException("No encontrado"));
        mvc.perform(get("/cliente/listar").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/cliente/buscar-por-documento").param("documento", "ausente")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void erroresInternosNoExponenDetalles() throws Exception {
        when(service.actualizarCliente(eq(7L), any()))
                .thenThrow(new IllegalStateException("detalle interno de persistencia"));
        mvc.perform(post("/cliente/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrio un error interno al procesar la solicitud."));
    }
}
