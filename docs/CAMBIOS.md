# Cambios

## Fase 1: seguridad y correcciones

### Pasos obligatorios antes de ejecutar

1. Respaldar la base `clinica` y ejecutar una sola vez `db/02_migracion_examen_final.sql`. Sin ella no se puede iniciar sesión.
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

### Pendiente para cuando exista el contrato o el modelo

- `@PreAuthorize` por rol (C6) y verificación de propiedad del médico (requiere `medico.usuario_id`). Hoy cualquier usuario autenticado accede a todas las rutas protegidas.
- Rol por `perfil.codigo` (C2). Provisionalmente es el nombre del perfil en mayúsculas, sin tildes.
- Duración de la cita según `servicio.duracionMinutos`. Provisionalmente 30 minutos.
- Bloqueo frente a dos citas creadas a la vez (fase de citas).

## Desviaciones del contrato

Sin desviaciones registradas: el contrato compartido aún no se ha recibido.
