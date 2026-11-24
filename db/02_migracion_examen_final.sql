-- Migracion del examen final. MySQL 8. Hacer un respaldo antes de ejecutarla.
-- Se ejecuta UNA sola vez sobre la base `clinica` (los ALTER no son repetibles).
-- Fases 1 y 2: sesiones, roles y vinculo medico-usuario. Las fases siguientes agregan sus cambios a continuacion.

-- El JWT ya no se guarda; solo su identificador (jti). Se conserva la columna historica `token`.
ALTER TABLE `session`
    MODIFY COLUMN `token` TEXT NULL,
    ADD COLUMN `jti` VARCHAR(36) NULL,
    ADD COLUMN `activa` BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN `fecha_cierre` DATETIME NULL,
    ADD CONSTRAINT `uk_session_jti` UNIQUE (`jti`);

-- Mascotas creadas sin valor por el fallo anterior.
UPDATE `mascota` SET `activo` = TRUE WHERE `activo` IS NULL AND `mascota_id` > 0;

-- Roles del contrato (C2). Los perfiles que no coincidan quedan con codigo NULL y no pueden entrar:
-- revisar el SELECT final y asignarles el codigo a mano.
ALTER TABLE `perfil`
    ADD COLUMN `codigo` VARCHAR(30) NULL,
    ADD CONSTRAINT `uk_perfil_codigo` UNIQUE (`codigo`);

UPDATE `perfil` p
JOIN (SELECT MIN(`perfil_id`) AS id,
             CASE WHEN UPPER(`nombre`) LIKE 'DESPACHO%' THEN 'DESPACHO_MEDICO'
                  WHEN UPPER(`nombre`) LIKE 'ADMIN%' THEN 'ADMINISTRADOR'
                  WHEN UPPER(`nombre`) LIKE 'SECRETARI%' THEN 'SECRETARIA'
                  WHEN UPPER(`nombre`) LIKE 'MEDIC%' OR UPPER(`nombre`) LIKE 'VETERINARI%' THEN 'MEDICO'
             END AS cod
      FROM `perfil` GROUP BY cod) x ON x.id = p.`perfil_id` AND x.cod IS NOT NULL
SET p.`codigo` = x.cod;

ALTER TABLE `medico`
    ADD COLUMN `usuario_id` BIGINT NULL,
    ADD CONSTRAINT `uk_medico_usuario` UNIQUE (`usuario_id`);
-- La clave foranea a usuario se agrega en la fase 3, al conocer el tipo exacto de usuario.id.

SELECT `perfil_id`, `nombre`, `codigo` FROM `perfil`;
