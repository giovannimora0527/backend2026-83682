-- Revisar y aplicar explícitamente sobre el esquema clinica.
-- Las columnas id son BIGINT SIGNED en Compose y BIGINT UNSIGNED en el MySQL local.
SELECT COLUMN_TYPE INTO @tipo_usuario FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'id';
SELECT COLUMN_TYPE INTO @tipo_medico FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'medico' AND column_name = 'id';

SET @ddl_vinculo = CONCAT(
  'CREATE TABLE IF NOT EXISTS usuario_medico (usuario_id ', @tipo_usuario,
  ' NOT NULL, medico_id ', @tipo_medico, ' NOT NULL,',
  ' PRIMARY KEY (usuario_id), UNIQUE KEY uq_usuario_medico_medico (medico_id),',
  ' CONSTRAINT fk_usuario_medico_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),',
  ' CONSTRAINT fk_usuario_medico_medico FOREIGN KEY (medico_id) REFERENCES medico(id)) ENGINE=InnoDB'
);
PREPARE crear_vinculo FROM @ddl_vinculo;
EXECUTE crear_vinculo;
DEALLOCATE PREPARE crear_vinculo;

INSERT INTO perfil (nombre, activo)
SELECT 'DESPACHO_MEDICO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE UPPER(REPLACE(TRIM(nombre), ' ', '_')) = 'DESPACHO_MEDICO');

INSERT INTO perfil_menu (perfil_id, menu_id)
SELECT p.perfil_id, m.menu_id FROM perfil p CROSS JOIN menu m
WHERE UPPER(REPLACE(TRIM(p.nombre), ' ', '_')) = 'DESPACHO_MEDICO'
  AND m.activo = TRUE AND m.url = '/inicio/medicamentos'
  AND NOT EXISTS (SELECT 1 FROM perfil_menu pm
                  WHERE pm.perfil_id = p.perfil_id AND pm.menu_id = m.menu_id);

-- Registrar los vínculos médico-usuario verificados en una operación separada.
-- No se crean usuarios ni se copian contraseñas, menús ni datos clínicos.
