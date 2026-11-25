package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.ForbiddenException;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Carga masiva de inventario desde CSV o Excel en dos pasos:
 * <ol>
 *   <li><b>Vista previa</b>: valida estructura, encabezados, campos obligatorios, duplicados (en el archivo y en
 *   la base de datos), precios, cantidades y fechas. Guarda cada fila con sus errores (log de cargue).</li>
 *   <li><b>Confirmación</b>: importa solo las filas válidas. Cada fila se procesa en su propia transacción, de modo
 *   que un error en un registro no deja inconsistencias en los demás.</li>
 * </ol>
 * Se registra el usuario y la fecha de la carga y de la confirmación.
 */
@Service
public class ImportacionService {

    /** Columnas esperadas en el archivo de importación. */
    static final List<String> COLUMNAS = List.of("codigo", "nombre", "descripcion", "presentacion", "tipo", "invima",
            "precioCompra", "precioVenta", "stockMinimo", "fechaCompra", "fechaIngreso", "fechaProximoPedido",
            "fechaVence", "activo", "numeroLote", "cantidad");

    /** Validación y registro de productos. */
    private final InventarioService inventario;
    /** Transacción independiente por fila importada. */
    private final TransactionTemplate porFila;

    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /** Acceso a datos del módulo (SQL). */
    private final ImportacionRepository repositorio;

