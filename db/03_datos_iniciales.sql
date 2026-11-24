-- Datos iniciales y de prueba (ficticios). Se puede ejecutar mas de una vez sin duplicar.
-- Requiere 01 y 02. Usuarios y contrasenas de prueba: ver README. No usar en produccion.
SET NAMES utf8mb4;
SET SESSION sql_safe_updates = 0;

-- ============================ Menus (C4) ============================
INSERT INTO `menu` (`code`, `title`, `url`, `icon`, `activo`, `fecha_creacion`)
SELECT * FROM (
    SELECT 'clientes' c, 'Clientes' t, '/inicio/clientes' u, 'icon-users' i, TRUE a, NOW() f UNION ALL
    SELECT 'mascotas' c, 'Mascotas' t, '/inicio/mascotas' u, 'icon-heart' i, TRUE a, NOW() f UNION ALL
    SELECT 'medicos' c, 'Médicos' t, '/inicio/medicos' u, 'icon-user-check' i, TRUE a, NOW() f UNION ALL
    SELECT 'especializaciones' c, 'Especializaciones' t, '/inicio/especializaciones' u, 'icon-award' i, TRUE a, NOW() f UNION ALL
    SELECT 'citas' c, 'Citas' t, '/inicio/citas' u, 'icon-calendar' i, TRUE a, NOW() f UNION ALL
    SELECT 'calendario' c, 'Mi calendario' t, '/inicio/calendario' u, 'icon-calendar' i, TRUE a, NOW() f UNION ALL
    SELECT 'atencion' c, 'Atención médica' t, '/inicio/atencion' u, 'icon-activity' i, TRUE a, NOW() f UNION ALL
    SELECT 'historias' c, 'Historias clínicas' t, '/inicio/historias' u, 'icon-file-text' i, TRUE a, NOW() f UNION ALL
    SELECT 'formulas' c, 'Fórmulas médicas' t, '/inicio/formulas' u, 'icon-clipboard' i, TRUE a, NOW() f UNION ALL
    SELECT 'inventario' c, 'Inventario' t, '/inicio/inventario' u, 'icon-package' i, TRUE a, NOW() f UNION ALL
    SELECT 'carga-inventario' c, 'Carga de inventario' t, '/inicio/carga-inventario' u, 'icon-upload' i, TRUE a, NOW() f UNION ALL
    SELECT 'despacho' c, 'Despacho' t, '/inicio/despacho' u, 'icon-truck' i, TRUE a, NOW() f UNION ALL
    SELECT 'tarifas' c, 'Tarifas' t, '/inicio/tarifas' u, 'icon-tag' i, TRUE a, NOW() f UNION ALL
    SELECT 'facturacion' c, 'Facturación' t, '/inicio/facturacion' u, 'icon-dollar-sign' i, TRUE a, NOW() f UNION ALL
    SELECT 'reportes' c, 'Reportes' t, '/inicio/reportes' u, 'icon-bar-chart-2' i, TRUE a, NOW() f UNION ALL
    SELECT 'usuarios' c, 'Usuarios' t, '/inicio/usuarios' u, 'icon-user' i, TRUE a, NOW() f
) n WHERE NOT EXISTS (SELECT 1 FROM `menu` m WHERE m.`code` = n.c);

-- `medicamentos` lo reemplaza `inventario`.
UPDATE `menu` SET `activo` = FALSE, `fecha_modificacion` = NOW() WHERE `code` = 'medicamentos';

-- Los codigos del contrato quedan visibles exactamente para sus roles.
DELETE pm FROM `perfil_menu` pm JOIN `menu` m ON m.`menu_id` = pm.`menu_id`
WHERE m.`code` IN ('clientes', 'mascotas', 'medicos', 'especializaciones', 'citas', 'calendario', 'atencion', 'historias', 'formulas', 'inventario', 'carga-inventario', 'despacho', 'tarifas', 'facturacion', 'reportes', 'usuarios');

