package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.util.RangoFechas;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Valores recibidos. Solo conserva los que el reporte declara y recuerda los aplicados, para mostrarlos. */
public class Filtros {

    private final Map<String, String> valores = new LinkedHashMap<>();
    private final Map<String, Object> aplicados = new LinkedHashMap<>();

    public Filtros(List<Filtro> declarados, Map<String, String> recibidos) {
        for (Filtro f : declarados) {
            String valor = recibidos.get(f.clave());
            if (valor != null && !valor.isBlank()) {
                if (f.opciones() != null && !f.opciones().contains(valor.trim())) {
                    throw new BadRequestException("El filtro " + f.clave() + " debe ser uno de: "
                            + String.join(", ", f.opciones()) + ".");
                }
                valores.put(f.clave(), valor.trim());
                aplicados.put(f.clave(), valor.trim());
            } else if (f.obligatorio()) {
                throw new BadRequestException("El filtro " + f.clave() + " es obligatorio.");
            }
        }
        if (desde() != null && hasta() != null && desde().isAfter(hasta())) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la final.");
        }
    }

    public LocalDate desde() {
        return fecha("desde");
    }

    public LocalDate hasta() {
        return fecha("hasta");
    }

    public RangoFechas rango() {
        return RangoFechas.de(desde(), hasta());
    }

    public String texto(String clave, String porDefecto) {
        String valor = valores.getOrDefault(clave, porDefecto);
        if (valor != null) {
            aplicados.put(clave, valor);
        }
        return valor;
    }

    public Long id(String clave) {
        String valor = valores.get(clave);
        if (valor == null) {
            return null;
        }
        try {
            return Long.valueOf(valor);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("El filtro " + clave + " no es valido.");
        }
    }

    public int entero(String clave, int porDefecto, int minimo, int maximo) {
        Long valor = id(clave);
        long n = valor == null ? porDefecto : valor;
        if (n < minimo || n > maximo) {
            throw new BadRequestException("El filtro " + clave + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        aplicados.put(clave, n);
        return (int) n;
    }

    public Map<String, Object> aplicados() {
        return aplicados;
    }

    private LocalDate fecha(String clave) {
        String valor = valores.get(clave);
        if (valor == null) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("El filtro " + clave + " debe tener el formato yyyy-MM-dd.");
        }
    }
}
