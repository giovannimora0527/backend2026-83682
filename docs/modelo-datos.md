# Modelo de datos

MySQL 8, InnoDB, `utf8mb4`. Lo crean, en orden, `db/01_esquema_base.sql`, `db/02_migracion_examen_final.sql` y `db/03_datos_iniciales.sql`. Las tablas de este documento se obtuvieron del esquema ya creado. El diagrama suelto está en `modelo-datos.mmd`.

## Diagrama entidad-relación

```mermaid
erDiagram
    cita |o--o{ anotacion_historia : "cita_id"
    historia_medica ||--o{ anotacion_historia : "historia_id"
    medico ||--o{ anotacion_historia : "medico_id"
    cita ||--o| atencion_medica : "cita_id"
    historia_medica ||--o{ atencion_medica : "historia_medica_id"
    mascota ||--o{ atencion_medica : "mascota_id"
    medico ||--o{ atencion_medica : "medico_id"
    atencion_medica ||--o{ atencion_servicio : "atencion_id"
    servicio ||--o{ atencion_servicio : "servicio_id"
    usuario |o--o{ carga_inventario : "usuario_confirmacion_id"
    usuario ||--o{ carga_inventario : "usuario_id"
    carga_inventario ||--o{ carga_inventario_fila : "carga_id"
    lote_medicamento |o--o{ carga_inventario_fila : "lote_id"
    medicamento |o--o{ carga_inventario_fila : "medicamento_id"
    cliente ||--o{ cita : "cliente_id"
    mascota ||--o{ cita : "mascota_id"
    medico ||--o{ cita : "medico_id"
    servicio |o--o{ cita : "servicio_id"
    usuario |o--o{ cita : "usuario_creacion_id"
    cita ||--o{ cita_historial_estado : "cita_id"
    usuario ||--o{ cita_historial_estado : "usuario_id"
    formula_medica ||--o{ despacho : "formula_id"
    usuario ||--o{ despacho : "usuario_id"
    usuario |o--o{ despacho : "usuario_reversion_id"
    despacho ||--o{ despacho_detalle : "despacho_id"
    formula_medica_detalle ||--o{ despacho_detalle : "formula_detalle_id"
    lote_medicamento ||--o{ despacho_detalle : "lote_id"
    medicamento ||--o{ despacho_detalle : "medicamento_id"
    atencion_medica ||--o{ factura : "atencion_id"
    cita ||--o{ factura : "cita_id"
    cliente ||--o{ factura : "cliente_id"
    mascota ||--o{ factura : "mascota_id"
    medico ||--o{ factura : "medico_id"
    usuario |o--o{ factura : "usuario_anulacion_id"
    usuario ||--o{ factura : "usuario_id"
    despacho_detalle |o--o{ factura_detalle : "despacho_detalle_id"
    factura ||--o{ factura_detalle : "factura_id"
    medicamento |o--o{ factura_detalle : "medicamento_id"
    servicio |o--o{ factura_detalle : "servicio_id"
    tarifa |o--o{ factura_detalle : "tarifa_id"
    factura ||--o{ factura_historial_estado : "factura_id"
    usuario ||--o{ factura_historial_estado : "usuario_id"
    formula_medica ||--o{ formula_historial_estado : "formula_id"
    usuario ||--o{ formula_historial_estado : "usuario_id"
    atencion_medica |o--o{ formula_medica : "atencion_id"
    cita ||--o{ formula_medica : "cita_id"
    cliente ||--o{ formula_medica : "cliente_id"
    mascota ||--o{ formula_medica : "mascota_id"
    medicamento |o--o{ formula_medica : "medicamento_id"
    medico ||--o{ formula_medica : "medico_id"
    formula_medica ||--o{ formula_medica_detalle : "formula_id"
    medicamento ||--o{ formula_medica_detalle : "medicamento_id"
    mascota ||--o{ historia_medica : "paciente_id"
    medico ||--o{ horario_medico : "medico_id"
    medicamento ||--o{ lote_medicamento : "medicamento_id"
    cliente ||--o{ mascota : "cliente_id"
    raza ||--o{ mascota : "raza_id"
    impuesto |o--o{ medicamento : "impuesto_id"
    especializacion ||--o{ medico : "especializacion_id"
    usuario |o--o| medico : "usuario_id"
    carga_inventario |o--o{ movimiento_inventario : "carga_id"
    despacho_detalle |o--o{ movimiento_inventario : "despacho_detalle_id"
    despacho |o--o{ movimiento_inventario : "despacho_id"
    formula_medica |o--o{ movimiento_inventario : "formula_id"
    lote_medicamento ||--o{ movimiento_inventario : "lote_id"
    medicamento ||--o{ movimiento_inventario : "medicamento_id"
    movimiento_inventario |o--o| movimiento_inventario : "movimiento_revertido_id"
    usuario ||--o{ movimiento_inventario : "usuario_id"
    usuario ||--o{ otp_token : "usuario_id"
    factura ||--o{ pago : "factura_id"
    usuario ||--o{ pago : "usuario_id"
    menu ||--o{ perfil_menu : "menu_id"
    perfil ||--o{ perfil_menu : "perfil_id"
    especializacion |o--o{ servicio : "especializacion_id"
    impuesto ||--o{ tarifa : "impuesto_id"
    servicio ||--o{ tarifa : "servicio_id"
    servicio ||--o{ tarifa_historial : "servicio_id"
    tarifa ||--o{ tarifa_historial : "tarifa_id"
    usuario ||--o{ tarifa_historial : "usuario_id"
    perfil |o--o{ usuario : "perfil_id"
    anotacion_historia {
        bigint_unsigned id PK
        bigint_unsigned historia_id FK
        bigint_unsigned medico_id FK
        datetime fecha
        text descripcion
        bigint_unsigned cita_id FK
        text diagnostico
        text observaciones
    }
    atencion_medica {
        bigint_unsigned id PK
        bigint_unsigned cita_id FK
        bigint_unsigned historia_medica_id FK
        bigint_unsigned medico_id FK
        int mascota_id FK
        text motivo_consulta
        text sintomas
        text diagnostico
        text observaciones
        text recomendaciones
        datetime fecha_inicio
        datetime fecha_fin
        tinyint finalizada
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    atencion_servicio {
        bigint_unsigned id PK
        bigint_unsigned atencion_id FK
        bigint_unsigned servicio_id FK
        int cantidad
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    carga_inventario {
        bigint_unsigned id PK
        varchar nombre_archivo
        varchar formato
        bigint_unsigned usuario_id FK
        datetime fecha_carga
        bigint_unsigned usuario_confirmacion_id FK
        datetime fecha_confirmacion
        varchar estado
        int total_filas
        int filas_validas
        int filas_con_error
        int filas_importadas
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    carga_inventario_fila {
        bigint_unsigned id PK
        bigint_unsigned carga_id FK
        int numero_fila
        json datos_json
        tinyint valida
        varchar accion
        json errores_json
        tinyint importada
        varchar error_importacion
        bigint_unsigned medicamento_id FK
        bigint_unsigned lote_id FK
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    cita {
        bigint_unsigned id PK
        bigint_unsigned cliente_id FK
        int mascota_id FK
        bigint_unsigned medico_id FK
        datetime fecha_inicio
        varchar estado
        text motivo
        datetime fecha_fin
        bigint_unsigned servicio_id FK
        varchar motivo_cancelacion
        bigint_unsigned usuario_creacion_id FK
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    cita_historial_estado {
        bigint_unsigned id PK
        bigint_unsigned cita_id FK
        varchar estado_anterior
        varchar estado_nuevo
        datetime fecha_inicio_anterior
        datetime fecha_inicio_nueva
        varchar motivo
        bigint_unsigned usuario_id FK
        datetime fecha
    }
    cliente {
        bigint_unsigned cliente_id PK
        int usuario_id UK
        varchar tipo_documento
        varchar numero_documento UK
        varchar nombres
        varchar apellidos
        date fecha_nacimiento
        varchar genero
        varchar telefono
        text direccion
        tinyint activo
        varchar correo
    }
    configuracion_clinica {
        bigint_unsigned id PK
        varchar nombre
        varchar nit
        varchar direccion
        varchar telefono
        varchar correo
        varchar prefijo_factura
        bigint consecutivo_factura
        varchar prefijo_formula
        bigint consecutivo_formula
        varchar prefijo_despacho
        bigint consecutivo_despacho
        decimal tope_descuento_secretaria
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
        tinyint unica UK
    }
    despacho {
        bigint_unsigned id PK
        bigint_unsigned formula_id FK
        varchar numero UK
        bigint_unsigned usuario_id FK
        varchar estado
        datetime fecha_inicio
        datetime fecha_confirmacion
        varchar observacion
        varchar idempotency_key UK
        datetime fecha_reversion
        bigint_unsigned usuario_reversion_id FK
        varchar motivo_reversion
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
        bigint_unsigned formula_en_preparacion UK
    }
    despacho_detalle {
        bigint_unsigned id PK
        bigint_unsigned despacho_id FK
        bigint_unsigned formula_detalle_id FK
        bigint_unsigned medicamento_id FK
        bigint_unsigned lote_id FK
        int cantidad_entregada
        decimal precio_venta_unitario
        decimal porcentaje_iva
        tinyint facturado
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    especializacion {
        bigint_unsigned id PK
        varchar nombre UK
        text descripcion
        varchar codigo_especializacion UK
        tinyint activo
    }
    factura {
        bigint_unsigned id PK
        varchar numero UK
        bigint_unsigned cita_id FK
        bigint_unsigned atencion_id FK
        bigint_unsigned cliente_id FK
        int mascota_id FK
        bigint_unsigned medico_id FK
        varchar cliente_nombre
        varchar cliente_documento
        varchar mascota_nombre
        varchar medico_nombre
        varchar especialidad_nombre
        datetime fecha_generacion
        decimal subtotal
        decimal total_descuento
        decimal total_impuestos
        decimal total
        decimal total_pagado
        decimal saldo
        varchar estado
        varchar observacion
        bigint_unsigned usuario_id FK
        datetime fecha_anulacion
        bigint_unsigned usuario_anulacion_id FK
        varchar motivo_anulacion
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
        bigint_unsigned cita_vigente UK
    }
    factura_detalle {
        bigint_unsigned id PK
        bigint_unsigned factura_id FK
        varchar tipo_item
        bigint_unsigned servicio_id FK
        bigint_unsigned tarifa_id FK
        bigint_unsigned medicamento_id FK
        bigint_unsigned despacho_detalle_id FK
        varchar descripcion
        int cantidad
        decimal precio_unitario
        decimal porcentaje_descuento
        decimal valor_descuento
        varchar motivo_descuento
        decimal porcentaje_iva
        decimal valor_iva
        decimal subtotal
        decimal total
        tinyint vigente
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
        bigint_unsigned despacho_detalle_vigente UK
    }
    factura_historial_estado {
        bigint_unsigned id PK
        bigint_unsigned factura_id FK
        varchar estado_anterior
        varchar estado_nuevo
        varchar motivo
        bigint_unsigned usuario_id FK
        datetime fecha
    }
    formula_historial_estado {
        bigint_unsigned id PK
        bigint_unsigned formula_id FK
        varchar estado_despacho_anterior
        varchar estado_despacho_nuevo
        varchar estado_formula_anterior
        varchar estado_formula_nuevo
        varchar motivo
        bigint_unsigned usuario_id FK
        datetime fecha
    }
    formula_medica {
        bigint_unsigned id PK
        bigint_unsigned cita_id FK
        bigint_unsigned medicamento_id FK
        text dosis
        text indicaciones
        datetime fecha_creacion_registro
        datetime fecha_actualizacion_registro
        varchar frecuencia
        varchar duracion
        varchar numero UK
        bigint_unsigned atencion_id FK
        bigint_unsigned medico_id FK
        int mascota_id FK
        bigint_unsigned cliente_id FK
        datetime fecha_generacion
        text indicaciones_generales
        varchar estado_formula
        varchar estado_despacho
        datetime fecha_confirmacion
        varchar motivo_anulacion
        int version
    }
    formula_medica_detalle {
        bigint_unsigned id PK
        bigint_unsigned formula_id FK
        bigint_unsigned medicamento_id FK
        decimal dosis
        varchar unidad_dosis
        int frecuencia_horas
        int duracion_dias
        varchar via_administracion
        int cantidad_prescrita
        int cantidad_despachada
        text indicaciones
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    historia_medica {
        bigint_unsigned id PK
        int paciente_id FK
        datetime fecha_creacion
    }
    horario_medico {
        bigint_unsigned id PK
        bigint_unsigned medico_id FK
        tinyint dia_semana
        time hora_inicio
        time hora_fin
        tinyint activo
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    impuesto {
        bigint_unsigned id PK
        varchar nombre UK
        decimal porcentaje
        tinyint activo
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    lote_medicamento {
        bigint_unsigned id PK
        bigint_unsigned medicamento_id FK
        varchar numero_lote
        decimal precio_compra
        int cantidad_inicial
        int cantidad_disponible
        date fecha_compra
        date fecha_ingreso
        date fecha_vencimiento
        tinyint activo
        int version
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    mascota {
        int mascota_id PK
        varchar nombre_mascota
        int edad
        int raza_id FK
        datetime fecha_registro
        datetime fecha_modificacion
        bigint_unsigned cliente_id FK
        tinyint activo
    }
    medicamento {
        bigint_unsigned id PK
        varchar nombre UK
        text descripcion
        varchar presentacion
        date fecha_compra
        date fecha_vence
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
        char codigo UK
        varchar tipo_producto
        tinyint requiere_invima
        varchar registro_invima
        varchar unidad_medida
        decimal precio_venta
        bigint_unsigned impuesto_id FK
        int stock_minimo
        date fecha_proximo_pedido
        tinyint activo
        int version
    }
    medico {
        bigint_unsigned id PK
        varchar tipo_documento
        varchar numero_documento UK
        varchar nombres
        varchar apellidos
        varchar telefono
        varchar registro_profesional UK
        bigint_unsigned especializacion_id FK
        tinyint estado
        bigint_unsigned usuario_id FK
    }
    menu {
        int menu_id PK
        varchar code UK
        varchar title
        longtext url
        varchar icon
        tinyint activo
        datetime fecha_creacion
        datetime fecha_modificacion
    }
    movimiento_inventario {
        bigint_unsigned id PK
        datetime fecha
        varchar tipo
        bigint_unsigned medicamento_id FK
        bigint_unsigned lote_id FK
        int cantidad
        int saldo_lote_resultante
        int saldo_producto_resultante
        decimal costo_unitario
        bigint_unsigned usuario_id FK
        bigint_unsigned formula_id FK
        bigint_unsigned despacho_id FK
        bigint_unsigned despacho_detalle_id FK
        bigint_unsigned carga_id FK
        bigint_unsigned movimiento_revertido_id FK
        varchar observacion
    }
    otp_token {
        bigint_unsigned id PK
        bigint_unsigned usuario_id FK
        varchar codigo
        datetime fecha_creacion
        datetime fecha_expiracion
        int intentos
        tinyint usado
    }
    pago {
        bigint_unsigned id PK
        bigint_unsigned factura_id FK
        datetime fecha
        decimal valor
        varchar medio_pago
        varchar referencia
        bigint_unsigned usuario_id FK
        tinyint anulado
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    perfil {
        int perfil_id PK
        varchar nombre
        tinyint activo
        datetime fecha_creacion
        datetime fecha_modificacion
        varchar codigo UK
    }
    perfil_menu {
        int perfil_menu_id PK
        int perfil_id FK
        int menu_id FK
    }
    raza {
        int raza_id PK
        varchar nombre
        varchar especie
        datetime fecha_creacion
        datetime fecha_modificacion
    }
    servicio {
        bigint_unsigned id PK
        varchar codigo UK
        varchar nombre
        text descripcion
        varchar tipo_servicio
        bigint_unsigned especializacion_id FK
        int duracion_minutos
        tinyint activo
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
    }
    session {
        int session_id PK
        bigint_unsigned user_id
        text token
        datetime fecha_ini_sesion
        datetime fecha_expiracion
        varchar jti UK
        tinyint activa
        datetime fecha_cierre
    }
    tarifa {
        bigint_unsigned id PK
        bigint_unsigned servicio_id FK
        decimal valor
        bigint_unsigned impuesto_id FK
        date vigente_desde
        date vigente_hasta
        tinyint activa
        datetime fecha_creacion_registro
        datetime fecha_modificacion_registro
        bigint_unsigned servicio_vigente UK
    }
    tarifa_historial {
        bigint_unsigned id PK
        bigint_unsigned tarifa_id FK
        bigint_unsigned servicio_id FK
        decimal valor_anterior
        decimal valor_nuevo
        decimal porcentaje_iva_anterior
        decimal porcentaje_iva_nuevo
        varchar motivo
        bigint_unsigned usuario_id FK
        datetime fecha
    }
    usuario {
        bigint_unsigned id PK
        varchar username UK
        text password_hash
        varchar rol
        datetime fecha_creacion
        tinyint activo
        varchar email
        int perfil_id FK
    }
```