INSERT INTO `perfil_menu` (`perfil_id`, `menu_id`)
SELECT p.`perfil_id`, m.`menu_id` FROM (
    SELECT 'clientes' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'clientes' c, 'SECRETARIA' r UNION ALL
    SELECT 'mascotas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'mascotas' c, 'SECRETARIA' r UNION ALL
    SELECT 'mascotas' c, 'MEDICO' r UNION ALL
    SELECT 'medicos' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'medicos' c, 'SECRETARIA' r UNION ALL
    SELECT 'especializaciones' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'citas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'citas' c, 'SECRETARIA' r UNION ALL
    SELECT 'calendario' c, 'MEDICO' r UNION ALL
    SELECT 'atencion' c, 'MEDICO' r UNION ALL
    SELECT 'historias' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'historias' c, 'MEDICO' r UNION ALL
    SELECT 'formulas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'formulas' c, 'MEDICO' r UNION ALL
    SELECT 'inventario' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'inventario' c, 'DESPACHO_MEDICO' r UNION ALL
    SELECT 'carga-inventario' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'despacho' c, 'DESPACHO_MEDICO' r UNION ALL
    SELECT 'tarifas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'facturacion' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'facturacion' c, 'SECRETARIA' r UNION ALL
    SELECT 'reportes' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'usuarios' c, 'ADMINISTRADOR' r
) x JOIN `perfil` p ON p.`codigo` = x.r JOIN `menu` m ON m.`code` = x.c;

-- ============================ Usuarios (C7) ============================
-- Si el username ya existe, se respeta el usuario existente.
INSERT INTO `usuario` (`username`, `password_hash`, `rol`, `perfil_id`, `activo`, `email`)
SELECT n.u, n.h, n.r, p.`perfil_id`, TRUE, CONCAT(n.u, '@clinica.example') FROM (
    SELECT 'admin' u, '$2a$10$/VSj63imO7vKNdi5kLkUF.06XfGN0XiSi8icDlvF77ExWPE1eaiZO' h, 'ADMINISTRADOR' r UNION ALL
    SELECT 'secretaria' u, '$2a$10$aphbwR9OoC4gYXU8IAq3vOJ542pJa/TRyx2xM/Q7MFlTm2xj/vOUC' h, 'SECRETARIA' r UNION ALL
    SELECT 'medico1' u, '$2a$10$xFRWgIZH4.F0iyUJ2Anet.ZmZtfQqOPKd0YWHlaUMySaY39npMSIe' h, 'MEDICO' r UNION ALL
    SELECT 'medico2' u, '$2a$10$xFRWgIZH4.F0iyUJ2Anet.ZmZtfQqOPKd0YWHlaUMySaY39npMSIe' h, 'MEDICO' r UNION ALL
    SELECT 'despacho' u, '$2a$10$vIt4MKWhCPPbTkKP1YkQ7OwPQvL8rlB9ed1M4iJDGiw8Cw/klHkTW' h, 'DESPACHO_MEDICO' r
) n JOIN `perfil` p ON p.`codigo` = n.r
WHERE NOT EXISTS (SELECT 1 FROM `usuario` x WHERE x.`username` = n.u);

SET @admin = (SELECT `id` FROM `usuario` WHERE `username` = 'admin');

-- ============================ Especialidades, impuestos, servicios y tarifas ============================
INSERT INTO `especializacion` (`nombre`, `descripcion`, `codigo_especializacion`)
SELECT * FROM (
    SELECT 'Medicina general veterinaria' n, 'Consulta general de mascotas' d, 'VET_GEN' c UNION ALL
    SELECT 'Cirugía veterinaria', 'Procedimientos quirúrgicos', 'VET_CIR' UNION ALL
    SELECT 'Dermatología veterinaria', 'Enfermedades de la piel', 'VET_DER'
) e WHERE NOT EXISTS (SELECT 1 FROM `especializacion` x WHERE x.`codigo_especializacion` = e.c OR x.`nombre` = e.n);

INSERT INTO `impuesto` (`nombre`, `porcentaje`)
SELECT * FROM (SELECT 'IVA 0 %' n, 0 p UNION ALL SELECT 'IVA 5 %', 5 UNION ALL SELECT 'IVA 19 %', 19) i
WHERE NOT EXISTS (SELECT 1 FROM `impuesto` x WHERE x.`nombre` = i.n);

