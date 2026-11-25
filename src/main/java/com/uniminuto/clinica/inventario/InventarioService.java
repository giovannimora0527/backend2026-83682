package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Requerimiento 1: inventario de medicamentos y productos médicos.
 *
 * <p>Las existencias se llevan por lote ({@code medicamento_lote.cantidad}) y cada cambio queda en el
 * kardex ({@code movimiento_inventario}) con usuario, fecha, saldo y referencia. Ningún movimiento puede
 * dejar un lote con saldo negativo: los lotes se bloquean con {@code SELECT ... FOR UPDATE} y la base de
 * datos además lo impide con restricciones CHECK.</p>
 */
@Service
public class InventarioService {

    /** Tipos de producto. */
    static final Set<String> TIPOS = Set.of("MEDICAMENTO", "PRODUCTO", "UTENSILIO");

    /** Consulta base de la ficha de producto con existencias. */
    private static final String FICHA = """
            SELECT m.*,
                   COALESCE(SUM(l.cantidad), 0) AS existencia,
                   COALESCE(SUM(CASE WHEN l.fecha_vence IS NULL OR l.fecha_vence >= :hoy THEN l.cantidad END), 0) AS disponible,
                   MAX(CASE WHEN l.cantidad > 0 AND l.fecha_vence < :hoy THEN 1 ELSE 0 END) AS lote_vencido,
                   MAX(CASE WHEN l.cantidad > 0 AND l.fecha_vence BETWEEN :hoy AND :limite THEN 1 ELSE 0 END) AS lote_proximo,
                   MIN(CASE WHEN l.cantidad > 0 AND l.fecha_vence >= :hoy THEN l.fecha_vence END) AS vencimiento_lote
            FROM medicamento m
            LEFT JOIN medicamento_lote l ON l.medicamento_id = m.id
            """;

    /** Acceso a datos. */
    private final Db db;

    /** Log central de operaciones sensibles. */
    private final com.uniminuto.clinica.auditoria.AuditoriaService auditoria;

    /**
     * @param db acceso a datos.
     * @param auditoria log de operaciones sensibles.
     */
    public InventarioService(Db db, com.uniminuto.clinica.auditoria.AuditoriaService auditoria) {
        this.db = db;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ Consultas

    /**
     * @return productos con existencias.
     */
    public List<Map<String, Object>> listar() {
        return db.lista(FICHA + " GROUP BY m.id ORDER BY m.nombre", fechas()).stream().map(this::ficha).toList();
    }

    /**
     * @param id identificador del producto.
     * @return ficha del producto.
     */
    public Map<String, Object> ficha(long id) {
        Map<String, Object> fila = db.requerido(FICHA + " WHERE m.id = :id GROUP BY m.id",
                with(fechas(), "id", id), "El producto no existe.");
        return ficha(fila);
    }

    /**
     * @param f fila del producto.
     * @return ficha con lotes y alertas.
     */
    private Map<String, Object> ficha(Map<String, Object> f) {
        LocalDate hoy = Tiempo.hoy();
        LocalDate vence = (LocalDate) f.get("fecha_vence");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", f.get("id"));
        r.put("producto", producto(f));
        r.put("existencia", Validar.numero(f, "existencia"));
        r.put("disponible", Validar.numero(f, "disponible"));
        r.put("vencido", Validar.numero(f, "lote_vencido") == 1 || (vence != null && vence.isBefore(hoy)));
        r.put("proximoVencer", Validar.numero(f, "lote_proximo") == 1
                || (vence != null && !vence.isBefore(hoy) && !vence.isAfter(Tiempo.limiteProximoVencer())));
        r.put("vencimientoLote", f.get("vencimiento_lote"));
        return r;
    }

    /**
     * @param f fila del producto.
     * @return datos del producto para la respuesta.
     */
    static Map<String, Object> producto(Map<String, Object> f) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("codigo", f.get("codigo"));
        p.put("nombre", f.get("nombre"));
        p.put("descripcion", f.get("descripcion"));
        p.put("presentacion", f.get("presentacion"));
        p.put("tipo", f.get("tipo"));
        p.put("invima", f.get("invima"));
        p.put("precioCompra", f.get("precio_compra"));
        p.put("precioVenta", f.get("precio_venta"));
        p.put("ivaPorcentaje", f.get("iva_porcentaje"));
        p.put("stockMinimo", f.get("stock_minimo"));
        p.put("fechaCompra", f.get("fecha_compra"));
        p.put("fechaIngreso", f.get("fecha_ingreso"));
        p.put("fechaProximoPedido", f.get("fecha_proximo_pedido"));
        p.put("fechaVence", f.get("fecha_vence"));
        p.put("activo", Validar.bool(f, "activo"));
        return p;
    }

