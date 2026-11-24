-- Horario de lunes a viernes, 08:00-12:00 y 14:00-18:00, para los medicos que no tienen ninguno.
-- Requiere 02. Se puede ejecutar mas de una vez: no toca a los medicos que ya tienen horario.
SET NAMES utf8mb4;

INSERT INTO `horario_medico` (`medico_id`, `dia_semana`, `hora_inicio`, `hora_fin`, `activo`)
SELECT m.`id`, d.n, h.i, h.f, TRUE
FROM `medico` m
LEFT JOIN (SELECT DISTINCT `medico_id` FROM `horario_medico`) x ON x.`medico_id` = m.`id`
CROSS JOIN (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) d
CROSS JOIN (SELECT '08:00:00' i, '12:00:00' f UNION ALL SELECT '14:00:00', '18:00:00') h
WHERE x.`medico_id` IS NULL
ORDER BY m.`id`, d.n, h.i;

SELECT COUNT(*) AS medicos_sin_horario FROM `medico` m
WHERE NOT EXISTS (SELECT 1 FROM `horario_medico` h WHERE h.`medico_id` = m.`id`);
