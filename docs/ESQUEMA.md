# Esquema de persistencia

No se encontró un esquema SQL ni un sistema de migraciones preexistente en el repositorio. Las nuevas entidades esperan las tablas `veterinario`, `cita`, `historia_clinica`, `anotacion` y `formula_medica`; las referencias existentes son `cliente(cliente_id)` y `mascota(mascota_id)`. La migración manual `src/main/resources/db/manual/V1__modulos_clinicos.sql` documenta las columnas e índices usados por los mapeos.

La migración no se ejecuta al arrancar, para evitar cambios no revisados en la base real. Antes de aplicarla, compara cada tabla con `SHOW CREATE TABLE`, confirma las claves y tipos, adapta la migración a las tablas existentes y ejecútala primero en una copia de desarrollo. No contiene inserciones de veterinarios ni datos ficticios. Las fórmulas no tienen borrado físico expuesto; las historias y anotaciones son históricas.

La disponibilidad de citas se serializa bloqueando las filas del veterinario y de la mascota durante la transacción, con aislamiento `READ_COMMITTED`, y excluyendo las canceladas. Se exige una diferencia de al menos 15 minutos entre inicios para el mismo veterinario; para una mascota se rechaza la misma fecha y hora exactas porque el modelo no define duración.

Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` en el entorno antes de iniciar. En desarrollo, `CORS_ALLOWED_ORIGIN` permite cambiar el origen Angular (por defecto `http://localhost:4200`). Las propiedades ya no imprimen SQL ni mantienen la contraseña en el archivo.