## Justificación de los cambios

- **Lotes separados del producto.** Un medicamento puede tener varias compras con costo y vencimiento distintos; sin lotes no se puede aplicar FEFO ni bloquear vencidos.
- **La cantidad se calcula.** Es la suma de `lote_medicamento.cantidad_disponible`. Guardarla también en `medicamento` permitiría que ambas difieran.
- **Fórmula con encabezado y detalle.** Antes cada fila era un solo medicamento; ahora una fórmula admite varios y cada uno lleva su cantidad prescrita y despachada.
- **Valores congelados.** Factura y despacho copian precios, IVA y nombres: cambiar una tarifa o un cliente no altera lo ya emitido.
- **Columnas generadas con `UNIQUE`.** MySQL no tiene índices parciales. Una columna que vale `NULL` cuando la fila no aplica hace que la propia base impida: dos facturas vigentes de una cita (`factura.cita_vigente`), dos tarifas activas de un servicio (`tarifa.servicio_vigente`), dos despachos en preparación de una fórmula (`despacho.formula_en_preparacion`) y facturar dos veces la misma entrega (`factura_detalle.despacho_detalle_vigente`).
- **Logs solo de inserción.** Movimientos e historiales no tienen fecha de modificación y sus entidades son `@Immutable`. Una corrección es otro registro, por eso la trazabilidad no se pierde.
- **Reversión única.** `movimiento_revertido_id` es único: un movimiento no se puede revertir dos veces.
- **`ON DELETE RESTRICT` en todas las claves foráneas.** Nada con historia se puede borrar.
- **`version` y bloqueos.** Protegen de modificaciones simultáneas en lotes, fórmulas, despachos, citas y facturas.
- **`perfil.codigo`.** El rol del contrato no depende del nombre visible del perfil.
- **`session.jti`.** El JWT con menús no cabía en 500 caracteres; guardar su identificador basta para invalidarlo y evita almacenar credenciales.

