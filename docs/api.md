# API

URL base: `http://localhost:8080/clinica/v1`. Autenticación: `Authorization: Bearer <jwt>`.

Solo se documentan endpoints ya implementados; cada fase agrega los suyos. Los ejemplos provienen del sistema en ejecución con los datos de prueba, salvo el de fórmulas y el OTP, escritos según el DTO.

## Respuestas comunes

Éxito de endpoints nuevos que crean, editan o ejecutan acciones (`RespuestaRs<T>`): `{ "status": 200, "message": "...", "data": { } }`. Las consultas devuelven el objeto o arreglo directamente.

Error 409:

```json
{
  "error": "Conflict",
  "message": "El numero de documento ya pertenece a otro cliente.",
  "status": 409,
  "detalles": [
    {
      "campo": "numeroDocumento",
      "mensaje": "El numero de documento ya pertenece a otro cliente."
    }
  ]
}
```

Los 409 por duplicado incluyen `detalles: [{ "campo", "mensaje" }]`, con el nombre del campo de la petición. Los de regla de negocio pueden no incluirlo.

Error 400 con `detalles` (opcional):

```json
{
  "detalles": [
    {
      "campo": "correo",
      "mensaje": "El correo no es valido."
    },
    {
      "campo": "nombres",
      "mensaje": "Los nombres son obligatorios."
    }
  ],
  "error": "Bad Request",
  "message": "correo: El correo no es valido.; nombres: Los nombres son obligatorios.",
  "status": 400
}
```

Otros códigos con el mismo formato: 401, 403, 404, 410 y 500.

## `POST /auth/login`

Rol: público. Envía el OTP al correo. Si el correo falla responde 500.

```json
{
  "usuarioId": 1,
  "username": "admin",
  "login": true
}
```

## `POST /auth/validate-token`

Rol: público. Recibe `{ "usuarioId", "token" }` (el OTP).

```json
{
  "token": "<jwt>",
  "logueoExitoso": true
}
```

Claims del JWT (`medicoId` solo aparece si el usuario es médico; `menus` es texto JSON):

```json
{
  "jti": "322dd7d1-553c-4ead-870d-6bb0f890fc58",
  "sub": "3",
  "username": "medico1",
  "email": "medico1@clinica.example",
  "rol": "MEDICO",
  "menus": "[...]",
  "iat": 1795540496,
  "exp": 1795544096,
  "medicoId": 1
}
```

Elemento de `menus`:

```json
[
  {
    "code": "cita",
    "name": "Gestión de citas",
    "activo": true,
    "menuId": 1,
    "title": "Gestión de citas",
    "url": "/inicio/citas",
    "icon": "bi bi-calendar",
    "fechaCreacion": "2026-01-01T00:00:00",
    "fechaModificacion": null
  }
]
```

## `POST /auth/cerrar-sesion`

Rol: autenticado. Sin cuerpo. El token deja de servir (401).

```json
{
  "status": 200,
  "message": "Sesión cerrada correctamente.",
  "success": true
}
```

## `POST /cliente/guardar`

Rol: ADMINISTRADOR, SECRETARIA. Mismo cuerpo que `POST /cliente/{id}`, con `correo` opcional.

```json
{
  "status": 200,
  "message": "Cliente creado correctamente.",
  "data": {
    "activo": true,
    "apellidos": "Paz",
    "correo": "eva@example.com",
    "direccion": null,
    "fechaNacimiento": "1992-03-04",
    "genero": null,
    "id": 8,
    "nombres": "Eva",
    "numeroDocumento": "9000000099",
    "telefono": null,
    "tipoDocumento": "CC",
    "usuarioId": null
  }
}
```

## `GET /cita/{id}`

Rol: ADMINISTRADOR, SECRETARIA, MEDICO (solo las suyas). **JSON definitivo de la cita**, igual en `/cita/listar`, `/cita/listar-rango` y `/cita/listar-por-mascota` (arreglos). Use `fechaInicio` y `fechaFin`; `fechaHora` se mantiene como alias de solo lectura. En `/cita/crear` y `/cita/actualizar` el cuerpo acepta `fechaInicio` o `fechaHora`. `servicio` y `motivoCancelacion` pueden ser `null`. Estados en mayúsculas (C5). El evento de `/cita/calendario` será el del contrato.