    /**
     * @param producto identificador del producto.
     * @return lotes con estado de vencimiento.
     */
    public List<Map<String, Object>> lotes(long producto) {
        ficha(producto);
        LocalDate hoy = Tiempo.hoy();
        List<Map<String, Object>> lotes = new ArrayList<>();
        for (Map<String, Object> l : db.lista("""
                SELECT id, numero, costo, fecha_ingreso AS fechaIngreso, fecha_vence AS fechaVence, cantidad
                FROM medicamento_lote WHERE medicamento_id = :m
                ORDER BY fecha_vence IS NULL, fecha_vence, id
                """, Db.p("m", producto))) {
            LocalDate vence = (LocalDate) l.get("fechaVence");
            boolean vencido = vence != null && vence.isBefore(hoy);
            l.put("vencido", vencido);
            l.put("proximoVencer", vence != null && !vencido && !vence.isAfter(Tiempo.limiteProximoVencer()));
            l.put("disponible", !vencido && Validar.numero(l, "cantidad") > 0);
            lotes.add(l);
        }
        return lotes;
    }

    /**
     * @param producto identificador del producto.
     * @return movimientos de inventario.
     */
    public List<Map<String, Object>> movimientos(long producto) {
        ficha(producto);
        return db.lista("""
                SELECT mv.id, mv.tipo, mv.cantidad, mv.delta, mv.motivo, mv.referencia, l.numero AS lote,
                       u.username AS usuario, mv.fecha, mv.saldo_lote AS saldo, mv.formula_id AS formula,
                       mv.despacho_id AS despacho
                FROM movimiento_inventario mv
                JOIN medicamento_lote l ON l.id = mv.lote_id
                JOIN usuario u ON u.id = mv.usuario_id
                WHERE mv.medicamento_id = :m
                ORDER BY mv.fecha DESC, mv.id DESC
                """, Db.p("m", producto));
    }

    // ------------------------------------------------------------------ Registro y edición

    /**
     * @param datos datos del producto.
     * @return producto creado.
     */
    @Transactional
    public Map<String, Object> crear(ProductoRq datos) {
        Producto p = validarProducto(datos, null, true);
        long id = db.insertar("""
                INSERT INTO medicamento (codigo, nombre, descripcion, presentacion, tipo, invima, precio_compra,
                       precio_venta, iva_porcentaje, stock_minimo, fecha_compra, fecha_ingreso, fecha_proximo_pedido,
                       fecha_vence, activo, fecha_creacion_registro)
                VALUES (:codigo, :nombre, :descripcion, :presentacion, :tipo, :invima, :compra, :venta, :iva, :minimo,
                       :fcompra, :fingreso, :fpedido, :fvence, :activo, :ahora)
                """, p.parametros());
        auditoria.registrar("PRODUCTO_CREADO", "medicamento", null, "Producto " + datos.codigo() + " · " + datos.nombre());
        return ficha(id);
    }

    /**
     * @param id identificador del producto.
     * @param datos nuevos datos.
     * @return producto actualizado.
     */
    @Transactional
    public Map<String, Object> actualizar(long id, ProductoRq datos) {
        db.requerido("SELECT id FROM medicamento WHERE id = :id FOR UPDATE", Db.p("id", id), "El producto no existe.");
        Producto p = validarProducto(datos, id, true);
        Map<String, Object> params = p.parametros();
        params.put("id", id);
        db.ejecutar("""
                UPDATE medicamento SET codigo = :codigo, nombre = :nombre, descripcion = :descripcion,
                       presentacion = :presentacion, tipo = :tipo, invima = :invima, precio_compra = :compra,
                       precio_venta = :venta, iva_porcentaje = :iva, stock_minimo = :minimo, fecha_compra = :fcompra,
                       fecha_ingreso = :fingreso, fecha_proximo_pedido = :fpedido, fecha_vence = :fvence,
                       activo = :activo, fecha_modificacion_registro = :ahora
                WHERE id = :id
                """, params);
        auditoria.registrar("PRODUCTO_ACTUALIZADO", "medicamento", id, "Producto " + datos.codigo() + " · " + datos.nombre() + " (datos o precios)");
        return ficha(id);
    }

