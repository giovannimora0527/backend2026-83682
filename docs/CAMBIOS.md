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

## Fase 3: modelo de datos

- Scripts `db/01` a `db/03`, repetibles. Probados en MySQL 8.4 sobre una base nueva y sobre una copia de la base existente. `ddl-auto=validate` en `dev` y `prod`.
- 21 tablas nuevas con sus entidades y repositorios. Detalle y justificación en `modelo-datos.md`.
- Evidencias: `db/evidencias/carga_inventario_60.xlsx` y `.csv`. Errores en las filas 11 (precio negativo), 21 (código de 7 caracteres), 32 (sin Invima), 41 (vence antes de la compra) y 51 (repite la fila 50).
- `/formula-medica/listar` devuelve encabezados con `detalles`. `guardar` y `actualizar` responden 410 hasta que el frontend use `crear` y `PUT /{id}` (fase 6).
- Las citas exponen `fechaInicio`, `fechaFin`, `servicio` y `version`, y conservan `fechaHora`. `CitaRq` acepta `fechaInicio` o `fechaHora`.
- `cliente.correo` nuevo, opcional.
- Médicos disponibles: se descartan por cualquier cita no cancelada que se cruce, no solo las `PROGRAMADA`.

## Compatibilidad con las tablas del profesor

- Referencia: `db/referencia_profesor_perfiles_menus.sql` (no ejecutar: hace `DROP TABLE`).
- `02` crea o completa `perfil`, `menu` y `perfil_menu`. Conserva los 9 menús con sus ids; la fila 7 pasa de `medicamentos` a `inventario`; agrega los menús 10 a 16 y el perfil 4.
- Perfiles: 1 Administrador, 2 Recepcion (`SECRETARIA`), 3 Medico, 4 Despacho Medico.
- `perfil_menu`: FK, `UNIQUE(perfil_id, menu_id)` y matriz C4. No se borran permisos existentes; al final `02` lista los que no estén en C4.
- `03` ya no toca menús ni permisos, y no escribe `usuario.rol`, que puede ser texto o número.
- Probado dos veces por caso en MySQL 8.4.13 y 8.0.12: base vacía, base antigua, base con las tablas del profesor, ambas combinadas y `usuario.rol` numérico. En versiones anteriores a 8.0.16 MySQL no aplica los `CHECK`: esas reglas dependen del backend.
- `POST /cliente/guardar` responde `{status, message, data}` (C1) y `POST /auth/cerrar-sesion` incluye `status`.

### Cambio acordado del contrato (C4)

El código del menú de citas es `cita`, no `citas`; la ruta sigue siendo `/inicio/citas`. Los `@PreAuthorize` usan roles, no códigos de menú, así que no cambian.

## Pendientes reportados por el frontend

- `GET /perfil/listar`, `PATCH /usuario/{id}/estado`, horarios del médico, `PATCH /especializacion/{id}/estado` y facturas por cliente y mascota.
- 409 por duplicado con `detalles`. Las restricciones de la base y los choques de versión también responden 409.
- `FormulaRs` usa los nombres del frontend: `fecha`, `medicamentoCodigo`, `medicamentoNombre`.

## Fase 10: Swagger, Docker y documentación

- Swagger con 128 operaciones. Los roles de cada una se leen de `@PreAuthorize`; una prueba exige resumen, grupo y roles en todas.
- `docker-compose.yml`: MySQL, Mailpit, backend y frontend. El Dockerfile y el nginx del frontend están en `deploy/frontend`; su carpeta solo se lee.
- **Demora de MySQL:** no era MySQL ni Docker. El registro de energía de Windows muestra que el equipo se suspendió durante esos arranques (12:32–12:48 y 14:05–14:35 del 24/11/2026). Medido sin suspensión: 21 s en frío y 4 s al reiniciar. Aun así: datos en volumen de Docker, scripts solo con el volumen vacío, healthcheck por TCP y menos memoria (256 MB de buffer, sin `performance_schema` ni binlog).
- `scripts/levantar.ps1` evita la suspensión automática mientras levanta.
- El backend corre sin privilegios y genera su clave JWT si no se le da una.
- `MAIL_AUTH` y `MAIL_STARTTLS` configurables; `/actuator/health` público, sin detalles.

## Fase 9: reportes

- 21 reportes en vista previa, PDF y Excel. Cada uno es un bean `GeneradorReporte`: agregar otro no toca el servicio ni los exportadores.
- Consultas JPQL agregadas, sin cargar entidades. Los totales se compararon con SQL directo en MySQL 8.4.
- **Recaudado** en reportes por renglón (consultas y medicamentos): cada pago se reparte según el peso del renglón en su factura. Puede diferir centavos por redondeo.
- Los pagos de una factura anulada siguen contando como recaudado, porque el dinero entró; por eso recaudado puede superar a facturado.
- `facturas` lista las anuladas, pero solo las suma si se filtra por ANULADA.
- `kardex` empieza con el saldo anterior al rango. `consultas-*` cuentan atenciones finalizadas.
- Los de existencias no dependen del rango de fechas.

## Fase 8: facturación y pagos

- `/factura` completo según C6. Al generar se bloquean la cita y sus fórmulas: dos facturas simultáneas o facturar mientras se revierte un despacho no dejan datos incoherentes (probado en MySQL 8.4).
- Solo se facturan citas con atención finalizada; las ATENDIDAS antiguas sin atención no aparecen como pendientes.
- Un servicio sin tarifa vigente impide facturar; con descuento total la factura nace PAGADA.
- Lo que se despache después de facturar no entra en esa factura: para cobrarlo se anula y se genera otra.
- Anular conserva renglones, pagos e historial, y libera la cita y lo despachado.
- El PDF usa los nombres y valores congelados.
- Campos adicionales en la previsualización: `topeDescuento`, documento, especialidad y fecha de la cita.

