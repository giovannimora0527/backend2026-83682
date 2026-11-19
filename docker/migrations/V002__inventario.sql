-- Ejecutar una vez en Docker, con la aplicación detenida para escrituras y respaldo previo.
USE clinica;

-- Usar un cliente en modo batch sin --force. Cualquier diferencia de línea base
-- falla antes del primer DDL persistente.
CREATE TEMPORARY TABLE v002_precondicion (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO v002_precondicion VALUES (
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name IN ('medicamento','usuario')
       AND column_name = 'id' AND column_type = 'bigint') = 2
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'medicamento' AND column_name = 'codigo')
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
        WHERE table_schema = DATABASE() AND table_name IN
        ('inventario_lote','inventario_movimiento','inventario_importacion'))
    AND NOT EXISTS (SELECT 1 FROM medicamento WHERE id < 0 OR id >= 2821109907456)
);

ALTER TABLE medicamento
    ADD COLUMN codigo CHAR(8) CHARACTER SET ascii COLLATE ascii_general_ci NULL,
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'MEDICAMENTO',
    ADD COLUMN invima VARCHAR(100) NULL,
    ADD COLUMN precio_compra DECIMAL(14,2) NOT NULL DEFAULT 0,
    ADD COLUMN precio_venta DECIMAL(14,2) NOT NULL DEFAULT 0,
    ADD COLUMN stock_minimo INT NOT NULL DEFAULT 0,
    ADD COLUMN fecha_ingreso DATE NULL,
    ADD COLUMN fecha_proximo_pedido DATE NULL,
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    MODIFY COLUMN fecha_vence DATE NULL;

-- Conserva identificadores, fechas y referencias existentes. Los registros previos
-- requieren revisión de INVIMA antes de editarlos. El stock inicial es cero.
UPDATE medicamento
SET codigo = UPPER(LPAD(CONV(id, 10, 36), 8, '0')), fecha_ingreso = fecha_compra
WHERE codigo IS NULL;

ALTER TABLE medicamento
    MODIFY COLUMN codigo CHAR(8) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
    MODIFY COLUMN fecha_ingreso DATE NOT NULL,
    ADD CONSTRAINT uq_medicamento_codigo UNIQUE (codigo),
    ADD CONSTRAINT ck_medicamento_precios CHECK (precio_compra >= 0 AND precio_venta >= 0),
    ADD CONSTRAINT ck_medicamento_minimo CHECK (stock_minimo >= 0),
    ADD CONSTRAINT ck_medicamento_tipo CHECK (tipo IN ('MEDICAMENTO', 'PRODUCTO', 'UTENSILIO'));

CREATE TABLE inventario_lote (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    medicamento_id BIGINT NOT NULL,
    numero VARCHAR(60) NOT NULL,
    costo DECIMAL(14,2) NOT NULL,
    fecha_ingreso DATE NOT NULL,
    fecha_vence DATE NULL,
    cantidad INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventario_lote UNIQUE (medicamento_id, numero),
    CONSTRAINT fk_lote_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento(id),
    CONSTRAINT ck_lote_costo CHECK (costo >= 0),
    CONSTRAINT ck_lote_cantidad CHECK (cantidad >= 0)
) ENGINE=InnoDB;

CREATE TABLE inventario_movimiento (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    cantidad INT NOT NULL,
    saldo INT NOT NULL,
    fecha DATETIME(6) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    referencia VARCHAR(100) NULL,
    CONSTRAINT fk_movimiento_lote FOREIGN KEY (lote_id) REFERENCES inventario_lote(id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT ck_movimiento_tipo CHECK (
        (tipo IN ('ENTRADA','SALIDA') AND cantidad > 0) OR (tipo = 'AJUSTE' AND cantidad <> 0)),
    CONSTRAINT ck_movimiento_saldo CHECK (saldo >= 0),
    INDEX ix_movimiento_lote (lote_id, id)
) ENGINE=InnoDB;

CREATE TABLE inventario_importacion (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    archivo VARCHAR(200) NOT NULL,
    fecha DATETIME(6) NOT NULL,
    fecha_confirmacion DATETIME(6) NULL,
    filas JSON NOT NULL,
    importados INT NOT NULL DEFAULT 0,
    rechazados INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_importacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;
