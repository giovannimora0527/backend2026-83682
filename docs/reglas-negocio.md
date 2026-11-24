# Reglas de negocio

Cada regla indica dónde se valida. Las clases están en `src/main/java/com/uniminuto/clinica`.

## Seguridad

| # | Regla | Dónde se valida |
|---|---|---|
| S1 | Toda ruta exige JWT con sesión activa, salvo las públicas | `SecurityConfig`, `JwtAuthenticationFilter` |
| S2 | Permisos por rol en el backend | `SecurityConfig`, `@PreAuthorize` |
| S3 | Un MEDICO solo opera sobre lo suyo | `UsuarioActual.verificarPropiedad` |
| S4 | Contraseñas en BCrypt; las MD5 se migran al entrar | `AuthServiceImpl.validarPassword` |
| S5 | OTP de un solo uso, 15 minutos y 3 intentos | `AuthServiceImpl.validateOtpToken` |
| S6 | Cerrar sesión o inactivar al usuario invalida su token | `JwtAuthenticationFilter` |
| S7 | El responsable sale del token, nunca del cuerpo | `UsuarioActual` |

## Inventario y carga masiva

| # | Regla | Dónde se valida |
|---|---|---|
| I1 | Código de 8 alfanuméricos y nombre únicos | `InventarioServiceImpl.copiar`, `CHECK` y `UNIQUE` |
| I2 | MEDICAMENTO exige Invima | `InventarioServiceImpl.copiar`, `CHECK` |
| I3 | Editar un producto no cambia cantidades | `InventarioServiceImpl.actualizar` |
| I4 | La cantidad es la suma de los lotes | `InventarioServiceImpl.convertir` |
| I5 | Lote de medicamento con vencimiento posterior a la compra y no vencido | `ExistenciasService.validarLote` |
| I6 | Nunca hay cantidades negativas | `ExistenciasService`, `CHECK` |
| I7 | Cada cambio deja un movimiento con saldos, usuario y fecha | `ExistenciasService` |
| I8 | La previsualización no escribe en inventario | `CargaInventarioServiceImpl.previsualizar` |
| I9 | Encabezados exactos | `LectorArchivo` |
| I10 | Validación por fila, en el archivo y contra la base | `ConvertidorFila`, `ValidadorCarga` |
| I11 | Una fila con error no afecta a las demás | `ImportadorFila` (transacción propia) |
| I12 | Una carga se confirma una sola vez | `CargaInventarioServiceImpl.reservar` |

## Citas y horarios

| # | Regla | Dónde se valida |
|---|---|---|
| C1 | Fecha futura al crear y reprogramar | `AgendaService.reservar` |
| C2 | Médico y mascota activos; cliente dueño de la mascota | `AgendaService.reservar` |
| C3 | Duración según el servicio; no cruza la medianoche | `AgendaService.reservar` |
| C4 | Cabe completa en un bloque del horario | `AgendaService.cabe` |
| C5 | Sin solapamiento de médico ni de mascota, sin contar canceladas ni la propia cita | `CitaRepository.medicoOcupado`, `mascotaOcupada` |
| C6 | Solo se editan PROGRAMADAS y CONFIRMADAS | `CitaServiceImpl.exigirEditable` |
| C7 | Transiciones válidas; EN_ATENCION y ATENDIDA solo desde la atención | `EstadoCita`, `CitaServiceImpl.exigirTransicion` |
| C8 | Cancelar exige motivo y no borra | `CitaServiceImpl.aplicarEstado` |
| C9 | Cada cambio queda en el historial | `HistorialCitaService` |
| C10 | Bloques de horario sin superponerse | `HorarioServiceImpl.validar` |

## Tarifas, atención y fórmulas

| # | Regla | Dónde se valida |
|---|---|---|
| A1 | Una tarifa vigente por servicio; cambiarla exige motivo y deja log | `TarifaServiceImpl`, columna `servicio_vigente` única |
| A2 | Los valores e impuestos se configuran sin tocar código | `/tarifa`, `/impuesto`, `/configuracion-clinica` |
| A3 | Atender el día de la cita, desde 30 minutos antes | `AtencionServiceImpl.iniciar` |
| A4 | Finalizar exige motivo, síntomas, diagnóstico y tarifa | `AtencionServiceImpl.finalizar` |
| A5 | No se finaliza con fórmulas en borrador | `AtencionServiceImpl.finalizar` |
| A6 | Formular solo durante la atención o el mismo día | `FormulaServiceImpl.atencionParaFormular` |
| A7 | Cantidad sugerida = techo(dosis × 24 / frecuencia × días) | `FormulaRs.sugerida` |
| A8 | Una fórmula confirmada no se edita | `FormulaServiceImpl.exigirBorrador` |
| A9 | Formular no descuenta inventario | `FormulaServiceImpl` no usa `ExistenciasService` |
| A10 | No se anula una fórmula con entregas | `FormulaServiceImpl.anular` |

