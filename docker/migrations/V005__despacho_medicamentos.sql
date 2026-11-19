USE clinica;
CREATE TABLE despacho_formula (
 formula_id BIGINT PRIMARY KEY,
 estado VARCHAR(26) NOT NULL,
 FOREIGN KEY (formula_id) REFERENCES formula_clinica(id),
 CHECK (estado IN ('PENDIENTE_DESPACHO','EN_PREPARACION','DESPACHO_PARCIAL','DESPACHADO','PENDIENTE_DISPONIBILIDAD','CANCELADO'))
) ENGINE=InnoDB;
CREATE TABLE despacho_entrega (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 formula_id BIGINT NOT NULL, revision INT NOT NULL, usuario_id BIGINT NOT NULL,
 clave CHAR(36) CHARACTER SET ascii NOT NULL UNIQUE,
 plan JSON NOT NULL, fecha DATETIME(6) NOT NULL, confirmada DATETIME(6),
 estado_resultante VARCHAR(26), pendiente JSON,
 revertida DATETIME(6), reversor_id BIGINT, motivo_reversion VARCHAR(255),
 FOREIGN KEY (formula_id) REFERENCES despacho_formula(formula_id),
 FOREIGN KEY (usuario_id) REFERENCES usuario(id),
 FOREIGN KEY (reversor_id) REFERENCES usuario(id),
 UNIQUE (id,formula_id,revision),
 INDEX ix_entrega_formula (formula_id,confirmada,id)
) ENGINE=InnoDB;
CREATE TABLE despacho_detalle (
 entrega_id BIGINT NOT NULL, formula_id BIGINT NOT NULL, revision INT NOT NULL,
 medicamento_id BIGINT NOT NULL, lote_id BIGINT NOT NULL, cantidad INT NOT NULL,
 movimiento_id BIGINT NOT NULL UNIQUE, reversion_movimiento_id BIGINT UNIQUE,
 PRIMARY KEY (entrega_id,lote_id),
 FOREIGN KEY (entrega_id,formula_id,revision) REFERENCES despacho_entrega(id,formula_id,revision),
 FOREIGN KEY (formula_id,revision,medicamento_id) REFERENCES formula_clinica_detalle(formula_id,revision,medicamento_id),
 FOREIGN KEY (lote_id) REFERENCES inventario_lote(id),
 FOREIGN KEY (movimiento_id) REFERENCES inventario_movimiento(id),
 FOREIGN KEY (reversion_movimiento_id) REFERENCES inventario_movimiento(id),
 CHECK (cantidad > 0)
) ENGINE=InnoDB;
CREATE TABLE despacho_estado (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, formula_id BIGINT NOT NULL, usuario_id BIGINT NOT NULL,
 anterior VARCHAR(26) NOT NULL, nuevo VARCHAR(26) NOT NULL,
 fecha DATETIME(6) NOT NULL, motivo VARCHAR(255),
 FOREIGN KEY (formula_id) REFERENCES despacho_formula(formula_id),
 FOREIGN KEY (usuario_id) REFERENCES usuario(id),
 INDEX ix_despacho_estado (formula_id,id)
) ENGINE=InnoDB;

START TRANSACTION;
INSERT INTO menu(activo,code,fecha_creacion,icon,title,url)
SELECT b'1','despacho-medicamentos',NOW(6),'feather icon-package','Despacho de Medicamentos','/inicio/despacho'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code='despacho-medicamentos' OR url='/inicio/despacho');
INSERT INTO perfil_menu(perfil_id,menu_id)
SELECT p.perfil_id,m.menu_id FROM perfil p JOIN menu m ON m.code='despacho-medicamentos' AND m.url='/inicio/despacho'
WHERE UPPER(p.nombre)='DESPACHO_MEDICO'
AND NOT EXISTS (SELECT 1 FROM perfil_menu pm WHERE pm.perfil_id=p.perfil_id AND pm.menu_id=m.menu_id);
COMMIT;
