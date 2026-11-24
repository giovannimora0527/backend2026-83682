package com.uniminuto.clinica.auth;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ClinicaException;
import com.uniminuto.clinica.seguridad.ClaveUtil;
import com.uniminuto.clinica.seguridad.JwtServicio;
import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.SesionActual;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Inicio de sesión: verifica la contraseña (PBKDF2, o MD5 heredado que se migra),
 * emite el JWT y registra la sesión en la tabla {@code session}.
 */
@Service
public class AuthService {

    private final Db db;
    private final JwtServicio jwt;
    private final int horas;

    public AuthService(Db db, JwtServicio jwt, @Value("${clinica.jwt.horas}") int horas) {
        this.db = db;
        this.jwt = jwt;
        this.horas = horas;
    }

    @Transactional
    public AuthApi.LoginRs login(AuthApi.LoginRq datos) {
        if (datos == null || datos.username() == null || datos.username().isBlank()
                || datos.password() == null || datos.password().isEmpty()) {
            throw new BadRequestException("Debes ingresar tu usuario y contraseña.");
        }
        Map<String, Object> usuario = db.uno("""
                SELECT u.id, u.username, u.password_hash, u.activo, p.perfil_id, p.nombre AS perfil,
                       p.activo AS perfil_activo
                FROM usuario u JOIN perfil p ON p.perfil_id = u.rol
                WHERE u.username = :username
                """, Db.p("username", datos.username().trim()));
        String hash = usuario == null ? null : Validar.cadena(usuario, "password_hash");
        if (usuario == null || !ClaveUtil.verificar(datos.password(), hash)) {
            throw new ClinicaException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }
        if (!Validar.bool(usuario, "activo") || !Validar.bool(usuario, "perfil_activo")) {
            throw new ClinicaException(HttpStatus.FORBIDDEN, "Tu perfil no está habilitado para ingresar.");
        }
        long usuarioId = Validar.largo(usuario, "id");
        if (ClaveUtil.esHeredado(hash)) {
            db.ejecutar("UPDATE usuario SET password_hash = :hash WHERE id = :id",
                    Db.p("hash", ClaveUtil.hash(datos.password()), "id", usuarioId));
        }
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

    public AuthApi.LoginRs validar() {
        SesionUsuario s = SesionActual.obtener();
        return new AuthApi.LoginRs(null, "Bearer", s.usuarioId(), s.username(), s.perfilId(), s.perfil());
    }

    public void logout() {
        db.ejecutar("UPDATE session SET activo = 0 WHERE session_id = :id",
                Db.p("id", SesionActual.obtener().sesionId()));
    }
}
