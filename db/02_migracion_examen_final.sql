-- Migracion del examen final. MySQL 8. Conserva los datos existentes y se puede ejecutar mas de una vez.
-- Hacer un respaldo antes de ejecutarla sobre una base con datos.
-- Sirve para una base creada con 01_esquema_base.sql, para una base antigua sin perfil/menu/perfil_menu y para una
-- base que ya tenga esas tablas con los datos del profesor (se completan; no se borran sus filas).
-- Compatible desde MySQL 8.0.12 (los CHECK solo se aplican desde 8.0.16).
SET NAMES utf8mb4;
SET SESSION sql_safe_updates = 0;

DROP PROCEDURE IF EXISTS mig_ejecutar;
DROP PROCEDURE IF EXISTS mig_columna;
DROP PROCEDURE IF EXISTS mig_restriccion;
DROP PROCEDURE IF EXISTS mig_renombrar;
DROP PROCEDURE IF EXISTS mig_tipo;

DELIMITER $$
CREATE PROCEDURE mig_ejecutar(IN p_sql TEXT)
BEGIN
    SET @mig_sql = p_sql;
    PREPARE sentencia FROM @mig_sql;
    EXECUTE sentencia;
    DEALLOCATE PREPARE sentencia;
END$$

-- Agrega la columna si no existe.
CREATE PROCEDURE mig_columna(IN p_tabla VARCHAR(64), IN p_columna VARCHAR(64), IN p_definicion TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = p_tabla AND column_name = p_columna) THEN
        CALL mig_ejecutar(CONCAT('ALTER TABLE `', p_tabla, '` ADD COLUMN `', p_columna, '` ', p_definicion));
    END IF;
END$$

-- Agrega la restriccion (UNIQUE, FOREIGN KEY o CHECK) si no existe.
CREATE PROCEDURE mig_restriccion(IN p_tabla VARCHAR(64), IN p_nombre VARCHAR(64), IN p_definicion TEXT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE table_schema = DATABASE() AND table_name = p_tabla AND constraint_name = p_nombre) THEN
        CALL mig_ejecutar(CONCAT('ALTER TABLE `', p_tabla, '` ADD CONSTRAINT `', p_nombre, '` ', p_definicion));
    END IF;
END$$

-- Renombra la columna solo si todavia tiene el nombre anterior.
CREATE PROCEDURE mig_renombrar(IN p_tabla VARCHAR(64), IN p_anterior VARCHAR(64), IN p_nuevo VARCHAR(64))
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = DATABASE() AND table_name = p_tabla AND column_name = p_anterior)
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = DATABASE() AND table_name = p_tabla AND column_name = p_nuevo) THEN
        CALL mig_ejecutar(CONCAT('ALTER TABLE `', p_tabla, '` RENAME COLUMN `', p_anterior, '` TO `', p_nuevo, '`'));
    END IF;
END$$

-- Cambia el tipo de la columna solo si es distinto (no toca columnas que ya tienen clave foranea).
CREATE PROCEDURE mig_tipo(IN p_tabla VARCHAR(64), IN p_columna VARCHAR(64), IN p_tipo VARCHAR(64), IN p_resto VARCHAR(100))
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = DATABASE() AND table_name = p_tabla AND column_name = p_columna
                 AND REGEXP_REPLACE(column_type, '^(tinyint|int|bigint)[(][0-9]+[)]', '$1') <> p_tipo) THEN
        CALL mig_ejecutar(CONCAT('ALTER TABLE `', p_tabla, '` MODIFY COLUMN `', p_columna, '` ', p_tipo, ' ', p_resto));
    END IF;
END$$
DELIMITER ;