## Fase 7: despacho

- Todo `/despacho` según C6. Las operaciones bloquean siempre en el mismo orden (fórmula, despacho, productos, lotes), así que no se interbloquean. Probado en MySQL 8.4 con peticiones simultáneas: no hay doble descuento ni entregas por encima de lo disponible.
- Adicional al contrato: `POST /despacho/{despachoId}/liberar`, para que una preparación abandonada no bloquee la fórmula.
- Si algún item falla se devuelven todos los errores, por posición, y no se despacha nada.
- `Idempotency-Key` se guarda con el despacho; usarla en otro responde 409.
- Con existencias disponibles no se admite confirmar sin items.
- Revertir un despacho de una fórmula cancelada devuelve existencias sin reabrirla.
- Los lotes vencidos se muestran al final, después de los vigentes en orden FEFO.
- El historial muestra el estado actual de la fórmula; los pasos intermedios están en `historial-estados`.
- `docs/reglas-negocio.md` inicia con las reglas de despacho.

## Fase 6: tarifas, atención y fórmulas

- **Cambio acordado de C5:** `EN_ATENCION` solo se asigna con `POST /atencion/iniciar/{citaId}` y `ATENDIDA` solo con `POST /atencion/{id}/finalizar`. `PATCH /cita/{id}/estado` y `/cita/actualizar` responden 409 para esos estados; siguen permitiendo `CONFIRMADA` y `CANCELADA`.
- Tarifas: cambiar el valor o el impuesto cierra la vigente y crea otra, con motivo y usuario en `tarifa_historial`. Las vigencias futuras no se admiten.
- El porcentaje de un impuesto en uso no se edita, para que ningún cambio de IVA quede fuera del historial.
- Finalizar exige tarifa vigente en cada servicio, porque sin ella no se podría facturar.
- Al finalizar se crea una anotación en `anotacion_historia`; en los antecedentes no se repite.
- Fórmula olvidada: se admite con la cita ATENDIDA si se finalizó el mismo día.
- Anular una fórmula cancela una preparación abierta; no se permite si ya se entregó algo.
- El número se asigna al confirmar, con la configuración bloqueada.
- Los permisos de fórmulas y atención se validan antes que el cuerpo de la petición.
- `db/04_horarios_medicos_existentes.sql`: lunes a viernes 8–12 y 14–18 para médicos sin horario.
- PDF con OpenPDF 2.0.4.

## Fase 5: citas, horarios y calendario

- `AgendaService` reúne las reglas de 5.3 y bloquea médico y mascota, en ese orden: dos peticiones simultáneas no ocupan el mismo espacio. Probado en MySQL 8.4 con 8 peticiones a la vez.
- Duración según el servicio (30 minutos por defecto). Segundos de la fecha truncados al minuto.
- Al editar se validan todas las reglas; la de fecha futura, solo si cambia la fecha.
- Cada creación, cambio de estado y reprogramación queda en `cita_historial_estado`. Nada se borra.
- Crear y actualizar cita devuelven además `data`. Cancelar por `/cita/actualizar` exige `motivoCancelacion`.
- Un médico sin horario registrado no puede recibir citas: los médicos que existían antes de la migración necesitan que se les cargue.
- `/cita/historial-mascota` no se limita a las citas del médico autenticado, porque C6 no lo restringe.
- Choques de bloqueo de la base responden 409.
- `scripts/iniciar-dev.ps1` arranca el backend en segundo plano con Mailpit; `-Parar` lo detiene.

## Fase 4: inventario y carga masiva

- `/inventario/**` y `/inventario/carga/**` completos según C6. `ExistenciasService` es el único punto que cambia cantidades: bloquea el producto y deja siempre un movimiento con los saldos.
- Carga: cada fila se importa en su propia transacción y se revalida al confirmar. La confirmación reserva la carga antes de importar, así que dos a la vez no la duplican; si el servidor se cae a mitad, se puede reanudar a los 10 minutos sin repetir filas.
- Un `AJUSTE_POSITIVO` que supere lo ingresado sube también la cantidad inicial del lote.
- `cantidadDisponible` incluye lotes vencidos (5.1); `DISPONIBLE` aparece solo cuando no hay otro estado.
- Los 409 de ajustes y vencimientos también traen `detalles`. Las fechas con hora salen sin fracciones de segundo en todo el API.
- El CSV de errores antepone `'` a textos que Excel ejecutaría como fórmula.
- Librerías: Apache POI 5.5.1 y Commons CSV 1.14.1.

## Desviaciones del contrato

- Campos añadidos: `impuestoId` en producto; `formato`, `usuarioConfirmacion`, `fechaConfirmacion`, y por fila `advertencias`, `importada` y `errorImportacion` en la carga.

- `factura_detalle.despacho_detalle_id` no es `UNIQUE` simple: impediría volver a facturar tras anular. Se permite un solo renglón vigente por entrega.
- `formula_medica` conserva `fecha_actualizacion_registro` en lugar de `fecha_modificacion_registro`.
- `medicoId` se omite del JWT, en vez de ir como `null`, cuando el usuario no es médico.

- El JWT agrega el claim `jti`, que C3 no lista pero la sección 4 exige.
- Los menús del JWT incluyen, además de `code`, `name` y `activo`, los campos que el frontend actual exige (`menuId`, `title`, `url`, `icon`, `fechaCreacion`, `fechaModificacion`). Al migrar el frontend a C3 se pueden quitar.
- C1 dice que `/clinica/test` lleva token; la sección 4 lo declara público. Queda público.
