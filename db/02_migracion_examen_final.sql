-- Migracion del examen final. MySQL 8. Hacer un respaldo antes de ejecutarla.
-- Se ejecuta UNA sola vez sobre la base `clinica` (los ALTER no son repetibles).
-- Fase 1: sesiones. Las fases siguientes agregan sus cambios a continuacion.

-- El JWT ya no se guarda; solo su identificador (jti). Se conserva la columna historica `token`.
ALTER TABLE `session`
    MODIFY COLUMN `token` TEXT NULL,
    ADD COLUMN `jti` VARCHAR(36) NULL,
    ADD COLUMN `activa` BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN `fecha_cierre` DATETIME NULL,
    ADD CONSTRAINT `uk_session_jti` UNIQUE (`jti`);

-- Mascotas creadas sin valor por el fallo anterior.
UPDATE `mascota` SET `activo` = TRUE WHERE `activo` IS NULL AND `mascota_id` > 0;