-- ============================ Perfiles, menus, usuarios y sesiones ============================
CREATE TABLE IF NOT EXISTS `perfil` (
    `perfil_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(50) NOT NULL,
    `activo` BOOLEAN NOT NULL,
    `fecha_creacion` DATETIME NOT NULL,
    `fecha_modificacion` DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `menu` (
    `menu_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(100) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `url` LONGTEXT NOT NULL,
    `icon` VARCHAR(255) NOT NULL,
    `activo` BOOLEAN NOT NULL,
    `fecha_creacion` DATETIME NOT NULL,
    `fecha_modificacion` DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `perfil_menu` (
    `perfil_menu_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `perfil_id` INT NOT NULL,
    `menu_id` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `otp_token` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `codigo` VARCHAR(6) NOT NULL,
    `fecha_creacion` DATETIME NOT NULL,
    `fecha_expiracion` DATETIME NOT NULL,
    `intentos` INT NOT NULL,
    `usado` BOOLEAN NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CALL mig_columna('perfil', 'codigo', 'VARCHAR(30) NULL');
CALL mig_restriccion('perfil', 'uk_perfil_codigo', 'UNIQUE (`codigo`)');

-- Asigna codigo (C2) a los perfiles existentes por su nombre y crea los que falten.
UPDATE `perfil` p
JOIN (SELECT MIN(n.`perfil_id`) AS id, n.cod
      FROM (SELECT `perfil_id`,
       CASE WHEN UPPER(`nombre`) LIKE 'DESPACHO%' THEN 'DESPACHO_MEDICO'
            WHEN UPPER(`nombre`) LIKE 'ADMIN%' THEN 'ADMINISTRADOR'
            WHEN UPPER(`nombre`) LIKE 'SECRETARI%' OR UPPER(`nombre`) LIKE 'RECEPCI%' THEN 'SECRETARIA'
            WHEN UPPER(`nombre`) LIKE 'MEDIC%' OR UPPER(`nombre`) LIKE 'VETERINARI%' THEN 'MEDICO' END AS cod FROM `perfil` WHERE `codigo` IS NULL) n
      LEFT JOIN (SELECT `codigo` FROM `perfil` WHERE `codigo` IS NOT NULL) y ON y.`codigo` = n.cod
      WHERE n.cod IS NOT NULL AND y.`codigo` IS NULL
      GROUP BY n.cod) x ON x.id = p.`perfil_id`
SET p.`codigo` = x.cod
WHERE p.`codigo` IS NULL;

-- Mismos ids que el profesor si estan libres; si no, el siguiente disponible.
INSERT INTO `perfil` (`perfil_id`, `codigo`, `nombre`, `activo`, `fecha_creacion`)
SELECT c.id, c.cod, c.nom, TRUE, '2026-01-01 00:00:00' FROM (SELECT 1 id, 'ADMINISTRADOR' cod, 'Administrador' nom UNION ALL SELECT 2, 'SECRETARIA', 'Recepcion'
      UNION ALL SELECT 3, 'MEDICO', 'Medico' UNION ALL SELECT 4, 'DESPACHO_MEDICO', 'Despacho Medico') c
WHERE NOT EXISTS (SELECT 1 FROM `perfil` p WHERE p.`codigo` = c.cod OR p.`perfil_id` = c.id);
INSERT INTO `perfil` (`codigo`, `nombre`, `activo`, `fecha_creacion`)
SELECT c.cod, c.nom, TRUE, '2026-01-01 00:00:00' FROM (SELECT 1 id, 'ADMINISTRADOR' cod, 'Administrador' nom UNION ALL SELECT 2, 'SECRETARIA', 'Recepcion'
      UNION ALL SELECT 3, 'MEDICO', 'Medico' UNION ALL SELECT 4, 'DESPACHO_MEDICO', 'Despacho Medico') c
WHERE NOT EXISTS (SELECT 1 FROM `perfil` p WHERE p.`codigo` = c.cod);

-- La fila `medicamentos` del profesor se convierte en `inventario` conservando su id y sus permisos.
UPDATE `menu` m LEFT JOIN (SELECT `menu_id` FROM `menu` WHERE `code` = 'inventario') x ON TRUE
SET m.`code` = 'inventario', m.`title` = 'Gestión de Inventario', m.`url` = '/inicio/inventario', m.`fecha_modificacion` = NOW()
WHERE m.`code` = 'medicamentos' AND x.`menu_id` IS NULL;

-- Menus del profesor (1 a 9) y los nuevos (10 a 16). Los que ya existen no se tocan.
DROP TEMPORARY TABLE IF EXISTS `mig_menu`;
CREATE TEMPORARY TABLE `mig_menu` (`id` INT, `c` VARCHAR(100), `t` VARCHAR(255), `u` VARCHAR(255), `i` VARCHAR(255))
    DEFAULT CHARSET=utf8mb4;
INSERT INTO `mig_menu`
    SELECT 1 id, 'cita' c, 'Gestión de citas' t, '/inicio/citas' u, 'bi bi-calendar' i UNION ALL
    SELECT 2 id, 'clientes' c, 'Gestión de clientes' t, '/inicio/clientes' u, 'bi bi-person-check' i UNION ALL
    SELECT 3 id, 'especializaciones' c, 'Gestión de Especializaciones' t, '/inicio/especializaciones' u, 'fa-solid fa-user-graduate' i UNION ALL
    SELECT 4 id, 'formulas' c, 'Gestión de Formulas' t, '/inicio/formulas' u, 'fa-solid fa-clipboard-list' i UNION ALL
    SELECT 5 id, 'historias' c, 'Gestión de Historias' t, '/inicio/historias' u, 'fa-solid fa-book-medical' i UNION ALL
    SELECT 6 id, 'mascotas' c, 'Gestión de Mascotas' t, '/inicio/mascotas' u, 'fa-solid fa-paw' i UNION ALL
    SELECT 7 id, 'inventario' c, 'Gestión de Inventario' t, '/inicio/inventario' u, 'fa-solid fa-capsules' i UNION ALL
    SELECT 8 id, 'medicos' c, 'Gestión de Medicos' t, '/inicio/medicos' u, 'fa-solid fa-user-doctor' i UNION ALL
    SELECT 9 id, 'usuarios' c, 'Gestión de Usuarios' t, '/inicio/usuarios' u, 'feather icon-users' i UNION ALL
    SELECT 10 id, 'calendario' c, 'Calendario de citas' t, '/inicio/calendario' u, 'bi bi-calendar-week' i UNION ALL
    SELECT 11 id, 'atencion' c, 'Atención Médica' t, '/inicio/atencion' u, 'fa-solid fa-stethoscope' i UNION ALL
    SELECT 12 id, 'carga-inventario' c, 'Carga de Inventario' t, '/inicio/carga-inventario' u, 'fa-solid fa-file-arrow-up' i UNION ALL
    SELECT 13 id, 'despacho' c, 'Gestión de Despachos' t, '/inicio/despacho' u, 'fa-solid fa-prescription-bottle-medical' i UNION ALL
    SELECT 14 id, 'tarifas' c, 'Gestión de Tarifas' t, '/inicio/tarifas' u, 'fa-solid fa-tags' i UNION ALL
    SELECT 15 id, 'facturacion' c, 'Gestión de Facturación' t, '/inicio/facturacion' u, 'fa-solid fa-file-invoice-dollar' i UNION ALL
    SELECT 16 id, 'reportes' c, 'Gestión de Reportes' t, '/inicio/reportes' u, 'fa-solid fa-chart-column' i;
INSERT INTO `menu` (`menu_id`, `code`, `title`, `url`, `icon`, `activo`, `fecha_creacion`)
SELECT n.id, n.c, n.t, n.u, n.i, TRUE, '2026-01-01 00:00:00' FROM `mig_menu` n
LEFT JOIN `menu` m ON m.`code` COLLATE utf8mb4_0900_ai_ci = n.c COLLATE utf8mb4_0900_ai_ci OR m.`menu_id` = n.id
WHERE m.`menu_id` IS NULL;
INSERT INTO `menu` (`code`, `title`, `url`, `icon`, `activo`, `fecha_creacion`)
SELECT n.c, n.t, n.u, n.i, TRUE, '2026-01-01 00:00:00' FROM `mig_menu` n
LEFT JOIN `menu` m ON m.`code` COLLATE utf8mb4_0900_ai_ci = n.c COLLATE utf8mb4_0900_ai_ci
WHERE m.`menu_id` IS NULL ORDER BY n.id;
DROP TEMPORARY TABLE `mig_menu`;

-- Solo se eliminan copias exactas del mismo par, necesario para el UNIQUE.
DELETE a FROM `perfil_menu` a JOIN `perfil_menu` b
    ON b.`perfil_id` = a.`perfil_id` AND b.`menu_id` = a.`menu_id` AND b.`perfil_menu_id` < a.`perfil_menu_id`;
CALL mig_restriccion('perfil_menu', 'uk_perfil_menu', 'UNIQUE (`perfil_id`, `menu_id`)');
CALL mig_restriccion('menu', 'uk_menu_code', 'UNIQUE (`code`)');
CALL mig_restriccion('perfil_menu', 'fk_perfil_menu_perfil', 'FOREIGN KEY (`perfil_id`) REFERENCES `perfil` (`perfil_id`) ON DELETE RESTRICT');
CALL mig_restriccion('perfil_menu', 'fk_perfil_menu_menu', 'FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menu_id`) ON DELETE RESTRICT');

-- Matriz C4: agrega los permisos que falten.
INSERT INTO `perfil_menu` (`perfil_id`, `menu_id`)
SELECT p.`perfil_id`, m.`menu_id` FROM (
    SELECT 'cita' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'cita' c, 'SECRETARIA' r UNION ALL
    SELECT 'clientes' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'clientes' c, 'SECRETARIA' r UNION ALL
    SELECT 'especializaciones' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'formulas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'formulas' c, 'MEDICO' r UNION ALL
    SELECT 'historias' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'historias' c, 'MEDICO' r UNION ALL
    SELECT 'mascotas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'mascotas' c, 'SECRETARIA' r UNION ALL
    SELECT 'mascotas' c, 'MEDICO' r UNION ALL
    SELECT 'inventario' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'inventario' c, 'DESPACHO_MEDICO' r UNION ALL
    SELECT 'medicos' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'medicos' c, 'SECRETARIA' r UNION ALL
    SELECT 'usuarios' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'calendario' c, 'MEDICO' r UNION ALL
    SELECT 'atencion' c, 'MEDICO' r UNION ALL
    SELECT 'carga-inventario' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'despacho' c, 'DESPACHO_MEDICO' r UNION ALL
    SELECT 'tarifas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'facturacion' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'facturacion' c, 'SECRETARIA' r UNION ALL
    SELECT 'reportes' c, 'ADMINISTRADOR' r
) x JOIN `perfil` p ON p.`codigo` COLLATE utf8mb4_0900_ai_ci = x.r JOIN `menu` m ON m.`code` COLLATE utf8mb4_0900_ai_ci = x.c
LEFT JOIN `perfil_menu` pm ON pm.`perfil_id` = p.`perfil_id` AND pm.`menu_id` = m.`menu_id`
WHERE pm.`perfil_menu_id` IS NULL
ORDER BY p.`perfil_id`, m.`menu_id`;

-- usuario.rol (texto libre o id) pasa a usuario.perfil_id. La columna `rol` se conserva como historico.
CALL mig_columna('usuario', 'perfil_id', 'INT NULL');
CALL mig_ejecutar((SELECT CONCAT('ALTER TABLE `usuario` MODIFY COLUMN `rol` ', column_type, ' NULL') FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'rol'));
UPDATE `usuario` u JOIN `perfil` p ON CAST(p.`perfil_id` AS CHAR) = CAST(u.`rol` AS CHAR)
SET u.`perfil_id` = p.`perfil_id` WHERE u.`perfil_id` IS NULL;
UPDATE `usuario` u JOIN `perfil` p ON p.`codigo` =
       CASE WHEN UPPER(u.`rol`) LIKE 'DESPACHO%' THEN 'DESPACHO_MEDICO'
            WHEN UPPER(u.`rol`) LIKE 'ADMIN%' THEN 'ADMINISTRADOR'
            WHEN UPPER(u.`rol`) LIKE 'SECRETARI%' OR UPPER(u.`rol`) LIKE 'RECEPCI%' THEN 'SECRETARIA'
            WHEN UPPER(u.`rol`) LIKE 'MEDIC%' OR UPPER(u.`rol`) LIKE 'VETERINARI%' THEN 'MEDICO' END
SET u.`perfil_id` = p.`perfil_id` WHERE u.`perfil_id` IS NULL;

CALL mig_restriccion('usuario', 'fk_usuario_perfil', 'FOREIGN KEY (`perfil_id`) REFERENCES `perfil` (`perfil_id`) ON DELETE RESTRICT');

-- Los OTP son temporales: se descartan los de usuarios inexistentes antes de relacionarlos.
DELETE o FROM `otp_token` o LEFT JOIN `usuario` u ON u.`id` = o.`usuario_id` WHERE u.`id` IS NULL;
CALL mig_tipo('otp_token', 'usuario_id', 'bigint unsigned', 'NOT NULL');
CALL mig_restriccion('otp_token', 'fk_otp_token_usuario', 'FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT');

-- El JWT ya no se guarda, solo su identificador (jti). `token` se conserva como historico.
ALTER TABLE `session` MODIFY COLUMN `token` TEXT NULL;
CALL mig_tipo('session', 'user_id', 'bigint unsigned', 'NOT NULL');
CALL mig_columna('session', 'jti', 'VARCHAR(36) NULL');
CALL mig_columna('session', 'activa', 'BOOLEAN NOT NULL DEFAULT FALSE');
CALL mig_columna('session', 'fecha_cierre', 'DATETIME NULL');
CALL mig_restriccion('session', 'uk_session_jti', 'UNIQUE (`jti`)');

-- ============================ Clientes, mascotas, medicos y especialidades ============================
CALL mig_columna('cliente', 'correo', 'VARCHAR(255) NULL');
CALL mig_tipo('cliente', 'genero', 'varchar(1)', 'NULL');
CALL mig_tipo('mascota', 'cliente_id', 'bigint unsigned', 'NOT NULL');
CALL mig_columna('mascota', 'activo', 'BOOLEAN NOT NULL DEFAULT TRUE');
UPDATE `mascota` SET `activo` = TRUE WHERE `activo` IS NULL;
CALL mig_restriccion('mascota', 'fk_mascota_cliente', 'FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE RESTRICT');
CALL mig_restriccion('mascota', 'fk_mascota_raza', 'FOREIGN KEY (`raza_id`) REFERENCES `raza` (`raza_id`) ON DELETE RESTRICT');
CALL mig_columna('especializacion', 'activo', 'BOOLEAN NOT NULL DEFAULT TRUE');
CALL mig_tipo('medico', 'especializacion_id', 'bigint unsigned', 'NOT NULL');
CALL mig_columna('medico', 'estado', 'BOOLEAN NOT NULL DEFAULT TRUE');
CALL mig_columna('medico', 'usuario_id', 'BIGINT UNSIGNED NULL');
CALL mig_tipo('medico', 'usuario_id', 'bigint unsigned', 'NULL');
CALL mig_restriccion('medico', 'uk_medico_usuario', 'UNIQUE (`usuario_id`)');
CALL mig_restriccion('medico', 'fk_medico_especializacion', 'FOREIGN KEY (`especializacion_id`) REFERENCES `especializacion` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('medico', 'fk_medico_usuario', 'FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT');

-- Citas e historias que apuntan a mascotas inexistentes: se crea una mascota inactiva con ese id para no perderlas.
INSERT INTO `mascota` (`mascota_id`, `nombre_mascota`, `edad`, `raza_id`, `fecha_registro`, `cliente_id`, `activo`)
SELECT c.`mascota_id`, CONCAT('Mascota no registrada #', c.`mascota_id`), 0, (SELECT MIN(`raza_id`) FROM `raza`),
       NOW(), MIN(c.`cliente_id`), FALSE
FROM `cita` c LEFT JOIN `mascota` x ON x.`mascota_id` = c.`mascota_id`
WHERE x.`mascota_id` IS NULL GROUP BY c.`mascota_id`;

-- ============================ Especialidades, servicios y tarifas ============================
CREATE TABLE IF NOT EXISTS `impuesto` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(50) NOT NULL,
    `porcentaje` DECIMAL(5,2) NOT NULL,
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_impuesto_nombre` (`nombre`),
    CONSTRAINT `ck_impuesto_1` CHECK (porcentaje BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `servicio` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `codigo` VARCHAR(20) NOT NULL,
    `nombre` VARCHAR(100) NOT NULL,
    `descripcion` TEXT NULL,
    `tipo_servicio` VARCHAR(30) NOT NULL,
    `especializacion_id` BIGINT UNSIGNED NULL,
    `duracion_minutos` INT NOT NULL DEFAULT 30,
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_servicio_codigo` (`codigo`),
    CONSTRAINT `fk_servicio_especializacion` FOREIGN KEY (`especializacion_id`) REFERENCES `especializacion` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_servicio_1` CHECK (duracion_minutos > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tarifa` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `servicio_id` BIGINT UNSIGNED NOT NULL,
    `valor` DECIMAL(14,2) NOT NULL,
    `impuesto_id` BIGINT UNSIGNED NOT NULL,
    `vigente_desde` DATE NOT NULL,
    `vigente_hasta` DATE NULL,
    `activa` BOOLEAN NOT NULL DEFAULT TRUE,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    `servicio_vigente` BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN `activa` THEN `servicio_id` END) STORED,
    UNIQUE KEY `uk_tarifa_vigente` (`servicio_vigente`),
    CONSTRAINT `fk_tarifa_servicio` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_tarifa_impuesto` FOREIGN KEY (`impuesto_id`) REFERENCES `impuesto` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_tarifa_1` CHECK (valor >= 0),
    CONSTRAINT `ck_tarifa_2` CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tarifa_historial` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `tarifa_id` BIGINT UNSIGNED NOT NULL,
    `servicio_id` BIGINT UNSIGNED NOT NULL,
    `valor_anterior` DECIMAL(14,2) NULL,
    `valor_nuevo` DECIMAL(14,2) NOT NULL,
    `porcentaje_iva_anterior` DECIMAL(5,2) NULL,
    `porcentaje_iva_nuevo` DECIMAL(5,2) NOT NULL,
    `motivo` VARCHAR(500) NOT NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `fecha` DATETIME NOT NULL,
    KEY `idx_tarifa_historial_1` (`fecha`),
    CONSTRAINT `fk_tarifa_historial_tarifa` FOREIGN KEY (`tarifa_id`) REFERENCES `tarifa` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_tarifa_historial_servicio` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_tarifa_historial_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `configuracion_clinica` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(150) NOT NULL,
    `nit` VARCHAR(30) NOT NULL,
    `direccion` VARCHAR(200) NULL,
    `telefono` VARCHAR(30) NULL,
    `correo` VARCHAR(255) NULL,
    `prefijo_factura` VARCHAR(10) NOT NULL,
    `consecutivo_factura` BIGINT NOT NULL DEFAULT 0,
    `prefijo_formula` VARCHAR(10) NOT NULL,
    `consecutivo_formula` BIGINT NOT NULL DEFAULT 0,
    `prefijo_despacho` VARCHAR(10) NOT NULL,
    `consecutivo_despacho` BIGINT NOT NULL DEFAULT 0,
    `tope_descuento_secretaria` DECIMAL(5,2) NOT NULL DEFAULT 10,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    `unica` BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY `uk_configuracion_unica` (`unica`),
    CONSTRAINT `ck_configuracion_clinica_1` CHECK (tope_descuento_secretaria BETWEEN 0 AND 100),
    CONSTRAINT `ck_configuracion_clinica_2` CHECK (`unica` = TRUE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `horario_medico` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `medico_id` BIGINT UNSIGNED NOT NULL,
    `dia_semana` TINYINT NOT NULL,
    `hora_inicio` TIME NOT NULL,
    `hora_fin` TIME NOT NULL,
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    KEY `idx_horario_medico_1` (`medico_id`, `dia_semana`),
    CONSTRAINT `fk_horario_medico_medico` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_horario_medico_1` CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT `ck_horario_medico_2` CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================ Citas ============================
CALL mig_renombrar('cita', 'fecha_hora', 'fecha_inicio');
CALL mig_columna('cita', 'fecha_fin', 'DATETIME NULL');
UPDATE `cita` SET `fecha_fin` = DATE_ADD(`fecha_inicio`, INTERVAL 30 MINUTE) WHERE `fecha_fin` IS NULL;
ALTER TABLE `cita` MODIFY COLUMN `fecha_fin` DATETIME NOT NULL;
UPDATE `cita` SET `estado` = CASE UPPER(`estado`)
    WHEN 'CONFIRMADA' THEN 'CONFIRMADA' WHEN 'EN_ATENCION' THEN 'EN_ATENCION' WHEN 'CANCELADA' THEN 'CANCELADA'
    WHEN 'ATENDIDA' THEN 'ATENDIDA' WHEN 'FINALIZADA' THEN 'ATENDIDA' ELSE 'PROGRAMADA' END;
CALL mig_tipo('cita', 'cliente_id', 'bigint unsigned', 'NOT NULL');
CALL mig_tipo('cita', 'medico_id', 'bigint unsigned', 'NOT NULL');
CALL mig_columna('cita', 'servicio_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('cita', 'motivo_cancelacion', 'VARCHAR(255) NULL');
CALL mig_columna('cita', 'usuario_creacion_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('cita', 'version', 'INT NOT NULL DEFAULT 0');
CALL mig_columna('cita', 'fecha_creacion_registro', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL mig_columna('cita', 'fecha_modificacion_registro', 'DATETIME NULL');
CALL mig_restriccion('cita', 'fk_cita_cliente', 'FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE RESTRICT');
CALL mig_restriccion('cita', 'fk_cita_mascota', 'FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`mascota_id`) ON DELETE RESTRICT');
CALL mig_restriccion('cita', 'fk_cita_medico', 'FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('cita', 'fk_cita_servicio', 'FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('cita', 'fk_cita_usuario_creacion', 'FOREIGN KEY (`usuario_creacion_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('cita', 'ck_cita_fechas', 'CHECK (`fecha_fin` > `fecha_inicio`)');
-- Indices de agenda; incluyen id para poder crearse de forma repetible.
CALL mig_restriccion('cita', 'uk_cita_medico_horario', 'UNIQUE (`medico_id`, `fecha_inicio`, `fecha_fin`, `id`)');
CALL mig_restriccion('cita', 'uk_cita_mascota_horario', 'UNIQUE (`mascota_id`, `fecha_inicio`, `fecha_fin`, `id`)');
CREATE TABLE IF NOT EXISTS `cita_historial_estado` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `cita_id` BIGINT UNSIGNED NOT NULL,
    `estado_anterior` VARCHAR(30) NULL,
    `estado_nuevo` VARCHAR(30) NOT NULL,
    `fecha_inicio_anterior` DATETIME NULL,
    `fecha_inicio_nueva` DATETIME NULL,
    `motivo` VARCHAR(255) NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `fecha` DATETIME NOT NULL,
    CONSTRAINT `fk_cita_historial_estado_cita` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_cita_historial_estado_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================ Historias ============================
CALL mig_tipo('anotacion_historia', 'historia_id', 'bigint unsigned', 'NOT NULL');
CALL mig_tipo('anotacion_historia', 'medico_id', 'bigint unsigned', 'NOT NULL');
CALL mig_columna('anotacion_historia', 'cita_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('anotacion_historia', 'diagnostico', 'TEXT NULL');
CALL mig_columna('anotacion_historia', 'observaciones', 'TEXT NULL');
CALL mig_tipo('anotacion_historia', 'cita_id', 'bigint unsigned', 'NULL');
CALL mig_restriccion('anotacion_historia', 'fk_anotacion_historia_historia', 'FOREIGN KEY (`historia_id`) REFERENCES `historia_medica` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('anotacion_historia', 'fk_anotacion_historia_medico', 'FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('anotacion_historia', 'fk_anotacion_historia_cita', 'FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('historia_medica', 'fk_historia_medica_paciente', 'FOREIGN KEY (`paciente_id`) REFERENCES `mascota` (`mascota_id`) ON DELETE RESTRICT');

-- ============================ Inventario ============================
CALL mig_columna('medicamento', 'codigo', 'CHAR(8) NULL');
CALL mig_columna('medicamento', 'tipo_producto', 'VARCHAR(20) NOT NULL DEFAULT ''MEDICAMENTO''');
CALL mig_columna('medicamento', 'requiere_invima', 'BOOLEAN NOT NULL DEFAULT TRUE');
CALL mig_columna('medicamento', 'registro_invima', 'VARCHAR(50) NULL');
CALL mig_columna('medicamento', 'unidad_medida', 'VARCHAR(30) NOT NULL DEFAULT ''unidad''');
CALL mig_columna('medicamento', 'precio_venta', 'DECIMAL(14,2) NOT NULL DEFAULT 0');
CALL mig_columna('medicamento', 'impuesto_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('medicamento', 'stock_minimo', 'INT NOT NULL DEFAULT 0');
CALL mig_columna('medicamento', 'fecha_proximo_pedido', 'DATE NULL');
CALL mig_columna('medicamento', 'activo', 'BOOLEAN NOT NULL DEFAULT TRUE');
CALL mig_columna('medicamento', 'version', 'INT NOT NULL DEFAULT 0');
UPDATE `medicamento` SET `codigo` = CONCAT('MED', LPAD(`id`, 5, '0')) WHERE `codigo` IS NULL;
-- Valor provisional: el registro Invima real debe completarlo el administrador.
UPDATE `medicamento` SET `registro_invima` = CONCAT('PENDIENTE-', `id`)
WHERE `tipo_producto` = 'MEDICAMENTO' AND (`registro_invima` IS NULL OR `registro_invima` = '');
ALTER TABLE `medicamento` MODIFY COLUMN `codigo` CHAR(8) NOT NULL, MODIFY COLUMN `fecha_compra` DATE NULL, MODIFY COLUMN `fecha_vence` DATE NULL;
CALL mig_restriccion('medicamento', 'uk_medicamento_codigo', 'UNIQUE (`codigo`)');
CALL mig_restriccion('medicamento', 'fk_medicamento_impuesto', 'FOREIGN KEY (`impuesto_id`) REFERENCES `impuesto` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('medicamento', 'ck_medicamento_codigo', 'CHECK (REGEXP_LIKE(`codigo`, ''^[A-Z0-9]{8}$'', ''c''))');
CALL mig_restriccion('medicamento', 'ck_medicamento_precio', 'CHECK (`precio_venta` >= 0)');
CALL mig_restriccion('medicamento', 'ck_medicamento_stock', 'CHECK (`stock_minimo` >= 0)');
CALL mig_restriccion('medicamento', 'ck_medicamento_invima', 'CHECK (`tipo_producto` <> ''MEDICAMENTO'' OR (`registro_invima` IS NOT NULL AND `registro_invima` <> ''''))');
CREATE TABLE IF NOT EXISTS `lote_medicamento` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `medicamento_id` BIGINT UNSIGNED NOT NULL,
    `numero_lote` VARCHAR(50) NOT NULL,
    `precio_compra` DECIMAL(14,2) NOT NULL,
    `cantidad_inicial` INT NOT NULL,
    `cantidad_disponible` INT NOT NULL,
    `fecha_compra` DATE NOT NULL,
    `fecha_ingreso` DATE NOT NULL,
    `fecha_vencimiento` DATE NULL,
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_lote_medicamento_1` (`medicamento_id`, `numero_lote`),
    KEY `idx_lote_medicamento_1` (`medicamento_id`, `fecha_vencimiento`),
    CONSTRAINT `fk_lote_medicamento_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_lote_medicamento_1` CHECK (cantidad_disponible >= 0),
    CONSTRAINT `ck_lote_medicamento_2` CHECK (cantidad_disponible <= cantidad_inicial),
    CONSTRAINT `ck_lote_medicamento_3` CHECK (precio_compra >= 0),
    CONSTRAINT `ck_lote_medicamento_4` CHECK (fecha_vencimiento IS NULL OR fecha_vencimiento > fecha_compra)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Las fechas de compra y vencimiento pasan a un lote MIGRADO con cantidad 0 (no habia existencias registradas).
INSERT INTO `lote_medicamento` (`medicamento_id`, `numero_lote`, `precio_compra`, `cantidad_inicial`, `cantidad_disponible`,
                                `fecha_compra`, `fecha_ingreso`, `fecha_vencimiento`)
SELECT m.`id`, CONCAT('MIGRADO-', m.`id`), 0, 0, 0,
       LEAST(m.`fecha_compra`, DATE_SUB(m.`fecha_vence`, INTERVAL 1 DAY)), DATE(m.`fecha_creacion_registro`), m.`fecha_vence`
FROM `medicamento` m
WHERE m.`fecha_compra` IS NOT NULL AND m.`fecha_vence` IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `lote_medicamento` l WHERE l.`medicamento_id` = m.`id`);

CREATE TABLE IF NOT EXISTS `carga_inventario` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `nombre_archivo` VARCHAR(255) NOT NULL,
    `formato` VARCHAR(10) NOT NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `fecha_carga` DATETIME NOT NULL,
    `usuario_confirmacion_id` BIGINT UNSIGNED NULL,
    `fecha_confirmacion` DATETIME NULL,
    `estado` VARCHAR(30) NOT NULL,
    `total_filas` INT NOT NULL DEFAULT 0,
    `filas_validas` INT NOT NULL DEFAULT 0,
    `filas_con_error` INT NOT NULL DEFAULT 0,
    `filas_importadas` INT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    KEY `idx_carga_inventario_1` (`fecha_carga`),
    CONSTRAINT `fk_carga_inventario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_carga_inventario_usuario_confirmacion` FOREIGN KEY (`usuario_confirmacion_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `carga_inventario_fila` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `carga_id` BIGINT UNSIGNED NOT NULL,
    `numero_fila` INT NOT NULL,
    `datos_json` JSON NOT NULL,
    `valida` BOOLEAN NOT NULL,
    `accion` VARCHAR(30) NULL,
    `errores_json` JSON NOT NULL,
    `importada` BOOLEAN NOT NULL DEFAULT FALSE,
    `error_importacion` VARCHAR(500) NULL,
    `medicamento_id` BIGINT UNSIGNED NULL,
    `lote_id` BIGINT UNSIGNED NULL,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_carga_inventario_fila_1` (`carga_id`, `numero_fila`),
    CONSTRAINT `fk_carga_inventario_fila_carga` FOREIGN KEY (`carga_id`) REFERENCES `carga_inventario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_carga_inventario_fila_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_carga_inventario_fila_lote` FOREIGN KEY (`lote_id`) REFERENCES `lote_medicamento` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================ Atencion y formulas ============================
CREATE TABLE IF NOT EXISTS `atencion_medica` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `cita_id` BIGINT UNSIGNED NOT NULL,
    `historia_medica_id` BIGINT UNSIGNED NOT NULL,
    `medico_id` BIGINT UNSIGNED NOT NULL,
    `mascota_id` INT NOT NULL,
    `motivo_consulta` TEXT NULL,
    `sintomas` TEXT NULL,
    `diagnostico` TEXT NULL,
    `observaciones` TEXT NULL,
    `recomendaciones` TEXT NULL,
    `fecha_inicio` DATETIME NOT NULL,
    `fecha_fin` DATETIME NULL,
    `finalizada` BOOLEAN NOT NULL DEFAULT FALSE,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_atencion_medica_cita_id` (`cita_id`),
    CONSTRAINT `fk_atencion_medica_cita` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_atencion_medica_historia_medica` FOREIGN KEY (`historia_medica_id`) REFERENCES `historia_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_atencion_medica_medico` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_atencion_medica_mascota` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`mascota_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `atencion_servicio` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `atencion_id` BIGINT UNSIGNED NOT NULL,
    `servicio_id` BIGINT UNSIGNED NOT NULL,
    `cantidad` INT NOT NULL,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_atencion_servicio_1` (`atencion_id`, `servicio_id`),
    CONSTRAINT `fk_atencion_servicio_atencion` FOREIGN KEY (`atencion_id`) REFERENCES `atencion_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_atencion_servicio_servicio` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_atencion_servicio_1` CHECK (cantidad > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- formula_medica pasa a ser encabezado. Las columnas anteriores quedan como historico.
CALL mig_tipo('formula_medica', 'cita_id', 'bigint unsigned', 'NOT NULL');
CALL mig_tipo('formula_medica', 'medicamento_id', 'bigint unsigned', 'NULL');
ALTER TABLE `formula_medica` MODIFY COLUMN `medicamento_id` BIGINT UNSIGNED NULL, MODIFY COLUMN `dosis` TEXT NULL;
CALL mig_columna('formula_medica', 'frecuencia', 'VARCHAR(255) NULL');
CALL mig_columna('formula_medica', 'duracion', 'VARCHAR(255) NULL');
CALL mig_columna('formula_medica', 'numero', 'VARCHAR(20) NULL');
CALL mig_columna('formula_medica', 'atencion_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('formula_medica', 'medico_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('formula_medica', 'mascota_id', 'INT NULL');
CALL mig_columna('formula_medica', 'cliente_id', 'BIGINT UNSIGNED NULL');
CALL mig_columna('formula_medica', 'fecha_generacion', 'DATETIME NULL');
CALL mig_columna('formula_medica', 'indicaciones_generales', 'TEXT NULL');
CALL mig_columna('formula_medica', 'estado_formula', 'VARCHAR(30) NULL');
CALL mig_columna('formula_medica', 'estado_despacho', 'VARCHAR(30) NULL');
CALL mig_columna('formula_medica', 'fecha_confirmacion', 'DATETIME NULL');
CALL mig_columna('formula_medica', 'motivo_anulacion', 'VARCHAR(500) NULL');
CALL mig_columna('formula_medica', 'version', 'INT NOT NULL DEFAULT 0');
UPDATE `formula_medica` f JOIN `cita` c ON c.`id` = f.`cita_id`
SET f.`numero` = CONCAT('FM-', LPAD(f.`id`, 6, '0')), f.`medico_id` = c.`medico_id`, f.`mascota_id` = c.`mascota_id`,
    f.`cliente_id` = c.`cliente_id`, f.`fecha_generacion` = f.`fecha_creacion_registro`,
    f.`estado_formula` = 'CONFIRMADA', f.`estado_despacho` = 'PENDIENTE_DESPACHO',
    f.`fecha_confirmacion` = f.`fecha_creacion_registro`, f.`indicaciones_generales` = f.`indicaciones`
WHERE f.`estado_formula` IS NULL;
ALTER TABLE `formula_medica`
    MODIFY COLUMN `medico_id` BIGINT UNSIGNED NOT NULL, MODIFY COLUMN `mascota_id` INT NOT NULL,
    MODIFY COLUMN `cliente_id` BIGINT UNSIGNED NOT NULL, MODIFY COLUMN `fecha_generacion` DATETIME NOT NULL,
    MODIFY COLUMN `estado_formula` VARCHAR(30) NOT NULL;
CALL mig_restriccion('formula_medica', 'uk_formula_numero', 'UNIQUE (`numero`)');
CALL mig_restriccion('formula_medica', 'fk_formula_medica_cita', 'FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('formula_medica', 'fk_formula_medica_atencion', 'FOREIGN KEY (`atencion_id`) REFERENCES `atencion_medica` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('formula_medica', 'fk_formula_medica_medico', 'FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`) ON DELETE RESTRICT');
CALL mig_restriccion('formula_medica', 'fk_formula_medica_mascota', 'FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`mascota_id`) ON DELETE RESTRICT');
CALL mig_restriccion('formula_medica', 'fk_formula_medica_cliente', 'FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE RESTRICT');
CALL mig_restriccion('formula_medica', 'fk_formula_medica_medicamento', 'FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT');
CREATE TABLE IF NOT EXISTS `formula_medica_detalle` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `formula_id` BIGINT UNSIGNED NOT NULL,
    `medicamento_id` BIGINT UNSIGNED NOT NULL,
    `dosis` DECIMAL(10,2) NOT NULL,
    `unidad_dosis` VARCHAR(30) NOT NULL,
    `frecuencia_horas` INT NOT NULL,
    `duracion_dias` INT NOT NULL,
    `via_administracion` VARCHAR(30) NOT NULL,
    `cantidad_prescrita` INT NOT NULL,
    `cantidad_despachada` INT NOT NULL DEFAULT 0,
    `indicaciones` TEXT NULL,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_formula_medica_detalle_1` (`formula_id`, `medicamento_id`),
    CONSTRAINT `fk_formula_medica_detalle_formula` FOREIGN KEY (`formula_id`) REFERENCES `formula_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_formula_medica_detalle_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_formula_medica_detalle_1` CHECK (dosis > 0),
    CONSTRAINT `ck_formula_medica_detalle_2` CHECK (frecuencia_horas BETWEEN 1 AND 168),
    CONSTRAINT `ck_formula_medica_detalle_3` CHECK (duracion_dias > 0),
    CONSTRAINT `ck_formula_medica_detalle_4` CHECK (cantidad_prescrita > 0),
    CONSTRAINT `ck_formula_medica_detalle_5` CHECK (cantidad_despachada BETWEEN 0 AND cantidad_prescrita)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cada formula antigua se convierte en un encabezado con un detalle. La pauta original, escrita como texto libre,
-- se conserva completa en `indicaciones`; los valores numericos que no se pueden deducir quedan en 1.
INSERT INTO `formula_medica_detalle` (`formula_id`, `medicamento_id`, `dosis`, `unidad_dosis`, `frecuencia_horas`,
    `duracion_dias`, `via_administracion`, `cantidad_prescrita`, `indicaciones`)
SELECT f.`id`, f.`medicamento_id`,
       IF(f.`dosis` REGEXP '^[0-9]{1,6}$' AND f.`dosis` + 0 > 0, f.`dosis` + 0, 1), 'unidad', 24, 1, 'OTRA', 1,
       CONCAT_WS('. ', CONCAT('Pauta original: ', f.`dosis`), NULLIF(CONCAT('frecuencia ', f.`frecuencia`), ''),
                 NULLIF(CONCAT('duración ', f.`duracion`), ''), f.`indicaciones`)
FROM `formula_medica` f
WHERE f.`medicamento_id` IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `formula_medica_detalle` d WHERE d.`formula_id` = f.`id`);

CREATE TABLE IF NOT EXISTS `formula_historial_estado` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `formula_id` BIGINT UNSIGNED NOT NULL,
    `estado_despacho_anterior` VARCHAR(30) NULL,
    `estado_despacho_nuevo` VARCHAR(30) NULL,
    `estado_formula_anterior` VARCHAR(30) NULL,
    `estado_formula_nuevo` VARCHAR(30) NULL,
    `motivo` VARCHAR(500) NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `fecha` DATETIME NOT NULL,
    CONSTRAINT `fk_formula_historial_estado_formula` FOREIGN KEY (`formula_id`) REFERENCES `formula_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_formula_historial_estado_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `despacho` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `formula_id` BIGINT UNSIGNED NOT NULL,
    `numero` VARCHAR(20) NOT NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `estado` VARCHAR(30) NOT NULL,
    `fecha_inicio` DATETIME NOT NULL,
    `fecha_confirmacion` DATETIME NULL,
    `observacion` VARCHAR(500) NULL,
    `idempotency_key` VARCHAR(64) NULL,
    `fecha_reversion` DATETIME NULL,
    `usuario_reversion_id` BIGINT UNSIGNED NULL,
    `motivo_reversion` VARCHAR(500) NULL,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    `formula_en_preparacion` BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN `estado` = 'EN_PREPARACION' THEN `formula_id` END) STORED,
    UNIQUE KEY `uk_despacho_numero` (`numero`),
    UNIQUE KEY `uk_despacho_idempotency_key` (`idempotency_key`),
    UNIQUE KEY `uk_despacho_en_preparacion` (`formula_en_preparacion`),
    KEY `idx_despacho_1` (`fecha_confirmacion`),
    CONSTRAINT `fk_despacho_formula` FOREIGN KEY (`formula_id`) REFERENCES `formula_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_despacho_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_despacho_usuario_reversion` FOREIGN KEY (`usuario_reversion_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `despacho_detalle` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `despacho_id` BIGINT UNSIGNED NOT NULL,
    `formula_detalle_id` BIGINT UNSIGNED NOT NULL,
    `medicamento_id` BIGINT UNSIGNED NOT NULL,
    `lote_id` BIGINT UNSIGNED NOT NULL,
    `cantidad_entregada` INT NOT NULL,
    `precio_venta_unitario` DECIMAL(14,2) NOT NULL,
    `porcentaje_iva` DECIMAL(5,2) NOT NULL,
    `facturado` BOOLEAN NOT NULL DEFAULT FALSE,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    UNIQUE KEY `uk_despacho_detalle_1` (`despacho_id`, `formula_detalle_id`, `lote_id`),
    CONSTRAINT `fk_despacho_detalle_despacho` FOREIGN KEY (`despacho_id`) REFERENCES `despacho` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_despacho_detalle_formula_detalle` FOREIGN KEY (`formula_detalle_id`) REFERENCES `formula_medica_detalle` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_despacho_detalle_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_despacho_detalle_lote` FOREIGN KEY (`lote_id`) REFERENCES `lote_medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_despacho_detalle_1` CHECK (cantidad_entregada > 0),
    CONSTRAINT `ck_despacho_detalle_2` CHECK (precio_venta_unitario >= 0),
    CONSTRAINT `ck_despacho_detalle_3` CHECK (porcentaje_iva BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `movimiento_inventario` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `fecha` DATETIME NOT NULL,
    `tipo` VARCHAR(30) NOT NULL,
    `medicamento_id` BIGINT UNSIGNED NOT NULL,
    `lote_id` BIGINT UNSIGNED NOT NULL,
    `cantidad` INT NOT NULL,
    `saldo_lote_resultante` INT NOT NULL,
    `saldo_producto_resultante` INT NOT NULL,
    `costo_unitario` DECIMAL(14,2) NOT NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `formula_id` BIGINT UNSIGNED NULL,
    `despacho_id` BIGINT UNSIGNED NULL,
    `despacho_detalle_id` BIGINT UNSIGNED NULL,
    `carga_id` BIGINT UNSIGNED NULL,
    `movimiento_revertido_id` BIGINT UNSIGNED NULL,
    `observacion` VARCHAR(500) NULL,
    UNIQUE KEY `uk_movimiento_revertido` (`movimiento_revertido_id`),
    KEY `idx_movimiento_inventario_1` (`medicamento_id`, `fecha`),
    KEY `idx_movimiento_inventario_2` (`tipo`, `fecha`),
    CONSTRAINT `fk_movimiento_inventario_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_lote` FOREIGN KEY (`lote_id`) REFERENCES `lote_medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_formula` FOREIGN KEY (`formula_id`) REFERENCES `formula_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_despacho` FOREIGN KEY (`despacho_id`) REFERENCES `despacho` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_despacho_detalle` FOREIGN KEY (`despacho_detalle_id`) REFERENCES `despacho_detalle` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_carga` FOREIGN KEY (`carga_id`) REFERENCES `carga_inventario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_movimiento_inventario_movimiento_revertido` FOREIGN KEY (`movimiento_revertido_id`) REFERENCES `movimiento_inventario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_movimiento_inventario_1` CHECK (cantidad > 0),
    CONSTRAINT `ck_movimiento_inventario_2` CHECK (saldo_lote_resultante >= 0),
    CONSTRAINT `ck_movimiento_inventario_3` CHECK (saldo_producto_resultante >= 0),
    CONSTRAINT `ck_movimiento_inventario_4` CHECK (tipo <> 'SALIDA_DESPACHO' OR (formula_id IS NOT NULL AND despacho_id IS NOT NULL AND despacho_detalle_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `factura` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `numero` VARCHAR(20) NOT NULL,
    `cita_id` BIGINT UNSIGNED NOT NULL,
    `atencion_id` BIGINT UNSIGNED NOT NULL,
    `cliente_id` BIGINT UNSIGNED NOT NULL,
    `mascota_id` INT NOT NULL,
    `medico_id` BIGINT UNSIGNED NOT NULL,
    `cliente_nombre` VARCHAR(201) NOT NULL,
    `cliente_documento` VARCHAR(40) NOT NULL,
    `mascota_nombre` VARCHAR(100) NOT NULL,
    `medico_nombre` VARCHAR(201) NOT NULL,
    `especialidad_nombre` VARCHAR(100) NOT NULL,
    `fecha_generacion` DATETIME NOT NULL,
    `subtotal` DECIMAL(14,2) NOT NULL,
    `total_descuento` DECIMAL(14,2) NOT NULL,
    `total_impuestos` DECIMAL(14,2) NOT NULL,
    `total` DECIMAL(14,2) NOT NULL,
    `total_pagado` DECIMAL(14,2) NOT NULL DEFAULT 0,
    `saldo` DECIMAL(14,2) NOT NULL,
    `estado` VARCHAR(30) NOT NULL,
    `observacion` VARCHAR(500) NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `fecha_anulacion` DATETIME NULL,
    `usuario_anulacion_id` BIGINT UNSIGNED NULL,
    `motivo_anulacion` VARCHAR(500) NULL,
    `version` INT NOT NULL DEFAULT 0,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    `cita_vigente` BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN `estado` <> 'ANULADA' THEN `cita_id` END) STORED,
    UNIQUE KEY `uk_factura_numero` (`numero`),
    UNIQUE KEY `uk_factura_cita_vigente` (`cita_vigente`),
    KEY `idx_factura_1` (`fecha_generacion`),
    KEY `idx_factura_2` (`estado`),
    CONSTRAINT `fk_factura_cita` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_atencion` FOREIGN KEY (`atencion_id`) REFERENCES `atencion_medica` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_mascota` FOREIGN KEY (`mascota_id`) REFERENCES `mascota` (`mascota_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_medico` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_usuario_anulacion` FOREIGN KEY (`usuario_anulacion_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_factura_1` CHECK (saldo >= 0),
    CONSTRAINT `ck_factura_2` CHECK (total_pagado >= 0),
    CONSTRAINT `ck_factura_3` CHECK (total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `factura_detalle` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `factura_id` BIGINT UNSIGNED NOT NULL,
    `tipo_item` VARCHAR(30) NOT NULL,
    `servicio_id` BIGINT UNSIGNED NULL,
    `tarifa_id` BIGINT UNSIGNED NULL,
    `medicamento_id` BIGINT UNSIGNED NULL,
    `despacho_detalle_id` BIGINT UNSIGNED NULL,
    `descripcion` VARCHAR(255) NOT NULL,
    `cantidad` INT NOT NULL,
    `precio_unitario` DECIMAL(14,2) NOT NULL,
    `porcentaje_descuento` DECIMAL(5,2) NOT NULL DEFAULT 0,
    `valor_descuento` DECIMAL(14,2) NOT NULL DEFAULT 0,
    `motivo_descuento` VARCHAR(255) NULL,
    `porcentaje_iva` DECIMAL(5,2) NOT NULL,
    `valor_iva` DECIMAL(14,2) NOT NULL,
    `subtotal` DECIMAL(14,2) NOT NULL,
    `total` DECIMAL(14,2) NOT NULL,
    `vigente` BOOLEAN NOT NULL DEFAULT TRUE,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    `despacho_detalle_vigente` BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN `vigente` THEN `despacho_detalle_id` END) STORED,
    UNIQUE KEY `uk_factura_despacho_vigente` (`despacho_detalle_vigente`),
    CONSTRAINT `fk_factura_detalle_factura` FOREIGN KEY (`factura_id`) REFERENCES `factura` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_detalle_servicio` FOREIGN KEY (`servicio_id`) REFERENCES `servicio` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_detalle_tarifa` FOREIGN KEY (`tarifa_id`) REFERENCES `tarifa` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_detalle_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_detalle_despacho_detalle` FOREIGN KEY (`despacho_detalle_id`) REFERENCES `despacho_detalle` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_factura_detalle_1` CHECK (cantidad > 0),
    CONSTRAINT `ck_factura_detalle_2` CHECK (porcentaje_descuento BETWEEN 0 AND 100),
    CONSTRAINT `ck_factura_detalle_3` CHECK ((tipo_item = 'SERVICIO' AND servicio_id IS NOT NULL) OR (tipo_item = 'MEDICAMENTO' AND despacho_detalle_id IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pago` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `factura_id` BIGINT UNSIGNED NOT NULL,
    `fecha` DATETIME NOT NULL,
    `valor` DECIMAL(14,2) NOT NULL,
    `medio_pago` VARCHAR(30) NOT NULL,
    `referencia` VARCHAR(100) NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `anulado` BOOLEAN NOT NULL DEFAULT FALSE,
    `fecha_creacion_registro` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion_registro` DATETIME NULL,
    KEY `idx_pago_1` (`fecha`),
    CONSTRAINT `fk_pago_factura` FOREIGN KEY (`factura_id`) REFERENCES `factura` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_pago_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `ck_pago_1` CHECK (valor > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `factura_historial_estado` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `factura_id` BIGINT UNSIGNED NOT NULL,
    `estado_anterior` VARCHAR(30) NULL,
    `estado_nuevo` VARCHAR(30) NOT NULL,
    `motivo` VARCHAR(500) NULL,
    `usuario_id` BIGINT UNSIGNED NOT NULL,
    `fecha` DATETIME NOT NULL,
    CONSTRAINT `fk_factura_historial_estado_factura` FOREIGN KEY (`factura_id`) REFERENCES `factura` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_factura_historial_estado_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Configuracion de la clinica (una sola fila). Los consecutivos parten de lo ya existente.
INSERT INTO `configuracion_clinica` (`nombre`, `nit`, `direccion`, `telefono`, `correo`, `prefijo_factura`, `prefijo_formula`,
                                     `consecutivo_formula`, `prefijo_despacho`)
SELECT 'Clínica Veterinaria Uniminuto', '900000000-0', 'Calle 1 # 2-3, Bogotá', '6010000000', 'contacto@clinica.example',
       'FV', 'FM', COALESCE((SELECT MAX(`id`) FROM `formula_medica`), 0), 'DP'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `configuracion_clinica`);

DROP PROCEDURE mig_ejecutar;
DROP PROCEDURE mig_columna;
DROP PROCEDURE mig_restriccion;
DROP PROCEDURE mig_renombrar;
DROP PROCEDURE mig_tipo;

-- Revisar: todo usuario debe tener perfil y todo perfil, codigo.
SELECT `username`, `rol`, `perfil_id` FROM `usuario` WHERE `perfil_id` IS NULL;
SELECT `perfil_id`, `nombre` FROM `perfil` WHERE `codigo` IS NULL;

-- Permisos presentes que no estan en C4 (no se borran; revisar).
SELECT p.`codigo`, m.`code` FROM `perfil_menu` pm
JOIN `perfil` p ON p.`perfil_id` = pm.`perfil_id` JOIN `menu` m ON m.`menu_id` = pm.`menu_id`
LEFT JOIN (
    SELECT 'cita' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'cita' c, 'SECRETARIA' r UNION ALL
    SELECT 'clientes' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'clientes' c, 'SECRETARIA' r UNION ALL
    SELECT 'especializaciones' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'formulas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'formulas' c, 'MEDICO' r UNION ALL
    SELECT 'historias' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'historias' c, 'MEDICO' r UNION ALL
    SELECT 'mascotas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'mascotas' c, 'SECRETARIA' r UNION ALL
    SELECT 'mascotas' c, 'MEDICO' r UNION ALL
    SELECT 'inventario' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'inventario' c, 'DESPACHO_MEDICO' r UNION ALL
    SELECT 'medicos' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'medicos' c, 'SECRETARIA' r UNION ALL
    SELECT 'usuarios' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'calendario' c, 'MEDICO' r UNION ALL
    SELECT 'atencion' c, 'MEDICO' r UNION ALL
    SELECT 'carga-inventario' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'despacho' c, 'DESPACHO_MEDICO' r UNION ALL
    SELECT 'tarifas' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'facturacion' c, 'ADMINISTRADOR' r UNION ALL
    SELECT 'facturacion' c, 'SECRETARIA' r UNION ALL
    SELECT 'reportes' c, 'ADMINISTRADOR' r
) x ON x.c = m.`code` COLLATE utf8mb4_0900_ai_ci AND x.r = p.`codigo` COLLATE utf8mb4_0900_ai_ci
WHERE x.c IS NULL;
