USE clinica;

-- Esquema financiero y menú para Administración y Recepción.
CREATE TABLE factura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(40) NOT NULL UNIQUE,
    atencion_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    clave CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    huella BINARY(32) NOT NULL,
    emitida DATETIME(6) NOT NULL,
    clinica VARCHAR(160) NOT NULL,
    propietario VARCHAR(255) NOT NULL,
    mascota VARCHAR(160) NOT NULL,
    veterinario VARCHAR(255) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    descuento_pct DECIMAL(5,2) NOT NULL,
    motivo_descuento VARCHAR(255),
    subtotal DECIMAL(19,2) NOT NULL,
    descuento DECIMAL(19,2) NOT NULL,
    iva DECIMAL(19,2) NOT NULL,
    otros_impuestos DECIMAL(19,2) NOT NULL,
    total DECIMAL(19,2) NOT NULL,
    pagado DECIMAL(19,2) NOT NULL DEFAULT 0,
    estado VARCHAR(16) NOT NULL,
    FOREIGN KEY (atencion_id) REFERENCES atencion_veterinaria(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX ix_factura_atencion (atencion_id,id),
    INDEX ix_factura_fecha (emitida,id),
    INDEX ix_factura_estado_fecha (estado,emitida,id),
    CHECK (descuento_pct BETWEEN 0 AND 100),
    CHECK (descuento_pct=0 OR motivo_descuento IS NOT NULL),
    CHECK (subtotal>=descuento AND descuento>=0 AND iva>=0 AND otros_impuestos>=0),
    CHECK (total=subtotal-descuento+iva+otros_impuestos AND pagado BETWEEN 0 AND total),
    CHECK ((estado='PENDIENTE_PAGO' AND pagado=0 AND total>0)
        OR (estado='PAGO_PARCIAL' AND pagado>0 AND pagado<total)
        OR (estado='PAGADA' AND pagado=total)
        OR (estado='ANULADA' AND pagado=0))
) ENGINE=InnoDB;

CREATE TABLE factura_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    atencion_servicio_id BIGINT,
    servicio_id BIGINT,
    despacho_movimiento_id BIGINT,
    concepto VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(14,2) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL,
    descuento DECIMAL(19,2) NOT NULL,
    iva_pct DECIMAL(5,2) NOT NULL,
    otros_pct DECIMAL(5,2) NOT NULL,
    iva DECIMAL(19,2) NOT NULL,
    otros_impuestos DECIMAL(19,2) NOT NULL,
    total DECIMAL(19,2) NOT NULL,
    vigente BOOLEAN NOT NULL DEFAULT TRUE,
    servicio_vigente BIGINT GENERATED ALWAYS AS (CASE WHEN vigente THEN servicio_id END) STORED,
    atencion_vigente BIGINT GENERATED ALWAYS AS (CASE WHEN vigente THEN atencion_servicio_id END) STORED,
    despacho_vigente BIGINT GENERATED ALWAYS AS (CASE WHEN vigente THEN despacho_movimiento_id END) STORED,
    FOREIGN KEY (factura_id) REFERENCES factura(id),
    FOREIGN KEY (atencion_servicio_id,servicio_id) REFERENCES atencion_servicio(atencion_id,servicio_id),
    FOREIGN KEY (despacho_movimiento_id) REFERENCES despacho_detalle(movimiento_id),
    UNIQUE (atencion_vigente,servicio_vigente),
    UNIQUE (despacho_vigente),
    CHECK ((atencion_servicio_id IS NOT NULL AND servicio_id IS NOT NULL AND despacho_movimiento_id IS NULL)
        OR (atencion_servicio_id IS NULL AND servicio_id IS NULL AND despacho_movimiento_id IS NOT NULL)),
    CHECK (cantidad>0 AND precio_unitario>=0 AND subtotal=cantidad*precio_unitario),
    CHECK (iva_pct>=0 AND otros_pct>=0 AND iva_pct+otros_pct<=100),
    CHECK (subtotal>=descuento AND descuento>=0 AND iva>=0 AND otros_impuestos>=0),
    CHECK (total=subtotal-descuento+iva+otros_impuestos)
) ENGINE=InnoDB;

CREATE TABLE factura_pago (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    clave CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    fecha DATETIME(6) NOT NULL,
    importe DECIMAL(19,2) NOT NULL,
    metodo VARCHAR(32) NOT NULL,
    referencia VARCHAR(100),
    estado VARCHAR(16) NOT NULL DEFAULT 'REGISTRADO',
    FOREIGN KEY (factura_id) REFERENCES factura(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    UNIQUE (factura_id,clave),
    CHECK (importe>0 AND estado='REGISTRADO')
) ENGINE=InnoDB;

CREATE TABLE factura_estado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    anterior VARCHAR(16),
    nuevo VARCHAR(16) NOT NULL,
    motivo VARCHAR(255),
    fecha DATETIME(6) NOT NULL,
    FOREIGN KEY (factura_id) REFERENCES factura(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX ix_factura_estado_historial (factura_id,id),
    CHECK (nuevo IN ('PENDIENTE_PAGO','PAGO_PARCIAL','PAGADA','ANULADA')),
    CHECK (nuevo<>'ANULADA' OR motivo IS NOT NULL)
) ENGINE=InnoDB;

START TRANSACTION;
INSERT INTO menu(activo,code,fecha_creacion,icon,title,url)
SELECT b'1','facturacion',NOW(6),'feather icon-file-text','Facturación','/inicio/facturacion'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code='facturacion' OR url='/inicio/facturacion');
INSERT INTO perfil_menu(perfil_id,menu_id)
SELECT p.perfil_id,m.menu_id FROM perfil p JOIN menu m ON m.code='facturacion' AND m.url='/inicio/facturacion'
WHERE UPPER(p.nombre) IN ('ADMIN','ADMINISTRADOR','RECEPCION','SECRETARIA')
AND NOT EXISTS (SELECT 1 FROM perfil_menu pm WHERE pm.perfil_id=p.perfil_id AND pm.menu_id=m.menu_id);
COMMIT;