    /**
     * @param repositorio acceso a datos del módulo.
     * @param inventario servicio de inventario.
     * @param tm administrador de transacciones.
     * @param auditoria log de operaciones sensibles.
     */
    public ImportacionService(ImportacionRepository repositorio, InventarioService inventario, PlatformTransactionManager tm, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.inventario = inventario;
        this.auditoria = auditoria;
        this.porFila = new TransactionTemplate(tm);
        this.porFila.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Lee y valida un archivo CSV o Excel sin modificar el inventario.
     *
     * @param archivo archivo cargado.
     * @return resumen y filas con errores.
     */
    @Transactional
    public Map<String, Object> previsualizar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Selecciona un archivo .csv o .xlsx.");
        }
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("No fue posible leer el archivo.");
        }
        String nombre = archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename();
        List<LectorArchivo.Fila> filas = LectorArchivo.leer(nombre, contenido, COLUMNAS);

        long id = repositorio.insertarImportacionInventario(Db.p("u", SesionActual.usuarioId(), "a", nombre.length() > 255 ? nombre.substring(0, 255) : nombre,
                "f", Tiempo.ahora(), "t", filas.size()));
        Set<String> codigos = new HashSet<>();
        Set<String> nombres = new HashSet<>();
        int rechazados = 0;
        for (LectorArchivo.Fila fila : filas) {
            List<String> errores = new ArrayList<>();
            Map<String, String> v = fila.valores();
            ProductoRq producto = aProducto(v, errores);
            InventarioService.Producto valido = inventario.validarProducto(producto, null, errores);
            String codigo = v.get("codigo").toUpperCase(Locale.ROOT);
            if (!codigo.isEmpty() && !codigos.add(codigo)) {
                errores.add("Código duplicado dentro del archivo.");
            }
            String nombreProducto = v.get("nombre").toLowerCase(Locale.ROOT);
            if (!nombreProducto.isEmpty() && !nombres.add(nombreProducto)) {
                errores.add("Nombre duplicado dentro del archivo.");
            }
            Integer cantidad = entero(v.get("cantidad"), "cantidad", errores);
            String lote = v.get("numeroLote").isBlank() ? null : v.get("numeroLote");
            if (cantidad != null && cantidad < 0) {
                errores.add("La cantidad no puede ser negativa.");
            }
            if (cantidad != null && cantidad > 0 && lote == null) {
                errores.add("El número de lote es obligatorio cuando la cantidad es mayor que cero.");
            }
            if (lote != null && lote.length() > 50) {
                errores.add("El número de lote admite máximo 50 caracteres.");
            }
            if (valido != null && valido.fvence() != null && cantidad != null && cantidad > 0
                    && valido.fvence().isBefore(Tiempo.hoy())) {
                errores.add("No se pueden ingresar existencias vencidas.");
            }
            if (!errores.isEmpty()) {
                rechazados++;
            }
            guardarFila(id, fila.numero(), v, producto, lote, cantidad, errores);
        }
        repositorio.actualizarImportacionInventario(Db.p("v", filas.size() - rechazados, "r", rechazados, "id", id));
        return detalle(id);
    }

    /** Confirma la importación de las filas válidas; no admite confirmar dos veces la misma carga. */
    public Map<String, Object> confirmar(long id) {
        Map<String, Object> carga = porFila.execute(estado -> {
            Map<String, Object> c = repositorio.obtenerImportacionInventario(Db.p("id", id), "La importación no existe.");
            if (c.get("fecha_confirmacion") != null) {
                throw new ConflictException("La importación ya fue confirmada.");
            }
            if (Validar.largo(c, "usuario_id") != SesionActual.usuarioId()) {
                throw new ForbiddenException("Solo el usuario que cargó el archivo puede confirmarlo.");
            }
            repositorio.actualizarImportacionInventarioEnConfirmar(Db.p("f", Tiempo.ahora(), "id", id));
            return c;
        });
        int importados = 0;
        int rechazados = Validar.numero(carga, "rechazados");
        for (Map<String, Object> fila : repositorio.listarImportacionInventarioFila(Db.p("id", id))) {
            try {
                porFila.executeWithoutResult(estado -> importarFila(id, fila));
                importados++;
            } catch (RuntimeException e) {
                rechazados++;
                String mensaje = e instanceof com.uniminuto.clinica.exception.ClinicaException
                        ? e.getMessage() : "Error al importar: registro duplicado o inválido.";
                repositorio.actualizarImportacionInventarioFila(Db.p("e", mensaje, "fila", fila.get("id")));
            }
        }
        repositorio.actualizarImportacionInventarioEnConfirmar2(Db.p("i", importados, "r", rechazados, "id", id));
        auditoria.registrar("IMPORTACION_CONFIRMADA", "importacion_inventario", id, "Carga masiva " + id + " confirmada");
        return detalle(id);
    }

    /**
     * Importa una fila válida (producto y lote).
     *
     * @param importacion identificador de la importación.
     * @param f fila validada.
     */
    private void importarFila(long importacion, Map<String, Object> f) {
        List<String> errores = new ArrayList<>();
        ProductoRq rq = new ProductoRq(Validar.cadena(f, "codigo"), Validar.cadena(f, "nombre"),
                Validar.cadena(f, "descripcion"), Validar.cadena(f, "presentacion"), Validar.cadena(f, "tipo"),
                Validar.cadena(f, "invima"), (BigDecimal) f.get("precio_compra"), (BigDecimal) f.get("precio_venta"),
                (Integer) f.get("stock_minimo"), texto(f.get("fecha_compra")), texto(f.get("fecha_ingreso")),
                texto(f.get("fecha_proximo_pedido")), texto(f.get("fecha_vence")),
                f.get("activo") == null || Validar.bool(f, "activo"), null);
        InventarioService.Producto p = inventario.validarProducto(rq, null, errores);
        if (p == null) {
            throw new ConflictException(String.join(" · ", errores));
        }
        long medicamento = repositorio.insertarMedicamento(p.parametros());
        Integer cantidad = (Integer) f.get("cantidad");
        if (cantidad != null && cantidad > 0) {
            long lote = repositorio.insertarMedicamentoLote(Db.p("m", medicamento, "n", f.get("numero_lote"), "c", p.compra(), "i", p.fingreso(),
                    "v", p.fvence(), "q", cantidad));
            inventario.mover(lote, "ENTRADA", cantidad, "Carga masiva de inventario", "Importación " + importacion,
                    null, null, importacion);
        }
        repositorio.actualizarImportacionInventarioFilaEnImportarFila(Db.p("m", medicamento, "id", f.get("id")));
    }

    /**
     * @return importaciones realizadas.
     */
    public List<Map<String, Object>> historial() {
        return repositorio.listarImportacionInventario();
    }

    /**
     * @param id identificador de la importación.
     * @return importación con sus filas.
     */
    public Map<String, Object> detalle(long id) {
        Map<String, Object> resumen = repositorio.obtenerImportacionInventarioEnDetalle(Db.p("id", id), "La importación no existe.");
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Map<String, Object> f : repositorio.listarImportacionInventarioFilaEnDetalle(Db.p("id", id))) {
            Map<String, Object> producto = new LinkedHashMap<>();
            producto.put("codigo", f.get("codigo"));
            producto.put("nombre", f.get("nombre"));
            producto.put("descripcion", f.get("descripcion"));
            producto.put("presentacion", f.get("presentacion"));
            producto.put("tipo", f.get("tipo"));
            producto.put("invima", f.get("invima"));
            producto.put("precioCompra", f.get("precio_compra"));
            producto.put("precioVenta", f.get("precio_venta"));
            producto.put("stockMinimo", f.get("stock_minimo"));
            producto.put("fechaCompra", f.get("fecha_compra"));
            producto.put("fechaIngreso", f.get("fecha_ingreso"));
            producto.put("fechaProximoPedido", f.get("fecha_proximo_pedido"));
            producto.put("fechaVence", f.get("fecha_vence"));
            producto.put("activo", f.get("activo") == null || Validar.bool(f, "activo"));
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("numero", f.get("numero_fila"));
            fila.put("producto", producto);
            String errores = Validar.cadena(f, "errores");
            fila.put("errores", errores == null ? List.of() : Arrays.asList(errores.split(" · ")));
            fila.put("medicamentoId", f.get("medicamento_id"));
            filas.add(fila);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("resumen", resumen);
        r.put("filas", filas);
        return r;
    }

    // ------------------------------------------------------------------ Conversión de valores

    /**
     * @param v valores de la fila.
     * @param errores lista donde se agregan errores.
     * @return datos del producto.
     */
    private ProductoRq aProducto(Map<String, String> v, List<String> errores) {
        return new ProductoRq(v.get("codigo"), v.get("nombre"), v.get("descripcion"), v.get("presentacion"),
                v.get("tipo"), v.get("invima"), decimal(v.get("precioCompra"), "precio de compra", errores),
                decimal(v.get("precioVenta"), "precio de venta", errores),
                entero(v.get("stockMinimo"), "stock mínimo", errores), v.get("fechaCompra"), v.get("fechaIngreso"),
                v.get("fechaProximoPedido"), v.get("fechaVence"), booleano(v.get("activo"), errores), null);
    }

    /**
     * Guarda la fila con su resultado de validación.
     *
     * @param importacion identificador.
     * @param numero número de fila.
     * @param v valores.
     * @param p producto.
     * @param lote número de lote.
     * @param errores errores encontrados.
     */
    private void guardarFila(long importacion, int numero, Map<String, String> v, ProductoRq p, String lote,
                             Integer cantidad, List<String> errores) {
        repositorio.insertarImportacionInventarioFila(Db.p("imp", importacion, "num", numero, "codigo", corto(v.get("codigo").toUpperCase(Locale.ROOT), 50),
                "nombre", corto(v.get("nombre"), 255), "descripcion", vacio(v.get("descripcion")),
                "presentacion", corto(v.get("presentacion"), 255), "tipo", corto(v.get("tipo").toUpperCase(Locale.ROOT), 30),
                "invima", corto(v.get("invima"), 100), "compra", acotar(p.precioCompra()), "venta", acotar(p.precioVenta()),
                "minimo", p.stockMinimo(), "fcompra", fechaSegura(v.get("fechaCompra")),
                "fingreso", fechaSegura(v.get("fechaIngreso")), "fpedido", fechaSegura(v.get("fechaProximoPedido")),
                "fvence", fechaSegura(v.get("fechaVence")), "activo", p.activo(), "lote", corto(lote, 50),
                "cantidad", cantidad, "errores", errores.isEmpty() ? null : String.join(" · ", errores)));
    }

    /**
     * @param valor texto.
     * @param campo nombre del campo.
     * @param errores lista de errores.
     * @return número o null.
     */
    private static BigDecimal decimal(String valor, String campo, List<String> errores) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim().replace("$", "").replace(" ", "");
        if (limpio.contains(",") && !limpio.contains(".")) {
            limpio = limpio.replace(',', '.');
        }
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            errores.add("El " + campo + " no es un número válido.");
            return null;
        }
    }

    /**
     * @param valor texto.
     * @param campo nombre del campo.
     * @param errores lista de errores.
     * @return entero o null.
     */
    private static Integer entero(String valor, String campo, List<String> errores) {
        if (valor == null || valor.isBlank()) {
            return "cantidad".equals(campo) ? 0 : null;
        }
        try {
            return new BigDecimal(valor.trim()).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            errores.add("El campo " + campo + " debe ser un número entero.");
            return null;
        }
    }

    /**
     * @param valor texto (si/no, true/false, 1/0).
     * @param errores lista de errores.
     * @return valor o null.
     */
    private static Boolean booleano(String valor, List<String> errores) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "si", "sí", "s", "activo" -> true;
            case "false", "0", "no", "n", "inactivo" -> false;
            default -> {
                errores.add("El campo activo debe ser true o false.");
                yield true;
            }
        };
    }

    /**
     * @param valor texto.
     * @return fecha o null si no es válida.
     */
    private static LocalDate fechaSegura(String valor) {
        try {
            return Validar.fecha(valor, "fecha", false);
        } catch (BadRequestException e) {
            return null;
        }
    }

    /**
     * @param valor número.
     * @return número limitado al rango de la columna.
     */
    private static BigDecimal acotar(BigDecimal valor) {
        if (valor == null || valor.abs().compareTo(new BigDecimal("999999999999")) > 0) {
            return null;
        }
        return valor.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * @param valor texto.
     * @param maximo longitud máxima.
     * @return texto recortado.
     */
    private static String corto(String valor, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.length() > maximo ? valor.substring(0, maximo) : valor;
    }

    /**
     * @param valor texto.
     * @return null si está vacío.
     */
    private static String vacio(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    /**
     * @param valor valor.
     * @return texto o cadena vacía.
     */
    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}
