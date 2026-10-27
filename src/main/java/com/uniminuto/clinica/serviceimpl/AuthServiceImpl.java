package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.OtpToken;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AuthRq;
import com.uniminuto.clinica.models.AuthRs;
import com.uniminuto.clinica.models.LoginRs;
import com.uniminuto.clinica.models.TokenRq;
import com.uniminuto.clinica.repository.OtpTokenRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.AuthService;
import com.uniminuto.clinica.service.EmailService;
import com.uniminuto.clinica.util.PasswordUtil;
import com.uniminuto.clinica.util.Utilidades;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String FROM = "clinicaveterinariauniminutosis@gmail.com";

    private static final Integer OTP_TOKEN_EXPIRATION_MINUTES = 15; // Token expiration time in minutes

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;


    @Override
    public AuthRs login(AuthRq authRq) throws BadRequestException {
        this.validateAuthRq(authRq);
        Optional<Usuario> optUser = this.usuarioRepository.findByUsername(authRq.getUsername());
        if (optUser.isEmpty()) {
            throw new BadRequestException("Usuario o Password incorrecto. Por favor intente de nuevo.");
        }

        Usuario user = optUser.get();
        if (!user.getActivo()) {
            throw new BadRequestException("Se presentó un problema. Contacte al administrador del sistema.");
        }
        String passEncrypted = PasswordUtil.generarMD5(authRq.getPassword());
        if (!user.getPasswordHash().equals(passEncrypted)) {
            throw new BadRequestException("Usuario o Password no encontrado");
        }

        // Envio Token OTP al correo del usuario
        String tokenOtp = Utilidades.generarTokenOtp();
        String bodyHtml = this.generarHtmlBodyCorreo(user.getUsername(), tokenOtp);
        this.emailService.enviarCorreo(user.getEmail(), "Token OTP", bodyHtml, FROM);

        // Valido si el usuario ya tiene un token OTP usado = 0,
        // si es así, lo elimino antes de crear uno nuevo
        OtpToken token = new OtpToken();
        token.setUsuarioId(user.getId());
        token.setCodigo(tokenOtp);
        token.setUsado(Boolean.FALSE);
        token.setIntentos(0);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now()
                .plusMinutes(OTP_TOKEN_EXPIRATION_MINUTES));
        this.otpTokenRepository.save(token);

        AuthRs rta = new AuthRs();
        rta.setUsuarioId(user.getId());
        rta.setLogin(Boolean.TRUE);
        rta.setUsername(user.getUsername());
        return rta;
    }

    @Override
    public LoginRs validateToken(TokenRq tokenRq) throws BadRequestException {
        OtpToken otpToken = this.validateOtpToken(tokenRq);
        LoginRs loginRs = new LoginRs();
        //  Genero el JWT Token para el usuario
        String jwtToken = Utilidades.generarJwtToken(otpToken, jwtSecret);
        loginRs.setToken(jwtToken);
        loginRs.setLogueoExitoso(Boolean.TRUE);
        return loginRs;
    }

    private String generarHtmlBodyCorreo(String username, String otpToken) {
        return "Estimado usuario " + username + ",\n\n" +
                "Su token OTP es: " + otpToken + "\n\n" +
                "Por favor, utilice este token para completar su autenticación.\n\n" +
                "Saludos,\n" +
                "Equipo de Soporte";

    }

    private OtpToken validateOtpToken(TokenRq tokenRq) throws BadRequestException {
        if (tokenRq.getUsuarioId() == null || tokenRq.getToken() == null) {
            throw new BadRequestException("UsuarioId o Token no puede ser nulo");
        }
        if (tokenRq.getToken().isEmpty()) {
            throw new BadRequestException("Token OTP no puede estar vacío");
        }
        Optional<OtpToken> optToken = this.otpTokenRepository
                .findByUsuarioId(tokenRq.getUsuarioId());
        if (optToken.isEmpty()) {
            throw new BadRequestException("Se ha presentado un error con el token OTP. " +
                    "Contacte al administrador del sistema.");
        }
        OtpToken otpToken = optToken.get();
        if (otpToken.getUsado().equals(0)) {
            this.otpTokenRepository.deleteById(otpToken.getId());
        }
        if (otpToken.getIntentos() >= 3) {
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

    private void validateAuthRq(AuthRq authRq) throws BadRequestException {
        // Implement the AuthRq validation logic here
        if (authRq.getUsername() == null || authRq.getPassword() == null) {
            throw new BadRequestException("Usuario o password no puede ser nulo");
        }

        if (authRq.getUsername().isEmpty() || authRq.getPassword().isEmpty()) {
            throw new BadRequestException("Usuario o password no puede estar vacío");
        }
    }
}
