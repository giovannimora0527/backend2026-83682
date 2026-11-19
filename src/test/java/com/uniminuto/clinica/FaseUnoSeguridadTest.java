package com.uniminuto.clinica;

import com.uniminuto.clinica.apicontroller.ApiExceptionHandler;
import com.uniminuto.clinica.apicontroller.AuthController;
import com.uniminuto.clinica.apicontroller.MenuApiController;
import com.uniminuto.clinica.config.JwtAuthenticationFilter;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.AlcanceMedico;
import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import com.uniminuto.clinica.service.JwtService;
import com.uniminuto.clinica.service.MenuService;
import com.uniminuto.clinica.serviceimpl.AutenticacionServiceImpl;
import io.jsonwebtoken.Jwts;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FaseUnoSeguridadTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final IdentidadService identidades = mock(IdentidadService.class);
    private final MenuService menus = mock(MenuService.class);
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(4);
    private final JwtService jwt = new JwtService(Base64.getEncoder()
            .encodeToString(Jwts.SIG.HS256.key().build().getEncoded()));
    private final String passwordTemporal = UUID.randomUUID().toString();
    private MockMvc mvc;

    @BeforeEach
    void configurarMvc() {
        mvc = MockMvcBuilders.standaloneSetup(
                new AuthController(new AutenticacionServiceImpl(usuarios, bcrypt), identidades, jwt),
                new MenuApiController(menus), new Recursos())
                .setControllerAdvice(new ApiExceptionHandler())
                .addFilters(new JwtAuthenticationFilter(jwt, identidades))
                .build();
    }

    @Test
    void administradorPuedeEntrarYConsultarMenusPropios() throws Exception {
        registrar(1L, Rol.ADMIN);
        String bearer = login(Rol.ADMIN);
        Menu menu = new Menu();
        menu.setUrl("/inicio/usuarios");
        when(menus.obtenerMenusPorUsuario(1L)).thenReturn(List.of(menu));

        mvc.perform(get("/menu/propio").servletPath("/menu/propio").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/inicio/usuarios")));
        mvc.perform(get("/menu/usuario/2").servletPath("/menu/usuario/2").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isForbidden());
        verify(menus, never()).obtenerMenusPorUsuario(2L);
    }

    @Test
    void recepcionPuedeVerCitasPeroNoFormulas() throws Exception {
        registrar(2L, Rol.RECEPCION);
        String bearer = login(Rol.RECEPCION);

        mvc.perform(get("/cita/listar").servletPath("/cita/listar").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk());
        mvc.perform(get("/formula-medica/listar-todos").servletPath("/formula-medica/listar-todos")
                .header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/menu/listar-activos").servletPath("/menu/listar-activos")
                .header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isForbidden());
    }

    @Test
    void despachoPuedeLeerMedicamentosSinAccederACitas() throws Exception {
        registrar(3L, Rol.DESPACHO_MEDICO);
        String bearer = login(Rol.DESPACHO_MEDICO);
        mvc.perform(get("/medicamento/listar").servletPath("/medicamento/listar")
                .header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isOk());
        mvc.perform(put("/medicamento/listar").servletPath("/medicamento/listar")
                .header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/cita/listar").servletPath("/cita/listar")
                .header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isForbidden());
    }

    @Test
    void devuelve401SinTokenYCuandoElUsuarioDejaDeEstarActivo() throws Exception {
        registrar(1L, Rol.ADMIN);
        String bearer = login(Rol.ADMIN);
        when(identidades.usuarioActivo("ADMIN", 1L)).thenReturn(Optional.empty());

        mvc.perform(get("/auth/validar").servletPath("/auth/validar"))
                .andExpect(status().isUnauthorized()).andExpect(header().string("WWW-Authenticate", "Bearer"));
        mvc.perform(get("/auth/validar").servletPath("/auth/validar").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/validar").servletPath("/auth/validar")
                .header(HttpHeaders.AUTHORIZATION, "Bearer inválido")).andExpect(status().isUnauthorized());
    }

    @Test
    void medicoSoloPuedeUsarSuIdentificadorYCitasAsignadas() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        doReturn(List.of(12)).when(jdbc).query(eq("SELECT medico_id FROM usuario_medico WHERE usuario_id = ?"),
                any(RowMapper.class), eq(4L));
        when(jdbc.queryForList("SELECT id FROM cita WHERE medico_id = ?", Integer.class, 12))
                .thenReturn(List.of(32));
        var request = new MockHttpServletRequest();
        request.setAttribute(IdentidadService.ATRIBUTO, new Identidad(4L, "medico", 3, "Medico", Rol.MEDICO));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            var alcance = new AlcanceMedico(jdbc);
            assertDoesNotThrow(() -> alcance.exigirMedico(12));
            assertDoesNotThrow(() -> alcance.exigirCita(32));
            Cita cita = new Cita();
            cita.setMedicoId(12);
            cita.setMascotaId(21);
            Cita cambio = new Cita();
            cambio.setMedicoId(12);
            cambio.setMascotaId(21);
            assertDoesNotThrow(() -> alcance.exigirEdicionCita(cita, cambio));
            cambio.setMascotaId(22);
            assertEquals(403, assertThrows(ResponseStatusException.class,
                    () -> alcance.exigirEdicionCita(cita, cambio)).getStatusCode().value());
            assertEquals(403, assertThrows(ResponseStatusException.class,
                    () -> alcance.exigirMedico(13)).getStatusCode().value());
            assertEquals(403, assertThrows(ResponseStatusException.class,
                    () -> alcance.exigirCita(33)).getStatusCode().value());
            doReturn(List.of()).when(jdbc).query(eq("SELECT medico_id FROM usuario_medico WHERE usuario_id = ?"),
                    any(RowMapper.class), eq(4L));
            assertEquals(403, assertThrows(ResponseStatusException.class,
                    alcance::medicoId).getStatusCode().value());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    private void registrar(Long id, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setActivo(true);
        usuario.setUsername(rol.name());
        usuario.setPerfilId(id.intValue());
        usuario.setPasswordHash(bcrypt.encode(passwordTemporal));
        when(usuarios.findByUsername(rol.name())).thenReturn(Optional.of(usuario));
        when(identidades.usuarioActivo(rol.name(), id)).thenReturn(Optional.of(usuario));
        when(identidades.perfilActivo(usuario)).thenReturn(
                new Identidad(id, rol.name(), id.intValue(), rol.name(), rol));
    }

    private String login(Rol rol) throws Exception {
        var response = mvc.perform(post("/auth/login").servletPath("/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"" + rol.name() + "\",\"password\":\"" + passwordTemporal + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse();
        String token = new tools.jackson.databind.ObjectMapper().readTree(response.getContentAsString())
                .get("token").asString();
        assertTrue(rol.name().equals(jwt.leerToken(token).getSubject()));
        return "Bearer " + token;
    }

    @RestController
    static class Recursos {
        @GetMapping({"/cita/listar", "/formula-medica/listar-todos", "/medicamento/listar"})
        List<Object> listar() {
            return List.of();
        }
    }
}
