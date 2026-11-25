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

    /** Caracteres del código (sin 0, O, 1 ni I para evitar confusiones). */
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    /** Generador criptográfico de códigos. */
    private static final SecureRandom ALEATORIO = new SecureRandom();
    /** Tiempo mínimo entre reenvíos de código. */
    private static final int SEGUNDOS_REENVIO = 30;

    /** Emisión de tokens. */
    private final JwtServicio jwt;
    /** Envío de códigos por correo. */
    private final CorreoService correo;
    /** Duración de la sesión en horas. */
    private final int horas;
    /** Si el segundo paso (código por correo) está activo. */
    private final boolean otpHabilitado;
    /** Vigencia del código en minutos. */
    private final int minutos;
    /** Intentos permitidos por código. */
    private final int intentos;

    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /** Acceso a datos del módulo (SQL). */
    private final AuthRepository repositorio;
    /** Permisos del rol para la respuesta del login. */
    private final com.uniminuto.clinica.seguridad.SesionRepository sesiones;

    /**
     * @param repositorio acceso a datos del módulo.
     * @param sesiones permisos del rol.
     * @param jwt servicio de tokens.
     * @param correo servicio de correo.
     * @param horas duración de la sesión.
     * @param otpHabilitado si se exige el código por correo.
     * @param minutos vigencia del código.
     * @param intentos intentos permitidos.
     * @param auditoria log de operaciones sensibles.
     */
    public AuthService(AuthRepository repositorio, com.uniminuto.clinica.seguridad.SesionRepository sesiones, JwtServicio jwt, CorreoService correo, @Value("${clinica.jwt.horas}") int horas,
                       @Value("${clinica.otp.habilitado:true}") boolean otpHabilitado,
                       @Value("${clinica.otp.minutos:5}") int minutos, @Value("${clinica.otp.intentos:5}") int intentos, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.sesiones = sesiones;
        this.auditoria = auditoria;
        this.jwt = jwt;
        this.correo = correo;
        this.horas = horas;
        this.otpHabilitado = otpHabilitado;
        this.minutos = minutos;
        this.intentos = intentos;
    }

    // ------------------------------------------------------------------ Paso 1: credenciales

    /**
     * Paso 1: valida usuario y contraseña y envía el código al correo.
     *
     * @param datos credenciales.
     * @return desafío a verificar (o la sesión si la verificación está deshabilitada).
     */
    @Transactional
    public AuthApi.DesafioRs login(AuthApi.LoginRq datos) {
        if (datos == null || datos.username() == null || datos.username().isBlank()
                || datos.password() == null || datos.password().isEmpty()) {
            throw new BadRequestException("Debes ingresar tu usuario y contraseña.");
        }
        Map<String, Object> usuario = repositorio.usuarioPor(AuthRepository.Criterio.USERNAME, datos.username().trim());
        String hash = usuario == null ? null : Validar.cadena(usuario, "password_hash");
        if (usuario == null || !ClaveUtil.verificar(datos.password(), hash)) {
            auditoria.registrarSinSesion(usuario == null ? null : Validar.largo(usuario, "id"), datos.username().trim(), null,
                    "LOGIN_FALLIDO", usuario == null ? "Usuario inexistente" : "Contraseña incorrecta");
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }
        verificarActivo(usuario);
        long usuarioId = Validar.largo(usuario, "id");
        if (ClaveUtil.esHeredado(hash)) {
            repositorio.actualizarUsuario(Db.p("hash", ClaveUtil.hash(datos.password()), "id", usuarioId));
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
        Map<String, Object> usuario = repositorio.usuarioPor(AuthRepository.Criterio.ID, token.get("usuario_id"));
        verificarActivo(usuario);
        return emitirSesion(usuario);
    }

    /**
     * Genera y envía un nuevo código para el mismo desafío (invalida el anterior).
     *
     * @param desafio identificador del desafío.
     * @return desafío vigente.
     */
    @Transactional
    public AuthApi.DesafioRs reenviar(String desafio) {
        Map<String, Object> token = desafio == null ? null : repositorio.buscarOtpToken(Db.p("d", desafio));
        if (token == null || Validar.bool(token, "usado")) {
            throw new ConflictException("La solicitud expiró. Vuelve a iniciar el proceso.");
        }
        LocalDateTime creado = (LocalDateTime) token.get("fecha_creacion");
        if (creado.plusSeconds(SEGUNDOS_REENVIO).isAfter(Tiempo.ahora())) {
            throw new ConflictException("Espera unos segundos antes de solicitar un nuevo código.");
        }
        Map<String, Object> usuario = repositorio.usuarioPor(AuthRepository.Criterio.ID, token.get("usuario_id"));
        verificarActivo(usuario);
        String codigo = generarCodigo();
        LocalDateTime ahora = Tiempo.ahora();
        repositorio.actualizarOtpToken(Db.p("h", ClaveUtil.sha256(codigo), "ahora", ahora, "vence", ahora.plusMinutes(minutos),
                "id", token.get("id")));
        String proposito = Validar.cadena(token, "proposito");
        correo.enviarCodigo(Validar.cadena(usuario, "email"), Validar.cadena(usuario, "username"), codigo, proposito, minutos);
        return new AuthApi.DesafioRs(true, desafio, enmascarar(Validar.cadena(usuario, "email")), minutos, null);
    }

    // ------------------------------------------------------------------ Recuperación de contraseña

    /**
     * Inicia la recuperación de contraseña sin revelar si la cuenta existe.
     *
     * @param valor usuario o correo.
     * @return desafío de recuperación.
     */
    @Transactional
    public AuthApi.DesafioRs recuperar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("Ingresa tu usuario o tu correo electrónico.");
        }
        Map<String, Object> usuario = repositorio.usuarioPor(AuthRepository.Criterio.USERNAME_O_CORREO, valor.trim());
        if (usuario == null || !Validar.bool(usuario, "activo")) {
            // Misma respuesta que un usuario existente: no se revela si la cuenta existe.
            return new AuthApi.DesafioRs(true, UUID.randomUUID().toString(), null, minutos, null);
        }
        return enviarDesafio(usuario, "RECUPERACION");
    }

    /**
     * Cambia la contraseña con el código recibido y cierra las sesiones abiertas.
     *
     * @param datos desafío, código y nueva contraseña.
     */
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
        repositorio.actualizarUsuarioEnRestablecer(Db.p("h", ClaveUtil.hash(clave), "id", usuarioId));
        repositorio.actualizarSession(Db.p("id", usuarioId));
    }

    // ------------------------------------------------------------------ Sesión

    /**
     * @return datos de la sesión actual (sin token).
     */
    public AuthApi.LoginRs validar() {
        SesionUsuario s = SesionActual.obtener();
        return new AuthApi.LoginRs(null, "Bearer", s.usuarioId(), s.username(), s.perfilId(), s.perfil(), s.permisos());
    }

    /** Cierra la sesión actual (el token deja de ser válido). */
    public void logout() {
        repositorio.actualizarSessionEnLogout(Db.p("id", SesionActual.obtener().sesionId()));
        auditoria.registrar("LOGOUT", "usuario", SesionActual.usuarioId(), "Cierre de sesión");
    }

    // ------------------------------------------------------------------ Utilidades

    /**
     * Crea el código (guardado como hash) y lo envía al correo del usuario.
     *
     * @param usuario fila del usuario.
     * @param proposito LOGIN o RECUPERACION.
     * @return desafío creado.
     */
    private AuthApi.DesafioRs enviarDesafio(Map<String, Object> usuario, String proposito) {
        long usuarioId = Validar.largo(usuario, "id");
        repositorio.actualizarOtpTokenEnLogout(Db.p("u", usuarioId, "p", proposito));
        String desafio = UUID.randomUUID().toString();
        String codigo = generarCodigo();
        LocalDateTime ahora = Tiempo.ahora();
        repositorio.insertarOtpToken(Db.p("u", usuarioId, "d", desafio, "p", proposito, "h", ClaveUtil.sha256(codigo), "ahora", ahora,
                "vence", ahora.plusMinutes(minutos)));
        correo.enviarCodigo(Validar.cadena(usuario, "email"), Validar.cadena(usuario, "username"), codigo, proposito, minutos);
        return new AuthApi.DesafioRs(true, desafio, enmascarar(Validar.cadena(usuario, "email")), minutos, null);
    }

    /**
     * Valida el código: existencia, vigencia, intentos y coincidencia; lo marca como usado.
     *
     * @param desafio identificador del desafío.
     * @param codigo código ingresado.
     * @param proposito propósito esperado.
     * @return fila del token validado.
     */
    private Map<String, Object> validarCodigo(String desafio, String codigo, String proposito) {
        if (desafio == null || desafio.isBlank() || codigo == null || codigo.isBlank()) {
            throw new BadRequestException("Ingresa el código de 6 caracteres.");
        }
        Map<String, Object> token = repositorio.buscarOtpTokenEnValidarCodigo(Db.p("d", desafio, "p", proposito));
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
            repositorio.actualizarOtpTokenEnValidarCodigo(Db.p("id", token.get("id")));
            int restantes = intentos - Validar.numero(token, "intentos") - 1;
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, restantes > 0
                    ? "Código incorrecto. Te quedan " + restantes + " intento(s)."
                    : "Código incorrecto. Superaste el número de intentos; solicita un nuevo código.");
        }
        repositorio.actualizarOtpTokenEnValidarCodigo2(Db.p("id", token.get("id")));
        return token;
    }

    /**
     * Emite el JWT y registra la sesión.
     *
     * @param usuario fila del usuario.
     * @return sesión iniciada.
     */
    private AuthApi.LoginRs emitirSesion(Map<String, Object> usuario) {
        long usuarioId = Validar.largo(usuario, "id");
        String perfil = Perfiles.codigo(Validar.cadena(usuario, "perfil"));
        LocalDateTime inicio = Tiempo.ahora();
        LocalDateTime expira = inicio.plusHours(horas);
        String token = jwt.emitir(usuarioId, Validar.cadena(usuario, "username"), perfil,
                inicio.toEpochSecond(ZoneOffset.ofHours(-5)), expira.toEpochSecond(ZoneOffset.ofHours(-5)));
        repositorio.insertarSession(Db.p("usuario", usuarioId, "token", ClaveUtil.sha256(token), "inicio", inicio, "expira", expira));
        auditoria.registrarSinSesion(usuarioId, Validar.cadena(usuario, "username"), perfil, "LOGIN", "Inicio de sesión exitoso (contraseña y código verificados)");
        return new AuthApi.LoginRs(token, "Bearer", usuarioId, Validar.cadena(usuario, "username"),
                Validar.largo(usuario, "perfil_id"), perfil,
                sesiones.permisos(Validar.largo(usuario, "perfil_id")));
    }



    /**
     * Verifica que el usuario y su perfil estén activos.
     *
     * @param usuario fila del usuario.
     */
    private static void verificarActivo(Map<String, Object> usuario) {
        if (usuario == null || !Validar.bool(usuario, "activo") || !Validar.bool(usuario, "perfil_activo")) {
            throw new ClinicaException(HttpStatus.FORBIDDEN, "Tu perfil no está habilitado para ingresar.");
        }
    }

    /**
     * @return código aleatorio de 6 caracteres.
     */
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