## Decisiones de migración

- `usuario.rol` (texto) pasa a `usuario.perfil_id`; `rol` queda como histórico.
- `cita.fecha_hora` se renombra `fecha_inicio`; `fecha_fin` se calcula con 30 minutos. El JSON mantiene `fechaHora` para los consumidores actuales.
- `cita.motivo` sigue siendo `TEXT`: pasarlo a 255 caracteres podría recortar datos.
- Citas con mascota inexistente: se crea una mascota inactiva con ese id.
- Medicamentos existentes: código `MED` + id, Invima `PENDIENTE-<id>` y un lote `MIGRADO-<id>` con cantidad 0. El administrador debe completar Invima, precio y existencias.
- Fórmulas antiguas: un encabezado y un detalle por fila. La pauta era texto libre, así que se conserva completa en `indicaciones`; dosis, frecuencia, duración y cantidad quedan en 1 (24 horas para la frecuencia) si no se pueden deducir.
- `medicamento.fecha_compra` y `fecha_vence`, y las columnas antiguas de `formula_medica`, se conservan como históricas.
- `cliente.usuario_id` no tiene clave foránea: la base existente contiene ids sin usuario.
- `medico.estado` cumple la función de `activo`.
- Tarifas: al cambiar un valor se cierra la vigente y se crea otra.
- Al finalizar una atención se crea una anotación en `anotacion_historia`, para que el módulo de historias la muestre.

