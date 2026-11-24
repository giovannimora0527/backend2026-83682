package com.uniminuto.clinica.comun;

import com.uniminuto.clinica.exception.NotFoundException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos con SQL parametrizado (NamedParameterJdbcTemplate).
 *
 * <p>Las filas se devuelven como mapas ordenados cuyas claves son los alias de las
 * columnas, de modo que el JSON resultante coincide con el contrato del frontend.
 * Todos los parámetros se enlazan con nombre (:param), nunca se concatenan valores,
 * lo que evita inyección SQL.</p>
 */
@Component
public class Db {

    private final NamedParameterJdbcTemplate jdbc;

    public Db(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Convierte tipos JDBC a tipos que Jackson serializa como texto ISO. */
    private static final RowMapper<Map<String, Object>> FILA = (rs, n) -> {
        ResultSetMetaData md = rs.getMetaData();
        Map<String, Object> fila = new LinkedHashMap<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            fila.put(md.getColumnLabel(i), convertir(rs.getObject(i)));
        }
        return fila;
    };

    static Object convertir(Object valor) {
        if (valor instanceof Timestamp t) {
            return t.toLocalDateTime();
        }
        if (valor instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (valor instanceof java.sql.Time t) {
            return t.toLocalTime().toString();
        }
        if (valor instanceof BigInteger b) {
            return b.longValue();
        }
        if (valor instanceof LocalDateTime l) {
            return l.withNano(0);
        }
        return valor;
    }

    /** Crea un mapa de parámetros que admite valores nulos: p("id", 1, "nombre", null). */
    public static Map<String, Object> p(Object... claveValor) {
        Map<String, Object> mapa = new HashMap<>();
        for (int i = 0; i + 1 < claveValor.length; i += 2) {
            mapa.put((String) claveValor[i], claveValor[i + 1]);
        }
        return mapa;
    }

    public List<Map<String, Object>> lista(String sql, Map<String, ?> params) {
        return jdbc.query(sql, params, FILA);
    }

    public List<Map<String, Object>> lista(String sql) {
        return lista(sql, Map.of());
    }

    /** Devuelve la primera fila o {@code null} si no hay resultados. */
    public Map<String, Object> uno(String sql, Map<String, ?> params) {
        List<Map<String, Object>> filas = lista(sql, params);
        return filas.isEmpty() ? null : filas.get(0);
    }

    /** Devuelve la primera fila o lanza 404 con el mensaje indicado. */
    public Map<String, Object> requerido(String sql, Map<String, ?> params, String mensaje) {
        Map<String, Object> fila = uno(sql, params);
        if (fila == null) {
            throw new NotFoundException(mensaje);
        }
        return fila;
    }

    public <T> T valor(String sql, Map<String, ?> params, Class<T> tipo) {
        List<T> valores = jdbc.queryForList(sql, params, tipo);
        return valores.isEmpty() ? null : valores.get(0);
    }

    public long contar(String sql, Map<String, ?> params) {
        Number n = valor(sql, params, Long.class);
        return n == null ? 0 : n.longValue();
    }

    public boolean existe(String sql, Map<String, ?> params) {
        return !jdbc.queryForList(sql, params).isEmpty();
    }

    public int ejecutar(String sql, Map<String, ?> params) {
        return jdbc.update(sql, params);
    }

    /** Ejecuta un INSERT y devuelve la clave autogenerada. */
    public long insertar(String sql, Map<String, ?> params) {
        GeneratedKeyHolder llave = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(params), llave, new String[]{"id"});
        Number key = llave.getKey();
        return key == null ? 0 : key.longValue();
    }
}