INSERT INTO `servicio` (`codigo`, `nombre`, `descripcion`, `tipo_servicio`, `especializacion_id`, `duracion_minutos`)
SELECT s.c, s.n, s.n, s.t, e.`id`, s.d FROM (
    SELECT 'CONS-GEN' c, 'Consulta general' n, 'CONSULTA' t, 'VET_GEN' e, 30 d UNION ALL
    SELECT 'CONS-CIR' c, 'Consulta de cirugía' n, 'CONSULTA' t, 'VET_CIR' e, 30 d UNION ALL
    SELECT 'CONS-DER' c, 'Consulta de dermatología' n, 'CONSULTA' t, 'VET_DER' e, 30 d UNION ALL
    SELECT 'VACUNA' c, 'Vacunación' n, 'VACUNACION' t, NULL e, 30 d UNION ALL
    SELECT 'CURACION' c, 'Curación' n, 'PROCEDIMIENTO' t, NULL e, 30 d UNION ALL
    SELECT 'CIRU-MEN' c, 'Cirugía menor' n, 'CIRUGIA' t, 'VET_CIR' e, 60 d UNION ALL
    SELECT 'HEMOGRAMA' c, 'Hemograma' n, 'LABORATORIO' t, NULL e, 30 d
) s LEFT JOIN `especializacion` e ON e.`codigo_especializacion` = s.e
WHERE NOT EXISTS (SELECT 1 FROM `servicio` x WHERE x.`codigo` = s.c);

INSERT INTO `tarifa` (`servicio_id`, `valor`, `impuesto_id`, `vigente_desde`)
SELECT s.`id`, t.v, i.`id`, '2026-01-01' FROM (
    SELECT 'CONS-GEN' c, 50000 v, 'IVA 19 %' i UNION ALL
    SELECT 'CONS-CIR' c, 80000 v, 'IVA 19 %' i UNION ALL
    SELECT 'CONS-DER' c, 75000 v, 'IVA 19 %' i UNION ALL
    SELECT 'VACUNA' c, 35000 v, 'IVA 0 %' i UNION ALL
    SELECT 'CURACION' c, 40000 v, 'IVA 19 %' i UNION ALL
    SELECT 'CIRU-MEN' c, 250000 v, 'IVA 19 %' i UNION ALL
    SELECT 'HEMOGRAMA' c, 45000 v, 'IVA 0 %' i
) t JOIN `servicio` s ON s.`codigo` = t.c JOIN `impuesto` i ON i.`nombre` = t.i
WHERE NOT EXISTS (SELECT 1 FROM `tarifa` x WHERE x.`servicio_id` = s.`id`);

-- ============================ Razas, clientes y mascotas ============================
INSERT INTO `raza` (`nombre`, `especie`, `fecha_creacion`)
SELECT n, e, NOW() FROM (SELECT 'Criollo' n, 'Perro' e UNION ALL SELECT 'Labrador', 'Perro' UNION ALL SELECT 'Poodle', 'Perro'
    UNION ALL SELECT 'Criollo', 'Gato' UNION ALL SELECT 'Siamés', 'Gato') r
WHERE NOT EXISTS (SELECT 1 FROM `raza` x WHERE x.`nombre` = r.n AND x.`especie` = r.e);

INSERT INTO `cliente` (`tipo_documento`, `numero_documento`, `nombres`, `apellidos`, `fecha_nacimiento`, `genero`, `telefono`, `correo`, `direccion`, `activo`)
SELECT 'CC', d, n, a, '1990-05-10', g, CONCAT('300000000', RIGHT(d, 1)), CONCAT('cliente', RIGHT(d, 1), '@clinica.example'), 'Dirección de prueba', TRUE FROM (
    SELECT '9000000001' d, 'Juan' n, 'Pérez' a, 'M' g UNION ALL
    SELECT '9000000002' d, 'Laura' n, 'Gómez' a, 'F' g UNION ALL
    SELECT '9000000003' d, 'Carlos' n, 'Ruiz' a, 'M' g UNION ALL
    SELECT '9000000004' d, 'Andrea' n, 'Torres' a, 'F' g UNION ALL
    SELECT '9000000005' d, 'Miguel' n, 'Castro' a, 'M' g
) c WHERE NOT EXISTS (SELECT 1 FROM `cliente` x WHERE x.`numero_documento` = c.d);

