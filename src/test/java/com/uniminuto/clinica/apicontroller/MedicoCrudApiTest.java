package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Veterinario;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Crear y editar médicos (tabla medico) con el contrato del formulario Angular; H2 en memoria. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MedicoCrudApiTest {
    @Autowired private WebApplicationContext context;
    @Autowired private EntityManager em;
    @Autowired private VeterinarioRepository veterinarios;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        em.persist(especializacion(1, "Cardiología", "COD_01"));
        em.persist(especializacion(2, "Neurología", "COD_02"));
        em.flush();
    }

    @Test
    void listaEspecializacionesOrdenadasParaElFormulario() throws Exception {
        mvc.perform(get("/especializaciones").header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(jsonPath("$[0].nombre").value("Cardiología"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void creaConsultaYActualizaUnMedico() throws Exception {
        String creado = mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tipoDocumento":"cc","numeroDocumento":" 5550001 ","nombres":"Sofía","apellidos":"Pardo",
                         "telefono":"3001234567","registroProfesional":"RP-9001","especializacionId":1}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.veterinarioId").isNumber())
                .andExpect(jsonPath("$.tipoDocumento").value("CC"))
                .andExpect(jsonPath("$.numeroDocumento").value("5550001"))
                .andExpect(jsonPath("$.especializacion.nombre").value("Cardiología"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(creado.replaceAll(".*\"veterinarioId\":(\\d+).*", "$1"));
        em.flush(); em.clear();

        mvc.perform(put("/veterinarios/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"tipoDocumento":"CE","numeroDocumento":"5550001","nombres":"Sofía Andrea","apellidos":"Pardo Ruiz",
                         "telefono":"","registroProfesional":"RP-9001","especializacionId":2}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Sofía Andrea"))
                .andExpect(jsonPath("$.especializacion.id").value(2));
        em.flush(); em.clear();

        Veterinario guardado = veterinarios.findById(id).orElseThrow();
        assertEquals("CE", guardado.getTipoDocumento());
        assertEquals("Pardo Ruiz", guardado.getApellidos());
        assertEquals(null, guardado.getTelefono());
        assertEquals(2, guardado.getEspecializacion().getId());
        mvc.perform(get("/veterinarios/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.registroProfesional").value("RP-9001"));
    }

    @Test
    void rechazaDuplicadosDeLasColumnasUnicasYDatosInvalidos() throws Exception {
        crear("1000", "RP-1");
        Long segundo = crear("2000", "RP-2");

        mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo("1000", "RP-3", 1)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("documento")));
        mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo("3000", "RP-1", 1)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("registro")));
        mvc.perform(put("/veterinarios/{id}", segundo).contentType(MediaType.APPLICATION_JSON).content(cuerpo("1000", "RP-2", 1)))
                .andExpect(status().isConflict());
        mvc.perform(put("/veterinarios/{id}", segundo).contentType(MediaType.APPLICATION_JSON).content(cuerpo("2000", "RP-2", 1)))
                .andExpect(status().isOk());

        mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo("4000", "RP-4", 99)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoDocumento\":\"CC\",\"numeroDocumento\":\"5000\",\"apellidos\":\"X\",\"registroProfesional\":\"RP-5\",\"especializacionId\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("nombres")));
        mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("6000", "RP-6", 1).replace("\"Ana\"", "\"" + "A".repeat(101) + "\"")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("100")));
        mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("7000", "RP-7", 1).replace("3001234567", "abc")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("telefono")));
        mvc.perform(put("/veterinarios/{id}", 999999).contentType(MediaType.APPLICATION_JSON).content(cuerpo("8000", "RP-8", 1)))
                .andExpect(status().isNotFound());
    }

    private Long crear(String documento, String registro) throws Exception {
        String json = mvc.perform(post("/veterinarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo(documento, registro, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll(".*\"veterinarioId\":(\\d+).*", "$1"));
    }

    private String cuerpo(String documento, String registro, int especializacionId) {
        return """
                {"tipoDocumento":"CC","numeroDocumento":"%s","nombres":"Ana","apellidos":"Lopez",
                 "telefono":"3001234567","registroProfesional":"%s","especializacionId":%d}"""
                .formatted(documento, registro, especializacionId);
    }

    private Especializacion especializacion(int id, String nombre, String codigo) {
        Especializacion e = new Especializacion();
        e.setId(id); e.setNombre(nombre); e.setCodigoEspecializacion(codigo);
        return e;
    }
}
