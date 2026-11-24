# Guion de sustentación

Duración aproximada: 25 a 30 minutos. Cada paso indica el usuario, qué hacer y qué debe verse. Si una pantalla falla, el mismo paso se puede hacer en Swagger con el endpoint indicado.

## 0. Antes de empezar

1. Conecta el portátil a la corriente y evita que se suspenda (en PowerShell):

   ```powershell
   powercfg /change standby-timeout-ac 0
   ```

   No cierres la tapa: al suspenderse Windows, los contenedores se congelan.
2. Si tienes el entorno de desarrollo abierto, detenlo, porque usa los mismos puertos: `powershell -ExecutionPolicy Bypass -File scripts\iniciar-dev.ps1 -Parar`, `docker stop clinica-mailpit` y cierra `ng serve`.
3. Levanta todo con la base limpia:

   ```powershell
   powershell -ExecutionPolicy Bypass -File scripts\levantar.ps1 -DesdeCero
   ```

4. Abre estas pestañas:

   | Qué | Dirección |
   |---|---|
   | Aplicación | http://localhost:4200 |
   | Correo (OTP) | http://localhost:8025 |
   | Swagger | http://localhost:8080/clinica/v1/swagger-ui.html |

5. Usuarios:

   | Usuario | Contraseña | Rol |
   |---|---|---|
   | `admin` | `Admin2026*` | ADMINISTRADOR |
   | `secretaria` | `Secre2026*` | SECRETARIA |
   | `medico1` | `Medico2026*` | MEDICO (Sofía Ramírez) |
   | `medico2` | `Medico2026*` | MEDICO (Andrés López) |
   | `despacho` | `Despacho2026*` | DESPACHO_MEDICO |

**Cómo entrar:** usuario y contraseña → abrir el correo más reciente en Mailpit → escribir el código de 6 caracteres. Para tener varios usuarios a la vez, usa una ventana normal y otra de incógnito.

## 1. Seguridad y menús por perfil (2 min)

| Usuario | Acción | Debe verse |
|---|---|---|
| `despacho` | Entrar | Solo Inventario y Despacho |
| `medico1` | Entrar | Calendario, Atención, Historias, Fórmulas y Mascotas |
| cualquiera | En Swagger, `GET /cita/listar` con el token de `despacho` | 403: el permiso se valida en el backend |
| — | El mismo sin token | 401 |

## 2. Carga masiva de 60 filas (3 min) — usuario `admin`

Archivo: `db/evidencias/carga_inventario_60.xlsx` (también está en `.csv`).

1. Carga de inventario → seleccionar el archivo → previsualizar. **Debe verse: 60 filas, 55 válidas y 5 con error.**

   | Fila | Error |
   |---|---|
   | 11 | Precio de venta negativo |
   | 21 | Código de 7 caracteres |
   | 32 | Medicamento sin registro Invima |
   | 41 | Vencimiento anterior a la compra |
   | 51 | Repite el código y el lote de la fila 50 |

2. Explicar que todavía no se ha escrito nada en inventario.
3. Descargar el informe de errores (fila, campo, valor y motivo).
4. Confirmar. **Debe verse: 55 importadas, estado `CONFIRMADA_CON_ERRORES`, usuario y fecha.**
5. Confirmar otra vez (Swagger: `POST /inventario/carga/{id}/confirmar`). **409: no se importa dos veces.**
6. Inventario → buscar `EVI00001` → movimientos. **Debe verse una `ENTRADA_CARGA_MASIVA`.**

Solo se puede mostrar una vez por base; para repetirlo, vuelve a ejecutar el paso 0.3.

## 3. Crear una cita para hoy (3 min) — usuario `secretaria`

La atención solo se inicia **el día de la cita y desde 30 minutos antes**. Por eso la cita debe ser hoy y empezar dentro de la próxima media hora.

1. **Si hoy es de lunes a viernes entre 8:00–12:00 o 14:00–18:00**, pasa al punto 3.
2. **Si es otra hora o fin de semana**, amplía primero el horario: Médicos → Sofía Ramírez → Horarios → agregar en el día de hoy un bloque que incluya la hora actual (por ejemplo 18:00–22:00) sin cruzarse con los existentes → guardar. Swagger: `PUT /medico/1/horarios` (reemplaza la lista completa).
3. Citas → Crear: mascota **Firulais**, médico **Sofía Ramírez**, servicio **Consulta general**, hora de inicio 5 a 10 minutos después de ahora, motivo «Vómito».
   - La cita dura 30 minutos y debe caber completa en el bloque del horario.
4. Cambiar el estado a **CONFIRMADA**.

**Demostrar las reglas** (todas responden 409 con su mensaje):

| Intento | Mensaje |
|---|---|
| Otra mascota, mismo médico y hora | El médico ya tiene una cita |
| Firulais con Andrés López a la misma hora | La mascota ya tiene una cita |
| Una cita a las 12:30 de un día hábil | Fuera del horario |
| Cancelar sin motivo | El motivo es obligatorio (400) |

## 4. Calendario del médico (2 min) — usuario `medico1`

1. Calendario: día, semana y mes. **Debe verse la cita nueva en azul (CONFIRMADA)** y las de prueba del próximo lunes y martes en otros colores.
2. Abrir el detalle: propietario, mascota, motivo, inicio y fin.
3. Reprogramar una cita del lunes a otra hora libre y luego a las 13:00. **La segunda se rechaza.**
4. Reporte Excel: descargar con rango de fechas. **Debe tener filas coloreadas por estado y resumen con total.**

## 5. Atención médica (3 min) — usuario `medico1`

