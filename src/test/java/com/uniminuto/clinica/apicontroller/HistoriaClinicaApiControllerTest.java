package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.AnotacionService;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HistoriaClinicaApiControllerTest {
    private HistoriaClinicaService historias;
    private AnotacionService anotaciones;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        historias = mock(HistoriaClinicaService.class);
        anotaciones = mock(AnotacionService.class);
        mvc = MockMvcBuilders.standaloneSetup(
                new HistoriaClinicaApiController(historias, anotaciones)).build();
    }

    @Test
    void consultaDetalleYAnotacionesConElMismoIdQueDevuelveLaHistoria() throws Exception {
        long id = 77L;
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 16, 12, 0);
        HistoriaResponse historia = new HistoriaResponse(id, 1L, "Ana Lopez", 2, "Luna",
                null, 3L, "Luis Vega", fecha, "Control", "Estable", "Seguimiento");
        AnotacionResponse nota = new AnotacionResponse(9L, id, 3L, "Luis Vega", fecha,
                "Motivo: Control", null, null);
        when(historias.consultar(id)).thenReturn(historia);
        when(anotaciones.listarHistoria(id, 0, 20, "fechaHora", "DESC"))
                .thenReturn(new PageResponse<>(List.of(nota), 0, 20, 1, 1));

        mvc.perform(get("/historias-clinicas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historia.historiaClinicaId").value(id))
                .andExpect(jsonPath("$.historia.mascotaNombre").value("Luna"))
                .andExpect(jsonPath("$.historia.clienteNombre").value("Ana Lopez"))
                .andExpect(jsonPath("$.historia.veterinarioId").value(3))
                .andExpect(jsonPath("$.anotaciones.content[0].anotacionId").value(9))
                .andExpect(jsonPath("$.anotaciones.content[0].historiaClinicaId").value(id));

        verify(historias).consultar(id);
        verify(anotaciones).listarHistoria(id, 0, 20, "fechaHora", "DESC");
    }

    @Test
    void endpointPaginadoDeAnotacionesUsaElIdDeLaHistoria() throws Exception {
        long id = 77L;
        when(anotaciones.listarHistoria(id, 0, 20, null, "DESC"))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mvc.perform(get("/historias-clinicas/{id}/anotaciones", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(anotaciones).listarHistoria(id, 0, 20, null, "DESC");
    }
}