INSERT INTO `mascota` (`nombre_mascota`, `edad`, `raza_id`, `fecha_registro`, `cliente_id`, `activo`)
SELECT m.n, m.e, r.`raza_id`, NOW(), c.`cliente_id`, TRUE FROM (
    SELECT 'Firulais' n, 3 e, 'Criollo' r, 'Perro' s, '9000000001' d UNION ALL
    SELECT 'Luna' n, 5 e, 'Labrador' r, 'Perro' s, '9000000001' d UNION ALL
    SELECT 'Michi' n, 2 e, 'Criollo' r, 'Gato' s, '9000000002' d UNION ALL
    SELECT 'Rocky' n, 7 e, 'Poodle' r, 'Perro' s, '9000000002' d UNION ALL
    SELECT 'Nala' n, 4 e, 'Siamés' r, 'Gato' s, '9000000003' d UNION ALL
    SELECT 'Toby' n, 1 e, 'Labrador' r, 'Perro' s, '9000000003' d UNION ALL
    SELECT 'Simba' n, 6 e, 'Criollo' r, 'Gato' s, '9000000004' d UNION ALL
    SELECT 'Max' n, 8 e, 'Criollo' r, 'Perro' s, '9000000005' d
) m JOIN `cliente` c ON c.`numero_documento` = m.d
JOIN (SELECT MIN(`raza_id`) `raza_id`, `nombre`, `especie` FROM `raza` GROUP BY `nombre`, `especie`) r ON r.`nombre` = m.r AND r.`especie` = m.s
WHERE NOT EXISTS (SELECT 1 FROM `mascota` x WHERE x.`nombre_mascota` = m.n AND x.`cliente_id` = c.`cliente_id`);

-- ============================ Medicos y horarios ============================
INSERT INTO `medico` (`tipo_documento`, `numero_documento`, `nombres`, `apellidos`, `telefono`, `registro_profesional`, `especializacion_id`, `estado`, `usuario_id`)
SELECT 'CC', m.d, m.n, m.a, '3100000000', m.r, e.`id`, TRUE,
       CASE WHEN EXISTS (SELECT 1 FROM `medico` y WHERE y.`usuario_id` = u.`id`) THEN NULL ELSE u.`id` END
FROM (SELECT '8000000001' d, 'Sofía' n, 'Ramírez' a, 'RP-VET-001' r, 'VET_GEN' e, 'medico1' u UNION ALL
      SELECT '8000000002', 'Andrés', 'López', 'RP-VET-002', 'VET_DER', 'medico2') m
JOIN `especializacion` e ON e.`codigo_especializacion` = m.e JOIN `usuario` u ON u.`username` = m.u
WHERE NOT EXISTS (SELECT 1 FROM `medico` x WHERE x.`numero_documento` = m.d);

INSERT INTO `horario_medico` (`medico_id`, `dia_semana`, `hora_inicio`, `hora_fin`)
SELECT m.`id`, d.n, h.i, h.f FROM `medico` m
CROSS JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) d
CROSS JOIN (SELECT '08:00:00' i, '12:00:00' f UNION ALL SELECT '14:00:00', '18:00:00') h
WHERE m.`numero_documento` IN ('8000000001', '8000000002')
  AND NOT EXISTS (SELECT 1 FROM `horario_medico` x WHERE x.`medico_id` = m.`id`);

