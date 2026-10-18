# Esquema de persistencia

La aplicación usa la base MySQL existente `clinica` **tal como está**. No hay migraciones automáticas
(`ddl-auto=none`, `spring.sql.init.mode=never`). La base no define claves foráneas, así que las validaciones
de integridad (por ejemplo, no eliminar una mascota con citas) se hacen en los servicios.

> Los scripts de `src/main/resources/db/manual/` (V1 y V2) son propuestas antiguas que **no deben aplicarse**:
> el código ya no los necesita. No se ejecutan nunca de forma automática.

## Entidades y tablas reales

| Entidad | Tabla | Columnas mapeadas |
|---|---|---|
| `Cliente` | `cliente` | cliente_id, usuario_id, tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo |
| `Mascota` | `mascota` | mascota_id, nombre_mascota, edad, raza_id, fecha_registro, fecha_modificacion, cliente_id |
| `Raza` | `raza` | raza_id, nombre, especie, fecha_creacion, fecha_modificacion |
| `Veterinario` (API `/veterinarios` y `/medico`) | `medico` | id, tipo_documento, numero_documento, nombres, apellidos, telefono, registro_profesional, especializacion_id |
| `Especializacion` | `especializacion` | id, nombre, descripcion, codigo_especializacion |
| `Cita` | `cita` | id, cliente_id, mascota_id, medico_id, fecha_hora, estado, motivo |
| `HistoriaClinica` | `historia_medica` | id, paciente_id (= mascota), fecha_creacion |
| `Anotacion` | `anotacion_historia` | id, historia_id, medico_id, fecha, descripcion |
| `FormulaMedica` | `formula_medica` | id, cita_id, medicamento_id, dosis, indicaciones, fecha_creacion_registro, fecha_actualizacion_registro |
| `Medicamento` | `medicamento` | id, nombre |
| `Usuario` | `usuario` | id, username, password_hash, rol, fecha_creacion, activo, email |

## Cómo se representan los datos que no tienen columna propia

- **Cita**: `fecha` y `hora` de la API se combinan en `fecha_hora`; `motivoConsulta` se guarda en `motivo`;
  `estado` se guarda en minúsculas (`programada`, `pendiente`, ...) y la API lo expone en mayúsculas.
- **Historia clínica**: cliente y veterinario se obtienen de la mascota (`paciente_id`) y de su anotación inicial.
  Motivo, diagnóstico y observaciones se guardan en la primera anotación (`descripcion`) con un formato versionado
  (`[[CLINICA_HISTORIA_V1]]`).
- **Anotación**: `observacion` y `diagnosticoEvolucion` se guardan en `descripcion` como líneas
  `Observación: ...` y `Diagnóstico de evolución: ...`, y la API las separa al leer.
- **Fórmula médica**: la tabla solo guarda `cita_id`; la mascota, el cliente y el veterinario salen de la cita.
  Si Angular no envía `citaId`, se usa la cita más reciente entre la mascota y el veterinario (si no existe, 409).
  `frecuencia` y `duracionDias` se guardan en `indicaciones` como `Frecuencia: ...` y `Duración en días: ...`
  (el mismo formato de los registros existentes). La fecha de emisión es la fecha de `fecha_creacion_registro`;
  si se indica otra, se añade la línea `Fecha de emisión: AAAA-MM-DD`. `historiaClinicaId` se deriva de la mascota.
- **Usuario**: `password_hash` guarda MD5 (formato del usuario existente) y nunca se devuelve en la API.

## Registros históricos incompletos

Algunas filas apuntan a registros que no existen (por ejemplo, la cita 1 apunta a `mascota_id = 1`). Las
relaciones usan `@NotFound(IGNORE)`: la API devuelve esos campos como `null` en lugar de fallar, y no modifica
esos registros.

## Disponibilidad de citas

Se bloquean las filas del veterinario y de la mascota durante la transacción (`READ_COMMITTED`), excluyendo las
canceladas. Se exige al menos 15 minutos entre inicios para el mismo veterinario; para una mascota se rechaza la
misma fecha y hora exactas.