1. Abrir la cita de hoy → **Iniciar atención**. La cita pasa a EN_ATENCION.
2. Ver antecedentes de la mascota.
3. Intentar finalizar vacío. **Pide síntomas y diagnóstico.**
4. Llenar síntomas, diagnóstico, observaciones y recomendaciones. Servicio: Consulta general.
5. No finalizar todavía: primero la fórmula.

## 6. Fórmula con varios medicamentos y PDF (3 min) — usuario `medico1`

1. Crear fórmula:

   | Medicamento | Dosis | Cada | Días | Cantidad |
   |---|---|---|---|---|
   | Amoxicilina 500 mg | 1 tableta | 8 h | 7 | **90** (la sugerida es 21; se sube para usar dos lotes) |
   | Ivermectina 1 % | 1 ml | 24 h | 1 | 1 (agotada: avisa, pero deja formular) |

2. Confirmar. **Recibe número `FM-000001` y queda PENDIENTE_DESPACHO.** Ya no se puede editar.
3. Inventario de la amoxicilina: **sigue en 180; formular no descuenta.**
4. Descargar el **PDF de la fórmula**.
5. Volver a la atención → **Finalizar**. La cita pasa a ATENDIDA.
6. Descargar la **prefactura PDF** («no válido como factura»).

## 7. Despacho y kardex (4 min) — usuario `despacho`

1. Despacho: la fórmula aparece en la bandeja. Buscarla por «Firulais».
2. **Preparar.** Debe verse la sugerencia FEFO: 80 del lote `AMX-A` (vence antes) y 10 de `AMX-B`. Ivermectina sin existencias.
3. Cambiar la cantidad de `AMX-A` a 81 y confirmar. **Se rechaza, no se descuenta nada y señala el renglón.**
4. Dejar 80 y 10 y **confirmar**. Estado **DESPACHO_PARCIAL**, con Ivermectina sin existencias.
5. Descargar el **comprobante PDF**.
6. Inventario → Amoxicilina → movimientos: **dos salidas con el número de despacho y de fórmula; quedan 90.**
7. Con `admin`: Reportes → **Kardex** → Amoxicilina. Debe coincidir: saldo final 90.
8. Inventario → Metronidazol 250 mg: el lote `MET-A` está vencido y nunca se sugiere.

**Reversión (opcional, `admin`):** revertir el despacho con motivo. Las existencias vuelven a 180, quedan las salidas y las entradas de reversión, y la fórmula vuelve a pendiente. Si lo haces, prepara y confirma otra vez con `despacho` antes de facturar.

## 8. Facturación y pagos (4 min) — usuario `secretaria`

1. Facturación → citas pendientes: aparece la cita, con aviso de despacho pendiente.
2. Previsualizar: consulta y **solo lo despachado** (90 amoxicilinas; la ivermectina no aparece).
3. Descuento del 15 % a la consulta. **Se rechaza: su tope es 10 %.** Poner 10 % con motivo.
4. Generar. **Factura `FV-000001`:**

   | Concepto | Valor |
   |---|---|
   | Consulta 50.000 − 10 % + IVA 19 % | 53.550 |
   | Amoxicilina 90 × 1.500, IVA 0 % | 135.000 |
   | **Total** | **188.550** |

5. Generar otra vez la misma cita. **409: ya tiene factura vigente.**
6. Pago de 100.000 en efectivo → **PAGO PARCIAL**, saldo 88.550. Intentar 90.000 → **supera el saldo**.
7. Pago de 88.550 con tarjeta → **PAGADA**.
8. Descargar la **factura PDF**.

## 9. Tarifas y log (2 min) — usuario `admin`

1. Tarifas → Consulta general → cambiar a 60.000 con motivo. Ver el **historial con usuario y fecha**.
2. Volver a la factura `FV-000001`: **sigue en 50.000; el valor está congelado.**

## 10. Reportes PDF y Excel (3 min) — usuario `admin`

Rango: del primer día del mes a hoy.

| Reporte | Debe verse |
|---|---|
| Ingresos por período (agrupación DIA) | Hoy: facturado 188.550 y recaudado 188.550 |
| Ingresos por veterinario | Sofía Ramírez con los mismos totales |
| Impuestos | IVA 8.550 |
| Descuentos | 5.000, con motivo y usuario |
| Existencias y valor del inventario | Total a costo y a precio de venta |
| Vencidos y próximos a vencer | Metronidazol `MET-A`, Doxiciclina `DOX-A`; Prednisolona `PRE-A` |
| Citas por período | Conteo por estado |

Descargar uno en **PDF** y el mismo en **Excel**; comparar los totales con la vista previa.

## 11. Cierre técnico (2 min)

- Swagger: los endpoints con sus roles; botón **Authorize**.
- `docs/modelo-datos.md`: diagrama y justificación.
- Pruebas: `.\mvnw.cmd test`.
- Concurrencia: dos confirmaciones simultáneas del mismo despacho, factura o cita solo dejan una (pruebas `DespachoTests`, `FacturacionTests` y `CitasTests`).

## Si algo falla

| Problema | Solución |
|---|---|
| No llega el OTP | Mailpit: http://localhost:8025, correo más reciente; cada intento de login genera uno nuevo |
| «Fuera del horario» | Paso 3.2 |
| «La atención se puede iniciar desde 30 minutos antes» | La cita empieza muy tarde; reprográmala |
| Sesión vencida (1 hora) | Volver a entrar |
| Contenedores congelados tras suspender | `docker compose restart` |
| Empezar de nuevo | `scripts\levantar.ps1 -DesdeCero` |
