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

    /** Clase de utilidades: no se instancia. */
    private Validar() {
    }

    /**
     * Valida un texto obligatorio con longitud máxima.
     *
     * @param valor texto recibido.
     * @param campo nombre del campo para el mensaje de error.
     * @param maximo longitud máxima.
     * @return texto sin espacios al inicio ni al final.
     */
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

    /**
     * Valida que un valor no sea nulo.
     *
     * @param valor valor recibido.
     * @param campo nombre del campo para el mensaje.
     */
    public static void requerido(Object valor, String campo) {
        if (valor == null) {
            throw new BadRequestException("El campo " + campo + " es obligatorio.");
        }
    }

    /**
     * Valida un entero obligatorio con mínimo.
     *
     * @param valor valor recibido.
     * @param campo nombre del campo.
     * @param minimo valor mínimo permitido.
     * @return el valor.
     */
    public static int entero(Integer valor, String campo, int minimo) {
        requerido(valor, campo);
        if (valor < minimo) {
            throw new BadRequestException("El campo " + campo + " debe ser mayor o igual a " + minimo + ".");
        }
        return valor;
    }

    /**
     * Valida un valor monetario no negativo con máximo dos decimales.
     *
     * @param valor valor recibido.
     * @param campo nombre del campo.
     * @return valor con escala 2.
     */
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

    /**
     * Valida un porcentaje entre 0 y 100 (nulo se toma como 0).
     *
     * @param valor valor recibido.
     * @param campo nombre del campo.
     * @return porcentaje con escala 2.
     */
    public static BigDecimal porcentaje(BigDecimal valor, String campo) {
        BigDecimal v = dinero(valor == null ? BigDecimal.ZERO : valor, campo);
        if (v.compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("El campo " + campo + " no puede superar 100%.");
        }
        return v;
    }

    /**
     * Convierte un texto AAAA-MM-DD en fecha.
     *
     * @param valor texto recibido.
     * @param campo nombre del campo.
     * @param obligatorio si es obligatorio.
     * @return fecha o null si es opcional y viene vacía.
     */
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

    /**
     * Convierte un texto AAAA-MM-DDTHH:MM[:SS] en fecha y hora.
     *
     * @param valor texto recibido.
     * @param campo nombre del campo.
     * @return fecha y hora.
     */
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

    /**
     * @param fila fila leída de la base de datos.
     * @param clave columna.
     * @return valor como texto o null.
     */
    public static String cadena(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? null : v.toString();
    }

    /**
     * @param fila fila leída de la base de datos.
     * @param clave columna numérica.
     * @return valor como long (0 si es nulo).
     */
    public static long largo(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? 0 : ((Number) v).longValue();
    }

    /**
     * @param fila fila leída de la base de datos.
     * @param clave columna numérica.
     * @return valor como Long o null.
     */
    public static Long largoNulo(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? null : ((Number) v).longValue();
    }

    /**
     * @param fila fila leída de la base de datos.
     * @param clave columna numérica.
     * @return valor como int (0 si es nulo).
     */
    public static int numero(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        return v == null ? 0 : ((Number) v).intValue();
    }

    /**
     * @param fila fila leída de la base de datos.
     * @param clave columna decimal.
     * @return valor como BigDecimal (0 si es nulo).
     */
    public static BigDecimal decimal(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return v instanceof BigDecimal b ? b : new BigDecimal(v.toString());
    }

    /**
     * @param fila fila leída de la base de datos.
     * @param clave columna booleana o TINYINT.
     * @return valor booleano.
     */
    public static boolean bool(Map<String, Object> fila, String clave) {
        Object v = fila.get(clave);
        if (v instanceof Boolean b) {
            return b;
        }
        return v instanceof Number n && n.intValue() != 0;
    }

    /**
     * @param pagina número de página recibido.
     * @return página válida (0 si es nula o negativa).
     */
    public static int pagina(Integer pagina) {
        return pagina == null || pagina < 0 ? 0 : pagina;
    }
}
