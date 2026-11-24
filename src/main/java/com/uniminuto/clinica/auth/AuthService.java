package com.uniminuto.clinica.auth;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ClinicaException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.ClaveUtil;
import com.uniminuto.clinica.seguridad.JwtServicio;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Inicio de sesión en dos pasos.
 *
 * <ol>
 *   <li>{@code login}: verifica usuario y contraseña (PBKDF2, o MD5 heredado que se migra) y envía al correo
 *   un código de 6 caracteres (guardado como SHA-256, vence en 5 minutos, 5 intentos).</li>
 *   <li>{@code verificar}: valida el código, emite el JWT y registra la sesión en la tabla {@code session}.</li>
 * </ol>
 * También permite recuperar la contraseña con un código enviado al correo.
 */
@Service
public class AuthService {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int SEGUNDOS_REENVIO = 30;

    private final Db db;
    private final JwtServicio jwt;
    private final CorreoService correo;
    private final int horas;
    private final boolean otpHabilitado;
    private final int minutos;
    private final int intentos;

    public AuthService(Db db, JwtServicio jwt, CorreoService correo, @Value("${clinica.jwt.horas}") int horas,
                       @Value("${clinica.otp.habilitado:true}") boolean otpHabilitado,
                       @Value("${clinica.otp.minutos:5}") int minutos, @Value("${clinica.otp.intentos:5}") int intentos) {
        this.db = db;
        this.jwt = jwt;
        this.correo = correo;
        this.horas = horas;
        this.otpHabilitado = otpHabilitado;
        this.minutos = minutos;
        this.intentos = intentos;
    }

    // ------------------------------------------------------------------ Paso 1: credenciales

    @Transactional
    public AuthApi.DesafioRs login(AuthApi.LoginRq datos) {
        if (datos == null || datos.username() == null || datos.username().isBlank()
                || datos.password() == null || datos.password().isEmpty()) {
            throw new BadRequestException("Debes ingresar tu usuario y contraseña.");
        }
        Map<String, Object> usuario = usuario("u.username = :valor", datos.username().trim());
        String hash = usuario == null ? null : Validar.cadena(usuario, "password_hash");
        if (usuario == null || !ClaveUtil.verificar(datos.password(), hash)) {
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }
        verificarActivo(usuario);
        long usuarioId = Validar.largo(usuario, "id");
        if (ClaveUtil.esHeredado(hash)) {
            db.ejecutar("UPDATE usuario SET password_hash = :hash WHERE id = :id",
                    Db.p("hash", ClaveUtil.hash(datos.password()), "id", usuarioId));
        }
        if (!otpHabilitado) {
            return new AuthApi.DesafioRs(false, null, null, 0, emitirSesion(usuario));
        }
        return enviarDesafio(usuario, "LOGIN");
    }

    // ------------------------------------------------------------------ Paso 2: código

    /** Los intentos fallidos se conservan aunque la respuesta sea un error (no se revierten). */
    @Transactional(noRollbackFor = ClinicaException.class)
    public AuthApi.LoginRs verificar(AuthApi.VerificarRq datos) {
        Map<String, Object> token = validarCodigo(datos == null ? null : datos.desafio(),
                datos == null ? null : datos.codigo(), "LOGIN");
        Map<String, Object> usuario = usuario("u.id = :valor", token.get("usuario_id"));
        verificarActivo(usuario);
        return emitirSesion(usuario);
    }