    /**
     * Registra un lote con su entrada inicial.
     *
     * @param producto identificador del producto.
     * @param l datos del lote.
     * @return ficha actualizada.
     */
    @Transactional
    public Map<String, Object> registrarLote(long producto, InventarioApi.LoteRq l) {
        Map<String, Object> m = db.requerido("SELECT tipo, activo, codigo FROM medicamento WHERE id = :id FOR UPDATE",
                Db.p("id", producto), "El producto no existe.");
        if (l == null) {
            throw new BadRequestException("Los datos del lote son obligatorios.");
        }
        String numero = Validar.texto(l.numero(), "número de lote", 50);
        BigDecimal costo = Validar.dinero(l.costo(), "costo");
        LocalDate ingreso = Validar.fecha(l.fechaIngreso(), "fecha de ingreso", true);
        LocalDate vence = Validar.fecha(l.fechaVence(), "fecha de vencimiento",
                "MEDICAMENTO".equals(m.get("tipo")));
        int cantidad = Validar.entero(l.cantidad(), "cantidad", 1);
        if (ingreso.isAfter(Tiempo.hoy())) {
            throw new BadRequestException("La fecha de ingreso no puede ser futura.");
        }
        if (vence != null && !vence.isAfter(ingreso)) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la de ingreso.");
        }
        if (vence != null && vence.isBefore(Tiempo.hoy())) {
            throw new BadRequestException("No se pueden ingresar lotes vencidos.");
        }
        if (db.existe("SELECT 1 FROM medicamento_lote WHERE medicamento_id = :m AND numero = :n",
                Db.p("m", producto, "n", numero))) {
            throw new ConflictException("El lote " + numero + " ya existe para este producto.");
        }
        long lote = db.insertar("""
                INSERT INTO medicamento_lote (medicamento_id, numero, costo, fecha_ingreso, fecha_vence, cantidad_inicial, cantidad)
                VALUES (:m, :n, :c, :i, :v, :q, 0)
                """, Db.p("m", producto, "n", numero, "c", costo, "i", ingreso, "v", vence, "q", cantidad));
        mover(lote, "ENTRADA", cantidad, "Ingreso de lote", Validar.opcional(l.referencia(), "referencia", 255),
                null, null, null);
        auditoria.registrar("LOTE_REGISTRADO", "medicamento", producto, "Lote " + l.numero() + " del producto " + producto);
        return Map.of("id", lote, "numero", numero, "cantidad", cantidad);
    }

    /**
     * Registra entrada, salida o ajuste sin dejar existencias negativas.
     *
     * @param producto identificador del producto.
     * @param lote identificador del lote.
     * @param mv datos del movimiento.
     * @return ficha actualizada.
     */
    @Transactional
    public Map<String, Object> registrarMovimiento(long producto, long lote, InventarioApi.MovimientoRq mv) {
        Map<String, Object> l = db.requerido("SELECT medicamento_id FROM medicamento_lote WHERE id = :id",
                Db.p("id", lote), "El lote no existe.");
        if (Validar.largo(l, "medicamento_id") != producto) {
            throw new BadRequestException("El lote no pertenece al producto indicado.");
        }
        if (mv == null) {
            throw new BadRequestException("Los datos del movimiento son obligatorios.");
        }
        String tipo = Validar.texto(mv.tipo(), "tipo", 10).toUpperCase();
        Validar.requerido(mv.cantidad(), "cantidad");
        int delta = switch (tipo) {
            case "ENTRADA" -> Validar.entero(mv.cantidad(), "cantidad", 1);
            case "SALIDA" -> -Validar.entero(mv.cantidad(), "cantidad", 1);
            case "AJUSTE" -> {
                if (mv.cantidad() == 0) {
                    throw new BadRequestException("El ajuste debe ser distinto de cero (positivo suma, negativo resta).");
                }
                yield mv.cantidad();
            }
            default -> throw new BadRequestException("El tipo de movimiento debe ser ENTRADA, SALIDA o AJUSTE.");
        };
        long id = mover(lote, tipo, delta, Validar.texto(mv.motivo(), "motivo", 255),
                Validar.opcional(mv.referencia(), "referencia", 255), null, null, null);
        auditoria.registrar("AJUSTE_INVENTARIO", "medicamento_lote", lote, "Movimiento " + mv.tipo() + " de " + mv.cantidad() + " unidades en el lote " + lote + " del producto " + producto + ". Motivo: " + mv.motivo());
        return Map.of("id", id);
    }

    /**
     * Aplica un movimiento sobre un lote bloqueándolo previamente. Se usa también desde despacho
     * (salidas por dispensación y entradas por reversión) e importación.
     *
     * @param delta cantidad con signo: positiva suma existencias y negativa las descuenta.
     * @return id del movimiento registrado.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public long mover(long lote, String tipo, int delta, String motivo, String referencia, Long formula,
                      Long despacho, Long importacion) {
        Map<String, Object> l = db.requerido("SELECT medicamento_id, cantidad FROM medicamento_lote WHERE id = :id FOR UPDATE",
                Db.p("id", lote), "El lote no existe.");
        int saldo = Validar.numero(l, "cantidad") + delta;
        if (saldo < 0) {
            throw new ConflictException("El movimiento dejaría el lote con existencias negativas (disponible: "
                    + Validar.numero(l, "cantidad") + ").");
        }
        db.ejecutar("UPDATE medicamento_lote SET cantidad = :s WHERE id = :id", Db.p("s", saldo, "id", lote));
        return db.insertar("""
                INSERT INTO movimiento_inventario (medicamento_id, lote_id, tipo, cantidad, delta, saldo_lote, motivo,
                       referencia, formula_id, despacho_id, importacion_id, usuario_id, fecha)
                VALUES (:m, :l, :tipo, :cantidad, :delta, :saldo, :motivo, :ref, :formula, :despacho, :importacion,
                       :usuario, :ahora)
                """, Db.p("m", l.get("medicamento_id"), "l", lote, "tipo", tipo, "cantidad", Math.abs(delta),
                "delta", delta, "saldo", saldo, "motivo", motivo, "ref", referencia, "formula", formula,
                "despacho", despacho, "importacion", importacion, "usuario", SesionActual.usuarioId(),
                "ahora", Tiempo.ahora()));
    }

    // ------------------------------------------------------------------ Validación

    /** Producto validado y normalizado. */
    record Producto(String codigo, String nombre, String descripcion, String presentacion, String tipo, String invima,
                    BigDecimal compra, BigDecimal venta, BigDecimal iva, int minimo, LocalDate fcompra,
                    LocalDate fingreso, LocalDate fpedido, LocalDate fvence, boolean activo) {

        Map<String, Object> parametros() {
            return Db.p("codigo", codigo, "nombre", nombre, "descripcion", descripcion, "presentacion", presentacion,
                    "tipo", tipo, "invima", invima, "compra", compra, "venta", venta, "iva", iva, "minimo", minimo,
                    "fcompra", fcompra, "fingreso", fingreso, "fpedido", fpedido, "fvence", fvence, "activo", activo,
                    "ahora", Tiempo.ahora());
        }
    }

    /**
     * Valida un producto. Con {@code lanzar=true} lanza la primera excepción; con {@code false}
     * (carga masiva) acumula los errores en {@code errores} y devuelve null si hay alguno.
     */
    Producto validarProducto(ProductoRq d, Long id, boolean lanzar) {
        List<String> errores = new ArrayList<>();
        Producto p = validarProducto(d, id, errores);
        if (!errores.isEmpty() && lanzar) {
            boolean duplicado = errores.get(0).startsWith("Ya existe");
            throw duplicado ? new ConflictException(errores.get(0)) : new BadRequestException(errores.get(0));
        }
        return p;
    }

    /**
     * Valida los datos del producto acumulando errores.
     *
     * @param d datos.
     * @param id producto actualizado o null.
     * @param errores lista de errores.
     * @return producto validado.
     */
    Producto validarProducto(ProductoRq d, Long id, List<String> errores) {
        if (d == null) {
            errores.add("Los datos del producto son obligatorios.");
            return null;
        }
        String codigo = d.codigo() == null ? "" : d.codigo().trim().toUpperCase();
        if (!codigo.matches("[A-Z0-9]{8}")) {
            errores.add("El código debe tener exactamente 8 caracteres alfanuméricos.");
        }
        String nombre = d.nombre() == null ? "" : d.nombre().trim();
        if (nombre.isEmpty() || nombre.length() > 100) {
            errores.add("El nombre es obligatorio (máximo 100 caracteres).");
        }
        String tipo = d.tipo() == null ? "" : d.tipo().trim().toUpperCase();
        if (!TIPOS.contains(tipo)) {
            errores.add("El tipo debe ser MEDICAMENTO, PRODUCTO o UTENSILIO.");
        }
        String invima = d.invima() == null || d.invima().isBlank() ? null : d.invima().trim();
        if ("MEDICAMENTO".equals(tipo) && invima == null) {
            errores.add("El registro INVIMA es obligatorio para medicamentos.");
        }
        if (invima != null && invima.length() > 50) {
            errores.add("El registro INVIMA admite máximo 50 caracteres.");
        }
        BigDecimal compra = dinero(d.precioCompra(), "precio de compra", errores);
        BigDecimal venta = dinero(d.precioVenta(), "precio de venta", errores);
        if (compra != null && venta != null && venta.compareTo(compra) < 0) {
            errores.add("El precio de venta no puede ser menor que el precio de compra.");
        }
        BigDecimal iva = d.ivaPorcentaje() == null
                ? ("MEDICAMENTO".equals(tipo) ? BigDecimal.ZERO : new BigDecimal("19"))
                : d.ivaPorcentaje();
        if (iva.signum() < 0 || iva.compareTo(new BigDecimal("100")) > 0) {
            errores.add("El IVA debe estar entre 0 y 100.");
        }
        Integer minimo = d.stockMinimo();
        if (minimo == null || minimo < 0) {
            errores.add("El stock mínimo es obligatorio y no puede ser negativo.");
        }
        LocalDate hoy = Tiempo.hoy();
        LocalDate fcompra = fecha(d.fechaCompra(), "fecha de compra", true, errores);
        LocalDate fingreso = fecha(d.fechaIngreso(), "fecha de ingreso", true, errores);
        LocalDate fpedido = fecha(d.fechaProximoPedido(), "fecha del próximo pedido", false, errores);
        LocalDate fvence = fecha(d.fechaVence(), "fecha de vencimiento", "MEDICAMENTO".equals(tipo), errores);
        if (fcompra != null && fcompra.isAfter(hoy)) {
            errores.add("La fecha de compra no puede ser futura.");
        }
        if (fcompra != null && fingreso != null && fingreso.isBefore(fcompra)) {
            errores.add("La fecha de ingreso no puede ser anterior a la de compra.");
        }
        if (fingreso != null && fingreso.isAfter(hoy)) {
            errores.add("La fecha de ingreso no puede ser futura.");
        }
        if (fcompra != null && fvence != null && !fvence.isAfter(fcompra)) {
            errores.add("La fecha de vencimiento debe ser posterior a la de compra.");
        }
        if (fcompra != null && fpedido != null && fpedido.isBefore(fcompra)) {
            errores.add("La fecha del próximo pedido no puede ser anterior a la de compra.");
        }
        if (!codigo.isEmpty() && db.existe("SELECT 1 FROM medicamento WHERE codigo = :c AND (:id IS NULL OR id <> :id)",
                Db.p("c", codigo, "id", id))) {
            errores.add("Ya existe un producto con el código " + codigo + ".");
        }
        if (!nombre.isEmpty() && db.existe("SELECT 1 FROM medicamento WHERE nombre = :n AND (:id IS NULL OR id <> :id)",
                Db.p("n", nombre, "id", id))) {
            errores.add("Ya existe un producto con el nombre " + nombre + ".");
        }
        String descripcion = d.descripcion() == null || d.descripcion().isBlank() ? null : d.descripcion().trim();
        String presentacion = d.presentacion() == null || d.presentacion().isBlank() ? null : d.presentacion().trim();
        if (presentacion != null && presentacion.length() > 100) {
            errores.add("La presentación admite máximo 100 caracteres.");
        }
        if (!errores.isEmpty()) {
            return null;
        }
        return new Producto(codigo, nombre, descripcion, presentacion, tipo, invima, compra, venta,
                iva.setScale(2, RoundingMode.HALF_UP), minimo, fcompra, fingreso, fpedido, fvence,
                d.activo() == null || d.activo());
    }

    /**
     * @param valor valor.
     * @param campo campo.
     * @param errores lista de errores.
     * @return valor válido o null.
     */
    private static BigDecimal dinero(BigDecimal valor, String campo, List<String> errores) {
        if (valor == null) {
            errores.add("El " + campo + " es obligatorio.");
            return null;
        }
        if (valor.signum() < 0) {
            errores.add("El " + campo + " no puede ser negativo.");
            return null;
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            errores.add("El " + campo + " admite máximo dos decimales.");
            return null;
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @param valor texto.
     * @param campo campo.
     * @param obligatorio si es obligatorio.
     * @param errores lista de errores.
     * @return fecha o null.
     */
    private static LocalDate fecha(String valor, String campo, boolean obligatorio, List<String> errores) {
        try {
            return Validar.fecha(valor, campo, obligatorio);
        } catch (BadRequestException e) {
            errores.add(e.getMessage());
            return null;
        }
    }

    /**
     * @return parámetros con hoy y la fecha límite de próximo a vencer.
     */
    private static Map<String, Object> fechas() {
        return Db.p("hoy", Tiempo.hoy(), "limite", Tiempo.limiteProximoVencer());
    }

    /**
     * @param mapa mapa base.
     * @param clave clave.
     * @param valor valor.
     * @return copia del mapa con la clave agregada.
     */
    private static Map<String, Object> with(Map<String, Object> mapa, String clave, Object valor) {
        mapa.put(clave, valor);
        return mapa;
    }
}
