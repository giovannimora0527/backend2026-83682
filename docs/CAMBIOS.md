# Cambios

## Fase 1: seguridad y correcciones

### Pasos obligatorios antes de ejecutar

1. Respaldar la base `clinica` y ejecutar `db/02_migracion_examen_final.sql` (repetible) y, si se quieren datos de prueba, `db/03_datos_iniciales.sql`. Sin la migración no se puede iniciar sesión.
2. Copiar `.env.example` como `.env` en la raíz del backend y completarlo (en `dev` se carga automáticamente; en `prod` se usan variables de entorno reales).
3. **Rotar las credenciales filtradas.** La contraseña de correo, la de la base de datos y la clave JWT siguen en el historial de Git. Quitarlas del código no las invalida: hay que cambiarlas en Gmail y en MySQL y generar otro `JWT_SECRET`.

### Seguridad

- Todas las rutas exigen JWT con sesión activa, salvo login, validación del OTP, Swagger, `/clinica/test` y health. Sin autenticación: 401; sin permiso: 403.
- El JWT lleva `jti`; en `session` se guarda ese identificador y no el token. Cerrar sesión lo invalida. Los JWT anteriores dejan de servir.
- También se rechaza el token de un usuario inactivado.
- Contraseñas en BCrypt. Las MD5 existentes se convierten en el siguiente login correcto. Máximo 72 bytes.
- El OTP se usa una sola vez y con bloqueo de fila. Un nuevo login elimina los anteriores. En `dev` se escribe en el log.
- CORS restringido a `CORS_ORIGINS`.

### Correcciones

- Login: si falla el correo responde 500 con un mensaje claro. El OTP se conserva para poder continuar en `dev` con el código del log.
- Citas: un único cálculo de solapamiento (30 minutos, médico o mascota), sin contar las canceladas ni la propia cita.
- Mascotas: búsqueda por nombre devuelve lista; al crear, `activo = true`.
- Especialización: validación de duplicados al actualizar.
- Usuario: contraseña opcional al actualizar y `perfilId` obligatorio.
- Fórmula: valida `medicamentoId`.
- Cliente: `POST /cliente/guardar` (409 si el documento existe). Responde el cliente creado.
- `POST /auth/cerrar-sesion` implementado: toma la sesión del token e ignora el cuerpo.
- Los GET ya no exigen `Content-Type`.
- Correos no críticos: `CorreoAsyncService` (`@Async` real). Todavía no lo usa ningún módulo.
- Eliminado sin uso: `CitaMedica`, `CitaRequest`, `AnotacionRq`, `FormulaMedicaService` y su implementación, `EmailConfig`.
- Tests con perfil `test` sobre H2 en modo MySQL.

## Fase 2: permisos y contrato

- Roles por `perfil.codigo` (C2), leídos de la base en cada petición. Un perfil sin código o inactivo no puede entrar.
- `@PreAuthorize` en todos los controladores y una segunda barrera por módulo en `SecurityConfig`, que ya protege las rutas aún no implementadas.
- Propiedad: un MEDICO solo crea, ve o modifica sus citas y fórmulas, y solo anota en sus citas (403 si son de otro médico).
- JWT según C3: `rol`, `medicoId`, `menus` con `code`, `name` y `activo`. Solo se incluyen menús activos.
- `medico.usuario_id`: `POST /medico/crear` y `/medico/actualizar` aceptan `usuarioId` (debe tener rol MEDICO y estar libre).
- Errores según C1 (`Bad Request`, `Conflict`, `detalles`). Los inexistentes pasan de 400 a 404 y los duplicados o cruces de 400 a 409. Rutas inexistentes: 404 en lugar de 500.
- Nuevo `GET /cita/{id}`; el detalle completo llega en la fase 5.

### Decisiones donde el contrato no fija el rol

- `/cita/listar` y `/cita/listar-rango`: ADMINISTRADOR y SECRETARIA (el médico usará el calendario).
- Mascotas y razas: lectura ADMINISTRADOR, SECRETARIA y MEDICO; escritura ADMINISTRADOR y SECRETARIA.
- Historias clínicas: ADMINISTRADOR y MEDICO. Anotaciones: solo MEDICO.
- Correo de prueba: ADMINISTRADOR.

### Pendiente de las fases siguientes

- Duración de la cita según `servicio` y bloqueo frente a citas simultáneas (fase 5).
- `/medico/listar-disponibles` todavía recibe `fechaInicio` y `fechaFin`; pasará a `inicio` y `fin` (fase 5).
- 200 de DESPACHO_MEDICO en `GET /inventario/productos`: se prueba en la fase 4, cuando exista.

## Fase 3: modelo de datos

- Scripts `db/01` a `db/03`, repetibles. Probados en MySQL 8.4 sobre una base nueva y sobre una copia de la base existente. `ddl-auto=validate` en `dev` y `prod`.
- 21 tablas nuevas con sus entidades y repositorios. Detalle y justificación en `modelo-datos.md`.
- Evidencias: `db/evidencias/carga_inventario_60.xlsx` y `.csv`. Errores en las filas 11 (precio negativo), 21 (código de 7 caracteres), 32 (sin Invima), 41 (vence antes de la compra) y 51 (repite la fila 50).
- `/formula-medica/listar` devuelve encabezados con `detalles`. `guardar` y `actualizar` responden 410 hasta que el frontend use `crear` y `PUT /{id}` (fase 6).
- Las citas exponen `fechaInicio`, `fechaFin`, `servicio` y `version`, y conservan `fechaHora`. `CitaRq` acepta `fechaInicio` o `fechaHora`.
- `cliente.correo` nuevo, opcional.
- Médicos disponibles: se descartan por cualquier cita no cancelada que se cruce, no solo las `PROGRAMADA`.

## Desviaciones del contrato

- `factura_detalle.despacho_detalle_id` no es `UNIQUE` simple: impediría volver a facturar tras anular. Se permite un solo renglón vigente por entrega.
- `formula_medica` conserva `fecha_actualizacion_registro` en lugar de `fecha_modificacion_registro`.
- `medicoId` se omite del JWT, en vez de ir como `null`, cuando el usuario no es médico.

- El JWT agrega el claim `jti`, que C3 no lista pero la sección 4 exige.
- Los menús del JWT incluyen, además de `code`, `name` y `activo`, los campos que el frontend actual exige (`menuId`, `title`, `url`, `icon`, `fechaCreacion`, `fechaModificacion`). Al migrar el frontend a C3 se pueden quitar.
- C1 dice que `/clinica/test` lleva token; la sección 4 lo declara público. Queda público.
