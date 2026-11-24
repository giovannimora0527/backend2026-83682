package com.uniminuto.clinica;

import com.uniminuto.clinica.entity.OtpToken;
import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.Session;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.PerfilRepository;
import com.uniminuto.clinica.repository.SessionRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.EmailService;
import com.uniminuto.clinica.util.Utilidades;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/** Base de pruebas con seguridad real: usuarios por rol y JWT con sesion. */
@SpringBootTest
@ActiveProfiles("test")
public abstract class PruebaIntegracion {

    @Autowired private WebApplicationContext contexto;
    @Autowired private Utilidades utilidades;
    @Autowired protected UsuarioRepository usuarios;
    @Autowired private PerfilRepository perfiles;
    @Autowired private SessionRepository sesiones;
    @MockitoBean private EmailService emailService;
    @Value("${JWT_SECRET}") private String secreto;

    protected MockMvc mvc;

    @BeforeEach
    void prepararMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @AfterEach
    void limpiar() {
        limpiarDatos();
        sesiones.deleteAll();
        usuarios.deleteAll();
        perfiles.deleteAll();
    }

    /** Borra los datos propios de la prueba, antes de los usuarios. */
    protected abstract void limpiarDatos();

    protected String token(String rol) {
        return token(usuario(rol));
    }

    protected String token(Usuario usuario) {
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

    protected Usuario usuario(String rol) {
        Perfil perfil = perfiles.findAll().stream()
                .filter(p -> rol.equals(p.getCodigo())).findFirst().orElseGet(() -> {
                    Perfil nuevo = new Perfil();
                    nuevo.setNombre(rol);
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
}