    @Transactional
    public AuthApi.DesafioRs reenviar(String desafio) {
        Map<String, Object> token = desafio == null ? null : db.uno(
                "SELECT * FROM otp_token WHERE desafio = :d FOR UPDATE", Db.p("d", desafio));
        if (token == null || Validar.bool(token, "usado")) {
            throw new ConflictException("La solicitud expiró. Vuelve a iniciar el proceso.");
        }
        LocalDateTime creado = (LocalDateTime) token.get("fecha_creacion");
        if (creado.plusSeconds(SEGUNDOS_REENVIO).isAfter(Tiempo.ahora())) {
            throw new ConflictException("Espera unos segundos antes de solicitar un nuevo código.");
        }
        Map<String, Object> usuario = usuario("u.id = :valor", token.get("usuario_id"));
        verificarActivo(usuario);
        String codigo = generarCodigo();
        LocalDateTime ahora = Tiempo.ahora();
        db.ejecutar("""
                UPDATE otp_token SET codigo_hash = :h, fecha_creacion = :ahora, fecha_expiracion = :vence, intentos = 0
                WHERE id = :id
                """, Db.p("h", ClaveUtil.sha256(codigo), "ahora", ahora, "vence", ahora.plusMinutes(minutos),
                "id", token.get("id")));
        String proposito = Validar.cadena(token, "proposito");
        correo.enviarCodigo(Validar.cadena(usuario, "email"), Validar.cadena(usuario, "username"), codigo, proposito, minutos);
        return new AuthApi.DesafioRs(true, desafio, enmascarar(Validar.cadena(usuario, "email")), minutos, null);
    }

    // ------------------------------------------------------------------ Recuperación de contraseña

