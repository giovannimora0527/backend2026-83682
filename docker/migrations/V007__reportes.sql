USE clinica;

START TRANSACTION;
INSERT INTO menu(activo,code,fecha_creacion,icon,title,url)
SELECT b'1','reportes',NOW(6),'feather icon-bar-chart-2','Reportes','/inicio/reportes'
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code='reportes' OR url='/inicio/reportes');

INSERT INTO perfil_menu(perfil_id,menu_id)
SELECT p.perfil_id,m.menu_id
FROM perfil p JOIN menu m ON m.code='reportes' AND m.url='/inicio/reportes'
WHERE UPPER(TRIM(p.nombre)) IN ('ADMIN','ADMINISTRADOR','RECEPCION','SECRETARIA')
AND NOT EXISTS (SELECT 1 FROM perfil_menu pm WHERE pm.perfil_id=p.perfil_id AND pm.menu_id=m.menu_id);
COMMIT;