## Relaciones

| Tabla | Columna | Referencia | Cardinalidad |
|---|---|---|---|
| `anotacion_historia` | `cita_id` | `cita.id` | cero o uno a muchos |
| `anotacion_historia` | `historia_id` | `historia_medica.id` | uno a muchos |
| `anotacion_historia` | `medico_id` | `medico.id` | uno a muchos |
| `atencion_medica` | `cita_id` | `cita.id` | uno a cero o uno |
| `atencion_medica` | `historia_medica_id` | `historia_medica.id` | uno a muchos |
| `atencion_medica` | `mascota_id` | `mascota.mascota_id` | uno a muchos |
| `atencion_medica` | `medico_id` | `medico.id` | uno a muchos |
| `atencion_servicio` | `atencion_id` | `atencion_medica.id` | uno a muchos |
| `atencion_servicio` | `servicio_id` | `servicio.id` | uno a muchos |
| `carga_inventario` | `usuario_confirmacion_id` | `usuario.id` | cero o uno a muchos |
| `carga_inventario` | `usuario_id` | `usuario.id` | uno a muchos |
| `carga_inventario_fila` | `carga_id` | `carga_inventario.id` | uno a muchos |
| `carga_inventario_fila` | `lote_id` | `lote_medicamento.id` | cero o uno a muchos |
| `carga_inventario_fila` | `medicamento_id` | `medicamento.id` | cero o uno a muchos |
| `cita` | `cliente_id` | `cliente.cliente_id` | uno a muchos |
| `cita` | `mascota_id` | `mascota.mascota_id` | uno a muchos |
| `cita` | `medico_id` | `medico.id` | uno a muchos |
| `cita` | `servicio_id` | `servicio.id` | cero o uno a muchos |
| `cita` | `usuario_creacion_id` | `usuario.id` | cero o uno a muchos |
| `cita_historial_estado` | `cita_id` | `cita.id` | uno a muchos |
| `cita_historial_estado` | `usuario_id` | `usuario.id` | uno a muchos |
| `despacho` | `formula_id` | `formula_medica.id` | uno a muchos |
| `despacho` | `usuario_id` | `usuario.id` | uno a muchos |
| `despacho` | `usuario_reversion_id` | `usuario.id` | cero o uno a muchos |
| `despacho_detalle` | `despacho_id` | `despacho.id` | uno a muchos |
| `despacho_detalle` | `formula_detalle_id` | `formula_medica_detalle.id` | uno a muchos |
| `despacho_detalle` | `lote_id` | `lote_medicamento.id` | uno a muchos |
| `despacho_detalle` | `medicamento_id` | `medicamento.id` | uno a muchos |
| `factura` | `atencion_id` | `atencion_medica.id` | uno a muchos |
| `factura` | `cita_id` | `cita.id` | uno a muchos |
| `factura` | `cliente_id` | `cliente.cliente_id` | uno a muchos |
| `factura` | `mascota_id` | `mascota.mascota_id` | uno a muchos |
| `factura` | `medico_id` | `medico.id` | uno a muchos |
| `factura` | `usuario_anulacion_id` | `usuario.id` | cero o uno a muchos |
| `factura` | `usuario_id` | `usuario.id` | uno a muchos |
| `factura_detalle` | `despacho_detalle_id` | `despacho_detalle.id` | cero o uno a muchos |
| `factura_detalle` | `factura_id` | `factura.id` | uno a muchos |
| `factura_detalle` | `medicamento_id` | `medicamento.id` | cero o uno a muchos |
| `factura_detalle` | `servicio_id` | `servicio.id` | cero o uno a muchos |
| `factura_detalle` | `tarifa_id` | `tarifa.id` | cero o uno a muchos |
| `factura_historial_estado` | `factura_id` | `factura.id` | uno a muchos |
| `factura_historial_estado` | `usuario_id` | `usuario.id` | uno a muchos |
| `formula_historial_estado` | `formula_id` | `formula_medica.id` | uno a muchos |
| `formula_historial_estado` | `usuario_id` | `usuario.id` | uno a muchos |
| `formula_medica` | `atencion_id` | `atencion_medica.id` | cero o uno a muchos |
| `formula_medica` | `cita_id` | `cita.id` | uno a muchos |
| `formula_medica` | `cliente_id` | `cliente.cliente_id` | uno a muchos |
| `formula_medica` | `mascota_id` | `mascota.mascota_id` | uno a muchos |
| `formula_medica` | `medicamento_id` | `medicamento.id` | cero o uno a muchos |
| `formula_medica` | `medico_id` | `medico.id` | uno a muchos |
| `formula_medica_detalle` | `formula_id` | `formula_medica.id` | uno a muchos |
| `formula_medica_detalle` | `medicamento_id` | `medicamento.id` | uno a muchos |
| `historia_medica` | `paciente_id` | `mascota.mascota_id` | uno a muchos |
| `horario_medico` | `medico_id` | `medico.id` | uno a muchos |
| `lote_medicamento` | `medicamento_id` | `medicamento.id` | uno a muchos |
| `mascota` | `cliente_id` | `cliente.cliente_id` | uno a muchos |
| `mascota` | `raza_id` | `raza.raza_id` | uno a muchos |
| `medicamento` | `impuesto_id` | `impuesto.id` | cero o uno a muchos |
| `medico` | `especializacion_id` | `especializacion.id` | uno a muchos |
| `medico` | `usuario_id` | `usuario.id` | cero o uno a cero o uno |
| `movimiento_inventario` | `carga_id` | `carga_inventario.id` | cero o uno a muchos |
| `movimiento_inventario` | `despacho_detalle_id` | `despacho_detalle.id` | cero o uno a muchos |
| `movimiento_inventario` | `despacho_id` | `despacho.id` | cero o uno a muchos |
| `movimiento_inventario` | `formula_id` | `formula_medica.id` | cero o uno a muchos |
| `movimiento_inventario` | `lote_id` | `lote_medicamento.id` | uno a muchos |
| `movimiento_inventario` | `medicamento_id` | `medicamento.id` | uno a muchos |
| `movimiento_inventario` | `movimiento_revertido_id` | `movimiento_inventario.id` | cero o uno a cero o uno |
| `movimiento_inventario` | `usuario_id` | `usuario.id` | uno a muchos |
| `otp_token` | `usuario_id` | `usuario.id` | uno a muchos |
| `pago` | `factura_id` | `factura.id` | uno a muchos |
| `pago` | `usuario_id` | `usuario.id` | uno a muchos |
| `perfil_menu` | `menu_id` | `menu.menu_id` | uno a muchos |
| `perfil_menu` | `perfil_id` | `perfil.perfil_id` | uno a muchos |
| `servicio` | `especializacion_id` | `especializacion.id` | cero o uno a muchos |
| `tarifa` | `impuesto_id` | `impuesto.id` | uno a muchos |
| `tarifa` | `servicio_id` | `servicio.id` | uno a muchos |
| `tarifa_historial` | `servicio_id` | `servicio.id` | uno a muchos |
| `tarifa_historial` | `tarifa_id` | `tarifa.id` | uno a muchos |
| `tarifa_historial` | `usuario_id` | `usuario.id` | uno a muchos |
| `usuario` | `perfil_id` | `perfil.perfil_id` | cero o uno a muchos |

