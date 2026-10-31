package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.entity.OtpToken;
import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.MenuService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UtilidadesTests {

    @Test
    void generarJwtTokenDebeFirmarConHs512YExpirarEnUnaHora() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);
        Utilidades utilidades = crearUtilidades(usuarioId -> List.of());

        String token = utilidades.crearJwtToken(
                otpToken, Encoders.BASE64.encode(key.getEncoded()));
        var jwt = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims claims = jwt.getPayload();

        assertEquals("HS512", jwt.getHeader().getAlgorithm());
        assertEquals("42", claims.getSubject());
        assertEquals(3_600_000L,
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void generarJwtTokenDebeRechazarUsuarioNulo() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        Utilidades utilidades = crearUtilidades(usuarioId -> List.of());
        String secret = Encoders.BASE64.encode(key.getEncoded());

        assertThrows(IllegalArgumentException.class,
                () -> utilidades.crearJwtToken(null, secret));
        assertThrows(IllegalArgumentException.class,
                () -> utilidades.crearJwtToken(new OtpToken(), secret));
    }

    @Test
    void generarJwtTokenDebeRechazarClaveInsuficienteParaHs512() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);
        Utilidades utilidades = crearUtilidades(usuarioId -> List.of());

        assertThrows(WeakKeyException.class,
                () -> utilidades.crearJwtToken(
                        otpToken, Encoders.BASE64.encode(key.getEncoded())));
    }

    @Test
    void generarJwtTokenDebeUsarSecretoConfiguradoEnBase64() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);
        Utilidades utilidades = crearUtilidades(usuarioId -> List.of());

        String token = utilidades.crearJwtToken(
                otpToken, Encoders.BASE64.encode(key.getEncoded()));

        assertEquals("42", Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject());
    }

    @Test
    void crearJwtTokenDebeIncluirLosMenusDelServicioEnElClaim() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);
        Menu menu = new Menu();
        menu.setMenuId(7);
        menu.setCode("DASHBOARD");
        menu.setTitle("Dashboard");
        MenuService menuService = usuarioId -> {
            assertEquals(42L, usuarioId);
            return List.of(menu);
        };
        Utilidades utilidades = crearUtilidades(menuService);

        String token = utilidades.crearJwtToken(
                otpToken, Encoders.BASE64.encode(key.getEncoded()));
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        String menusJson = claims.get("menus", String.class);
        assertTrue(menusJson.startsWith("["));
        List<?> menus = parseMenus(menusJson);
        Map<?, ?> menuClaim = (Map<?, ?>) menus.get(0);

        assertEquals(7, menuClaim.get("menuId"));
        assertEquals("DASHBOARD", menuClaim.get("code"));
        assertEquals("Dashboard", menuClaim.get("title"));
    }

    @Test
    void generarJwtTokenDebeRechazarSecretoVacio() {
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);
        Utilidades utilidades = crearUtilidades(usuarioId -> List.of());

        assertThrows(IllegalStateException.class,
                () -> utilidades.crearJwtToken(otpToken, ""));
    }

    @Test
    void generarTokenOtpDebeRetornarSeisCaracteresAlfanumericos() {
        for (int i = 0; i < 1000; i++) {
            String token = Utilidades.generarTokenOtp();

            assertEquals(6, token.length());
            assertTrue(token.matches("[A-Z0-9]{6}"));
        }
    }

    @Test
    void crearJwtTokenDebeIncluirInformacionBasicaDelUsuario() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        Usuario usuario = usuario();
        Utilidades utilidades = crearUtilidades(usuarioId -> List.of(), usuario);
        OtpToken otpToken = otpToken();

        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(utilidades.crearJwtToken(
                        otpToken, Encoders.BASE64.encode(key.getEncoded())))
                .getPayload();

        assertEquals("luna", claims.get("username", String.class));
        assertEquals("luna@example.com", claims.get("email", String.class));
        assertEquals("Administrador", claims.get("rol", String.class));
    }

    private static Utilidades crearUtilidades(MenuService menuService) {
        return crearUtilidades(menuService, usuario());
    }

    private static Utilidades crearUtilidades(MenuService menuService, Usuario usuario) {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        when(usuarioRepository.findById(42L)).thenReturn(Optional.of(usuario));
        return new Utilidades(menuService, usuarioRepository, new ObjectMapper());
    }

    private static Usuario usuario() {
        Perfil perfil = new Perfil();
        perfil.setNombre("Administrador");
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setUsername("luna");
        usuario.setEmail("luna@example.com");
        usuario.setPerfil(perfil);
        return usuario;
    }

    private static OtpToken otpToken() {
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);
        return otpToken;
    }

    private static List<?> parseMenus(String menusJson) {
        return new ObjectMapper().readValue(menusJson, List.class);
    }
}