```json
{
  "cliente": {
    "activo": true,
    "apellidos": "Pérez",
    "correo": "cliente1@clinica.example",
    "direccion": "Dirección de prueba",
    "fechaNacimiento": "1990-05-10",
    "genero": "M",
    "id": 1,
    "nombres": "Juan",
    "numeroDocumento": "9000000001",
    "telefono": "3000000001",
    "tipoDocumento": "CC",
    "usuarioId": null
  },
  "estado": "PROGRAMADA",
  "fechaCreacionRegistro": "2026-11-24T17:09:14",
  "fechaFin": "2026-11-30T09:30:00",
  "fechaHora": "2026-11-30T09:00:00",
  "fechaInicio": "2026-11-30T09:00:00",
  "fechaModificacionRegistro": null,
  "id": 1,
  "mascota": {
    "activo": true,
    "cliente": {
      "activo": true,
      "apellidos": "Pérez",
      "correo": "cliente1@clinica.example",
      "direccion": "Dirección de prueba",
      "fechaNacimiento": "1990-05-10",
      "genero": "M",
      "id": 1,
      "nombres": "Juan",
      "numeroDocumento": "9000000001",
      "telefono": "3000000001",
      "tipoDocumento": "CC",
      "usuarioId": null
    },
    "edad": 3,
    "fechaModificacion": null,
    "fechaRegistro": "2026-11-24T17:09:14",
    "mascotaId": 1,
    "nombreMascota": "Firulais",
    "raza": {
      "especie": "Perro",
      "fechaCreacion": "2026-11-24T17:09:14",
      "fechaModificacion": null,
      "nombre": "Criollo",
      "razaId": 1
    }
  },
  "medico": {
    "apellidos": "Ramírez",
    "especializacion": {
      "activo": true,
      "codigoEspecializacion": "VET_GEN",
      "descripcion": "Consulta general de mascotas",
      "id": 1,
      "nombre": "Medicina general veterinaria"
    },
    "estado": true,
    "id": 1,
    "nombres": "Sofía",
    "numeroDocumento": "8000000001",
    "registroProfesional": "RP-VET-001",
    "telefono": "3100000000",
    "tipoDocumento": "CC",
    "usuarioId": 3
  },
  "motivo": "[Prueba] Control general",
  "motivoCancelacion": null,
  "servicio": {
    "activo": true,
    "codigo": "CONS-GEN",
    "descripcion": "Consulta general",
    "duracionMinutos": 30,
    "especializacion": {
      "activo": true,
      "codigoEspecializacion": "VET_GEN",
      "descripcion": "Consulta general de mascotas",
      "id": 1,
      "nombre": "Medicina general veterinaria"
    },
    "fechaCreacionRegistro": "2026-11-24T17:09:14",
    "fechaModificacionRegistro": null,
    "id": 1,
    "nombre": "Consulta general",
    "tipoServicio": "CONSULTA"
  },
  "usuarioCreacionId": 1,
  "version": 0
}
```

## `GET /formula-medica/listar`

Rol: ADMINISTRADOR, MEDICO (las suyas). **JSON definitivo de la fórmula**: encabezado con detalles; es el mismo objeto que devolverán `GET /formula-medica/{id}`, `/cita/{citaId}` y el `data` de crear, editar, confirmar y anular (estos agregarán `cantidadSugerida` en cada detalle). `numero` y `estadoDespacho` son `null` en BORRADOR. Los filtros llegan en la fase 6. `POST /guardar` y `POST /actualizar` responden 410.

```json
[
  {
    "id": 1,
    "numero": "FM-000001",
    "citaId": 5,
    "medicoId": 1,
    "medicoNombre": "Sofía Ramírez",
    "mascotaId": 3,
    "mascotaNombre": "Luna",
    "clienteId": 1,
    "clienteNombre": "Juan Pérez",
    "fecha": "2026-11-16T09:25:00",
    "indicacionesGenerales": "Reposo",
    "estadoFormula": "CONFIRMADA",
    "estadoDespacho": "PENDIENTE_DESPACHO",
    "fechaConfirmacion": "2026-11-16T09:26:00",
    "detalles": [
      {
        "id": 1,
        "medicamentoId": 1,
        "medicamentoCodigo": "AMX00500",
        "medicamentoNombre": "Amoxicilina 500 mg",
        "dosis": 1.0,
        "unidadDosis": "tableta",
        "frecuenciaHoras": 8,
        "duracionDias": 7,
        "viaAdministracion": "ORAL",
        "cantidadPrescrita": 21,
        "cantidadDespachada": 0,
        "indicaciones": "con comida"
      }
    ]
  }
]
```

## `GET /perfil/listar`

Rol: ADMINISTRADOR. Perfiles activos con código.

```json
[{ "id": 1, "codigo": "ADMINISTRADOR", "nombre": "Administrador", "activo": true }]
```

## `PATCH /usuario/{id}/estado`

Rol: ADMINISTRADOR. Cuerpo `{ "activo": false }`. Inactivar invalida sus tokens; no permite inactivarse a sí mismo (409). `GET /usuario/listar-ordenado` devuelve el mismo objeto en un arreglo (nuevos: `rolCodigo`, `perfilId`).

