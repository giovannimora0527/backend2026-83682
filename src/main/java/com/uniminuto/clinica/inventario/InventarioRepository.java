package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link InventarioService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class InventarioRepository {

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

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public InventarioRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link InventarioService} (listar): GROUP BY m.id ORDER BY m.nombre
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamento(Map<String, ?> p) {
        return db.lista(FICHA + " GROUP BY m.id ORDER BY m.nombre", p);
    }

    /**
     * Usada por {@link InventarioService} (ficha): WHERE m.id = :id GROUP BY m.id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamento(Map<String, ?> p, String mensaje) {
        return db.requerido(FICHA + " WHERE m.id = :id GROUP BY m.id", p, mensaje);
    }

    /**
     * Usada por {@link InventarioService} (lotes): SELECT id, numero, costo, fecha_ingreso AS fechaIngreso, fecha_vence AS fechaVence, cantidad FROM medicamento_…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamentoLote(Map<String, ?> p) {
        return db.lista("""
                SELECT id, numero, costo, fecha_ingreso AS fechaIngreso, fecha_vence AS fechaVence, cantidad
                FROM medicamento_lote WHERE medicamento_id = :m
                ORDER BY fecha_vence IS NULL, fecha_vence, id
                """, p);
    }

    /**
     * Usada por {@link InventarioService} (movimientos): SELECT mv.id, mv.tipo, mv.cantidad, mv.delta, mv.motivo, mv.referencia, l.numero AS lote, u.username AS usuari…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMovimientoInventario(Map<String, ?> p) {
        return db.lista("""
                SELECT mv.id, mv.tipo, mv.cantidad, mv.delta, mv.motivo, mv.referencia, l.numero AS lote,
                       u.username AS usuario, mv.fecha, mv.saldo_lote AS saldo, mv.formula_id AS formula,
                       mv.despacho_id AS despacho
                FROM movimiento_inventario mv
                JOIN medicamento_lote l ON l.id = mv.lote_id
                JOIN usuario u ON u.id = mv.usuario_id
                WHERE mv.medicamento_id = :m
                ORDER BY mv.fecha DESC, mv.id DESC
                """, p);
    }

    /**
     * Usada por {@link InventarioService} (crear): INSERT INTO medicamento (codigo, nombre, descripcion, presentacion, tipo, invima, precio_compra, precio_venta,…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarMedicamento(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO medicamento (codigo, nombre, descripcion, presentacion, tipo, invima, precio_compra,
                       precio_venta, iva_porcentaje, stock_minimo, fecha_compra, fecha_ingreso, fecha_proximo_pedido,
                       fecha_vence, activo, fecha_creacion_registro)
                VALUES (:codigo, :nombre, :descripcion, :presentacion, :tipo, :invima, :compra, :venta, :iva, :minimo,
                       :fcompra, :fingreso, :fpedido, :fvence, :activo, :ahora)
                """, p);
    }

    /**
     * Usada por {@link InventarioService} (actualizar): SELECT id FROM medicamento WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamentoEnActualizar(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM medicamento WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link InventarioService} (actualizar): UPDATE medicamento SET codigo = :codigo, nombre = :nombre, descripcion = :descripcion, presentacion = :present…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarMedicamento(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE medicamento SET codigo = :codigo, nombre = :nombre, descripcion = :descripcion,
                       presentacion = :presentacion, tipo = :tipo, invima = :invima, precio_compra = :compra,
                       precio_venta = :venta, iva_porcentaje = :iva, stock_minimo = :minimo, fecha_compra = :fcompra,
                       fecha_ingreso = :fingreso, fecha_proximo_pedido = :fpedido, fecha_vence = :fvence,
                       activo = :activo, fecha_modificacion_registro = :ahora
                WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link InventarioService} (registrarLote): SELECT tipo, activo, codigo FROM medicamento WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamentoEnRegistrarLote(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT tipo, activo, codigo FROM medicamento WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link InventarioService} (registrarLote): SELECT 1 FROM medicamento_lote WHERE medicamento_id = :m AND numero = :n
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeMedicamentoLote(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM medicamento_lote WHERE medicamento_id = :m AND numero = :n", p);
    }

    /**
     * Usada por {@link InventarioService} (registrarLote): INSERT INTO medicamento_lote (medicamento_id, numero, costo, fecha_ingreso, fecha_vence, cantidad_inicial, can…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarMedicamentoLote(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO medicamento_lote (medicamento_id, numero, costo, fecha_ingreso, fecha_vence, cantidad_inicial, cantidad)
                VALUES (:m, :n, :c, :i, :v, :q, 0)
                """, p);
    }

    /**
     * Usada por {@link InventarioService} (registrarMovimiento): SELECT medicamento_id FROM medicamento_lote WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamentoLote(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT medicamento_id FROM medicamento_lote WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link InventarioService} (mover): SELECT medicamento_id, cantidad FROM medicamento_lote WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamentoLoteEnMover(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT medicamento_id, cantidad FROM medicamento_lote WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link InventarioService} (mover): UPDATE medicamento_lote SET cantidad = :s WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarMedicamentoLote(Map<String, ?> p) {
        return db.ejecutar("UPDATE medicamento_lote SET cantidad = :s WHERE id = :id", p);
    }

    /**
     * Usada por {@link InventarioService} (mover): INSERT INTO movimiento_inventario (medicamento_id, lote_id, tipo, cantidad, delta, saldo_lote, motivo, referen…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarMovimientoInventario(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO movimiento_inventario (medicamento_id, lote_id, tipo, cantidad, delta, saldo_lote, motivo,
                       referencia, formula_id, despacho_id, importacion_id, usuario_id, fecha)
                VALUES (:m, :l, :tipo, :cantidad, :delta, :saldo, :motivo, :ref, :formula, :despacho, :importacion,
                       :usuario, :ahora)
                """, p);
    }

    /**
     * Usada por {@link InventarioService} (validarProducto): SELECT 1 FROM medicamento WHERE codigo = :c AND (:id IS NULL OR id <> :id)
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeMedicamento(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM medicamento WHERE codigo = :c AND (:id IS NULL OR id <> :id)", p);
    }

    /**
     * Usada por {@link InventarioService} (validarProducto): SELECT 1 FROM medicamento WHERE nombre = :n AND (:id IS NULL OR id <> :id)
     *
     * @param p parámetros con nombre de la consulta.
     * @return resultado.
     */
    public boolean existeMedicamentoEnValidarProducto(Map<String, ?> p) {
        return db.existe("SELECT 1 FROM medicamento WHERE nombre = :n AND (:id IS NULL OR id <> :id)", p);
    }
}
