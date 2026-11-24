package com.uniminuto.clinica.carga;

import com.uniminuto.clinica.entity.ErrorFila;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Convierte y valida una fila por si sola. Acumula todos los errores en lugar de detenerse en el primero. */
public class ConvertidorFila {

    private static final List<DateTimeFormatter> FECHAS = List.of(
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));

    private final Map<String, String> datos;
    private final List<ErrorFila> errores = new ArrayList<>();
    private final List<ErrorFila> advertencias = new ArrayList<>();
    private final DatosFila resultado;

    public ConvertidorFila(Map<String, String> datos, Collection<BigDecimal> porcentajesIva, LocalDate hoy) {
        this.datos = datos;
        String codigo = texto("codigo", true, 8);
        if (codigo != null && !codigo.matches("[A-Za-z0-9]{8}")) {
            error("codigo", "Debe tener exactamente 8 caracteres alfanuméricos");
        }
        TipoProducto tipo = tipo();
        boolean requiere = booleano("requiere_invima") || tipo == TipoProducto.MEDICAMENTO;
        String invima = texto("registro_invima", false, 50);
        if (requiere && invima == null) {
            error("registro_invima", "Es obligatorio para este producto");
        }
        BigDecimal compra = dinero("precio_compra");
        BigDecimal venta = dinero("precio_venta");
        if (compra != null && venta != null && venta.compareTo(compra) < 0) {
            advertencias.add(new ErrorFila("precio_venta", "Es menor que el precio de compra"));
        }
        BigDecimal iva = numero("porcentaje_iva", false);
        BigDecimal ivaFinal = iva == null ? BigDecimal.ZERO : iva;
        if (porcentajesIva.stream().noneMatch(p -> p.compareTo(ivaFinal) == 0) && !tieneError("porcentaje_iva")) {
            error("porcentaje_iva", "No hay un impuesto activo con ese porcentaje");
        }
        Integer stock = entero("stock_minimo", 0);
        Integer cantidad = entero("cantidad", 1);
        LocalDate fechaCompra = fecha("fecha_compra", true);
        if (fechaCompra != null && fechaCompra.isAfter(hoy)) {
            error("fecha_compra", "No puede ser futura");
        }
        LocalDate vence = fecha("fecha_vencimiento", tipo == TipoProducto.MEDICAMENTO);
        if (vence != null && fechaCompra != null && !vence.isAfter(fechaCompra)) {
            error("fecha_vencimiento", "Debe ser posterior a la fecha de compra");
        } else if (vence != null && vence.isBefore(hoy)) {
            error("fecha_vencimiento", "El lote ya está vencido");
        }
        String unidad = texto("unidad_medida", false, 30);
        resultado = new DatosFila(codigo == null ? null : codigo.toUpperCase(Locale.ROOT),
                texto("nombre", true, 100), tipo, texto("descripcion", false, 2000),
                texto("presentacion", false, 100), unidad == null ? "unidad" : unidad, requiere, invima, compra,
                venta, ivaFinal, stock, texto("numero_lote", true, 50), cantidad, fechaCompra, vence,
                fecha("fecha_proximo_pedido", false));
    }

    public DatosFila resultado() {
        return resultado;
    }

    public List<ErrorFila> errores() {
        return errores;
    }

    public List<ErrorFila> advertencias() {
        return advertencias;
    }

    public void error(String campo, String mensaje) {
        errores.add(new ErrorFila(campo, mensaje));
    }

    private boolean tieneError(String campo) {
        return errores.stream().anyMatch(e -> e.campo().equals(campo));
    }

    private String texto(String campo, boolean obligatorio, int maximo) {
        String valor = datos.getOrDefault(campo, "").trim();
        if (valor.isEmpty()) {
            if (obligatorio) {
                error(campo, "Es obligatorio");
            }
            return null;
        }
        if (valor.length() > maximo && !"codigo".equals(campo)) {
            error(campo, "Admite hasta " + maximo + " caracteres");
        }
        return valor;
    }

    private TipoProducto tipo() {
        String valor = texto("tipo_producto", true, 20);
        if (valor == null) {
            return null;
        }
        try {
            return TipoProducto.valueOf(valor.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            error("tipo_producto", "Debe ser MEDICAMENTO o INSUMO");
            return null;
        }
    }

    private boolean booleano(String campo) {
        String valor = datos.getOrDefault(campo, "").trim().toUpperCase(Locale.ROOT);
        if (Set.of("SI", "SÍ", "TRUE", "VERDADERO", "1").contains(valor)) {
            return true;
        }
        if (!Set.of("", "NO", "FALSE", "FALSO", "0").contains(valor)) {
            error(campo, "Debe ser SI o NO");
        }
        return false;
    }

    private BigDecimal numero(String campo, boolean obligatorio) {
        String valor = texto(campo, obligatorio, 30);
        if (valor == null) {
            return null;
        }
        if (!valor.matches("-?\\d{1,12}([.,]\\d+)?")) {
            error(campo, "No es un número válido");
            return null;
        }
        return new BigDecimal(valor.replace(',', '.'));
    }

    private BigDecimal dinero(String campo) {
        BigDecimal valor = numero(campo, true);
        if (valor == null) {
            return null;
        }
        if (valor.signum() <= 0) {
            error(campo, "Debe ser mayor que 0");
            return null;
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            error(campo, "Admite hasta 2 decimales");
            return null;
        }
        return valor;
    }

    private Integer entero(String campo, int minimo) {
        BigDecimal valor = numero(campo, true);
        if (valor == null) {
            return null;
        }
        if (valor.stripTrailingZeros().scale() > 0 || valor.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            error(campo, "Debe ser un número entero");
            return null;
        }
        if (valor.compareTo(BigDecimal.valueOf(minimo)) < 0) {
            error(campo, minimo == 0 ? "No puede ser negativo" : "Debe ser mayor que 0");
            return null;
        }
        return valor.intValueExact();
    }

    private LocalDate fecha(String campo, boolean obligatorio) {
        String valor = texto(campo, obligatorio, 30);
        if (valor == null) {
            return null;
        }
        for (DateTimeFormatter formato : FECHAS) {
            try {
                return LocalDate.parse(valor, formato);
            } catch (DateTimeParseException ex) {
                // Se prueba el siguiente formato.
            }
        }
        error(campo, "Use el formato yyyy-MM-dd o dd/MM/yyyy");
        return null;
    }
}
