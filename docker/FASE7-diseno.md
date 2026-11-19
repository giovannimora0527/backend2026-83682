# Reportes de la Clínica Veterinaria UNIMINUTO

## Matriz aprobada

| Perfil | Financiero | Inventario general | Gestión médica general | Resumen operativo de citas |
|---|---|---|---|---|
| Administración | Consulta, PDF y Excel | Consulta, PDF y Excel | Consulta, PDF y Excel | Consulta, PDF y Excel |
| Recepción | — | — | — | Consulta, PDF y Excel |
| Médico | — | — | — | Exportación de agenda propia por la ruta existente |
| Despacho Médico | — | — | — | — |

El resumen operativo de Recepción agrupa por fecha, estado y profesional. Devuelve
conteos y admite filtros de fecha, estado, profesional y especialidad. Omite
identificadores y nombres de pacientes o propietarios y cualquier detalle clínico
o financiero. El menú Reportes se asignará únicamente a Administración y
Recepción. Spring verificará cada consulta y sus exportaciones por tipo de reporte.

## Fuentes y criterios de conciliación

- **Facturación emitida.** Agrupar facturas válidas por fecha de emisión. Excluir
  anuladas de los importes y mostrar su número y valor nominal por separado.
  Usar los importes históricos de factura y detalle. Por médico y especialidad,
  usar la relación clínica del emisor y la especialidad guardada en la factura.
  Los subtotales por servicio y medicamento se calculan por línea persistida.
- **Recaudación efectiva.** Agrupar pagos registrados por fecha del pago, aunque
  la factura tenga otra fecha de emisión. Excluir facturas anuladas. Los pagos
  no se unen directamente con cada detalle. Los impuestos y descuentos se
  presentan como importes registrados de facturas, pues el pago de la Fase 6
  no distribuye el abono entre impuestos ni conceptos.
- **Cuentas por cobrar.** Mostrar saldo al cierre del período de las facturas
  emitidas dentro de ese período. Una factura emitida es vigente al cierre si
  todavía no había sido anulada. El saldo resta los pagos hasta el cierre,
  incluidos los pagos anteriores al final que correspondan a la factura.
  Los estados al cierre se derivan de esos pagos y del historial de anulación.
- **Inventario.** Las existencias y su valoración corresponden al momento de
  consulta en Bogotá. Los filtros de fecha aplican al libro de movimientos,
  a la dispensación y a los rangos de vencimiento, según el reporte. Existencia
  física es la suma de saldos, incluidos los vencidos. Disponible excluye los
  vencidos y los productos inactivos. El costo usa cantidad por costo del lote,
  y el potencial de venta usa cantidad disponible por precio vigente.
  Mostrar dispensación bruta y reversión en sus propias fechas y calcular la
  cantidad neta del período. Mantener la trazabilidad con los movimientos.
- **Gestión médica.** Contar citas por inicio programado y normalizar el estado
  anterior programada como pendiente. Una consulta efectivamente atendida
  corresponde a una cita atendida o finalizada. Contar atenciones finalizadas
  por fecha de finalización y mascotas atendidas de forma distinta por el
  conjunto filtrado. Conservar el grano de una fila por cita al consultar
  fórmulas o entregas mediante sus relaciones de origen.

Los rangos usan inicio inclusivo y medianoche siguiente al final exclusivo,
con zona America/Bogota. Spring calcula totales sobre el conjunto filtrado y
usa BigDecimal para COP. Vista previa, PDF y XLSX comparten el mismo contrato,
criterios y consultas. Cada consulta refleja una lectura consistente de MySQL.
Si hay cambios entre consultas, una exportación nueva puede reflejarlos. El
resumen y la página visible son independientes. Se reutilizarán PDFBox y Apache
POI, con celdas monetarias numéricas y fechas en Excel.

## Migración y verificación

V007 registra el menú y las dos relaciones de permisos aprobadas. El esquema
actual tiene las tablas fuente necesarias. Se medirá el costo de consultas
antes de considerar índices adicionales. Se verificará la migración y la
conciliación contra SQL independiente en una copia desechable de V006.
El despliegue activo requiere autorización expresa tras revisar las pruebas.

## API implementada

Todas las rutas son GET bajo /clinica/v1/reportes. /{tipo} devuelve título,
filtros aplicados, columnas, totales, filas y paginación. /{tipo}/pdf y
/{tipo}/excel descargan el conjunto filtrado completo. Los tipos aceptados son
FINANCIERO, FACTURAS, PAGOS, POR_MEDICO, POR_ESPECIALIDAD, CONCEPTOS,
EXISTENCIAS, LOTES, MOVIMIENTOS, DISPENSACIONES, CITAS, CONSULTAS, ATENCIONES
y CITAS_OPERATIVAS. Recepción solo accede a CITAS_OPERATIVAS.

Los parámetros desde y hasta son fechas ISO. EXISTENCIAS toma el corte de
Bogotá al generar. LOTES acepta esas fechas opcionales para el vencimiento.
FINANCIERO acepta agrupacion DIA, MES o ANIO. Los filtros por ID son medico,
especialidad, medicamento, lote, mascota y propietario según el reporte.
El parámetro estado admite únicamente los estados propios del reporte.
pagina empieza en cero. Cada página contiene hasta 50 filas y conserva el
total del filtro. Si una consulta supera 10.000 filas, acota el rango o usa
filtros. Excel conserva celdas numéricas y rechaza cifras que excedan sus
15 dígitos de precisión. En ese caso se puede exportar PDF.

Los importes con IVA y otros impuestos son valores registrados. La
recaudación por fecha y los saldos al cierre se calculan por separado.
