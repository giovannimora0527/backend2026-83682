package com.uniminuto.clinica.auth;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link AuthService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class AuthRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public AuthRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link AuthService} (consulta): UPDATE usuario SET password_hash = :hash WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarUsuario(Map<String, ?> p) {
        return db.ejecutar("UPDATE usuario SET password_hash = :hash WHERE id = :id", p);
    }

    /**
     * Usada por {@link AuthService} (consulta): SELECT * FROM otp_token WHERE desafio = :d FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarOtpToken(Map<String, ?> p) {
        return db.uno("SELECT * FROM otp_token WHERE desafio = :d FOR UPDATE", p);
    }

    /**
     * Usada por {@link AuthService} (consulta): UPDATE otp_token SET codigo_hash = :h, fecha_creacion = :ahora, fecha_expiracion = :vence, intentos = 0 WHERE …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarOtpToken(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE otp_token SET codigo_hash = :h, fecha_creacion = :ahora, fecha_expiracion = :vence, intentos = 0
                WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link AuthService} (restablecer): UPDATE usuario SET password_hash = :h WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarUsuarioEnRestablecer(Map<String, ?> p) {
        return db.ejecutar("UPDATE usuario SET password_hash = :h WHERE id = :id", p);
    }

    /**
     * Usada por {@link AuthService} (restablecer): UPDATE session SET activo = 0 WHERE user_id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarSession(Map<String, ?> p) {
        return db.ejecutar("UPDATE session SET activo = 0 WHERE user_id = :id", p);
    }

    /**
     * Usada por {@link AuthService} (logout): UPDATE session SET activo = 0 WHERE session_id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarSessionEnLogout(Map<String, ?> p) {
        return db.ejecutar("UPDATE session SET activo = 0 WHERE session_id = :id", p);
    }

    /**
     * Usada por {@link AuthService} (logout): UPDATE otp_token SET usado = 1 WHERE usuario_id = :u AND proposito = :p AND usado = 0
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarOtpTokenEnLogout(Map<String, ?> p) {
        return db.ejecutar("UPDATE otp_token SET usado = 1 WHERE usuario_id = :u AND proposito = :p AND usado = 0", p);
    }

    /**
     * Usada por {@link AuthService} (logout): INSERT INTO otp_token (usuario_id, desafio, proposito, codigo_hash, fecha_creacion, fecha_expiracion, intentos…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarOtpToken(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO otp_token (usuario_id, desafio, proposito, codigo_hash, fecha_creacion, fecha_expiracion, intentos, usado)
                VALUES (:u, :d, :p, :h, :ahora, :vence, 0, 0)
                """, p);
    }

    /**
     * Usada por {@link AuthService} (validarCodigo): SELECT * FROM otp_token WHERE desafio = :d AND proposito = :p FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarOtpTokenEnValidarCodigo(Map<String, ?> p) {
        return db.uno("SELECT * FROM otp_token WHERE desafio = :d AND proposito = :p FOR UPDATE", p);
    }

    /**
     * Usada por {@link AuthService} (validarCodigo): UPDATE otp_token SET intentos = intentos + 1 WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarOtpTokenEnValidarCodigo(Map<String, ?> p) {
        return db.ejecutar("UPDATE otp_token SET intentos = intentos + 1 WHERE id = :id", p);
    }

    /**
     * Usada por {@link AuthService} (validarCodigo): UPDATE otp_token SET usado = 1 WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarOtpTokenEnValidarCodigo2(Map<String, ?> p) {
        return db.ejecutar("UPDATE otp_token SET usado = 1 WHERE id = :id", p);
    }

    /**
     * Usada por {@link AuthService} (validarCodigo): INSERT INTO session (user_id, token, fecha_ini_sesion, fecha_expiracion, activo) VALUES (:usuario, :token, :in…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarSession(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO session (user_id, token, fecha_ini_sesion, fecha_expiracion, activo)
                VALUES (:usuario, :token, :inicio, :expira, 1)
                """, p);
    }

    /** Criterios cerrados para buscar un usuario (evita armar SQL con texto recibido). */
    public enum Criterio {
        /** Por nombre de usuario. */
        USERNAME("u.username = :valor"),
        /** Por identificador. */
        ID("u.id = :valor"),
        /** Por nombre de usuario o correo (recuperación de contraseña). */
        USERNAME_O_CORREO("(u.username = :valor OR u.email = :valor)");

        /** Condición SQL fija del criterio. */
        private final String condicion;

        /**
         * @param condicion condición SQL.
         */
        Criterio(String condicion) {
            this.condicion = condicion;
        }
    }

    /**
     * Usuario con su rol según un criterio.
     *
     * @param criterio criterio de búsqueda.
     * @param valor valor buscado.
     * @return usuario o null.
     */
    public Map<String, Object> usuarioPor(Criterio criterio, Object valor) {
        return db.uno("""
                SELECT u.id, u.username, u.email, u.password_hash, u.activo, p.perfil_id, p.nombre AS perfil,
                       p.activo AS perfil_activo
                FROM usuario u JOIN perfil p ON p.perfil_id = u.rol
                WHERE
                """ + criterio.condicion, Db.p("valor", valor));
    }
}
