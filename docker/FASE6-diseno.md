# Facturación y pagos de la clínica

## Matriz propuesta para aprobación

| Operación | Administración | Recepción | Médico | Despacho Médico |
|---|---|---|---|---|
| Consultar elegibles, facturas, pagos e historiales y descargar PDF | Sí | Sí | — | — |
| Emitir factura | Sí | Sí, sin descuento | — | — |
| Registrar abono o pago total | Sí | Sí | — | — |
| Autorizar descuento al emitir | Sí, con motivo | — | — | — |
| Anular una factura sin pagos | Sí, con motivo | — | — | — |

El menú se vinculará a Administración y Recepción por el mecanismo existente.
Spring aplicará permisos por operación y el guard de Angular usará el menú real.

## Modelo de facturación

La atención debe estar finalizada y su cita en estado finalizada. La primera
factura incluye todos los servicios activos aún sin cobrar y las entregas
confirmadas y vigentes al emitir. La siguiente factura incluye las entregas
posteriores aún sin cobrar. Una atención puede tener varias facturas. Se cobra
por línea de despacho, que identifica el lote y la salida y conserva la
referencia a la revisión de la fórmula. Una prescripción sin entrega queda
fuera del cobro.

Las facturas guardan las identificaciones visibles, conceptos, cantidades,
importes e impuestos en el momento de emitir. Los servicios conservan el
precio y las tasas aplicadas en la atención. Los medicamentos usan el precio
unitario guardado al confirmar la fórmula, aplicado solo a lo entregado.
En ausencia de configuración tributaria para medicamentos, no se incorpora
un impuesto al cálculo académico. El PDF identifica el documento como
factura interna del proyecto académico.

La moneda es COP. Se usan BigDecimal y dos decimales. Un descuento autorizado
se expresa como porcentaje de dos decimales y se calcula por línea antes de
impuestos. Se redondean descuento, IVA y otros impuestos por línea con
HALF_UP a dos decimales. La cabecera suma las líneas ya redondeadas. Se admite
un total cero, que nace PAGADA con importe pagado cero.

## Consistencia y auditoría

La emisión se serializa por atención, con llave UUID única y verificación del
contenido de una petición repetida. La previsualización identifica la versión
de los conceptos facturables para impedir una emisión con un total distinto del
revisado. Un servicio activo y una línea de despacho activa tienen una sola
asignación facturada vigente cada uno, protegida por MySQL.

El pago bloquea la factura. Su UUID es único por factura y un reintento con
el mismo importe, método y referencia devuelve el pago original. Un importe
superior al saldo, un saldo desactualizado o la reutilización de una llave con
otro contenido generan conflicto. Los estados cambian de PENDIENTE_PAGO a
PAGO_PARCIAL y PAGADA según el saldo y se registra el actor de cada transición.

Administración puede anular una factura sin pagos y registra motivo, fecha y
usuario. Se conservan el número, las líneas y el historial. La anulación libera
los conceptos para una nueva emisión. Una entrega con facturación vigente
queda bloqueada para reversión. La emisión y la reversión toman un bloqueo
común de la entrega para evitar la carrera. Una devolución de dinero requeriría
un procedimiento financiero adicional aprobado.

## Despliegue

Se revisará el SQL exacto y se comprobará un respaldo restaurable antes de
solicitar autorización para V006 en el MySQL activo. Las pruebas con escrituras
se ejecutarán en Docker desechable.