    @Transactional
    public AuthApi.DesafioRs recuperar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("Ingresa tu usuario o tu correo electrónico.");
        }
        Map<String, Object> usuario = usuario("(u.username = :valor OR u.email = :valor)", valor.trim());
        if (usuario == null || !Validar.bool(usuario, "activo")) {
            // Misma respuesta que un usuario existente: no se revela si la cuenta existe.
            return new AuthApi.DesafioRs(true, UUID.randomUUID().toString(), null, minutos, null);
        }
        return enviarDesafio(usuario, "RECUPERACION");
    }

    @Transactional(noRollbackFor = ClinicaException.class)
    public void restablecer(AuthApi.RestablecerRq datos) {
        if (datos == null) {
            throw new BadRequestException("Los datos son obligatorios.");
        }
        String clave = datos.nuevaClave();
        if (clave == null || clave.length() < 8 || !clave.matches(".*[A-Za-z].*") || !clave.matches(".*\\d.*")) {
            throw new BadRequestException("La nueva contraseña debe tener al menos 8 caracteres, con letras y números.");
        }
        Map<String, Object> token = validarCodigo(datos.desafio(), datos.codigo(), "RECUPERACION");
        long usuarioId = Validar.largo(token, "usuario_id");
        db.ejecutar("UPDATE usuario SET password_hash = :h WHERE id = :id",
                Db.p("h", ClaveUtil.hash(clave), "id", usuarioId));
        db.ejecutar("UPDATE session SET activo = 0 WHERE user_id = :id", Db.p("id", usuarioId));
    }

    // ------------------------------------------------------------------ Sesión

    public AuthApi.LoginRs validar() {
        SesionUsuario s = SesionActual.obtener();
        return new AuthApi.LoginRs(null, "Bearer", s.usuarioId(), s.username(), s.perfilId(), s.perfil());
    }

    public void logout() {
        db.ejecutar("UPDATE session SET activo = 0 WHERE session_id = :id",
                Db.p("id", SesionActual.obtener().sesionId()));
    }

    // ------------------------------------------------------------------ Utilidades

    private AuthApi.DesafioRs enviarDesafio(Map<String, Object> usuario, String proposito) {
        long usuarioId = Validar.largo(usuario, "id");
        db.ejecutar("UPDATE otp_token SET usado = 1 WHERE usuario_id = :u AND proposito = :p AND usado = 0",
                Db.p("u", usuarioId, "p", proposito));
        String desafio = UUID.randomUUID().toString();
        String codigo = generarCodigo();
        LocalDateTime ahora = Tiempo.ahora();
        db.insertar("""
                INSERT INTO otp_token (usuario_id, desafio, proposito, codigo_hash, fecha_creacion, fecha_expiracion, intentos, usado)
                VALUES (:u, :d, :p, :h, :ahora, :vence, 0, 0)
                """, Db.p("u", usuarioId, "d", desafio, "p", proposito, "h", ClaveUtil.sha256(codigo), "ahora", ahora,
                "vence", ahora.plusMinutes(minutos)));
        correo.enviarCodigo(Validar.cadena(usuario, "email"), Validar.cadena(usuario, "username"), codigo, proposito, minutos);
        return new AuthApi.DesafioRs(true, desafio, enmascarar(Validar.cadena(usuario, "email")), minutos, null);
    }

    private Map<String, Object> validarCodigo(String desafio, String codigo, String proposito) {
        if (desafio == null || desafio.isBlank() || codigo == null || codigo.isBlank()) {
            throw new BadRequestException("Ingresa el código de 6 caracteres.");
        }
        Map<String, Object> token = db.uno("SELECT * FROM otp_token WHERE desafio = :d AND proposito = :p FOR UPDATE",
                Db.p("d", desafio, "p", proposito));
        if (token == null || Validar.bool(token, "usado")) {
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, "El código no es válido. Solicita uno nuevo.");
        }
        if (((LocalDateTime) token.get("fecha_expiracion")).isBefore(Tiempo.ahora())) {
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, "El código venció. Usa «Reenviar código».");
        }
        if (Validar.numero(token, "intentos") >= intentos) {
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, "Superaste el número de intentos. Solicita un nuevo código.");
        }
        String limpio = codigo.trim().toUpperCase(Locale.ROOT);
        if (!ClaveUtil.sha256(limpio).equals(Validar.cadena(token, "codigo_hash"))) {
            db.ejecutar("UPDATE otp_token SET intentos = intentos + 1 WHERE id = :id", Db.p("id", token.get("id")));
            int restantes = intentos - Validar.numero(token, "intentos") - 1;
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, restantes > 0
                    ? "Código incorrecto. Te quedan " + restantes + " intento(s)."
                    : "Código incorrecto. Superaste el número de intentos; solicita un nuevo código.");
        }
        db.ejecutar("UPDATE otp_token SET usado = 1 WHERE id = :id", Db.p("id", token.get("id")));
        return token;
    }

    private AuthApi.LoginRs emitirSesion(Map<String, Object> usuario) {
        long usuarioId = Validar.largo(usuario, "id");
        String perfil = Perfiles.codigo(Validar.cadena(usuario, "perfil"));
        LocalDateTime inicio = Tiempo.ahora();
        LocalDateTime expira = inicio.plusHours(horas);
        String token = jwt.emitir(usuarioId, Validar.cadena(usuario, "username"), perfil,
                inicio.toEpochSecond(ZoneOffset.ofHours(-5)), expira.toEpochSecond(ZoneOffset.ofHours(-5)));
        db.insertar("""
                INSERT INTO session (user_id, token, fecha_ini_sesion, fecha_expiracion, activo)
                VALUES (:usuario, :token, :inicio, :expira, 1)
                """, Db.p("usuario", usuarioId, "token", ClaveUtil.sha256(token), "inicio", inicio, "expira", expira));
        return new AuthApi.LoginRs(token, "Bearer", usuarioId, Validar.cadena(usuario, "username"),
                Validar.largo(usuario, "perfil_id"), perfil);
    }

    private Map<String, Object> usuario(String filtro, Object valor) {
        return db.uno("""
                SELECT u.id, u.username, u.email, u.password_hash, u.activo, p.perfil_id, p.nombre AS perfil,
                       p.activo AS perfil_activo
                FROM usuario u JOIN perfil p ON p.perfil_id = u.rol
                WHERE
                """ + filtro, Db.p("valor", valor));
    }

    private static void verificarActivo(Map<String, Object> usuario) {
        if (usuario == null || !Validar.bool(usuario, "activo") || !Validar.bool(usuario, "perfil_activo")) {
            throw new ClinicaException(HttpStatus.FORBIDDEN, "Tu perfil no está habilitado para ingresar.");
        }
    }

    static String generarCodigo() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    /** juan.perez@correo.com → j*********@correo.com */
    static String enmascarar(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        int arroba = email.indexOf('@');
        return email.charAt(0) + "*".repeat(Math.max(3, arroba - 1)) + email.substring(arroba);
    }
}