## Tablas

### `anotacion_historia`

Anotaciones de la historia.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `historia_id` | bigint unsigned | No | FK |
| `medico_id` | bigint unsigned | No | FK |
| `fecha` | datetime | Sí |  |
| `descripcion` | text | No |  |
| `cita_id` | bigint unsigned | Sí | FK |
| `diagnostico` | text | Sí |  |
| `observaciones` | text | Sí |  |

### `atencion_medica`

Atención de una cita.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `cita_id` | bigint unsigned | No | FK |
| `historia_medica_id` | bigint unsigned | No | FK |
| `medico_id` | bigint unsigned | No | FK |
| `mascota_id` | int | No | FK |
| `motivo_consulta` | text | Sí |  |
| `sintomas` | text | Sí |  |
| `diagnostico` | text | Sí |  |
| `observaciones` | text | Sí |  |
| `recomendaciones` | text | Sí |  |
| `fecha_inicio` | datetime | No |  |
| `fecha_fin` | datetime | Sí |  |
| `finalizada` | tinyint(1) | No |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

### `atencion_servicio`

Servicios prestados en la atención.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `atencion_id` | bigint unsigned | No | FK |
| `servicio_id` | bigint unsigned | No | FK |
| `cantidad` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Únicas compuestas: `(atencion_id, servicio_id)`.

Validaciones:

- `(cantidad > 0)`

### `carga_inventario`

Log de cargas masivas.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `nombre_archivo` | varchar(255) | No |  |
| `formato` | varchar(10) | No |  |
| `usuario_id` | bigint unsigned | No | FK |
| `fecha_carga` | datetime | No |  |
| `usuario_confirmacion_id` | bigint unsigned | Sí | FK |
| `fecha_confirmacion` | datetime | Sí |  |
| `estado` | varchar(30) | No |  |
| `total_filas` | int | No |  |
| `filas_validas` | int | No |  |
| `filas_con_error` | int | No |  |
| `filas_importadas` | int | No |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

### `carga_inventario_fila`

Filas de cada carga con sus errores.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `carga_id` | bigint unsigned | No | FK |
| `numero_fila` | int | No |  |
| `datos_json` | json | No |  |
| `valida` | tinyint(1) | No |  |
| `accion` | varchar(30) | Sí |  |
| `errores_json` | json | No |  |
| `importada` | tinyint(1) | No |  |
| `error_importacion` | varchar(500) | Sí |  |
| `medicamento_id` | bigint unsigned | Sí | FK |
| `lote_id` | bigint unsigned | Sí | FK |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Únicas compuestas: `(carga_id, numero_fila)`.

### `cita`

Agenda; nunca se borra.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `cliente_id` | bigint unsigned | No | FK |
| `mascota_id` | int | No | FK |
| `medico_id` | bigint unsigned | No | FK |
| `fecha_inicio` | datetime | No |  |
| `estado` | varchar(20) | No |  |
| `motivo` | text | Sí |  |
| `fecha_fin` | datetime | No |  |
| `servicio_id` | bigint unsigned | Sí | FK |
| `motivo_cancelacion` | varchar(255) | Sí |  |
| `usuario_creacion_id` | bigint unsigned | Sí | FK |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Únicas compuestas: `(mascota_id, fecha_inicio, fecha_fin, id)`; `(medico_id, fecha_inicio, fecha_fin, id)`.

Validaciones:

- `(fecha_fin > fecha_inicio)`

### `cita_historial_estado`

Cambios de estado y reprogramaciones.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `cita_id` | bigint unsigned | No | FK |
| `estado_anterior` | varchar(30) | Sí |  |
| `estado_nuevo` | varchar(30) | No |  |
| `fecha_inicio_anterior` | datetime | Sí |  |
| `fecha_inicio_nueva` | datetime | Sí |  |
| `motivo` | varchar(255) | Sí |  |
| `usuario_id` | bigint unsigned | No | FK |
| `fecha` | datetime | No |  |

