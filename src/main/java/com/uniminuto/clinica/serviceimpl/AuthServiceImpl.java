package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.OtpToken;
import com.uniminuto.clinica.entity.Session;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.CorreoException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.CloseSession;
import com.uniminuto.clinica.models.CloseSessionRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import com.uniminuto.clinica.repository.OtpTokenRepository;
import com.uniminuto.clinica.repository.SessionRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.AuthService;
import com.uniminuto.clinica.service.EmailService;
import com.uniminuto.clinica.util.PasswordUtil;
import com.uniminuto.clinica.util.Utilidades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int OTP_MINUTOS = 15;
    private static final int OTP_INTENTOS_MAXIMOS = 3;
    private static final int SESION_MINUTOS = 60;
    private static final String CREDENCIALES_INVALIDAS =
            "Usuario o contraseña incorrectos. Por favor intente de nuevo.";

    private final String jwtSecret;
    private final boolean mostrarOtpEnLog;
    private final OtpTokenRepository otpTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final SessionRepository sessionRepository;
    private final EmailService emailService;
    private final Utilidades utilidades;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActual usuarioActual;

    public AuthServiceImpl(@Value("${JWT_SECRET}") String jwtSecret,
                           @Value("${app.otp.mostrar-en-log:false}") boolean mostrarOtpEnLog,
                           OtpTokenRepository otpTokenRepository, UsuarioRepository usuarioRepository,
                           SessionRepository sessionRepository, EmailService emailService,
                           Utilidades utilidades, PasswordEncoder passwordEncoder, UsuarioActual usuarioActual) {
        this.jwtSecret = jwtSecret;
        this.mostrarOtpEnLog = mostrarOtpEnLog;
        this.otpTokenRepository = otpTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.sessionRepository = sessionRepository;
        this.emailService = emailService;
        this.utilidades = utilidades;
        this.passwordEncoder = passwordEncoder;
        this.usuarioActual = usuarioActual;
    }

    /** Si el correo falla se conserva el OTP: en dev se puede continuar con el codigo escrito en el log. */
    @Override
    @Transactional(noRollbackFor = CorreoException.class)
    public AuthRs login(AuthRq authRq) {
        this.validateAuthRq(authRq);
        Usuario user = this.usuarioRepository.findByUsername(authRq.getUsername())
                .orElseThrow(() -> new BadRequestException(CREDENCIALES_INVALIDAS));
        this.validarPassword(user, authRq.getPassword());
        if (!Boolean.TRUE.equals(user.getActivo())) {
            throw new BadRequestException("Se presentó un problema. Contacte al administrador del sistema.");
        }

        this.otpTokenRepository.eliminarPorUsuario(user.getId());
        LocalDateTime ahora = LocalDateTime.now();
        OtpToken token = new OtpToken();
        token.setUsuarioId(user.getId());
        token.setCodigo(Utilidades.generarTokenOtp());
        token.setUsado(Boolean.FALSE);
        token.setIntentos(0);
        token.setFechaCreacion(ahora);
        token.setFechaExpiracion(ahora.plusMinutes(OTP_MINUTOS));
        this.otpTokenRepository.save(token);

        if (mostrarOtpEnLog) {
            LOGGER.info("OTP de desarrollo para el usuario {} (id {}): {}",
                    user.getUsername(), user.getId(), token.getCodigo());
        }
        this.enviarOtp(user, token.getCodigo());

        AuthRs rta = new AuthRs();
        rta.setUsuarioId(user.getId());
        rta.setLogin(Boolean.TRUE);
        rta.setUsername(user.getUsername());
        return rta;
    }

    /** Los intentos fallidos se guardan aunque se responda 400. */
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public LoginRs validateToken(TokenRq tokenRq) {
        OtpToken otpToken = this.validateOtpToken(tokenRq);
        String jti = UUID.randomUUID().toString();
        String jwtToken = utilidades.crearJwtToken(otpToken, jwtSecret, jti);

        LocalDateTime ahora = LocalDateTime.now();
        Session session = new Session();
        session.setUserId(otpToken.getUsuarioId());
        session.setJti(jti);
        session.setActiva(Boolean.TRUE);
        session.setFechaIniSesion(ahora);
        session.setFechaExpiracion(ahora.plusMinutes(SESION_MINUTOS));
        this.sessionRepository.save(session);

        LoginRs loginRs = new LoginRs();
        loginRs.setToken(jwtToken);
        loginRs.setLogueoExitoso(Boolean.TRUE);
        return loginRs;
    }

    /** Cierra la sesion del token recibido; el usuarioId del cuerpo no se usa. */
    @Override
    @Transactional
    public CloseSessionRs cerrarSesion(CloseSession closeSession) {
        this.sessionRepository.findByJti(usuarioActual.obtener().jti()).ifPresent(session -> {
            session.setActiva(Boolean.FALSE);
            session.setFechaCierre(LocalDateTime.now());
            this.sessionRepository.save(session);
        });
        CloseSessionRs rta = new CloseSessionRs();
        rta.setSuccess(true);
        rta.setMessage("Sesión cerrada correctamente.");
        return rta;
    }

    private void validarPassword(Usuario user, String password) {
        String hash = user.getPasswordHash();
        if (PasswordUtil.esMD5(hash)) {
            if (!hash.equalsIgnoreCase(PasswordUtil.generarMD5(password))) {
                throw new BadRequestException(CREDENCIALES_INVALIDAS);
            }
            if (PasswordUtil.cabeEnBCrypt(password)) {
                user.setPasswordHash(passwordEncoder.encode(password));
                this.usuarioRepository.save(user);
            }
        } else if (hash == null || !passwordEncoder.matches(password, hash)) {
            throw new BadRequestException(CREDENCIALES_INVALIDAS);
        }
    }

    private void enviarOtp(Usuario user, String codigo) {
        String cuerpo = "<p>Estimado usuario " + user.getUsername() + ",</p>"
                + "<p>Su token OTP es: <strong>" + codigo + "</strong></p>"
                + "<p>Por favor, utilice este token para completar su autenticación. Vence en "
                + OTP_MINUTOS + " minutos.</p><p>Saludos,<br>Equipo de Soporte</p>";
        try {
            this.emailService.enviarCorreo(user.getEmail(), "Token OTP", cuerpo, this.emailService.getTo());
        } catch (CorreoException ex) {
            throw new CorreoException(
                    "No fue posible enviar el código OTP al correo registrado. Intente de nuevo más tarde.", ex);
        }
    }

    private OtpToken validateOtpToken(TokenRq tokenRq) {
        if (tokenRq == null || tokenRq.getUsuarioId() == null || tokenRq.getToken() == null) {
            throw new BadRequestException("UsuarioId o Token no puede ser nulo");
        }
        if (tokenRq.getToken().isEmpty()) {
            throw new BadRequestException("Token OTP no puede estar vacío");
        }
        OtpToken otpToken = this.otpTokenRepository
                .findFirstByUsuarioIdOrderByIdDesc(tokenRq.getUsuarioId())
                .orElseThrow(() -> new BadRequestException("No hay un token OTP vigente. Inicie sesión de nuevo."));
        if (Boolean.TRUE.equals(otpToken.getUsado())) {
            throw new BadRequestException("El token OTP ya fue utilizado. Inicie sesión de nuevo.");
        }
        if (otpToken.getIntentos() >= OTP_INTENTOS_MAXIMOS) {
            throw new BadRequestException("Se ha excedido el número máximo de intentos para validar el token OTP.");
        }
        if (otpToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token OTP ha expirado. Por favor, solicite un nuevo token.");
        }
        if (!otpToken.getCodigo().equals(tokenRq.getToken())) {
            otpToken.setIntentos(otpToken.getIntentos() + 1);
            this.otpTokenRepository.save(otpToken);
            throw new BadRequestException("Token OTP incorrecto. Por favor, intente de nuevo.");
        }
        otpToken.setUsado(Boolean.TRUE);
        this.otpTokenRepository.save(otpToken);
        return otpToken;
    }

    private void validateAuthRq(AuthRq authRq) {
        if (authRq == null || authRq.getUsername() == null || authRq.getPassword() == null) {
            throw new BadRequestException("Usuario o password no puede ser nulo");
        }
        if (authRq.getUsername().isEmpty() || authRq.getPassword().isEmpty()) {
            throw new BadRequestException("Usuario o password no puede estar vacío");
        }
    }
}
