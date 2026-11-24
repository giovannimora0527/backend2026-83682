package com.uniminuto.clinica;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.CorreoException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.*;
import com.uniminuto.clinica.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Recorre los fallos corregidos en la fase 1 con seguridad real y una base H2 en modo MySQL. */
@SpringBootTest
@ActiveProfiles("test")
class Fase1IntegracionTests {

    private static final String CLAVE = "Clave-123";

    @Autowired WebApplicationContext contexto;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder encoder;
    @Autowired UsuarioRepository usuarios;
    @Autowired PerfilRepository perfiles;
    @Autowired OtpTokenRepository otps;
    @Autowired SessionRepository sesiones;
    @Autowired ClienteRepository clientes;
    @Autowired MascotaRepository mascotas;
    @Autowired RazaRepository razas;
    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired CitaRepository citas;
    @Autowired CitaService citaService;
    @Autowired UsuarioService usuarioService;
    @Autowired EspecializacionService especializacionService;
    @Autowired FormulaService formulaService;
    @MockitoBean EmailService emailService;

    private MockMvc mvc;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        Perfil perfil = new Perfil();
        perfil.setNombre("Administrador");
        perfil.setActivo(true);
        perfil.setFechaCreacion(LocalDateTime.now());
        usuario = new Usuario();
        usuario.setUsername("ana");
        usuario.setEmail("ana@example.com");
        usuario.setPasswordHash(PasswordUtil.generarMD5(CLAVE));
        usuario.setPerfil(perfiles.save(perfil));
        usuario.setActivo(true);
        usuario = usuarios.save(usuario);
    }

    @AfterEach
    void limpiar() {
        citas.deleteAll();
        medicos.deleteAll();
        especializaciones.deleteAll();
        mascotas.deleteAll();
        razas.deleteAll();
        clientes.deleteAll();
        sesiones.deleteAll();
        otps.deleteAll();
        usuarios.deleteAll();
        perfiles.deleteAll();
    }

    @Test
    void sinTokenOConTokenInvalidoResponde401() throws Exception {
        mvc.perform(get("/cliente/listar")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty());
        mvc.perform(get("/cliente/listar").header("Authorization", "Bearer abc"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/clinica/test")).andExpect(status().isOk());
    }

    @Test
    void loginMigraMd5ABcryptYElTokenPermiteAccederSinContentType() throws Exception {
        String token = iniciarSesion();

        String hash = usuarios.findByUsername("ana").orElseThrow().getPasswordHash();
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches(CLAVE, hash));
        mvc.perform(get("/cliente/listar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        login(CLAVE).andExpect(status().isOk());
        login("otra").andExpect(status().isBadRequest());
    }

    @Test
    void otpNoSePuedeReutilizar() throws Exception {
        login(CLAVE).andExpect(status().isOk());
        String codigo = otp().getCodigo();
        validar(codigo).andExpect(status().isOk());
        validar(codigo).andExpect(status().isBadRequest());
        assertEquals(1, sesiones.count());
    }

    @Test
    void nuevoLoginInvalidaElOtpAnteriorYLosIntentosSeConservan() throws Exception {
        login(CLAVE);
        String anterior = otp().getCodigo();
        login(CLAVE);
        assertEquals(1, otps.count());
        String vigente = otp().getCodigo();
        assertNotEquals(anterior, vigente);
        for (int i = 0; i < 3; i++) {
            validar(anterior).andExpect(status().isBadRequest());
        }
        assertEquals(3, otp().getIntentos());
        validar(vigente).andExpect(status().isBadRequest());
    }

    @Test
    void siElCorreoFallaElLoginResponde500Controlado() throws Exception {
        doThrow(new CorreoException("detalle smtp", null)).when(emailService)
                .enviarCorreo(any(), any(), any(), any());
        login(CLAVE).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.login").doesNotExist())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("OTP")));
        assertEquals(1, otps.count());
    }

    @Test
    void cerrarSesionInvalidaElToken() throws Exception {
        String token = iniciarSesion();
        mvc.perform(post("/auth/cerrar-sesion").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mvc.perform(get("/cliente/listar").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        assertNotNull(sesiones.findAll().getFirst().getFechaCierre());
    }

    @Test
    void usuarioInactivadoPierdeElAcceso() throws Exception {
        String token = iniciarSesion();
        Usuario actual = usuarios.findByUsername("ana").orElseThrow();
        actual.setActivo(false);
        usuarios.save(actual);
        mvc.perform(get("/cliente/listar").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsSoloAceptaOrigenesConfigurados() throws Exception {
        mvc.perform(options("/cliente/listar").header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        mvc.perform(options("/cliente/listar").header("Origin", "http://malo.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearClienteExigeDocumentoUnico() throws Exception {
        String token = iniciarSesion();
        String body = """
                {"tipoDocumento":"CC","numeroDocumento":" 77 ","nombres":"Eva","apellidos":"Paz",
                 "fechaNacimiento":"1990-01-01"}""";
        mvc.perform(post("/cliente/guardar").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.numeroDocumento").value("77"));
        mvc.perform(post("/cliente/guardar").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void citasDetectanSolapamientoRealYNoChocanConsigoMismas() {
        Mascota luna = mascota("Luna");
        Mascota max = mascota("Max");
        Medico medico = medico();
        LocalDateTime base = LocalDate.now().plusDays(2).atTime(10, 0);

        citaService.crearCita(cita(null, luna, medico, base, null));
        citaService.crearCita(cita(null, luna, medico, base.plusMinutes(30), null));
        // Antes fallaba con 500 al tener dos citas con el mismo medico.
        citaService.crearCita(cita(null, luna, medico, base.plusMinutes(60), null));
        assertThrows(BadRequestException.class,
                () -> citaService.crearCita(cita(null, max, medico, base.plusMinutes(29), null)));

        Cita primera = citas.findAll().getFirst();
        citaService.actualizarCita(cita(primera.getId(), luna, medico, base, "PROGRAMADA"));
        assertThrows(BadRequestException.class, () -> citaService.actualizarCita(
                cita(primera.getId(), luna, medico, base.plusMinutes(10), "PROGRAMADA")));

        citaService.actualizarCita(cita(primera.getId(), luna, medico, base, "CANCELADA"));
        citaService.crearCita(cita(null, max, medico, base, null));
        assertEquals(4, citas.count());
    }

    @Test
    void buscarMascotasConNombreRepetidoDevuelveTodas() throws Exception {
        String token = iniciarSesion();
        mascota("Luna");
        mascota("Luna");
        mvc.perform(get("/mascota/buscar-nombre").param("nombre", "Luna")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void mascotaNuevaQuedaActiva() throws Exception {
        String token = iniciarSesion();
        Mascota base = mascota("Base");
        mvc.perform(post("/mascota/guardar").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nombre", "Nueva", "edad", 2,
                                "razaId", base.getRaza().getRazaId(), "clienteId", base.getCliente().getId()))))
                .andExpect(status().isOk());
        assertEquals(Boolean.TRUE, mascotas.findByNombreMascota("Nueva").getFirst().getActivo());
    }

    @Test
    void actualizarEspecializacionRechazaDuplicados() {
        especializacion("A1", "Cirugia");
        Especializacion otra = especializacion("B2", "Dermatologia");
        Especializacion cambio = new Especializacion();
        cambio.setId(otra.getId());
        cambio.setCodigoEspecializacion("A1");
        cambio.setNombre("Dermatologia");
        cambio.setDescripcion("x");
        assertThrows(BadRequestException.class, () -> especializacionService.actualizarEspecializacion(cambio));
        cambio.setCodigoEspecializacion("B2");
        cambio.setNombre("Cirugia");
        assertThrows(BadRequestException.class, () -> especializacionService.actualizarEspecializacion(cambio));
    }

    @Test
    void actualizarUsuarioTieneContrasenaOpcionalYCambiaPerfil() {
        Perfil nuevo = new Perfil();
        nuevo.setNombre("Medico");
        nuevo.setActivo(true);
        nuevo.setFechaCreacion(LocalDateTime.now());
        nuevo = perfiles.save(nuevo);
        UsuarioRq rq = new UsuarioRq();
        rq.setId(usuario.getId());
        rq.setUsername("ana");
        rq.setEmail("ana@example.com");
        rq.setPerfilId(nuevo.getPerfilId());

        usuarioService.actualizarUsuario(rq);
        Usuario guardado = usuarios.findByUsername("ana").orElseThrow();
        assertEquals("Medico", guardado.getPerfil().getNombre());
        assertEquals(PasswordUtil.generarMD5(CLAVE), guardado.getPasswordHash());
        assertTrue(guardado.getActivo());

        rq.setPassword("Nueva-456");
        usuarioService.actualizarUsuario(rq);
        assertTrue(encoder.matches("Nueva-456", usuarios.findByUsername("ana").orElseThrow().getPasswordHash()));
    }

    @Test
    void formulaSinMedicamentoSeRechaza() {
        FormulaRq rq = new FormulaRq();
        rq.setCitaId(1L);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> formulaService.guardarFormula(rq));
        assertTrue(ex.getMessage().contains("medicamento"));
    }

    private String iniciarSesion() throws Exception {
        login(CLAVE).andExpect(status().isOk());
        String cuerpo = validar(otp().getCodigo()).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return (String) json.readValue(cuerpo, Map.class).get("token");
    }

    private org.springframework.test.web.servlet.ResultActions login(String clave) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "ana", "password", clave))));
    }

    private org.springframework.test.web.servlet.ResultActions validar(String codigo) throws Exception {
        return mvc.perform(post("/auth/validate-token").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("usuarioId", usuario.getId(), "token", codigo))));
    }

    private OtpToken otp() {
        List<OtpToken> todos = otps.findAll();
        return todos.getLast();
    }

    private Mascota mascota(String nombre) {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento(String.valueOf(System.nanoTime()));
        cliente.setNombres("Eva");
        cliente.setApellidos("Paz");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setActivo(true);
        Raza raza = new Raza();
        raza.setNombre("Criollo");
        raza.setEspecie("Perro");
        raza.setFechaCreacion(LocalDateTime.now());
        Mascota mascota = new Mascota();
        mascota.setNombreMascota(nombre);
        mascota.setCliente(clientes.save(cliente));
        mascota.setRaza(razas.save(raza));
        mascota.setActivo(true);
        return mascotas.save(mascota);
    }

    private Especializacion especializacion(String codigo, String nombre) {
        Especializacion e = new Especializacion();
        e.setCodigoEspecializacion(codigo);
        e.setNombre(nombre);
        e.setDescripcion("x");
        return especializaciones.save(e);
    }

    private Medico medico() {
        Medico medico = new Medico();
        medico.setTipoDocumento("CC");
        medico.setNumeroDocumento("900");
        medico.setNombres("Juan");
        medico.setApellidos("Rojas");
        medico.setRegistroProfesional("RP1");
        medico.setEspecializacion(especializacion("G1", "General"));
        medico.setEstado(true);
        return medicos.save(medico);
    }

    private CitaRq cita(Long id, Mascota mascota, Medico medico, LocalDateTime fecha, String estado) {
        CitaRq rq = new CitaRq();
        rq.setId(id);
        rq.setMascotaId(mascota.getMascotaId());
        rq.setMedicoId(medico.getId());
        rq.setFechaHora(fecha);
        rq.setMotivo("Control");
        rq.setEstado(estado);
        return rq;
    }
}
