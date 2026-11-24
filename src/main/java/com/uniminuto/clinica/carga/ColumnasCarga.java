package com.uniminuto.clinica.carga;

import java.util.List;

/** Encabezados exactos del archivo (C6). */
public final class ColumnasCarga {

    public static final List<String> TODAS = List.of("codigo", "nombre", "tipo_producto", "descripcion",
            "presentacion", "unidad_medida", "requiere_invima", "registro_invima", "precio_compra", "precio_venta",
            "porcentaje_iva", "stock_minimo", "numero_lote", "cantidad", "fecha_compra", "fecha_vencimiento",
            "fecha_proximo_pedido");

    private ColumnasCarga() {
    }
}
