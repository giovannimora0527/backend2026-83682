package com.uniminuto.clinica;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PendientesFrontendTests extends PruebaIntegracion {

    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired HorarioMedicoRepository horarios;
    @Autowired ClienteRepository clientes;

    @Test
    void listaPerfilesConCodigo() throws Exception {
        mvc.perform(get("/perfil/listar").header("Authorization", token("ADMINISTRADOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$[0].id").isNumber());
        mvc.perform(get("/perfil/listar").header("Authorization", token("SECRETARIA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void inactivarUsuarioInvalidaSuTokenYNoPermiteInactivarseASiMismo() throws Exception {
        Usuario admin = usuario("ADMINISTRADOR");
        Usuario otro = usuario("SECRETARIA");
        String tokenOtro = token(otro);
        mvc.perform(json(patch("/usuario/" + otro.getId() + "/estado"), token(admin), "{\"activo\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.activo").value(false));
        mvc.perform(get("/cliente/listar").header("Authorization", tokenOtro)).andExpect(status().isUnauthorized());
        mvc.perform(json(patch("/usuario/" + admin.getId() + "/estado"), token(admin), "{\"activo\":false}"))
                .andExpect(status().isConflict());
        mvc.perform(json(patch("/usuario/" + otro.getId() + "/estado"), token(admin), "{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalles[0].campo").value("activo"));
        mvc.perform(json(patch("/usuario/999999/estado"), token(admin), "{\"activo\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicadosIndicanElCampo() throws Exception {
        Usuario existente = usuario("SECRETARIA");
        mvc.perform(json(post("/usuario/guardar"), token("ADMINISTRADOR"), """
                        {"username":"%s","password":"Clave-1","email":"nuevo@example.com","perfilId":%d}"""
                        .formatted(existente.getUsername(), existente.getPerfil().getPerfilId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0].campo").value("username"))
                .andExpect(jsonPath("$.detalles[0].mensaje").isNotEmpty());
    }

    @Test
    void horariosSeReemplazanYValidan() throws Exception {
        Usuario duenio = usuario("MEDICO");
        Medico medico = medico(duenio);
        String ruta = "/medico/" + medico.getId() + "/horarios";
        String secretaria = token("SECRETARIA");
        mvc.perform(json(put(ruta), secretaria, """
                        [{"diaSemana":1,"horaInicio":"14:00","horaFin":"18:00","activo":true},
                         {"diaSemana":1,"horaInicio":"08:00","horaFin":"12:00"}]"""))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].horaInicio").value("08:00"));
        mvc.perform(json(put(ruta), secretaria, """
                        [{"diaSemana":2,"horaInicio":"08:00","horaFin":"12:00"},
                         {"diaSemana":2,"horaInicio":"11:30","horaFin":"13:00"}]"""))
                .andExpect(status().isConflict());
        mvc.perform(json(put(ruta), secretaria, "[{\"diaSemana\":8,\"horaInicio\":\"08:00\",\"horaFin\":\"12:00\"}]"))
                .andExpect(status().isBadRequest());
        mvc.perform(json(put(ruta), secretaria, "[{\"diaSemana\":1,\"horaInicio\":\"12:00\",\"horaFin\":\"08:00\"}]"))
                .andExpect(status().isBadRequest());
        // Los rechazos no borran lo anterior.
        mvc.perform(get("/medico/mis-horarios").header("Authorization", token(duenio)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(json(put(ruta), token(duenio), "[]")).andExpect(status().isForbidden());
        mvc.perform(json(put(ruta), secretaria, "[]")).andExpect(status().isOk());
        mvc.perform(get(ruta).header("Authorization", secretaria)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void especializacionInactivaSeFiltra() throws Exception {
        Medico medico = medico(usuario("MEDICO"));
        Long id = medico.getEspecializacion().getId();
        String admin = token("ADMINISTRADOR");
        mvc.perform(json(patch("/especializacion/" + id + "/estado"), admin, "{\"activo\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.activo").value(false));
        mvc.perform(get("/especializacion/listar?soloActivas=true").header("Authorization", admin))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/especializacion/listar").header("Authorization", admin))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void facturasPorClienteVaciaYPermisos() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento("1");
        cliente.setNombres("Eva");
        cliente.setApellidos("Paz");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setActivo(true);
        cliente = clientes.save(cliente);
        mvc.perform(get("/factura/cliente/" + cliente.getId()).header("Authorization", token("SECRETARIA")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/factura/cliente/999999").header("Authorization", token("SECRETARIA")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/factura/cliente/1").header("Authorization", token("MEDICO")))
                .andExpect(status().isForbidden());
    }

    @Override
    protected void limpiarDatos() {
        horarios.deleteAll();
        medicos.deleteAll();
        especializaciones.deleteAll();
        clientes.deleteAll();
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String token, String cuerpo) {
        return peticion.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private Medico medico(Usuario usuario) {
        Especializacion especializacion = new Especializacion();
        especializacion.setNombre("General");
        especializacion.setCodigoEspecializacion("GEN");
        Medico medico = new Medico();
        medico.setTipoDocumento("CC");
        medico.setNumeroDocumento("77");
        medico.setNombres("Juan");
        medico.setApellidos("Rojas");
        medico.setRegistroProfesional("RP77");
        medico.setEspecializacion(especializaciones.save(especializacion));
        medico.setEstado(true);
        medico.setUsuarioId(usuario.getId());
        return medicos.save(medico);
    }
}
