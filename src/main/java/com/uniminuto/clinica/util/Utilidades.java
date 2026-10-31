package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.entity.OtpToken;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.MenuService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class Utilidades {
    /**
     * Genera un token OTP (One-Time Password) de 6 caracteres alfanuméricos.
     * @return Token OTP generado.
     */
    private static final String CARACTERES_OTP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    /**
     * Longitud del token OTP.
     */
    private static final int LONGITUD_OTP = 6;
    /**
     * Generador de números aleatorios seguro para la generación de tokens.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MenuService menuService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public Utilidades(MenuService menuService,
                      UsuarioRepository usuarioRepository,
                      ObjectMapper objectMapper) {
        this.menuService = menuService;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    public static String generarTokenOtp() {
        StringBuilder token = new StringBuilder(LONGITUD_OTP);
        for (int i = 0; i < LONGITUD_OTP; i++) {
            token.append(CARACTERES_OTP.charAt(RANDOM.nextInt(CARACTERES_OTP.length())));
        }
        return token.toString();
    }

    public String crearJwtToken(OtpToken otpToken, String secret) {
        validarOtpToken(otpToken);
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Debe configurar JWT_SECRET en Base64 con al menos 64 bytes aleatorios.");
        }
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        Usuario usuario = usuarioRepository.findById(otpToken.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario del token no existe."));
        List<Menu> menus = menuService.obtenerListaMenus(otpToken.getUsuarioId());
        String menusJson = serializarMenus(menus);
        return firmarJwtToken(otpToken, usuario, key, menusJson);
    }

    private String serializarMenus(List<Menu> menus) {
        try {
            return objectMapper.writeValueAsString(menus);
        } catch (JacksonException e) {
            throw new IllegalStateException("No se pudieron serializar los menús del usuario.", e);
        }
    }

    private static String firmarJwtToken(OtpToken otpToken, Usuario usuario,
                                         SecretKey key, String menusJson) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(otpToken.getUsuarioId().toString())
                .claim("username", usuario.getUsername())
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getPerfil().getNombre())
                .claim("menus", menusJson)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private static void validarOtpToken(OtpToken otpToken) {
        if (otpToken == null || otpToken.getUsuarioId() == null) {
            throw new IllegalArgumentException("El token OTP debe tener un usuario asociado.");
        }
    }
}