-- ============================ Inventario: 15 productos ============================
INSERT INTO `medicamento` (`codigo`, `nombre`, `descripcion`, `presentacion`, `tipo_producto`, `requiere_invima`, `registro_invima`, `unidad_medida`, `precio_venta`, `impuesto_id`, `stock_minimo`, `fecha_proximo_pedido`, `fecha_creacion_registro`)
SELECT p.c, p.n, p.n, p.pr, p.t, p.t = 'MEDICAMENTO', p.inv, p.u, p.v, i.`id`, p.s, DATE_ADD(CURDATE(), INTERVAL 20 DAY), NOW() FROM (
    SELECT 'AMX00500' c, 'Amoxicilina 500 mg' n, 'MEDICAMENTO' t, 'Caja x 10 tabletas' pr, 'tableta' u, 1500 v, 'IVA 0 %' i, 20 s, 'INVIMA PRUEBA-001' inv UNION ALL
    SELECT 'MEL00015' c, 'Meloxicam 1,5 mg/ml' n, 'MEDICAMENTO' t, 'Frasco 10 ml' pr, 'frasco' u, 18000 v, 'IVA 0 %' i, 10 s, 'INVIMA PRUEBA-002' inv UNION ALL
    SELECT 'IVE00010' c, 'Ivermectina 1 %' n, 'MEDICAMENTO' t, 'Frasco 50 ml' pr, 'frasco' u, 22000 v, 'IVA 0 %' i, 8 s, 'INVIMA PRUEBA-003' inv UNION ALL
    SELECT 'CEF00250' c, 'Cefalexina 250 mg' n, 'MEDICAMENTO' t, 'Caja x 12 cápsulas' pr, 'cápsula' u, 1200 v, 'IVA 0 %' i, 24 s, 'INVIMA PRUEBA-004' inv UNION ALL
    SELECT 'PRE00005' c, 'Prednisolona 5 mg' n, 'MEDICAMENTO' t, 'Caja x 20 tabletas' pr, 'tableta' u, 600 v, 'IVA 0 %' i, 40 s, 'INVIMA PRUEBA-005' inv UNION ALL
    SELECT 'TRA00050' c, 'Tramadol 50 mg' n, 'MEDICAMENTO' t, 'Caja x 10 tabletas' pr, 'tableta' u, 1800 v, 'IVA 0 %' i, 15 s, 'INVIMA PRUEBA-006' inv UNION ALL
    SELECT 'MET00250' c, 'Metronidazol 250 mg' n, 'MEDICAMENTO' t, 'Caja x 20 tabletas' pr, 'tableta' u, 700 v, 'IVA 0 %' i, 20 s, 'INVIMA PRUEBA-007' inv UNION ALL
    SELECT 'VACRAB01' c, 'Vacuna antirrábica' n, 'MEDICAMENTO' t, 'Dosis 1 ml' pr, 'dosis' u, 25000 v, 'IVA 0 %' i, 10 s, 'INVIMA PRUEBA-008' inv UNION ALL
    SELECT 'ENR00050' c, 'Enrofloxacina 50 mg' n, 'MEDICAMENTO' t, 'Caja x 10 tabletas' pr, 'tableta' u, 2100 v, 'IVA 0 %' i, 20 s, 'INVIMA PRUEBA-009' inv UNION ALL
    SELECT 'DOX00100' c, 'Doxiciclina 100 mg' n, 'MEDICAMENTO' t, 'Caja x 10 tabletas' pr, 'tableta' u, 1600 v, 'IVA 0 %' i, 20 s, 'INVIMA PRUEBA-010' inv UNION ALL
    SELECT 'OME00020' c, 'Omeprazol 20 mg' n, 'MEDICAMENTO' t, 'Caja x 14 cápsulas' pr, 'cápsula' u, 500 v, 'IVA 0 %' i, 28 s, 'INVIMA PRUEBA-011' inv UNION ALL
    SELECT 'SOL00500' c, 'Solución salina 500 ml' n, 'MEDICAMENTO' t, 'Bolsa 500 ml' pr, 'bolsa' u, 6000 v, 'IVA 0 %' i, 12 s, 'INVIMA PRUEBA-012' inv UNION ALL
    SELECT 'JER00005' c, 'Jeringa 5 ml' n, 'INSUMO' t, 'Unidad' pr, 'unidad' u, 900 v, 'IVA 19 %' i, 50 s, NULL inv UNION ALL
    SELECT 'GAS00010' c, 'Gasa estéril' n, 'INSUMO' t, 'Paquete x 10' pr, 'paquete' u, 3500 v, 'IVA 19 %' i, 20 s, NULL inv UNION ALL
    SELECT 'GUA00100' c, 'Guantes de nitrilo' n, 'INSUMO' t, 'Caja x 100' pr, 'caja' u, 32000 v, 'IVA 19 %' i, 5 s, NULL inv
) p JOIN `impuesto` i ON i.`nombre` = p.i
WHERE NOT EXISTS (SELECT 1 FROM `medicamento` x WHERE x.`codigo` = p.c OR x.`nombre` = p.n);

