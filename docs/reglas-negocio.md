# Reglas de negocio

Se completa con los demás módulos en la fase 10.

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
