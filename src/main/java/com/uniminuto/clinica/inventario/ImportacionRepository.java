package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link ImportacionService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class ImportacionRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public ImportacionRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link ImportacionService} (previsualizar): INSERT INTO importacion_inventario (usuario_id, archivo, fecha, total) VALUES (:u, :a, :f, :t)
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarImportacionInventario(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO importacion_inventario (usuario_id, archivo, fecha, total) VALUES (:u, :a, :f, :t)
                """, p);
    }

    /**
     * Usada por {@link ImportacionService} (previsualizar): UPDATE importacion_inventario SET validos = :v, rechazados = :r WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarImportacionInventario(Map<String, ?> p) {
        return db.ejecutar("UPDATE importacion_inventario SET validos = :v, rechazados = :r WHERE id = :id", p);
    }

    /**
     * Usada por {@link ImportacionService} (confirmar): SELECT * FROM importacion_inventario WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerImportacionInventario(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM importacion_inventario WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link ImportacionService} (confirmar): UPDATE importacion_inventario SET fecha_confirmacion = :f WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarImportacionInventarioEnConfirmar(Map<String, ?> p) {
        return db.ejecutar("UPDATE importacion_inventario SET fecha_confirmacion = :f WHERE id = :id", p);
    }

    /**
     * Usada por {@link ImportacionService} (confirmar): SELECT * FROM importacion_inventario_fila WHERE importacion_id = :id AND errores IS NULL ORDER BY numero_fila
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarImportacionInventarioFila(Map<String, ?> p) {
        return db.lista("""
                SELECT * FROM importacion_inventario_fila WHERE importacion_id = :id AND errores IS NULL ORDER BY numero_fila
                """, p);
    }

    /**
     * Usada por {@link ImportacionService} (confirmar): UPDATE importacion_inventario_fila SET errores = :e WHERE id = :fila
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarImportacionInventarioFila(Map<String, ?> p) {
        return db.ejecutar("UPDATE importacion_inventario_fila SET errores = :e WHERE id = :fila", p);
    }

    /**
     * Usada por {@link ImportacionService} (confirmar): UPDATE importacion_inventario SET importados = :i, rechazados = :r WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarImportacionInventarioEnConfirmar2(Map<String, ?> p) {
        return db.ejecutar("UPDATE importacion_inventario SET importados = :i, rechazados = :r WHERE id = :id", p);
    }

    /**
     * Usada por {@link ImportacionService} (importarFila): INSERT INTO medicamento (codigo, nombre, descripcion, presentacion, tipo, invima, precio_compra, precio_venta,…
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
     * Usada por {@link ImportacionService} (importarFila): INSERT INTO medicamento_lote (medicamento_id, numero, costo, fecha_ingreso, fecha_vence, cantidad_inicial, can…
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
     * Usada por {@link ImportacionService} (importarFila): UPDATE importacion_inventario_fila SET medicamento_id = :m WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarImportacionInventarioFilaEnImportarFila(Map<String, ?> p) {
        return db.ejecutar("UPDATE importacion_inventario_fila SET medicamento_id = :m WHERE id = :id", p);
    }

    /**
     * Usada por {@link ImportacionService} (historial): SELECT i.id, i.usuario_id AS usuarioId, u.username AS usuario, i.archivo, i.fecha, i.fecha_confirmacion AS fec…
     *
     * @return filas.
     */
    public List<Map<String, Object>> listarImportacionInventario() {
        return db.lista("""
                SELECT i.id, i.usuario_id AS usuarioId, u.username AS usuario, i.archivo, i.fecha,
                       i.fecha_confirmacion AS fechaConfirmacion, i.total, i.importados, i.rechazados
                FROM importacion_inventario i JOIN usuario u ON u.id = i.usuario_id
                ORDER BY i.fecha DESC, i.id DESC LIMIT 100
                """);
    }

    /**
     * Usada por {@link ImportacionService} (detalle): SELECT i.id, i.usuario_id AS usuarioId, u.username AS usuario, i.archivo, i.fecha, i.fecha_confirmacion AS fec…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerImportacionInventarioEnDetalle(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT i.id, i.usuario_id AS usuarioId, u.username AS usuario, i.archivo, i.fecha,
                       i.fecha_confirmacion AS fechaConfirmacion, i.total, i.importados, i.rechazados
                FROM importacion_inventario i JOIN usuario u ON u.id = i.usuario_id WHERE i.id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link ImportacionService} (detalle): SELECT * FROM importacion_inventario_fila WHERE importacion_id = :id ORDER BY numero_fila
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarImportacionInventarioFilaEnDetalle(Map<String, ?> p) {
        return db.lista("""
                SELECT * FROM importacion_inventario_fila WHERE importacion_id = :id ORDER BY numero_fila
                """, p);
    }

    /**
     * Usada por {@link ImportacionService} (guardarFila): INSERT INTO importacion_inventario_fila (importacion_id, numero_fila, codigo, nombre, descripcion, presentacio…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarImportacionInventarioFila(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO importacion_inventario_fila (importacion_id, numero_fila, codigo, nombre, descripcion,
                       presentacion, tipo, invima, precio_compra, precio_venta, stock_minimo, fecha_compra, fecha_ingreso,
                       fecha_proximo_pedido, fecha_vence, activo, numero_lote, cantidad, errores)
                VALUES (:imp, :num, :codigo, :nombre, :descripcion, :presentacion, :tipo, :invima, :compra, :venta,
                       :minimo, :fcompra, :fingreso, :fpedido, :fvence, :activo, :lote, :cantidad, :errores)
                """, p);
    }
}