INSERT INTO `lote_medicamento` (`medicamento_id`, `numero_lote`, `precio_compra`, `cantidad_inicial`, `cantidad_disponible`, `fecha_compra`, `fecha_ingreso`, `fecha_vencimiento`)
SELECT m.`id`, l.l, l.p, l.ci, l.cd, DATE_ADD(CURDATE(), INTERVAL l.co DAY), DATE_ADD(CURDATE(), INTERVAL l.co + 1 DAY), DATE_ADD(CURDATE(), INTERVAL l.ve DAY) FROM (
    SELECT 'AMX00500' c, 'AMX-A' l, 900 p, 100 ci, 80 cd, -60 co, 20 ve UNION ALL
    SELECT 'AMX00500' c, 'AMX-B' l, 950 p, 100 ci, 100 cd, -20 co, 300 ve UNION ALL
    SELECT 'MEL00015' c, 'MEL-A' l, 11000 p, 30 ci, 30 cd, -30 co, 400 ve UNION ALL
    SELECT 'IVE00010' c, 'IVE-A' l, 13000 p, 20 ci, 0 cd, -90 co, 200 ve UNION ALL
    SELECT 'CEF00250' c, 'CEF-A' l, 700 p, 60 ci, 24 cd, -60 co, 250 ve UNION ALL
    SELECT 'PRE00005' c, 'PRE-A' l, 300 p, 200 ci, 150 cd, -40 co, 15 ve UNION ALL
    SELECT 'PRE00005' c, 'PRE-B' l, 310 p, 100 ci, 100 cd, -10 co, 500 ve UNION ALL
    SELECT 'TRA00050' c, 'TRA-A' l, 1000 p, 50 ci, 10 cd, -50 co, 180 ve UNION ALL
    SELECT 'MET00250' c, 'MET-A' l, 350 p, 80 ci, 40 cd, -400 co, -10 ve UNION ALL
    SELECT 'MET00250' c, 'MET-B' l, 380 p, 60 ci, 60 cd, -15 co, 350 ve UNION ALL
    SELECT 'VACRAB01' c, 'VAC-A' l, 15000 p, 40 ci, 25 cd, -30 co, 120 ve UNION ALL
    SELECT 'ENR00050' c, 'ENR-A' l, 1200 p, 50 ci, 0 cd, -100 co, 90 ve UNION ALL
    SELECT 'DOX00100' c, 'DOX-A' l, 900 p, 40 ci, 30 cd, -380 co, -30 ve UNION ALL
    SELECT 'OME00020' c, 'OME-A' l, 250 p, 140 ci, 140 cd, -25 co, 365 ve UNION ALL
    SELECT 'SOL00500' c, 'SOL-A' l, 3500 p, 24 ci, 20 cd, -30 co, 28 ve UNION ALL
    SELECT 'JER00005' c, 'JER-A' l, 500 p, 300 ci, 220 cd, -45 co, NULL ve UNION ALL
    SELECT 'GAS00010' c, 'GAS-A' l, 2000 p, 60 ci, 18 cd, -60 co, NULL ve UNION ALL
    SELECT 'GUA00100' c, 'GUA-A' l, 21000 p, 20 ci, 12 cd, -35 co, NULL ve
) l JOIN `medicamento` m ON m.`codigo` = l.c
WHERE NOT EXISTS (SELECT 1 FROM `lote_medicamento` x WHERE x.`medicamento_id` = m.`id` AND x.`numero_lote` = l.l);

