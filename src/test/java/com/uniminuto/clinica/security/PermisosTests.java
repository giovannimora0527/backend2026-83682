package com.uniminuto.clinica.security;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.EmailService;
import com.uniminuto.clinica.util.Utilidades;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Permisos por rol (C6) y propiedad del medico, con JWT y sesiones reales. */
@SpringBootTest
@ActiveProfiles("test")
class PermisosTests {

    @Autowired WebApplicationContext contexto;
    @Autowired Utilidades utilidades;
    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilRepository perfiles;
    @Autowired SessionRepository sesiones;
    @Autowired ClienteRepository clientes;
    @Autowired RazaRepository razas;
    @Autowired MascotaRepository mascotas;
    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired CitaRepository citas;
    @Autowired CitaHistorialEstadoRepository historialCitas;
    @MockitoBean EmailService emailService;
    @Value("${JWT_SECRET}") String secreto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @AfterEach
    void limpiar() {
        historialCitas.deleteAll();
        citas.deleteAll();
        medicos.deleteAll();
        especializaciones.deleteAll();
        mascotas.deleteAll();
        razas.deleteAll();
        clientes.deleteAll();
        sesiones.deleteAll();
        usuarios.deleteAll();
        perfiles.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/cita/listar", "GET,/factura/listar", "GET,/reporte/catalogo", "GET,/historia-clinica/listar",
            "POST,/formula-medica/crear", "POST,/formula-medica/guardar", "GET,/formula-medica/listar",
            "POST,/inventario/productos", "GET,/usuario/listar-ordenado", "GET,/cliente/listar",
            "POST,/tarifa/crear", "POST,/anotacion-historia/crear", "GET,/inventario/carga/historial",
            "GET,/atencion/cita/1", "GET,/medico/listar"})
    void despachoNoAccedeAOtrosModulos(String metodo, String ruta) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta).header("Authorization", token("DESPACHO_MEDICO"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @ParameterizedTest
    @CsvSource({"SECRETARIA,/usuario/listar-ordenado", "SECRETARIA,/historia-clinica/listar",
            "SECRETARIA,/reporte/catalogo", "MEDICO,/usuario/listar-ordenado", "MEDICO,/cita/listar",
            "MEDICO,/factura/listar", "MEDICO,/despacho/historial"})
    void otrosRolesTambienEstanLimitados(String rol, String ruta) throws Exception {
        mvc.perform(get(ruta).header("Authorization", token(rol))).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({"ADMINISTRADOR,/usuario/listar-ordenado", "SECRETARIA,/cita/listar", "MEDICO,/cliente/listar",
            "MEDICO,/formula-medica/listar", "DESPACHO_MEDICO,/medicamento/listar",
            "DESPACHO_MEDICO,/especializacion/listar"})
    void rolesAutorizadosAcceden(String rol, String ruta) throws Exception {
        mvc.perform(get(ruta).header("Authorization", token(rol))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/cita/listar", "/inventario/productos", "/despacho/historial", "/factura/listar"})
    void sinTokenResponde401(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void secretariaLeeEspecializacionesPeroNoLasCrea() throws Exception {
        mvc.perform(post("/especializacion/crear").header("Authorization", token("SECRETARIA"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfilSinCodigoNoPuedeEntrar() throws Exception {
        mvc.perform(get("/medicamento/listar").header("Authorization", token((String) null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaInexistenteResponde404() throws Exception {
        mvc.perform(get("/no-existe").header("Authorization", token("ADMINISTRADOR")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void medicoSoloVeYModificaSusCitas() throws Exception {
        Usuario duenio = usuario("MEDICO");
        Usuario ajeno = usuario("MEDICO");
        Medico medico = medico(duenio, "1");
        medico(ajeno, "2");
        Cita cita = cita(medico);
        String suyo = token(duenio);
        String otro = token(ajeno);

        mvc.perform(get("/cita/" + cita.getId()).header("Authorization", suyo)).andExpect(status().isOk());
        mvc.perform(get("/cita/" + cita.getId()).header("Authorization", otro)).andExpect(status().isForbidden());
        // Sin medico vinculado tampoco ve nada ajeno.
        mvc.perform(get("/cita/" + cita.getId()).header("Authorization", token("MEDICO")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/cita/" + cita.getId()).header("Authorization", token("SECRETARIA")))
                .andExpect(status().isOk());

        String body = """
                {"id":%d,"mascotaId":%d,"medicoId":%d,"fechaHora":"%s","motivo":"x","estado":"CANCELADA","motivoCancelacion":"y"}"""
                .formatted(cita.getId(), cita.getMascota().getMascotaId(), medico.getId(),
                        LocalDate.now().plusDays(3).atTime(9, 0, 0).toString() + ":00");
        mvc.perform(post("/cita/actualizar").header("Authorization", otro)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/cita/actualizar").header("Authorization", suyo)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(get("/cita/999999").header("Authorization", suyo)).andExpect(status().isNotFound());
    }

    private String token(String rol) {
        return token(usuario(rol));
    }

    private String token(Usuario usuario) {
        String jti = UUID.randomUUID().toString();
        OtpToken otp = new OtpToken();
        otp.setUsuarioId(usuario.getId());
        Session sesion = new Session();
        sesion.setUserId(usuario.getId());
        sesion.setJti(jti);
        sesion.setActiva(true);
        sesion.setFechaExpiracion(LocalDateTime.now().plusHours(1));
        sesiones.save(sesion);
        return "Bearer " + utilidades.crearJwtToken(otp, secreto, jti);
    }

    private Usuario usuario(String rol) {
        Perfil perfil = perfiles.findAll().stream()
                .filter(p -> java.util.Objects.equals(p.getCodigo(), rol)).findFirst().orElseGet(() -> {
                    Perfil nuevo = new Perfil();
                    nuevo.setNombre(rol == null ? "Sin codigo" : rol);
                    nuevo.setCodigo(rol);
                    nuevo.setActivo(true);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    return perfiles.save(nuevo);
                });
        Usuario usuario = new Usuario();
        usuario.setUsername(UUID.randomUUID().toString());
        usuario.setEmail(usuario.getUsername() + "@example.com");
        usuario.setPerfil(perfil);
        usuario.setActivo(true);
        return usuarios.save(usuario);
    }

    private Medico medico(Usuario usuario, String documento) {
        Especializacion especializacion = new Especializacion();
        especializacion.setNombre("E" + documento);
        especializacion.setCodigoEspecializacion("C" + documento);
        Medico medico = new Medico();
        medico.setTipoDocumento("CC");
        medico.setNumeroDocumento(documento);
        medico.setNombres("Juan");
        medico.setApellidos("Rojas");
        medico.setRegistroProfesional("RP" + documento);
        medico.setEspecializacion(especializaciones.save(especializacion));
        medico.setEstado(true);
        medico.setUsuarioId(usuario.getId());
        return medicos.save(medico);
    }

    private Cita cita(Medico medico) {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento("55");
        cliente.setNombres("Eva");
        cliente.setApellidos("Paz");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setActivo(true);
        Raza raza = new Raza();
        raza.setNombre("Criollo");
        raza.setEspecie("Perro");
        raza.setFechaCreacion(LocalDateTime.now());
        Mascota mascota = new Mascota();
        mascota.setNombreMascota("Luna");
        mascota.setCliente(clientes.save(cliente));
        mascota.setRaza(razas.save(raza));
        mascota.setActivo(true);
        Cita cita = new Cita();
        cita.setMascota(mascotas.save(mascota));
        cita.setCliente(mascota.getCliente());
        cita.setMedico(medico);
        cita.setFechaInicio(LocalDate.now().plusDays(2).atTime(9, 0));
        cita.setFechaFin(cita.getFechaInicio().plusMinutes(30));
        cita.setEstado(com.uniminuto.clinica.entity.enums.EstadoCita.PROGRAMADA);
        cita.setMotivo("Control");
        return citas.save(cita);
    }
}