## Despacho

| # | Regla | Dónde se valida |
|---|---|---|
| D1 | Solo DESPACHO_MEDICO prepara y confirma | `SecurityConfig`, `DespachoApiController` |
| D2 | Solo el médico crea y confirma fórmulas | `FormulaMedicaApiController`, `FormulaServiceImpl` |
| D3 | Despacho no modifica indicaciones, dosis ni cantidades prescritas | Ningún endpoint de despacho escribe esos campos |
| D4 | El inventario solo se descuenta al confirmar | `DespachoServiceImpl.confirmar` |
| D5 | No se dispensa un lote vencido | `DespachoServiceImpl.validar` |
| D6 | No se entrega más de lo disponible ni de lo formulado | `DespachoServiceImpl.validar`, `CHECK` de `lote_medicamento` y `formula_medica_detalle` |
| D7 | Se permiten entregas parciales | `DespachoServiceImpl.calcularEstado` |
| D8 | Cada salida queda ligada a un despacho y una fórmula | `ExistenciasService.despachar`, `CHECK` de `movimiento_inventario` |
| D9 | No se descuenta dos veces la misma dispensación | Bloqueo y estado en `confirmar`; `idempotency_key` única; una sola preparación por fórmula en la base |
| D10 | Toda operación guarda usuario, fecha y hora | Usuario del token en `despacho`, movimientos e historial |
| D11 | Lo confirmado no se elimina; se revierte con motivo y una sola vez | `DespachoServiceImpl.revertir`; `movimiento_revertido_id` único |
| D12 | Permisos validados en el backend | `SecurityConfig`, `@PreAuthorize` |
| D13 | No se revierte lo facturado | `DespachoServiceImpl.revertir` |
| D14 | Con entregas, solo ADMINISTRADOR cancela | `DespachoServiceImpl.cancelar` |

### Estado de la fórmula después de confirmar, liberar o revertir

| Pendiente | ¿Se ha entregado algo? | ¿Hay existencias vigentes de lo pendiente? | Estado |
|---|---|---|---|
| Nada | Sí | No aplica | `DESPACHADO` |
| Algo | Sí | Sí o no | `DESPACHO_PARCIAL`; `sinExistencias` nombra lo agotado |
| Algo | No | No | `PENDIENTE_DISPONIBILIDAD` |
| Algo | No | Sí (solo tras liberar o revertir) | `PENDIENTE_DESPACHO` |

Al revertir se conserva `CANCELADO` o `EN_PREPARACION` si la fórmula estaba así.

## Facturación y pagos

| # | Regla | Dónde se valida |
|---|---|---|
| F1 | Solo se facturan citas ATENDIDAS con atención finalizada | `FacturaServiceImpl.exigirFacturable` |
| F2 | Una sola factura vigente por cita | `exigirFacturable`; `factura.cita_vigente` única |
| F3 | Un servicio no se cobra dos veces en una atención | `atencion_servicio` única por atención y servicio |
| F4 | Solo se cobra lo dispensado, no revertido ni facturado | `DespachoDetalleRepository.porFacturar`; `despacho_detalle_vigente` única |
| F5 | Los valores se congelan | `FacturaServiceImpl.guardar`, `DespachoServiceImpl.entregar` |
| F6 | IVA según la configuración, sobre la base con descuento, redondeo HALF_UP | `RenglonCobro` |
| F7 | Descuento con motivo; SECRETARIA hasta el tope | `FacturaServiceImpl.validarDescuentos` |
| F8 | El pago es mayor que 0 y no supera el saldo | `FacturaServiceImpl.pagar`, `CHECK` de `factura` y `pago` |
| F9 | PENDIENTE, PAGO_PARCIAL y PAGADA según el saldo | `FacturaServiceImpl.pagar` |
| F10 | Anular es del ADMINISTRADOR, con motivo, y no borra nada | `SecurityConfig`, `FacturaServiceImpl.anular` |
| F11 | Cada cambio guarda usuario y fecha | `factura_historial_estado` |

## Reportes

| # | Regla | Dónde se valida |
|---|---|---|
| R1 | Acceso por rol en cada reporte | `SecurityConfig`, `ReporteServiceImpl.generar` |
| R2 | Filtros obligatorios, valores permitidos y `desde <= hasta` | `Filtros` |
| R3 | Facturado: facturas no anuladas por fecha de generación | `ReportesFinancieros` |
| R4 | Recaudado: pagos no anulados por fecha de pago | `ReportesFinancieros` |
| R5 | Inventario desde lotes y movimientos | `ReportesInventario` |