### `cliente`

Propietarios.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `cliente_id` | bigint unsigned | No | PK |
| `usuario_id` | int | Sí | Única |
| `tipo_documento` | varchar(10) | No |  |
| `numero_documento` | varchar(20) | No | Única |
| `nombres` | varchar(100) | No |  |
| `apellidos` | varchar(100) | No |  |
| `fecha_nacimiento` | date | No |  |
| `genero` | varchar(1) | Sí |  |
| `telefono` | varchar(20) | Sí |  |
| `direccion` | text | Sí |  |
| `activo` | tinyint(1) | No |  |
| `correo` | varchar(255) | Sí |  |

### `configuracion_clinica`

Datos de la clínica y consecutivos (una fila).

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `nombre` | varchar(150) | No |  |
| `nit` | varchar(30) | No |  |
| `direccion` | varchar(200) | Sí |  |
| `telefono` | varchar(30) | Sí |  |
| `correo` | varchar(255) | Sí |  |
| `prefijo_factura` | varchar(10) | No |  |
| `consecutivo_factura` | bigint | No |  |
| `prefijo_formula` | varchar(10) | No |  |
| `consecutivo_formula` | bigint | No |  |
| `prefijo_despacho` | varchar(10) | No |  |
| `consecutivo_despacho` | bigint | No |  |
| `tope_descuento_secretaria` | decimal(5,2) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |
| `unica` | tinyint(1) | No | Única |

Validaciones:

- `(tope_descuento_secretaria between 0 and 100)`
- `(unica = true)`

### `despacho`

Entrega de una fórmula.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `formula_id` | bigint unsigned | No | FK |
| `numero` | varchar(20) | No | Única |
| `usuario_id` | bigint unsigned | No | FK |
| `estado` | varchar(30) | No |  |
| `fecha_inicio` | datetime | No |  |
| `fecha_confirmacion` | datetime | Sí |  |
| `observacion` | varchar(500) | Sí |  |
| `idempotency_key` | varchar(64) | Sí | Única |
| `fecha_reversion` | datetime | Sí |  |
| `usuario_reversion_id` | bigint unsigned | Sí | FK |
| `motivo_reversion` | varchar(500) | Sí |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |
| `formula_en_preparacion` | bigint unsigned | Sí | Única (generada) |

### `despacho_detalle`

Cantidad entregada por lote.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `despacho_id` | bigint unsigned | No | FK |
| `formula_detalle_id` | bigint unsigned | No | FK |
| `medicamento_id` | bigint unsigned | No | FK |
| `lote_id` | bigint unsigned | No | FK |
| `cantidad_entregada` | int | No |  |
| `precio_venta_unitario` | decimal(14,2) | No |  |
| `porcentaje_iva` | decimal(5,2) | No |  |
| `facturado` | tinyint(1) | No |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Únicas compuestas: `(despacho_id, formula_detalle_id, lote_id)`.

Validaciones:

- `(cantidad_entregada > 0)`
- `(precio_venta_unitario >= 0)`
- `(porcentaje_iva between 0 and 100)`

### `especializacion`

Especialidades.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `nombre` | varchar(100) | No | Única |
| `descripcion` | text | Sí |  |
| `codigo_especializacion` | varchar(10) | No | Única |
| `activo` | tinyint(1) | No |  |

### `factura`

Factura de una cita atendida.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `numero` | varchar(20) | No | Única |
| `cita_id` | bigint unsigned | No | FK |
| `atencion_id` | bigint unsigned | No | FK |
| `cliente_id` | bigint unsigned | No | FK |
| `mascota_id` | int | No | FK |
| `medico_id` | bigint unsigned | No | FK |
| `cliente_nombre` | varchar(201) | No |  |
| `cliente_documento` | varchar(40) | No |  |
| `mascota_nombre` | varchar(100) | No |  |
| `medico_nombre` | varchar(201) | No |  |
| `especialidad_nombre` | varchar(100) | No |  |
| `fecha_generacion` | datetime | No |  |
| `subtotal` | decimal(14,2) | No |  |
| `total_descuento` | decimal(14,2) | No |  |
| `total_impuestos` | decimal(14,2) | No |  |
| `total` | decimal(14,2) | No |  |
| `total_pagado` | decimal(14,2) | No |  |
| `saldo` | decimal(14,2) | No |  |
| `estado` | varchar(30) | No |  |
| `observacion` | varchar(500) | Sí |  |
| `usuario_id` | bigint unsigned | No | FK |
| `fecha_anulacion` | datetime | Sí |  |
| `usuario_anulacion_id` | bigint unsigned | Sí | FK |
| `motivo_anulacion` | varchar(500) | Sí |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |
| `cita_vigente` | bigint unsigned | Sí | Única (generada) |

Validaciones:

- `(saldo >= 0)`
- `(total_pagado >= 0)`
- `(total >= 0)`

### `factura_detalle`

Renglones congelados.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `factura_id` | bigint unsigned | No | FK |
| `tipo_item` | varchar(30) | No |  |
| `servicio_id` | bigint unsigned | Sí | FK |
| `tarifa_id` | bigint unsigned | Sí | FK |
| `medicamento_id` | bigint unsigned | Sí | FK |
| `despacho_detalle_id` | bigint unsigned | Sí | FK |
| `descripcion` | varchar(255) | No |  |
| `cantidad` | int | No |  |
| `precio_unitario` | decimal(14,2) | No |  |
| `porcentaje_descuento` | decimal(5,2) | No |  |
| `valor_descuento` | decimal(14,2) | No |  |
| `motivo_descuento` | varchar(255) | Sí |  |
| `porcentaje_iva` | decimal(5,2) | No |  |
| `valor_iva` | decimal(14,2) | No |  |
| `subtotal` | decimal(14,2) | No |  |
| `total` | decimal(14,2) | No |  |
| `vigente` | tinyint(1) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |
| `despacho_detalle_vigente` | bigint unsigned | Sí | Única (generada) |

Validaciones:

- `(cantidad > 0)`
- `(porcentaje_descuento between 0 and 100)`
- `(((tipo_item = \'SERVICIO\') and (servicio_id is not null)) or ((tipo_item = \'MEDICAMENTO\') and (despacho_detalle_id is not null)))`