```json
{ "status": 200, "message": "Estado actualizado correctamente.",
  "data": { "id": 34, "username": "despacho", "rol": "Despacho Medico", "rolCodigo": "DESPACHO_MEDICO", "perfilId": 4,
            "fechaCreacion": "2026-11-24T12:20:00", "activo": false, "email": "despacho@clinica.example" } }
```

## `GET /medico/{id}/horarios` y `GET /medico/mis-horarios`

Rol: ADMINISTRADOR, SECRETARIA, MEDICO; `mis-horarios` solo MEDICO.

```json
[{ "id": 1, "diaSemana": 1, "horaInicio": "08:00", "horaFin": "12:00", "activo": true }]
```

## `PUT /medico/{id}/horarios`

Rol: ADMINISTRADOR, SECRETARIA. Cuerpo: arreglo completo (reemplaza; los `id` se ignoran y cambian). 400 si el día o las horas no son válidos; 409 si hay bloques superpuestos.

```json
{ "status": 200, "message": "Horarios guardados correctamente.",
  "data": [{ "id": 41, "diaSemana": 1, "horaInicio": "08:00", "horaFin": "12:00", "activo": true }] }
```

## `PATCH /especializacion/{id}/estado`

Rol: ADMINISTRADOR. Cuerpo `{ "activo": false }`. `GET /especializacion/listar` acepta `soloActivas=true`.

```json
{ "status": 200, "message": "Estado actualizado correctamente.",
  "data": { "id": 2, "nombre": "Cirugía veterinaria", "descripcion": "Procedimientos quirúrgicos", "codigoEspecializacion": "VET_CIR", "activo": false } }
```

## `GET /factura/cliente/{clienteId}` y `GET /factura/mascota/{mascotaId}`

Rol: ADMINISTRADOR, SECRETARIA. Lista vacía mientras no haya facturas; 404 si el cliente o la mascota no existe.

```json
[{ "id": 1, "numero": "FV-000001", "fecha": "2026-11-16T10:00:00", "citaId": 5, "clienteId": 1, "clienteNombre": "Juan Pérez",
   "clienteDocumento": "9000000001", "mascotaId": 3, "mascotaNombre": "Luna", "medicoNombre": "Sofía Ramírez",
   "especialidadNombre": "Medicina general veterinaria", "subtotal": 50000.00, "descuentos": 0.00, "iva": 9500.00,
   "total": 59500.00, "pagado": 0.00, "saldo": 59500.00, "estado": "PENDIENTE", "observacion": null }]
```

# Endpoints existentes cuya respuesta cambió

## `GET /medico/listar`

Rol: ADMINISTRADOR, SECRETARIA, MEDICO. Nuevo: `usuarioId`.

```json
[
  {
    "apellidos": "López",
    "especializacion": {
      "activo": true,
      "codigoEspecializacion": "VET_DER",
      "descripcion": "Enfermedades de la piel",
      "id": 3,
      "nombre": "Dermatología veterinaria"
    },
    "estado": true,
    "id": 2,
    "nombres": "Andrés",
    "numeroDocumento": "8000000002",
    "registroProfesional": "RP-VET-002",
    "telefono": "3100000000",
    "tipoDocumento": "CC",
    "usuarioId": 4
  }
]
```

## `GET /especializacion/listar`

Rol: autenticado. Nuevo: `activo`.

```json
[
  {
    "activo": true,
    "codigoEspecializacion": "VET_CIR",
    "descripcion": "Procedimientos quirúrgicos",
    "id": 2,
    "nombre": "Cirugía veterinaria"
  }
]
```

## `GET /medicamento/listar`

Rol: autenticado. Nuevos: datos de producto. `fechaCompra` y `fechaVence` son históricos; las existencias y lotes se consultarán en `/inventario`.

```json
[
  {
    "activo": true,
    "codigo": "AMX00500",
    "descripcion": "Amoxicilina 500 mg",
    "fechaCompra": null,
    "fechaCreacionRegistro": "2026-11-24T17:09:14",
    "fechaModificacionRegistro": null,
    "fechaProximoPedido": "2026-12-14",
    "fechaVence": null,
    "id": 1,
    "impuesto": {
      "activo": true,
      "fechaCreacionRegistro": "2026-11-24T17:09:14",
      "fechaModificacionRegistro": null,
      "id": 1,
      "nombre": "IVA 0 %",
      "porcentaje": 0.0
    },
    "nombre": "Amoxicilina 500 mg",
    "precioVenta": 1500.0,
    "presentacion": "Caja x 10 tabletas",
    "registroInvima": "INVIMA PRUEBA-001",
    "requiereInvima": true,
    "stockMinimo": 20,
    "tipoProducto": "MEDICAMENTO",
    "unidadMedida": "tableta",
    "version": 0
  }
]
```
