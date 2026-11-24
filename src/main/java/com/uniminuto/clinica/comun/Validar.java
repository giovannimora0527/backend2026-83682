package com.uniminuto.clinica.comun;

import com.uniminuto.clinica.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Validaciones reutilizables del lado servidor. Cada método lanza
 * {@link BadRequestException} con un mensaje claro para el usuario.
 */
public final class Validar {

    private Validar() {
    }

    public static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new BadRequestException("El campo " + campo + " admite máximo " + maximo + " caracteres.");
        }
        return limpio;
    }

    /** Texto opcional: devuelve null si viene vacío. */
    public static String opcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, maximo);
    }

    public static void requerido(Object valor, String campo) {
        if (valor == null) {
            throw new BadRequestException("El campo " + campo + " es obligatorio.");
        }
    }

    public static int entero(Integer valor, String campo, int minimo) {
        requerido(valor, campo);
        if (valor < minimo) {
            throw new BadRequestException("El campo " + campo + " debe ser mayor o igual a " + minimo + ".");
        }
        return valor;
    }

    public static BigDecimal dinero(BigDecimal valor, String campo) {
        requerido(valor, campo);
        if (valor.signum() < 0) {
            throw new BadRequestException("El campo " + campo + " no puede ser negativo.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new BadRequestException("El campo " + campo + " admite máximo dos decimales.");
        }
        if (valor.compareTo(new BigDecimal("999999999999")) > 0) {
            throw new BadRequestException("El campo " + campo + " excede el valor máximo permitido.");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal porcentaje(BigDecimal valor, String campo) {
        BigDecimal v = dinero(valor == null ? BigDecimal.ZERO : valor, campo);
        if (v.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("El campo " + campo + " no puede superar 100%.");
        }
        return v;
    }

    public static LocalDate fecha(String valor, String campo, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new BadRequestException("El campo " + campo + " es obligatorio.");
            }
            return null;
        }
        String v = valor.trim();
        try {
            return LocalDate.parse(v.substring(0, Math.min(10, v.length())));
        } catch (DateTimeParseException e) {
            throw new BadRequestException("El campo " + campo + " debe tener formato AAAA-MM-DD.");
        }
    }

    public static LocalDateTime fechaHora(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("El campo " + campo + " es obligatorio.");
        }
        String v = valor.trim().replace(' ', 'T');
        if (v.length() == 16) {
            v = v + ":00";
        }
        try {
            return LocalDateTime.parse(v.substring(0, Math.min(19, v.length())));
        } catch (DateTimeParseException e) {
            throw new BadRequestException("El campo " + campo + " debe tener formato AAAA-MM-DDTHH:MM.");
        }
    }

    /** Identificador opcional recibido como texto en filtros de consulta. */
    public static Long id(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            long n = Long.parseLong(valor.trim());
            if (n <= 0) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            throw new BadRequestException("El filtro " + campo + " debe ser un identificador numérico.");
        }
    }

    public static String cadena(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? null : v.toString();
    }

    public static long largo(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? 0 : ((Number) v).longValue();
    }

    public static Long largoNulo(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? null : ((Number) v).longValue();
    }

    public static int numero(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? 0 : ((Number) v).intValue();
    }

    public static BigDecimal decimal(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return v instanceof BigDecimal b ? b : new BigDecimal(v.toString());
    }

    public static boolean bool(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        if (v instanceof Boolean b) {
            return b;
        }
        return v instanceof Number n && n.intValue() != 0;
    }

    public static int pagina(Integer pagina) {
        return pagina == null || pagina < 0 ? 0 : pagina;
    }
}