-- Movimientos que explican las existencias de los lotes de prueba.
INSERT INTO `movimiento_inventario` (`fecha`, `tipo`, `medicamento_id`, `lote_id`, `cantidad`, `saldo_lote_resultante`, `saldo_producto_resultante`, `costo_unitario`, `usuario_id`, `observacion`)
SELECT TIMESTAMP(l.`fecha_ingreso`, '09:00:00'), 'ENTRADA_COMPRA', l.`medicamento_id`, l.`id`, l.`cantidad_inicial`, l.`cantidad_inicial`,
       (SELECT SUM(x.`cantidad_inicial`) FROM `lote_medicamento` x WHERE x.`medicamento_id` = l.`medicamento_id`
         AND (x.`fecha_ingreso` < l.`fecha_ingreso` OR (x.`fecha_ingreso` = l.`fecha_ingreso` AND x.`id` <= l.`id`))),
       l.`precio_compra`, @admin, 'Dato inicial de prueba'
FROM `lote_medicamento` l
WHERE l.`cantidad_inicial` > 0 AND NOT EXISTS (SELECT 1 FROM `movimiento_inventario` m WHERE m.`lote_id` = l.`id`)
ORDER BY l.`fecha_ingreso`, l.`id`;

-- Los ajustes son posteriores a todas las entradas (hoy), por eso el saldo del producto parte del total ingresado.
INSERT INTO `movimiento_inventario` (`fecha`, `tipo`, `medicamento_id`, `lote_id`, `cantidad`, `saldo_lote_resultante`, `saldo_producto_resultante`, `costo_unitario`, `usuario_id`, `observacion`)
SELECT NOW(), 'AJUSTE_NEGATIVO', l.`medicamento_id`, l.`id`, l.`cantidad_inicial` - l.`cantidad_disponible`, l.`cantidad_disponible`,
       (SELECT SUM(IF(x.`id` <= l.`id`, x.`cantidad_disponible`, x.`cantidad_inicial`)) FROM `lote_medicamento` x WHERE x.`medicamento_id` = l.`medicamento_id`),
       l.`precio_compra`, @admin, 'Dato inicial de prueba: consumo previo'
FROM `lote_medicamento` l
WHERE l.`cantidad_disponible` < l.`cantidad_inicial`
  AND (SELECT COUNT(*) FROM `movimiento_inventario` m WHERE m.`lote_id` = l.`id`) = 1
  AND EXISTS (SELECT 1 FROM `movimiento_inventario` m WHERE m.`lote_id` = l.`id` AND m.`observacion` = 'Dato inicial de prueba')
ORDER BY l.`id`;

-- ============================ Citas en varios estados ============================
-- Proximo lunes y lunes anterior, dentro del horario de los medicos.
SET @lunes = DATE_ADD(CURDATE(), INTERVAL 7 - WEEKDAY(CURDATE()) DAY);
SET @pasado = DATE_SUB(@lunes, INTERVAL 14 DAY);
INSERT INTO `cita` (`cliente_id`, `mascota_id`, `medico_id`, `servicio_id`, `fecha_inicio`, `fecha_fin`, `estado`, `motivo`, `motivo_cancelacion`, `usuario_creacion_id`)
SELECT c.`cliente_id`, m.`mascota_id`, d.`id`, s.`id`, TIMESTAMP(x.f, x.h), DATE_ADD(TIMESTAMP(x.f, x.h), INTERVAL s.`duracion_minutos` MINUTE), x.e, x.mo,
       IF(x.e = 'CANCELADA', 'El propietario no puede asistir', NULL), @admin