### `factura_historial_estado`

Cambios de estado de la factura.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `factura_id` | bigint unsigned | No | FK |
| `estado_anterior` | varchar(30) | Sí |  |
| `estado_nuevo` | varchar(30) | No |  |
| `motivo` | varchar(500) | Sí |  |
| `usuario_id` | bigint unsigned | No | FK |
| `fecha` | datetime | No |  |

### `formula_historial_estado`

Cambios de estado de la fórmula.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `formula_id` | bigint unsigned | No | FK |
| `estado_despacho_anterior` | varchar(30) | Sí |  |
| `estado_despacho_nuevo` | varchar(30) | Sí |  |
| `estado_formula_anterior` | varchar(30) | Sí |  |
| `estado_formula_nuevo` | varchar(30) | Sí |  |
| `motivo` | varchar(500) | Sí |  |
| `usuario_id` | bigint unsigned | No | FK |
| `fecha` | datetime | No |  |

### `formula_medica`

Encabezado de la fórmula.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `cita_id` | bigint unsigned | No | FK |
| `medicamento_id` | bigint unsigned | Sí | FK |
| `dosis` | text | Sí |  |
| `indicaciones` | text | Sí |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_actualizacion_registro` | datetime | Sí |  |
| `frecuencia` | varchar(255) | Sí |  |
| `duracion` | varchar(255) | Sí |  |
| `numero` | varchar(20) | Sí | Única |
| `atencion_id` | bigint unsigned | Sí | FK |
| `medico_id` | bigint unsigned | No | FK |
| `mascota_id` | int | No | FK |
| `cliente_id` | bigint unsigned | No | FK |
| `fecha_generacion` | datetime | No |  |
| `indicaciones_generales` | text | Sí |  |
| `estado_formula` | varchar(30) | No |  |
| `estado_despacho` | varchar(30) | Sí |  |
| `fecha_confirmacion` | datetime | Sí |  |
| `motivo_anulacion` | varchar(500) | Sí |  |
| `version` | int | No |  |

### `formula_medica_detalle`

Medicamentos formulados.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `formula_id` | bigint unsigned | No | FK |
| `medicamento_id` | bigint unsigned | No | FK |
| `dosis` | decimal(10,2) | No |  |
| `unidad_dosis` | varchar(30) | No |  |
| `frecuencia_horas` | int | No |  |
| `duracion_dias` | int | No |  |
| `via_administracion` | varchar(30) | No |  |
| `cantidad_prescrita` | int | No |  |
| `cantidad_despachada` | int | No |  |
| `indicaciones` | text | Sí |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Únicas compuestas: `(formula_id, medicamento_id)`.

Validaciones:

- `(dosis > 0)`
- `(frecuencia_horas between 1 and 168)`
- `(duracion_dias > 0)`
- `(cantidad_prescrita > 0)`
- `(cantidad_despachada between 0 and cantidad_prescrita)`

### `historia_medica`

Historia clínica por mascota.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `paciente_id` | int | No | FK |
| `fecha_creacion` | datetime | Sí |  |

### `horario_medico`

Bloques de atención por día de la semana.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `medico_id` | bigint unsigned | No | FK |
| `dia_semana` | tinyint | No |  |
| `hora_inicio` | time | No |  |
| `hora_fin` | time | No |  |
| `activo` | tinyint(1) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Validaciones:

- `(dia_semana between 1 and 7)`
- `(hora_fin > hora_inicio)`

### `impuesto`

Porcentajes de IVA.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `nombre` | varchar(50) | No | Única |
| `porcentaje` | decimal(5,2) | No |  |
| `activo` | tinyint(1) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Validaciones:

- `(porcentaje between 0 and 100)`

### `lote_medicamento`

Existencias por lote.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `medicamento_id` | bigint unsigned | No | FK |
| `numero_lote` | varchar(50) | No |  |
| `precio_compra` | decimal(14,2) | No |  |
| `cantidad_inicial` | int | No |  |
| `cantidad_disponible` | int | No |  |
| `fecha_compra` | date | No |  |
| `fecha_ingreso` | date | No |  |
| `fecha_vencimiento` | date | Sí |  |
| `activo` | tinyint(1) | No |  |
| `version` | int | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Únicas compuestas: `(medicamento_id, numero_lote)`.

Validaciones:

- `(cantidad_disponible >= 0)`
- `(cantidad_disponible <= cantidad_inicial)`
- `(precio_compra >= 0)`
- `((fecha_vencimiento is null) or (fecha_vencimiento > fecha_compra))`

### `mascota`

Pacientes.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `mascota_id` | int | No | PK |
| `nombre_mascota` | varchar(100) | No |  |
| `edad` | int | No |  |
| `raza_id` | int | No | FK |
| `fecha_registro` | datetime | No |  |
| `fecha_modificacion` | datetime | Sí |  |
| `cliente_id` | bigint unsigned | No | FK |
| `activo` | tinyint(1) | No |  |

### `medicamento`

Productos: medicamentos e insumos.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `nombre` | varchar(100) | No | Única |
| `descripcion` | text | Sí |  |
| `presentacion` | varchar(100) | Sí |  |
| `fecha_compra` | date | Sí |  |
| `fecha_vence` | date | Sí |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |
| `codigo` | char(8) | No | Única |
| `tipo_producto` | varchar(20) | No |  |
| `requiere_invima` | tinyint(1) | No |  |
| `registro_invima` | varchar(50) | Sí |  |
| `unidad_medida` | varchar(30) | No |  |
| `precio_venta` | decimal(14,2) | No |  |
| `impuesto_id` | bigint unsigned | Sí | FK |
| `stock_minimo` | int | No |  |
| `fecha_proximo_pedido` | date | Sí |  |
| `activo` | tinyint(1) | No |  |
| `version` | int | No |  |

Validaciones:

- `regexp_like(codigo,\'^[A-Z0-9]{8}$\',\'c\')`
- `(precio_venta >= 0)`
- `(stock_minimo >= 0)`
- `((tipo_producto <> \'MEDICAMENTO\') or ((registro_invima is not null) and (registro_invima <> \'\')))`

### `medico`

Veterinarios.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `tipo_documento` | varchar(10) | No |  |
| `numero_documento` | varchar(20) | No | Única |
| `nombres` | varchar(100) | No |  |
| `apellidos` | varchar(100) | No |  |
| `telefono` | varchar(20) | Sí |  |
| `registro_profesional` | varchar(50) | No | Única |
| `especializacion_id` | bigint unsigned | No | FK |
| `estado` | tinyint(1) | No |  |
| `usuario_id` | bigint unsigned | Sí | FK |

### `menu`

Opciones del frontend (C4).

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `menu_id` | int | No | PK |
| `code` | varchar(100) | No | Única |
| `title` | varchar(255) | No |  |
| `url` | longtext | No |  |
| `icon` | varchar(255) | No |  |
| `activo` | tinyint(1) | No |  |
| `fecha_creacion` | datetime | No |  |
| `fecha_modificacion` | datetime | Sí |  |

### `movimiento_inventario`

Entradas y salidas.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `fecha` | datetime | No |  |
| `tipo` | varchar(30) | No |  |
| `medicamento_id` | bigint unsigned | No | FK |
| `lote_id` | bigint unsigned | No | FK |
| `cantidad` | int | No |  |
| `saldo_lote_resultante` | int | No |  |
| `saldo_producto_resultante` | int | No |  |
| `costo_unitario` | decimal(14,2) | No |  |
| `usuario_id` | bigint unsigned | No | FK |
| `formula_id` | bigint unsigned | Sí | FK |
| `despacho_id` | bigint unsigned | Sí | FK |
| `despacho_detalle_id` | bigint unsigned | Sí | FK |
| `carga_id` | bigint unsigned | Sí | FK |
| `movimiento_revertido_id` | bigint unsigned | Sí | FK |
| `observacion` | varchar(500) | Sí |  |

Validaciones:

- `(cantidad > 0)`
- `(saldo_lote_resultante >= 0)`
- `(saldo_producto_resultante >= 0)`
- `((tipo <> \'SALIDA_DESPACHO\') or ((formula_id is not null) and (despacho_id is not null) and (despacho_detalle_id is not null)))`

### `otp_token`

Código de acceso de un solo uso.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `usuario_id` | bigint unsigned | No | FK |
| `codigo` | varchar(6) | No |  |
| `fecha_creacion` | datetime | No |  |
| `fecha_expiracion` | datetime | No |  |
| `intentos` | int | No |  |
| `usado` | tinyint(1) | No |  |

### `pago`

Pagos de la factura.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `factura_id` | bigint unsigned | No | FK |
| `fecha` | datetime | No |  |
| `valor` | decimal(14,2) | No |  |
| `medio_pago` | varchar(30) | No |  |
| `referencia` | varchar(100) | Sí |  |
| `usuario_id` | bigint unsigned | No | FK |
| `anulado` | tinyint(1) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Validaciones:

- `(valor > 0)`

### `perfil`

Roles del sistema (C2).

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `perfil_id` | int | No | PK |
| `nombre` | varchar(50) | No |  |
| `activo` | tinyint(1) | No |  |
| `fecha_creacion` | datetime | No |  |
| `fecha_modificacion` | datetime | Sí |  |
| `codigo` | varchar(30) | Sí | Única |

### `perfil_menu`

Menús visibles por perfil.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `perfil_menu_id` | int | No | PK |
| `perfil_id` | int | No | FK |
| `menu_id` | int | No | FK |

Únicas compuestas: `(perfil_id, menu_id)`.

### `raza`

Razas y especies.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `raza_id` | int | No | PK |
| `nombre` | varchar(100) | No |  |
| `especie` | varchar(100) | No |  |
| `fecha_creacion` | datetime | No |  |
| `fecha_modificacion` | datetime | Sí |  |

### `servicio`

Servicios que se prestan y cobran.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `codigo` | varchar(20) | No | Única |
| `nombre` | varchar(100) | No |  |
| `descripcion` | text | Sí |  |
| `tipo_servicio` | varchar(30) | No |  |
| `especializacion_id` | bigint unsigned | Sí | FK |
| `duracion_minutos` | int | No |  |
| `activo` | tinyint(1) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |

Validaciones:

- `(duracion_minutos > 0)`

### `session`

Sesiones; el JWT se identifica por jti.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `session_id` | int | No | PK |
| `user_id` | bigint unsigned | No |  |
| `token` | text | Sí |  |
| `fecha_ini_sesion` | datetime | Sí |  |
| `fecha_expiracion` | datetime | Sí |  |
| `jti` | varchar(36) | Sí | Única |
| `activa` | tinyint(1) | No |  |
| `fecha_cierre` | datetime | Sí |  |

### `tarifa`

Valor de cada servicio en el tiempo.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `servicio_id` | bigint unsigned | No | FK |
| `valor` | decimal(14,2) | No |  |
| `impuesto_id` | bigint unsigned | No | FK |
| `vigente_desde` | date | No |  |
| `vigente_hasta` | date | Sí |  |
| `activa` | tinyint(1) | No |  |
| `fecha_creacion_registro` | datetime | No |  |
| `fecha_modificacion_registro` | datetime | Sí |  |
| `servicio_vigente` | bigint unsigned | Sí | Única (generada) |

Validaciones:

- `(valor >= 0)`
- `((vigente_hasta is null) or (vigente_hasta >= vigente_desde))`

### `tarifa_historial`

Log de cambios de tarifa.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `tarifa_id` | bigint unsigned | No | FK |
| `servicio_id` | bigint unsigned | No | FK |
| `valor_anterior` | decimal(14,2) | Sí |  |
| `valor_nuevo` | decimal(14,2) | No |  |
| `porcentaje_iva_anterior` | decimal(5,2) | Sí |  |
| `porcentaje_iva_nuevo` | decimal(5,2) | No |  |
| `motivo` | varchar(500) | No |  |
| `usuario_id` | bigint unsigned | No | FK |
| `fecha` | datetime | No |  |

### `usuario`

Cuentas de acceso.

| Columna | Tipo | Nulo | Clave |
|---|---|---|---|
| `id` | bigint unsigned | No | PK |
| `username` | varchar(50) | No | Única |
| `password_hash` | text | Sí |  |
| `rol` | varchar(30) | Sí |  |
| `fecha_creacion` | datetime | Sí |  |
| `activo` | tinyint(1) | Sí |  |
| `email` | varchar(255) | No |  |
| `perfil_id` | int | Sí | FK |