FROM (
    SELECT CAST(@lunes AS DATE) f, '09:00:00' h, 'Firulais' n, '9000000001' c, '8000000001' d, 'CONS-GEN' s, 'PROGRAMADA' e, '[Prueba] Control general' mo UNION ALL
    SELECT CAST(@lunes AS DATE) f, '10:00:00' h, 'Michi' n, '9000000002' c, '8000000001' d, 'CONS-GEN' s, 'CONFIRMADA' e, '[Prueba] Vómito' mo UNION ALL
    SELECT CAST(@lunes AS DATE) f, '11:00:00' h, 'Nala' n, '9000000003' c, '8000000001' d, 'CONS-GEN' s, 'CANCELADA' e, '[Prueba] Control de peso' mo UNION ALL
    SELECT CAST(DATE_ADD(@lunes, INTERVAL 1 DAY) AS DATE) f, '09:00:00' h, 'Rocky' n, '9000000002' c, '8000000002' d, 'CONS-DER' s, 'PROGRAMADA' e, '[Prueba] Picazón' mo UNION ALL
    SELECT CAST(DATE_ADD(@lunes, INTERVAL 1 DAY) AS DATE) f, '15:00:00' h, 'Max' n, '9000000005' c, '8000000001' d, 'VACUNA' s, 'CONFIRMADA' e, '[Prueba] Vacunación' mo UNION ALL
    SELECT CAST(@pasado AS DATE) f, '09:00:00' h, 'Luna' n, '9000000001' c, '8000000001' d, 'CONS-GEN' s, 'ATENDIDA' e, '[Prueba] Cojera' mo UNION ALL
    SELECT CAST(@pasado AS DATE) f, '10:00:00' h, 'Simba' n, '9000000004' c, '8000000002' d, 'CONS-DER' s, 'CANCELADA' e, '[Prueba] Caída de pelo' mo
) x JOIN `cliente` c ON c.`numero_documento` = x.c JOIN `mascota` m ON m.`nombre_mascota` = x.n AND m.`cliente_id` = c.`cliente_id`
JOIN `medico` d ON d.`numero_documento` = x.d JOIN `servicio` s ON s.`codigo` = x.s
WHERE NOT EXISTS (SELECT 1 FROM `cita` y WHERE y.`motivo` = x.mo);

-- La cita atendida de prueba tiene su historia, atencion, servicio y anotacion.
SET @cita = (SELECT `id` FROM `cita` WHERE `motivo` = '[Prueba] Cojera');
INSERT INTO `historia_medica` (`paciente_id`, `fecha_creacion`)
SELECT c.`mascota_id`, c.`fecha_inicio` FROM `cita` c
WHERE c.`id` = @cita AND NOT EXISTS (SELECT 1 FROM `historia_medica` h WHERE h.`paciente_id` = c.`mascota_id`);
INSERT INTO `atencion_medica` (`cita_id`, `historia_medica_id`, `medico_id`, `mascota_id`, `motivo_consulta`, `sintomas`, `diagnostico`, `observaciones`, `recomendaciones`, `fecha_inicio`, `fecha_fin`, `finalizada`)
SELECT c.`id`, h.`id`, c.`medico_id`, c.`mascota_id`, 'Cojera', 'Dolor en la pata trasera', 'Esguince leve', 'Sin fractura', 'Reposo 5 días', c.`fecha_inicio`, c.`fecha_fin`, TRUE
FROM `cita` c JOIN `historia_medica` h ON h.`paciente_id` = c.`mascota_id`
WHERE c.`id` = @cita AND NOT EXISTS (SELECT 1 FROM `atencion_medica` a WHERE a.`cita_id` = c.`id`);
INSERT INTO `atencion_servicio` (`atencion_id`, `servicio_id`, `cantidad`)
SELECT a.`id`, c.`servicio_id`, 1 FROM `atencion_medica` a JOIN `cita` c ON c.`id` = a.`cita_id`
WHERE c.`id` = @cita AND NOT EXISTS (SELECT 1 FROM `atencion_servicio` x WHERE x.`atencion_id` = a.`id`);
INSERT INTO `anotacion_historia` (`historia_id`, `medico_id`, `cita_id`, `fecha`, `descripcion`, `diagnostico`, `observaciones`)
SELECT a.`historia_medica_id`, a.`medico_id`, a.`cita_id`, a.`fecha_fin`, a.`sintomas`, a.`diagnostico`, a.`observaciones`
FROM `atencion_medica` a WHERE a.`cita_id` = @cita
  AND NOT EXISTS (SELECT 1 FROM `anotacion_historia` x WHERE x.`cita_id` = a.`cita_id`);
