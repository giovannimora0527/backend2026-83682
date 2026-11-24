# API

URL base: `http://localhost:8080/clinica/v1`. Autenticación: `Authorization: Bearer <jwt>`.

Los ejemplos provienen del sistema en ejecución con los datos de prueba. Al final está la tabla completa de endpoints con sus roles.

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

# Tarifas y configuración

Lectura: cualquier usuario autenticado. Escritura: ADMINISTRADOR. Crear, editar y cambiar estado devuelven `{ status, message, data }` con el mismo objeto del listado.

## Impuestos

`GET /impuesto/listar`, `POST /impuesto/crear`, `PUT /impuesto/{id}`. Cuerpo: `{ "nombre", "porcentaje", "activo" }`. 409 al cambiar el porcentaje de un impuesto en uso (cree otro) o al inactivar uno de una tarifa vigente.

```json
[
  {
    "id": 1,
    "nombre": "IVA 0 %",
    "porcentaje": 0.0,
    "activo": true
  }
]
```

## Servicios

`GET /servicio/listar?soloActivos=&especializacionId=`, `POST /servicio/crear`, `PUT /servicio/{id}`, `PATCH /servicio/{id}/estado`. `soloActivos` excluye también los de especialidades inactivas. `tarifaVigente` puede ser `null`.

```json
[
  {
    "id": 6,
    "codigo": "CIRU-MEN",
    "nombre": "Cirugía menor",
    "descripcion": "Cirugía menor",
    "tipoServicio": "CIRUGIA",
    "especializacionId": 2,
    "especializacionNombre": "Cirugía veterinaria",
    "duracionMinutos": 60,
    "activo": true,
    "tarifaVigente": {
      "id": 7,
      "valor": 250000.0,
      "impuestoId": 3,
      "porcentajeIva": 19.0
    }
  }
]
```

## Tarifas

`GET /tarifa/listar?servicioId=&soloVigentes=`:

```json
[
  {
    "id": 7,
    "servicioId": 6,
    "servicioCodigo": "CIRU-MEN",
    "servicioNombre": "Cirugía menor",
    "especializacionNombre": "Cirugía veterinaria",
    "valor": 250000.0,
    "impuestoId": 3,
    "impuestoNombre": "IVA 19 %",
    "porcentajeIva": 19.0,
    "vigenteDesde": "2026-01-01",
    "vigenteHasta": null,
    "activa": true
  }
]
```

`POST /tarifa/crear`: `{ "servicioId", "valor", "impuestoId", "vigenteDesde" }` (hoy si falta; no futura). 409 si el servicio ya tiene una vigente.

`PUT /tarifa/{id}`: `{ "valor", "impuestoId", "motivo" }`. Cierra la vigente y crea otra: **`data.id` es el id nuevo**. 409 si no hay cambios o si ya no está vigente.

`GET /tarifa/{id}/historial` (todo el historial del servicio) y `GET /tarifa/historial?desde=&hasta=`:

```json
[
  {
    "id": 1,
    "tarifaId": 8,
    "servicioId": 5,
    "servicioNombre": "Curación",
    "valorAnterior": 40000.0,
    "valorNuevo": 41004.0,
    "ivaAnterior": 19.0,
    "ivaNuevo": 19.0,
    "motivo": "Ajuste",
    "usuario": "admin",
    "fecha": "2026-11-24T13:26:25"
  }
]
```

## `GET` y `PUT /configuracion-clinica`

```json
{
  "nombre": "Clínica Veterinaria Uniminuto",
  "nit": "900000000-0",
  "direccion": "Calle 1 # 2-3, Bogotá",
  "telefono": "6010000000",
  "correo": "contacto@clinica.example",
  "prefijoFactura": "FV",
  "prefijoFormula": "FM",
  "prefijoDespacho": "DP",
  "topeDescuentoSecretaria": 10.0
}
```

# Atención médica

Escritura: el MEDICO dueño de la cita. Consulta: ese MEDICO y ADMINISTRADOR.

## `POST /atencion/iniciar/{citaId}`

Sin cuerpo. El mismo día de la cita y desde 30 minutos antes. Si ya existe, la devuelve. Respuesta como la de finalizar, con `estadoCita: "EN_ATENCION"`.

## `PUT /atencion/{id}`

Cuerpo: `{ "motivoConsulta", "sintomas", "diagnostico", "observaciones", "recomendaciones", "servicios": [{ "servicioId", "cantidad" }] }`. Guarda todos los textos enviados; si `servicios` no llega, se conservan. 409 si ya finalizó o hay servicios repetidos.

## `POST /atencion/{id}/finalizar`

Exige motivo, síntomas y diagnóstico (400 con `detalles`). 409 si hay fórmulas en borrador o un servicio sin tarifa. Sin servicios agrega el agendado o la consulta de la especialidad.

```json
{
  "status": 200,
  "message": "Atención finalizada correctamente.",
  "data": {
    "id": 2,
    "citaId": 8,
    "medicoId": 1,
    "medicoNombre": "Sofía Ramírez",
    "estadoCita": "ATENDIDA",
    "motivoConsulta": "Vómito",
    "sintomas": "Decaimiento",
    "diagnostico": "Gastritis",
    "observaciones": "Estable",
    "recomendaciones": "Dieta blanda",
    "servicios": [
      {
        "servicioId": 1,
        "servicioCodigo": "CONS-GEN",
        "servicioNombre": "Consulta general",
        "cantidad": 1
      },
      {
        "servicioId": 5,
        "servicioCodigo": "CURACION",
        "servicioNombre": "Curación",
        "cantidad": 1
      }
    ],
    "fechaInicio": "2026-11-24T13:26:26",
    "fechaFin": "2026-11-24T13:26:26",
    "finalizada": true
  }
}
```

## `GET /atencion/cita/{citaId}`

```json
{
  "atencion": "<atención, como en finalizar, o null si no se ha iniciado>",
  "cita": {
    "id": 8,
    "fechaInicio": "2026-11-24T13:26:25",
    "fechaFin": "2026-11-24T13:56:25",
    "estado": "ATENDIDA",
    "motivo": "Vomito",
    "servicioId": 1,
    "servicioNombre": "Consulta general"
  },
  "mascota": {
    "id": 1,
    "nombre": "Firulais",
    "especie": "Perro",
    "raza": "Criollo",
    "edad": 3,
    "activo": true
  },
  "propietario": {
    "id": 1,
    "nombre": "Juan Pérez",
    "documento": "9000000001",
    "telefono": "3000000001",
    "correo": "cliente1@clinica.example"
  },
  "antecedentes": {
    "atenciones": [
      {
        "id": 1,
        "citaId": 5,
        "medicoId": 1,
        "medicoNombre": "Sofía Ramírez",
        "estadoCita": "ATENDIDA",
        "motivoConsulta": "Vómito",
        "sintomas": "Decaimiento",
        "diagnostico": "Esguince leve",
        "observaciones": "Estable",
        "recomendaciones": "Dieta blanda",
        "servicios": [
          {
            "servicioId": 1,
            "servicioCodigo": "CONS-GEN",
            "servicioNombre": "Consulta general",
            "cantidad": 1
          },
          {
            "servicioId": 5,
            "servicioCodigo": "CURACION",
            "servicioNombre": "Curación",
            "cantidad": 1
          }
        ],
        "fechaInicio": "2026-11-24T13:26:26",
        "fechaFin": "2026-11-24T13:26:27",
        "finalizada": true
      }
    ],
    "anotaciones": [
      {
        "id": 3,
        "fecha": "2025-11-24T10:00:00",
        "medicoNombre": "Sofía Ramírez",
        "descripcion": "Control",
        "diagnostico": null,
        "observaciones": null
      }
    ],
    "formulas": [
      "<fórmula completa>"
    ]
  }
}
```

## `GET /atencion/{id}/prefactura/pdf`

Servicios con su tarifa vigente y medicamentos de fórmulas confirmadas. Descarga `prefactura_<id>.pdf`.

# Fórmulas médicas

## `POST /formula-medica/crear` y `PUT /formula-medica/{id}`

Rol: MEDICO dueño. Cuerpo del contrato; `cantidadPrescrita` es opcional (usa la sugerida). Solo durante la atención o el mismo día en que finalizó; editar, solo en BORRADOR. La disponibilidad informa y no bloquea.

```json
{
  "status": 200,
  "message": "Fórmula creada en borrador.",
  "data": {
    "id": 1,
    "numero": null,
    "citaId": 8,
    "atencionId": 2,
    "medicoId": 1,
    "medicoNombre": "Sofía Ramírez",
    "mascotaId": 1,
    "mascotaNombre": "Firulais",
    "clienteId": 1,
    "clienteNombre": "Juan Pérez",
    "fecha": "2026-11-24T13:26:26",
    "indicacionesGenerales": "Control en 8 días",
    "estadoFormula": "BORRADOR",
    "estadoDespacho": null,
    "fechaConfirmacion": null,
    "motivoAnulacion": null,
    "detalles": [
      {
        "id": 1,
        "medicamentoId": 1,
        "medicamentoCodigo": "AMX00500",
        "medicamentoNombre": "Amoxicilina 500 mg",
        "presentacion": "Caja x 10 tabletas",
        "dosis": 1,
        "unidadDosis": "tableta",
        "frecuenciaHoras": 8,
        "duracionDias": 7,
        "viaAdministracion": "ORAL",
        "cantidadSugerida": 21,
        "cantidadPrescrita": 21,
        "cantidadDespachada": 0,
        "indicaciones": "Con comida",
        "cantidadDisponibleVigente": 180,
        "alcanza": true
      },
      {
        "id": 2,
        "medicamentoId": 3,
        "medicamentoCodigo": "IVE00010",
        "medicamentoNombre": "Ivermectina 1 %",
        "presentacion": "Frasco 50 ml",
        "dosis": 1,
        "unidadDosis": "ml",
        "frecuenciaHoras": 8,
        "duracionDias": 7,
        "viaAdministracion": "ORAL",
        "cantidadSugerida": 21,
        "cantidadPrescrita": 1,
        "cantidadDespachada": 0,
        "indicaciones": "Con comida",
        "cantidadDisponibleVigente": 0,
        "alcanza": false
      }
    ]
  }
}
```

## `POST /formula-medica/{id}/confirmar`

Sin cuerpo. Asigna número. `data`:

```json
{
  "id": 1,
  "numero": "FM-000001",
  "estadoFormula": "CONFIRMADA",
  "estadoDespacho": "PENDIENTE_DESPACHO",
  "fechaConfirmacion": "2026-11-24T13:26:26"
}
```
(más los demás campos de la fórmula).

## `POST /formula-medica/{id}/anular`

Rol: MEDICO dueño, ADMINISTRADOR. Cuerpo `{ "motivo" }`. 409 si ya se entregó algo. Queda `ANULADA` y, si estaba confirmada, `estadoDespacho: "CANCELADO"`.

## Consultas

- `GET /formula-medica/listar?texto=&desde=&hasta=&estado=`: ADMINISTRADOR, MEDICO (las suyas). `estado` es el de la fórmula.
- `GET /formula-medica/cita/{citaId}`: ADMINISTRADOR, MEDICO.
- `GET /formula-medica/{id}` y `/pdf`: además DESPACHO_MEDICO, que no ve borradores (403).

# Despacho

Operan solo DESPACHO_MEDICO; ADMINISTRADOR cancela, libera, revierte y consulta el historial.

## `GET /despacho/formulas-pendientes?texto=&numero=&desde=&hasta=&estado=`

Rol: DESPACHO_MEDICO. `texto` busca en número, propietario y mascota sin tildes ni mayúsculas. Sin `estado` trae las cuatro pendientes. `usuarioPreparacion` es `null` si nadie la prepara.

```json
[
  {
    "formulaId": 1,
    "numero": "FM-000001",
    "fecha": "2026-11-24T13:50:54",
    "medicoNombre": "Sofía Ramírez",
    "clienteNombre": "Juan Pérez",
    "mascotaNombre": "Firulais",
    "totalMedicamentos": 3,
    "indicacionesGenerales": null,
    "estadoDespacho": "PENDIENTE_DESPACHO",
    "usuarioPreparacion": null,
    "medicamentos": [
      {
        "nombre": "Amoxicilina 500 mg",
        "dosis": 1.0,
        "unidadDosis": "tableta",
        "frecuenciaHoras": 8,
        "duracionDias": 7,
        "indicaciones": "Con comida",
        "cantidadPrescrita": 100,
        "cantidadDespachada": 0
      },
      {
        "nombre": "Ivermectina 1 %",
        "dosis": 1.0,
        "unidadDosis": "tableta",
        "frecuenciaHoras": 8,
        "duracionDias": 7,
        "indicaciones": "Con comida",
        "cantidadPrescrita": 1,
        "cantidadDespachada": 0
      },
      {
        "nombre": "Jeringa 5 ml",
        "dosis": 1.0,
        "unidadDosis": "tableta",
        "frecuenciaHoras": 8,
        "duracionDias": 7,
        "indicaciones": "Con comida",
        "cantidadPrescrita": 2,
        "cantidadDespachada": 0
      }
    ]
  }
]
```

## `GET /despacho/formula/{formulaId}` y `POST /despacho/formula/{formulaId}/preparar`

Rol: DESPACHO_MEDICO. Preparar no lleva cuerpo, devuelve lo mismo dentro de `data` y, repetido por el mismo usuario, la misma preparación; si la prepara otro, 409 con su nombre. `despachoId` es `null` si no hay preparación. Los lotes vienen en orden FEFO; los vencidos, al final con sugerencia 0.

```json
{
  "formulaId": 1,
  "despachoId": 1,
  "despachoNumero": "DP-000001",
  "usuarioPreparacion": "despacho",
  "numero": "FM-000001",
  "fecha": "2026-11-24T13:50:54",
  "medicoNombre": "Sofía Ramírez",
  "clienteNombre": "Juan Pérez",
  "clienteDocumento": "9000000001",
  "mascotaNombre": "Firulais",
  "indicacionesGenerales": null,
  "estadoDespacho": "EN_PREPARACION",
  "detalles": [
    {
      "formulaDetalleId": 1,
      "medicamentoId": 1,
      "codigo": "AMX00500",
      "nombre": "Amoxicilina 500 mg",
      "dosis": 1.0,
      "unidadDosis": "tableta",
      "frecuenciaHoras": 8,
      "duracionDias": 7,
      "viaAdministracion": "ORAL",
      "indicaciones": "Con comida",
      "cantidadPrescrita": 100,
      "cantidadDespachada": 0,
      "cantidadPendiente": 100,
      "existenciaTotalVigente": 180,
      "puedeEntregarse": true,
      "lotes": [
        {
          "loteId": 1,
          "numeroLote": "AMX-A",
          "fechaVencimiento": "2026-12-14",
          "cantidadDisponible": 80,
          "vencido": false,
          "proximoVencer": true,
          "cantidadSugerida": 80
        },
        {
          "loteId": 2,
          "numeroLote": "AMX-B",
          "fechaVencimiento": "2027-09-20",
          "cantidadDisponible": 100,
          "vencido": false,
          "proximoVencer": false,
          "cantidadSugerida": 20
        }
      ]
    },
    {
      "formulaDetalleId": 2,
      "medicamentoId": 3,
      "codigo": "IVE00010",
      "nombre": "Ivermectina 1 %",
      "dosis": 1.0,
      "unidadDosis": "tableta",
      "frecuenciaHoras": 8,
      "duracionDias": 7,
      "viaAdministracion": "ORAL",
      "indicaciones": "Con comida",
      "cantidadPrescrita": 1,
      "cantidadDespachada": 0,
      "cantidadPendiente": 1,
      "existenciaTotalVigente": 0,
      "puedeEntregarse": false,
      "lotes": []
    }
  ]
}
```

## `POST /despacho/{despachoId}/confirmar`

Rol: DESPACHO_MEDICO (quien la prepara; otro, 403). Cuerpo: `{ "observacion", "items": [{ "formulaDetalleId", "loteId", "cantidad" }] }`. Encabezado opcional `Idempotency-Key` (hasta 64 caracteres). Sin items solo se admite si nada de lo pendiente tiene existencias. `fecha` es la de confirmación; `estado`, el del despacho; `estadoDespacho`, el actual de la fórmula.

```json
{
  "status": 200,
  "message": "Despacho confirmado.",
  "data": {
    "id": 1,
    "numero": "DP-000001",
    "formulaId": 1,
    "formulaNumero": "FM-000001",
    "fecha": "2026-11-24T13:50:57",
    "fechaInicio": "2026-11-24T13:50:55",
    "mascotaNombre": "Firulais",
    "clienteNombre": "Juan Pérez",
    "medicoNombre": "Sofía Ramírez",
    "usuario": "despacho",
    "estado": "CONFIRMADO",
    "estadoDespacho": "DESPACHO_PARCIAL",
    "observacion": "Entrega",
    "revertido": false,
    "fechaReversion": null,
    "usuarioReversion": null,
    "motivoReversion": null,
    "items": [
      {
        "formulaDetalleId": 1,
        "medicamentoId": 1,
        "medicamentoNombre": "Amoxicilina 500 mg",
        "loteId": 1,
        "numeroLote": "AMX-A",
        "fechaVencimiento": "2026-12-14",
        "cantidad": 80,
        "facturado": false
      },
      {
        "formulaDetalleId": 1,
        "medicamentoId": 1,
        "medicamentoNombre": "Amoxicilina 500 mg",
        "loteId": 2,
        "numeroLote": "AMX-B",
        "fechaVencimiento": "2027-09-20",
        "cantidad": 20,
        "facturado": false
      },
      {
        "formulaDetalleId": 3,
        "medicamentoId": 13,
        "medicamentoNombre": "Jeringa 5 ml",
        "loteId": 16,
        "numeroLote": "JER-A",
        "fechaVencimiento": null,
        "cantidad": 2,
        "facturado": false
      }
    ],
    "sinExistencias": [
      "Ivermectina 1 %"
    ]
  }
}
```

Si un item falla no se despacha nada; `detalles` indica cuál por su posición:

```json
{
  "detalles": [
    {
      "campo": "items[0]",
      "mensaje": "Amoxicilina 500 mg: el lote AMX-A solo tiene 80."
    },
    {
      "campo": "items[0]",
      "mensaje": "Amoxicilina 500 mg: solo hay 100 pendientes por entregar."
    }
  ],
  "error": "Conflict",
  "message": "No se despachó nada: Amoxicilina 500 mg: el lote AMX-A solo tiene 80. Amoxicilina 500 mg: solo hay 100 pendientes por entregar.",
  "status": 409
}
```

## `POST /despacho/{despachoId}/liberar` (adicional al contrato)

Rol: DESPACHO_MEDICO (quien prepara), ADMINISTRADOR. Sin cuerpo. Cierra la preparación sin entregar, para que otro la tome. `data` como en preparar.

## `POST /despacho/formula/{formulaId}/cancelar`

Rol: DESPACHO_MEDICO (sin entregas), ADMINISTRADOR. Cuerpo `{ "motivo" }`. `data` como en preparar.

## `POST /despacho/{despachoId}/revertir`

Rol: ADMINISTRADOR. Cuerpo `{ "motivo" }`. 409 si no está confirmado, no entregó nada o tiene algo facturado. `data` como en confirmar, con `revertido: true`.

## `GET /despacho/historial?desde=&hasta=&texto=&estado=`

Arreglo como el `data` de confirmar, el más reciente primero, sin preparaciones abiertas. `estado` admite el del despacho (`CONFIRMADO`, `REVERTIDO`, `CANCELADO`) o el de la fórmula.

## `GET /despacho/medicamento/{medicamentoId}/historial`

```json
[
  {
    "despachoId": 1,
    "despachoNumero": "DP-000001",
    "formulaId": 1,
    "formulaNumero": "FM-000001",
    "fecha": "2026-11-24T13:50:57",
    "usuario": "despacho",
    "medicoNombre": "Sofía Ramírez",
    "clienteNombre": "Juan Pérez",
    "mascotaNombre": "Firulais",
    "loteId": 2,
    "numeroLote": "AMX-B",
    "cantidad": 20,
    "estado": "CONFIRMADO",
    "movimientoId": 46,
    "saldoLote": 80,
    "saldo": 80,
    "movimientoReversionId": null
  }
]
```

## `GET /despacho/formula/{formulaId}/historial-estados`

```json
[
  {
    "id": 2,
    "estadoAnterior": null,
    "estadoNuevo": "PENDIENTE_DESPACHO",
    "estadoFormulaAnterior": "BORRADOR",
    "estadoFormulaNuevo": "CONFIRMADA",
    "motivo": null,
    "usuario": "medico1",
    "fecha": "2026-11-24T13:50:55"
  },
  {
    "id": 3,
    "estadoAnterior": "PENDIENTE_DESPACHO",
    "estadoNuevo": "EN_PREPARACION",
    "estadoFormulaAnterior": "CONFIRMADA",
    "estadoFormulaNuevo": "CONFIRMADA",
    "motivo": null,
    "usuario": "despacho",
    "fecha": "2026-11-24T13:50:55"
  }
]
```

## `GET /despacho/{despachoId}/comprobante/pdf`

Solo de despachos confirmados o revertidos (409 si no). Descarga `despacho_<numero>.pdf`.

# Facturación

Rol: ADMINISTRADOR y SECRETARIA; anular, solo ADMINISTRADOR.

## `GET /factura/citas-pendientes`

Citas ATENDIDAS, con atención finalizada y sin factura vigente.

```json
[
  {
    "citaId": 8,
    "fecha": "2026-11-24T14:36:15",
    "clienteId": 1,
    "clienteNombre": "Juan Pérez",
    "clienteDocumento": "9000000001",
    "mascotaId": 1,
    "mascotaNombre": "Firulais",
    "medicoNombre": "Sofía Ramírez",
    "motivo": "Control",
    "despachoPendiente": true
  }
]
```

## `GET /factura/previsualizar/{citaId}`

Servicios con su tarifa vigente y medicamentos dispensados con el precio congelado en el despacho. `topeDescuento` es el máximo del usuario que consulta. 409 si la cita no se puede facturar.

```json
{
  "citaId": 8,
  "fechaCita": "2026-11-24T14:36:15",
  "clienteId": 1,
  "clienteNombre": "Juan Pérez",
  "clienteDocumento": "9000000001",
  "mascotaId": 1,
  "mascotaNombre": "Firulais",
  "medicoNombre": "Sofía Ramírez",
  "especialidadNombre": "Medicina general veterinaria",
  "detalles": [
    {
      "itemRef": "SERVICIO-1",
      "tipoItem": "SERVICIO",
      "descripcion": "Consulta general",
      "cantidad": 1,
      "precioUnitario": 50000.0,
      "subtotal": 50000.0,
      "porcentajeDescuento": 0,
      "valorDescuento": 0.0,
      "motivoDescuento": null,
      "porcentajeIva": 19.0,
      "valorIva": 9500.0,
      "total": 59500.0
    },
    {
      "itemRef": "MEDICAMENTO-2",
      "tipoItem": "MEDICAMENTO",
      "descripcion": "Jeringa 5 ml",
      "cantidad": 2,
      "precioUnitario": 900.0,
      "subtotal": 1800.0,
      "porcentajeDescuento": 0,
      "valorDescuento": 0.0,
      "motivoDescuento": null,
      "porcentajeIva": 19.0,
      "valorIva": 342.0,
      "total": 2142.0
    }
  ],
  "subtotal": 139300.0,
  "descuentos": 0.0,
  "iva": 25042.0,
  "total": 164342.0,
  "topeDescuento": 10.0,
  "advertencias": [
    "Hay medicamentos pendientes de despacho: los no despachados no se facturan."
  ]
}
```

## `POST /factura/generar`

Cuerpo: `{ "citaId", "observacion", "descuentos": [{ "itemRef", "porcentaje", "motivo" }] }`. `descuentos` es opcional. 409 si ya tiene factura, si un servicio no tiene tarifa o si el descuento supera el tope (con `detalles`, por ejemplo `descuentos[0].porcentaje`). `data` es la factura completa, igual que `GET /factura/{id}`; en los listados no vienen `detalles` ni `pagos`.

```json
{
  "status": 200,
  "message": "Factura generada correctamente.",
  "data": {
    "id": 1,
    "numero": "FV-000001",
    "fecha": "2026-11-24T14:36:16",
    "citaId": 8,
    "clienteId": 1,
    "clienteNombre": "Juan Pérez",
    "clienteDocumento": "CC 9000000001",
    "mascotaId": 1,
    "mascotaNombre": "Firulais",
    "medicoNombre": "Sofía Ramírez",
    "especialidadNombre": "Medicina general veterinaria",
    "subtotal": 139300.0,
    "descuentos": 5000.0,
    "iva": 24092.0,
    "total": 158392.0,
    "pagado": 0.0,
    "saldo": 158392.0,
    "estado": "PENDIENTE",
    "observacion": "Gracias",
    "usuario": "secretaria",
    "fechaAnulacion": null,
    "usuarioAnulacion": null,
    "motivoAnulacion": null,
    "detalles": [
      {
        "itemRef": "SERVICIO-1",
        "tipoItem": "SERVICIO",
        "descripcion": "Consulta general",
        "cantidad": 1,
        "precioUnitario": 50000.0,
        "subtotal": 50000.0,
        "porcentajeDescuento": 10,
        "valorDescuento": 5000.0,
        "motivoDescuento": "Cliente frecuente",
        "porcentajeIva": 19.0,
        "valorIva": 8550.0,
        "total": 53550.0
      }
    ],
    "pagos": []
  }
}
```

## `POST /factura/{id}/pagos`

Cuerpo: `{ "valor", "medioPago", "referencia" }`. `data` es la factura completa actualizada. 409 si supera el saldo o la factura está PAGADA o ANULADA.

## `GET /factura/{id}/pagos`

```json
[
  {
    "id": 1,
    "fecha": "2026-11-24T14:36:17",
    "valor": 47517.6,
    "medioPago": "TARJETA_DEBITO",
    "referencia": "T-5",
    "usuario": "secretaria",
    "anulado": false
  }
]
```

## `POST /factura/{id}/anular`

Cuerpo `{ "motivo" }`. `data` es la factura completa; `message` avisa si hay pagos que se conservan.

```json
{
  "status": 200,
  "message": "Factura anulada. Tiene pagos por $ 142.552,80 que se conservan y debe gestionar con el propietario.",
  "data": {
    "id": 1,
    "numero": "FV-000001",
    "estado": "ANULADA",
    "fechaAnulacion": "2026-11-24T14:36:18",
    "usuarioAnulacion": "admin",
    "motivoAnulacion": "Error en el servicio"
  }
}
```

## `GET /factura/{id}/historial-estados`

```json
[
  {
    "id": 1,
    "estadoAnterior": null,
    "estadoNuevo": "PENDIENTE",
    "motivo": null,
    "usuario": "secretaria",
    "fecha": "2026-11-24T14:36:17"
  },
  {
    "id": 2,
    "estadoAnterior": "PENDIENTE",
    "estadoNuevo": "PAGO_PARCIAL",
    "motivo": "Pago de $ 47.517,60 (TARJETA_DEBITO)",
    "usuario": "secretaria",
    "fecha": "2026-11-24T14:36:17"
  }
]
```

## Otras consultas

- `GET /factura/listar?numero=&estado=&desde=&hasta=&clienteId=&mascotaId=`
- `GET /factura/cliente/{clienteId}` y `GET /factura/mascota/{mascotaId}`
- `GET /factura/{id}/pdf`: descarga `factura_<numero>.pdf`, con marca de agua si está ANULADA.

# Reportes

Rol: ADMINISTRADOR.

## `GET /reporte/catalogo`

`opciones` solo viene si el filtro tiene valores fijos.

```json
[
  {
    "codigo": "ingresos-periodo",
    "titulo": "Ingresos por período",
    "grupo": "Financieros",
    "filtros": [
      {
        "clave": "desde",
        "obligatorio": true
      },
      {
        "clave": "hasta",
        "obligatorio": true
      },
      {
        "clave": "agrupacion",
        "obligatorio": false,
        "opciones": [
          "DIA",
          "MES",
          "ANIO"
        ]
      }
    ]
  },
  {
    "codigo": "kardex",
    "titulo": "Kardex por medicamento",
    "grupo": "Inventario",
    "filtros": [
      {
        "clave": "desde",
        "obligatorio": true
      },
      {
        "clave": "hasta",
        "obligatorio": true
      },
      {
        "clave": "medicamentoId",
        "obligatorio": true
      }
    ]
  }
]
```

## `GET /reporte/{codigo}?desde=&hasta=&<filtros>`

Vista previa. `desde` y `hasta` (`yyyy-MM-dd`) son obligatorias, salvo en los de existencias, que muestran la situación actual: `existencias`, `valor-inventario`, `agotados`, `stock-minimo`, `proximos-vencer` y `vencidos`. `totales` usa las claves de las columnas. Un valor puede ser `null`. 400 si falta un filtro obligatorio o no es válido.

```json
{
  "codigo": "ingresos-periodo",
  "titulo": "Ingresos por período",
  "generadoEn": "2026-11-24T14:48:56",
  "generadoPor": "admin",
  "filtros": {
    "desde": "2026-11-01",
    "hasta": "2026-12-31",
    "agrupacion": "MES"
  },
  "columnas": [
    {
      "clave": "periodo",
      "titulo": "Período",
      "tipo": "texto"
    },
    {
      "clave": "facturas",
      "titulo": "Facturas",
      "tipo": "numero"
    },
    {
      "clave": "subtotal",
      "titulo": "Subtotal",
      "tipo": "moneda"
    },
    {
      "clave": "descuentos",
      "titulo": "Descuentos",
      "tipo": "moneda"
    },
    {
      "clave": "iva",
      "titulo": "IVA",
      "tipo": "moneda"
    },
    {
      "clave": "facturado",
      "titulo": "Facturado",
      "tipo": "moneda"
    },
    {
      "clave": "recaudado",
      "titulo": "Recaudado",
      "tipo": "moneda"
    }
  ],
  "filas": [
    {
      "periodo": "2026-11",
      "facturas": 7,
      "subtotal": 921100.0,
      "descuentos": 0.0,
      "iva": 173584.0,
      "facturado": 1094684.0,
      "recaudado": 142552.8
    }
  ],
  "totales": {
    "facturas": 7,
    "subtotal": 921100.0,
    "descuentos": 0.0,
    "iva": 173584.0,
    "facturado": 1094684.0,
    "recaudado": 142552.8
  }
}
```

## `GET /reporte/{codigo}/exportar?formato=pdf|xlsx&desde=&hasta=&<filtros>`

Mismos filtros. Descarga `<codigo>_<fecha>.pdf` o `.xlsx`.

## Columnas y totales

| Código | Título | Columnas | Totales |
|---|---|---|---|
| `ingresos-periodo` | Ingresos por período | periodo, facturas, subtotal, descuentos, iva, facturado, recaudado | facturas, subtotal, descuentos, iva, facturado, recaudado |
| `ingresos-consultas` | Ingresos por consultas | nombre, cantidad, subtotal, descuentos, iva, facturado, recaudado | cantidad, subtotal, descuentos, iva, facturado, recaudado |
| `ingresos-medicamentos` | Ingresos por venta de medicamentos | nombre, cantidad, subtotal, descuentos, iva, facturado, recaudado | cantidad, subtotal, descuentos, iva, facturado, recaudado |
| `ingresos-especialidad` | Ingresos por especialidad | nombre, facturas, facturado, recaudado | facturas, facturado, recaudado |
| `ingresos-veterinario` | Ingresos por veterinario | nombre, facturas, facturado, recaudado | facturas, facturado, recaudado |
| `facturas` | Facturas | numero, fecha, cliente, mascota, veterinario, estado, subtotal, descuentos, iva, total, pagado, saldo | subtotal, descuentos, iva, total, pagado, saldo |
| `impuestos` | Impuestos | porcentaje, renglones, base, iva | renglones, base, iva |
| `descuentos` | Descuentos | fecha, factura, cliente, concepto, subtotal, porcentaje, descuento, motivo, usuario | subtotal, descuento |
| `existencias` | Existencias | codigo, nombre, tipo, unidad, cantidad, vigente, vencida, stockMinimo | cantidad, vigente, vencida |
| `agotados` | Productos agotados | codigo, nombre, tipo, unidad, cantidad, vigente, vencida, stockMinimo | cantidad, vigente, vencida |
| `stock-minimo` | Productos en stock mínimo | codigo, nombre, tipo, unidad, cantidad, vigente, vencida, stockMinimo | cantidad, vigente, vencida |
| `valor-inventario` | Valor del inventario | codigo, nombre, cantidad, precioVenta, valorCosto, valorVenta, utilidad | cantidad, valorCosto, valorVenta, utilidad |
| `proximos-vencer` | Próximos a vencer | codigo, nombre, lote, vencimiento, dias, cantidad, costo, valorCosto | cantidad, valorCosto |
| `vencidos` | Vencidos | codigo, nombre, lote, vencimiento, dias, cantidad, costo, valorCosto | cantidad, valorCosto |
| `movimientos` | Entradas y salidas | fecha, tipo, codigo, nombre, lote, entrada, salida, saldo, costo, valorEntrada, valorSalida, usuario, referencia | entrada, salida, valorEntrada, valorSalida |
| `kardex` | Kardex por medicamento | fecha, tipo, lote, entrada, salida, saldo, costo, valorEntrada, valorSalida, usuario, referencia | entrada, salida, valorEntrada, valorSalida, saldo |
| `citas-periodo` | Citas por período | periodo, programadas, confirmadas, enAtencion, atendidas, canceladas, total | programadas, confirmadas, enAtencion, atendidas, canceladas, total |
| `citas-estado` | Citas por estado | inicio, fin, mascota, cliente, veterinario, servicio, motivo, estado, cantidad | cantidad |
| `consultas-veterinario` | Consultas por veterinario | nombre, consultas, mascotas, canceladas | consultas, canceladas |
| `consultas-especialidad` | Consultas por especialidad | nombre, consultas, mascotas, canceladas | consultas, canceladas |
| `mascotas-atendidas` | Mascotas atendidas | mascota, especie, raza, cliente, atenciones, ultima, mascotas | atenciones, mascotas |

# Citas, horarios y calendario

`desde` y `hasta` aceptan `yyyy-MM-dd` (el día final se incluye), `yyyy-MM-ddTHH:mm:ss` o con zona, como los envía FullCalendar. Un `+` en la URL debe codificarse como `%2B`. Rango máximo: un año.

## `GET /cita/calendario?desde=&hasta=&medicoId=`

Rol: MEDICO (siempre lo suyo; ignora `medicoId`), ADMINISTRADOR, SECRETARIA. Incluye canceladas. `servicioId`, `servicioNombre` y `motivoCancelacion` pueden ser `null`.

```json
[
  {
    "id": 4,
    "title": "Max - Miguel Castro",
    "start": "2026-12-01T15:00:00",
    "end": "2026-12-01T15:30:00",
    "estado": "CONFIRMADA",
    "color": "#0d6efd",
    "extendedProps": {
      "motivo": "[Prueba] Vacunación",
      "mascotaId": 2,
      "mascotaNombre": "Max",
      "especie": "Perro",
      "raza": "Criollo",
      "edad": 8,
      "clienteId": 5,
      "clienteNombre": "Miguel Castro",
      "clienteTelefono": "3000000005",
      "clienteDocumento": "9000000005",
      "medicoId": 1,
      "medicoNombre": "Sofía Ramírez",
      "servicioId": 4,
      "servicioNombre": "Vacunación",
      "motivoCancelacion": null
    }
  }
]
```

## `POST /cita/crear` y `POST /cita/actualizar`

Rol: ADMINISTRADOR, SECRETARIA, MEDICO (para sí mismo o las suyas). Cuerpo: `{ "id" (solo actualizar), "mascotaId", "medicoId", "fechaInicio", "motivo", "servicioId", "clienteId", "estado", "motivoCancelacion" }`. `servicioId` y `clienteId` son opcionales; sin servicio dura 30 minutos. Al actualizar, `estado` es opcional y debe ser una transición válida; si es CANCELADA exige `motivoCancelacion`.

Antes devolvían `{status, message}`; ahora agregan `data` con la cita completa (objetos anidados como en `GET /cita/{id}`):

```json
{
  "status": 200,
  "message": "Cita creada correctamente.",
  "data": {
    "estado": "PROGRAMADA",
    "fechaCreacionRegistro": "2026-11-24T13:10:44",
    "fechaFin": "2026-11-30T14:30:00",
    "fechaHora": "2026-11-30T14:00:00",
    "fechaInicio": "2026-11-30T14:00:00",
    "fechaModificacionRegistro": null,
    "id": 8,
    "motivo": "Vacuna anual",
    "motivoCancelacion": null,
    "usuarioCreacionId": 5,
    "version": 0,
    "cliente": "{...}",
    "mascota": "{...}",
    "medico": "{...}",
    "servicio": "{...}"
  }
}
```

409 según la regla: fecha pasada, médico o mascota inactivos, cliente que no es propietario, servicio inactivo, cruza la medianoche, fuera del horario, médico ocupado, mascota ocupada, cita no editable o transición inválida.

```json
{
  "detalles": [
    {
      "campo": "fechaInicio",
      "mensaje": "La cita está fuera del horario de disponibilidad del médico."
    }
  ],
  "error": "Conflict",
  "message": "La cita está fuera del horario de disponibilidad del médico.",
  "status": 409
}
```

## `PATCH /cita/{id}/reprogramar` y `PATCH /cita/{id}/estado`

Mismos roles. Cuerpos: `{ "fechaInicio", "motivo" (opcional) }` y `{ "estado", "motivo" }` (obligatorio si es CANCELADA). Respuesta igual a la de crear.

## `GET /cita/{id}/historial-estados`

Del más antiguo al más reciente. La creación lleva `estadoAnterior` nulo; una reprogramación lleva las dos fechas.

```json
[
  {
    "id": 1,
    "estadoAnterior": null,
    "estadoNuevo": "PROGRAMADA",
    "fechaInicioAnterior": null,
    "fechaInicioNueva": "2026-11-30T14:00:00",
    "motivo": null,
    "usuario": "secretaria",
    "fecha": "2026-11-24T13:10:45"
  },
  {
    "id": 2,
    "estadoAnterior": "PROGRAMADA",
    "estadoNuevo": "PROGRAMADA",
    "fechaInicioAnterior": "2026-11-30T14:00:00",
    "fechaInicioNueva": "2026-11-30T15:00:00",
    "motivo": "Pedido del propietario",
    "usuario": "medico1",
    "fecha": "2026-11-24T13:10:45"
  },
  {
    "id": 3,
    "estadoAnterior": "PROGRAMADA",
    "estadoNuevo": "CONFIRMADA",
    "fechaInicioAnterior": null,
    "fechaInicioNueva": null,
    "motivo": null,
    "usuario": "medico1",
    "fecha": "2026-11-24T13:10:45"
  }
]
```

## `GET /cita/historial-mascota/{mascotaId}`

Rol: ADMINISTRADOR, SECRETARIA, MEDICO (de cualquier médico: son antecedentes del paciente). Arreglo de citas completas, la más reciente primero, con canceladas.

## `GET /cita/resumen-estados?desde=&hasta=&medicoId=`

Rol: MEDICO (las suyas), ADMINISTRADOR. Cuenta por fecha de inicio; siempre vienen los cinco estados.

```json
{
  "PROGRAMADA": 1,
  "CONFIRMADA": 3,
  "EN_ATENCION": 0,
  "ATENDIDA": 1,
  "CANCELADA": 1
}
```

## `GET /cita/reporte/excel?desde=&hasta=&medicoId=`

Rol: MEDICO (las suyas), ADMINISTRADOR (`medicoId` opcional; sin él agrega la columna Médico). Descarga `citas_<desde>_<hasta>.xlsx`.

## `GET /medico/listar-disponibles?inicio=&fin=`

Arreglo de médicos activos cuyo horario cubre el rango y sin citas que se crucen. También acepta `fechaInicio` y `fechaFin`, los nombres anteriores.

# Inventario

ADMINISTRADOR escribe; DESPACHO_MEDICO consulta todo; MEDICO solo productos y disponibilidad.

## `GET /inventario/productos?texto=&tipo=&estadoStock=&soloActivos=`

Busca en código y nombre, sin distinguir tildes ni mayúsculas. No incluye `lotes`.

```json
[
  {
    "id": 1,
    "codigo": "AMX00500",
    "nombre": "Amoxicilina 500 mg",
    "tipoProducto": "MEDICAMENTO",
    "descripcion": "Amoxicilina 500 mg",
    "presentacion": "Caja x 10 tabletas",
    "unidadMedida": "tableta",
    "requiereInvima": true,
    "registroInvima": "INVIMA PRUEBA-001",
    "precioVenta": 1500.0,
    "impuestoId": 1,
    "porcentajeIva": 0.0,
    "stockMinimo": 20,
    "fechaProximoPedido": "2026-12-14",
    "activo": true,
    "cantidadDisponible": 180,
    "precioCompraUltimo": 950.0,
    "precioCompraPromedio": 927.78,
    "proximoVencimiento": "2026-12-14",
    "estadosStock": [
      "PROXIMO_VENCER"
    ]
  }
]
```

## `GET /inventario/productos/{id}`

Igual, con `lotes` (el más reciente primero).

```json
{
  "id": 1,
  "codigo": "AMX00500",
  "nombre": "Amoxicilina 500 mg",
  "tipoProducto": "MEDICAMENTO",
  "descripcion": "Amoxicilina 500 mg",
  "presentacion": "Caja x 10 tabletas",
  "unidadMedida": "tableta",
  "requiereInvima": true,
  "registroInvima": "INVIMA PRUEBA-001",
  "precioVenta": 1500.0,
  "impuestoId": 1,
  "porcentajeIva": 0.0,
  "stockMinimo": 20,
  "fechaProximoPedido": "2026-12-14",
  "activo": true,
  "cantidadDisponible": 180,
  "precioCompraUltimo": 950.0,
  "precioCompraPromedio": 927.78,
  "proximoVencimiento": "2026-12-14",
  "estadosStock": [
    "PROXIMO_VENCER"
  ],
  "lotes": [
    {
      "id": 2,
      "numeroLote": "AMX-B",
      "precioCompra": 950.0,
      "cantidadInicial": 100,
      "cantidadDisponible": 100,
      "fechaCompra": "2026-11-04",
      "fechaIngreso": "2026-11-05",
      "fechaVencimiento": "2027-09-20",
      "vencido": false,
      "diasParaVencer": 300
    },
    {
      "id": 1,
      "numeroLote": "AMX-A",
      "precioCompra": 900.0,
      "cantidadInicial": 100,
      "cantidadDisponible": 80,
      "fechaCompra": "2026-09-25",
      "fechaIngreso": "2026-09-26",
      "fechaVencimiento": "2026-12-14",
      "vencido": false,
      "diasParaVencer": 20
    }
  ]
}
```

## `POST /inventario/productos`, `PUT /inventario/productos/{id}`

Cuerpo: `codigo`, `nombre`, `tipoProducto`, `descripcion`, `presentacion`, `unidadMedida`, `requiereInvima`, `registroInvima`, `precioVenta`, `impuestoId` o `porcentajeIva`, `stockMinimo`, `fechaProximoPedido` y, solo al crear, `loteInicial` (cuerpo de lotes). Editar no cambia cantidades.

Respuesta de crear, editar, `PATCH /{id}/estado`, `POST /{id}/lotes` y `POST /{id}/ajustes`: `{ "status": 200, "message": "...", "data": <producto con lotes> }`.

## `POST /inventario/productos/{id}/lotes`

Cuerpo: `{ "numeroLote", "precioCompra", "cantidad", "fechaCompra", "fechaIngreso" (hoy si falta), "fechaVencimiento" (obligatoria en MEDICAMENTO), "observacion" }`.

## `POST /inventario/productos/{id}/ajustes`

Cuerpo: `{ "loteId", "tipo": "AJUSTE_POSITIVO" | "AJUSTE_NEGATIVO", "cantidad", "motivo" }`. 409 si dejaría el lote en negativo.

## `GET /inventario/productos/{id}/lotes?soloVigentes=`

Arreglo de lotes, con la forma mostrada arriba.

## `GET /inventario/productos/{id}/movimientos?desde=&hasta=` y `GET /inventario/movimientos?desde=&hasta=&tipo=&medicamentoId=`

Más reciente primero. `saldo` es el del producto. `referencia` indica el despacho o la carga.

```json
[
  {
    "id": 32,
    "fecha": "2026-11-24T17:49:37",
    "tipo": "AJUSTE_NEGATIVO",
    "medicamentoId": 1,
    "medicamentoCodigo": "AMX00500",
    "medicamentoNombre": "Amoxicilina 500 mg",
    "loteId": 1,
    "numeroLote": "AMX-A",
    "cantidad": 20,
    "saldoLote": 80,
    "saldo": 180,
    "costoUnitario": 900.0,
    "usuario": "admin",
    "formulaId": null,
    "despachoId": null,
    "cargaId": null,
    "movimientoRevertidoId": null,
    "referencia": null,
    "motivo": "Conteo físico"
  }
]
```

## `GET /inventario/alertas`

```json
{
  "agotados": 2,
  "bajoStock": 3,
  "proximosVencer": 3,
  "vencidos": 2
}
```

## `GET /inventario/disponibilidad/{medicamentoId}`

Lotes en orden FEFO.

```json
{
  "medicamentoId": 1,
  "cantidadDisponibleVigente": 90,
  "cantidadVencida": 0,
  "lotesVigentes": [
    {
      "id": 1,
      "numeroLote": "AMX-A",
      "precioCompra": 900.0,
      "cantidadInicial": 100,
      "cantidadDisponible": 80,
      "fechaCompra": "2026-09-25",
      "fechaIngreso": "2026-09-26",
      "fechaVencimiento": "2026-12-14",
      "vencido": false,
      "diasParaVencer": 20
    }
  ]
}
```

# Carga masiva

Rol: ADMINISTRADOR.

## `POST /inventario/carga/previsualizar`

`multipart/form-data`, campo `archivo` (.xlsx o .csv UTF-8; 5 MB y 5000 filas). `GET /inventario/carga/{cargaId}` devuelve lo mismo. `fila` es el número visible en el archivo. Las `advertencias` no impiden importar.

```json
{
  "cargaId": 1,
  "nombreArchivo": "carga_inventario_60.xlsx",
  "formato": "xlsx",
  "estado": "PREVISUALIZADA",
  "totalFilas": 60,
  "filasValidas": 55,
  "filasConError": 5,
  "filasImportadas": 0,
  "usuario": "admin",
  "fecha": "2026-11-24T12:59:04",
  "usuarioConfirmacion": null,
  "fechaConfirmacion": null,
  "filas": [
    {
      "fila": 2,
      "valida": true,
      "accion": "NUEVO_PRODUCTO",
      "datos": {
        "codigo": "EVI00001",
        "nombre": "Producto evidencia 01",
        "tipo_producto": "MEDICAMENTO",
        "descripcion": "Dato ficticio de evidencia",
        "presentacion": "Caja x 10",
        "unidad_medida": "unidad",
        "requiere_invima": "SI",
        "registro_invima": "INVIMA PRUEBA-E001",
        "precio_compra": "510",
        "precio_venta": "915",
        "porcentaje_iva": "0",
        "stock_minimo": "10",
        "numero_lote": "LEV001",
        "cantidad": "21",
        "fecha_compra": "2026-10-01",
        "fecha_vencimiento": "2028-06-30",
        "fecha_proximo_pedido": "2027-01-15"
      },
      "errores": [],
      "advertencias": [],
      "importada": false,
      "errorImportacion": null
    },
    {
      "fila": 11,
      "valida": false,
      "accion": null,
      "datos": {
        "codigo": "EVI00010",
        "nombre": "Producto evidencia 10",
        "tipo_producto": "MEDICAMENTO",
        "descripcion": "Dato ficticio de evidencia",
        "presentacion": "Caja x 10",
        "unidad_medida": "unidad",
        "requiere_invima": "SI",
        "registro_invima": "INVIMA PRUEBA-E010",
        "precio_compra": "600",
        "precio_venta": "-100",
        "porcentaje_iva": "0",
        "stock_minimo": "10",
        "numero_lote": "LEV010",
        "cantidad": "30",
        "fecha_compra": "2026-10-01",
        "fecha_vencimiento": "2028-06-30",
        "fecha_proximo_pedido": "2027-01-15"
      },
      "errores": [
        {
          "campo": "precio_venta",
          "mensaje": "Debe ser mayor que 0"
        }
      ],
      "advertencias": [],
      "importada": false,
      "errorImportacion": null
    }
  ]
}
```

## `POST /inventario/carga/{cargaId}/confirmar` y `/descartar`

`{ "status": 200, "message": "...", "data": <carga con filas> }`. Confirmar dos veces: 409. Estado `CONFIRMADA` si se importaron todas las filas del archivo; `CONFIRMADA_CON_ERRORES` si alguna no. Una fila válida que falla al importar lleva `importada: false` y `errorImportacion`.

## `GET /inventario/carga/historial?desde=&hasta=`

```json
[
  {
    "cargaId": 1,
    "nombreArchivo": "carga_inventario_60.xlsx",
    "formato": "xlsx",
    "estado": "CONFIRMADA_CON_ERRORES",
    "totalFilas": 60,
    "filasValidas": 55,
    "filasConError": 5,
    "filasImportadas": 55,
    "usuario": "admin",
    "fecha": "2026-11-24T12:59:04",
    "usuarioConfirmacion": "admin",
    "fechaConfirmacion": "2026-11-24T12:59:08"
  }
]
```

## Descargas

- `GET /inventario/carga/plantilla?formato=xlsx|csv`
- `GET /inventario/carga/{cargaId}/errores/archivo?formato=xlsx|csv`: columnas `fila, campo, valor, motivo` y los datos originales.

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

# Todos los endpoints

Tabla generada desde OpenAPI (128 operaciones). Swagger: http://localhost:8080/clinica/v1/swagger-ui.html

## Atención médica

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/atencion/cita/{citaId}` | Consultar atención, mascota, propietario y antecedentes | ADMINISTRADOR, MEDICO |
| POST | `/atencion/iniciar/{citaId}` | Iniciar la atención de una cita | MEDICO |
| PUT | `/atencion/{id}` | Guardar el borrador de la atención | MEDICO |
| POST | `/atencion/{id}/finalizar` | Finalizar la atención | MEDICO |
| GET | `/atencion/{id}/prefactura/pdf` | Descargar la prefactura en PDF | ADMINISTRADOR, MEDICO |

## Autenticación

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/auth/cerrar-sesion` | Cerrar la sesión del token | cualquier usuario autenticado |
| POST | `/auth/login` | Iniciar sesión: verifica credenciales y envía el OTP por correo | público |
| POST | `/auth/validate-token` | Validar el OTP y obtener el JWT | público |

## Carga masiva de inventario

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/inventario/carga/historial` | Historial de cargas | ADMINISTRADOR |
| GET | `/inventario/carga/plantilla` | Descargar la plantilla | ADMINISTRADOR |
| POST | `/inventario/carga/previsualizar` | Previsualizar un archivo .xlsx o .csv | ADMINISTRADOR |
| GET | `/inventario/carga/{cargaId}` | Consultar una carga con sus filas | ADMINISTRADOR |
| POST | `/inventario/carga/{cargaId}/confirmar` | Confirmar la carga | ADMINISTRADOR |
| POST | `/inventario/carga/{cargaId}/descartar` | Descartar la carga | ADMINISTRADOR |
| GET | `/inventario/carga/{cargaId}/errores/archivo` | Descargar el informe de errores | ADMINISTRADOR |

## Citas y calendario

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/cita/actualizar` | Editar una cita | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/cita/calendario` | Eventos del calendario | ADMINISTRADOR, SECRETARIA, MEDICO |
| POST | `/cita/crear` | Crear una cita | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/cita/historial-mascota/{mascotaId}` | Historial de citas de una mascota, incluidas canceladas | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/cita/listar` | Listar citas | ADMINISTRADOR, SECRETARIA |
| GET | `/cita/listar-por-mascota` | Listar citas de una mascota | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/cita/listar-rango` | Listar citas por rango de fechas | ADMINISTRADOR, SECRETARIA |
| GET | `/cita/reporte/excel` | Reporte Excel de citas | ADMINISTRADOR, MEDICO |
| GET | `/cita/resumen-estados` | Conteo de citas por estado | ADMINISTRADOR, MEDICO |
| GET | `/cita/{id}` | Consultar una cita | ADMINISTRADOR, SECRETARIA, MEDICO |
| PATCH | `/cita/{id}/estado` | Confirmar o cancelar una cita | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/cita/{id}/historial-estados` | Historial de estados de la cita | ADMINISTRADOR, SECRETARIA, MEDICO |
| PATCH | `/cita/{id}/reprogramar` | Reprogramar una cita | ADMINISTRADOR, SECRETARIA, MEDICO |

## Clientes

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/cliente/buscar-por-documento` | Buscar un cliente por documento | ADMINISTRADOR, SECRETARIA, MEDICO |
| POST | `/cliente/guardar` | Crear un cliente | ADMINISTRADOR, SECRETARIA |
| GET | `/cliente/listar` | Listar clientes | ADMINISTRADOR, SECRETARIA, MEDICO |
| POST | `/cliente/{id}` | Actualizar un cliente | ADMINISTRADOR, SECRETARIA |

## Configuración de la clínica

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/configuracion-clinica` | Consultar la configuración | cualquier usuario autenticado |
| PUT | `/configuracion-clinica` | Actualizar la configuración | ADMINISTRADOR |

## Despacho

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/despacho/formula/{formulaId}` | Consultar una fórmula con lotes sugeridos (FEFO) | DESPACHO_MEDICO |
| POST | `/despacho/formula/{formulaId}/cancelar` | Cancelar el despacho de una fórmula | DESPACHO_MEDICO, ADMINISTRADOR |
| GET | `/despacho/formula/{formulaId}/historial-estados` | Historial de estados del despacho de una fórmula | DESPACHO_MEDICO, ADMINISTRADOR |
| POST | `/despacho/formula/{formulaId}/preparar` | Iniciar la preparación | DESPACHO_MEDICO |
| GET | `/despacho/formulas-pendientes` | Bandeja de fórmulas pendientes | DESPACHO_MEDICO |
| GET | `/despacho/historial` | Historial de despachos | DESPACHO_MEDICO, ADMINISTRADOR |
| GET | `/despacho/medicamento/{medicamentoId}/historial` | Historial de dispensaciones de un medicamento | DESPACHO_MEDICO, ADMINISTRADOR |
| GET | `/despacho/{despachoId}/comprobante/pdf` | Descargar el comprobante en PDF | DESPACHO_MEDICO, ADMINISTRADOR |
| POST | `/despacho/{despachoId}/confirmar` | Confirmar el despacho | DESPACHO_MEDICO |
| POST | `/despacho/{despachoId}/liberar` | Liberar una preparación sin entregar | DESPACHO_MEDICO, ADMINISTRADOR |
| POST | `/despacho/{despachoId}/revertir` | Revertir un despacho confirmado | ADMINISTRADOR |

## Especialidades

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| PUT | `/especializacion/actualizar` | Actualizar una especialidad | ADMINISTRADOR |
| POST | `/especializacion/crear` | Crear una especialidad | ADMINISTRADOR |
| GET | `/especializacion/listar` | Listar especialidades | cualquier usuario autenticado |
| PATCH | `/especializacion/{id}/estado` | Activar o inactivar una especialidad | ADMINISTRADOR |

## Facturación y pagos

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/factura/citas-pendientes` | Citas atendidas por facturar | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/cliente/{clienteId}` | Facturas de un cliente | ADMINISTRADOR, SECRETARIA |
| POST | `/factura/generar` | Generar la factura | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/listar` | Listar facturas | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/mascota/{mascotaId}` | Facturas de una mascota | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/previsualizar/{citaId}` | Previsualizar la factura de una cita | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/{id}` | Consultar una factura con renglones y pagos | ADMINISTRADOR, SECRETARIA |
| POST | `/factura/{id}/anular` | Anular una factura | ADMINISTRADOR |
| GET | `/factura/{id}/historial-estados` | Historial de estados de la factura | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/{id}/pagos` | Listar los pagos de una factura | ADMINISTRADOR, SECRETARIA |
| POST | `/factura/{id}/pagos` | Registrar un pago | ADMINISTRADOR, SECRETARIA |
| GET | `/factura/{id}/pdf` | Descargar la factura en PDF | ADMINISTRADOR, SECRETARIA |

## Fórmulas médicas

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/formula-medica/actualizar` | Retirado: responde 410 | MEDICO |
| GET | `/formula-medica/cita/{citaId}` | Fórmulas de una cita | ADMINISTRADOR, MEDICO |
| POST | `/formula-medica/crear` | Crear una fórmula en borrador | MEDICO |
| POST | `/formula-medica/guardar` | Retirado: responde 410 | MEDICO |
| GET | `/formula-medica/listar` | Listar fórmulas | ADMINISTRADOR, MEDICO |
| GET | `/formula-medica/{id}` | Consultar una fórmula | ADMINISTRADOR, MEDICO, DESPACHO_MEDICO |
| PUT | `/formula-medica/{id}` | Editar una fórmula en borrador | MEDICO |
| POST | `/formula-medica/{id}/anular` | Anular la fórmula | ADMINISTRADOR, MEDICO |
| POST | `/formula-medica/{id}/confirmar` | Confirmar la fórmula | MEDICO |
| GET | `/formula-medica/{id}/pdf` | Descargar la fórmula en PDF | ADMINISTRADOR, MEDICO, DESPACHO_MEDICO |

## Historias clínicas

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/anotacion-historia/crear` | Crear una anotación en la historia clínica | MEDICO |
| POST | `/historia-clinica/actualizar` | Actualizar una historia clínica | ADMINISTRADOR, MEDICO |
| POST | `/historia-clinica/guardar` | Crear una historia clínica | ADMINISTRADOR, MEDICO |
| GET | `/historia-clinica/listar` | Listar historias con anotaciones y fórmulas | ADMINISTRADOR, MEDICO |

## Impuestos

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/impuesto/crear` | Crear un impuesto | ADMINISTRADOR |
| GET | `/impuesto/listar` | Listar impuestos | cualquier usuario autenticado |
| PUT | `/impuesto/{id}` | Actualizar un impuesto | ADMINISTRADOR |

## Inventario

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/inventario/alertas` | Conteo de alertas | ADMINISTRADOR, DESPACHO_MEDICO |
| GET | `/inventario/disponibilidad/{medicamentoId}` | Disponibilidad vigente de un producto | ADMINISTRADOR, DESPACHO_MEDICO, MEDICO |
| GET | `/inventario/movimientos` | Movimientos de inventario | ADMINISTRADOR, DESPACHO_MEDICO |
| GET | `/inventario/productos` | Listar productos | ADMINISTRADOR, DESPACHO_MEDICO, MEDICO |
| POST | `/inventario/productos` | Crear un producto, con lote inicial opcional | ADMINISTRADOR |
| GET | `/inventario/productos/{id}` | Consultar un producto con sus lotes | ADMINISTRADOR, DESPACHO_MEDICO, MEDICO |
| PUT | `/inventario/productos/{id}` | Actualizar un producto | ADMINISTRADOR |
| POST | `/inventario/productos/{id}/ajustes` | Ajustar existencias con motivo | ADMINISTRADOR |
| PATCH | `/inventario/productos/{id}/estado` | Activar o inactivar un producto | ADMINISTRADOR |
| GET | `/inventario/productos/{id}/lotes` | Listar lotes de un producto | ADMINISTRADOR, DESPACHO_MEDICO |
| POST | `/inventario/productos/{id}/lotes` | Registrar una entrada por compra | ADMINISTRADOR |
| GET | `/inventario/productos/{id}/movimientos` | Movimientos de un producto | ADMINISTRADOR, DESPACHO_MEDICO |
| GET | `/medicamento/listar` | Listar medicamentos | cualquier usuario autenticado |

## Mascotas

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/mascota/actualizar` | Actualizar una mascota | ADMINISTRADOR, SECRETARIA |
| GET | `/mascota/buscar-by-cliente` | Mascotas de un cliente | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/mascota/buscar-by-raza` | Mascotas de una raza | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/mascota/buscar-nombre` | Buscar mascotas por nombre | ADMINISTRADOR, SECRETARIA, MEDICO |
| POST | `/mascota/guardar` | Crear una mascota | ADMINISTRADOR, SECRETARIA |
| GET | `/mascota/listar` | Listar mascotas | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/mascota/listar-ordenado` | Listar mascotas ordenadas por nombre | ADMINISTRADOR, SECRETARIA, MEDICO |
| POST | `/raza/guardar` | Crear una raza | ADMINISTRADOR, SECRETARIA |
| GET | `/raza/listar` | Listar razas | ADMINISTRADOR, SECRETARIA, MEDICO |

## Médicos y horarios

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/medico/actualizar` | Actualizar un médico | ADMINISTRADOR, SECRETARIA |
| POST | `/medico/crear` | Crear un médico | ADMINISTRADOR, SECRETARIA |
| GET | `/medico/listar` | Listar médicos | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/medico/listar-disponibles` | Médicos disponibles en un rango | ADMINISTRADOR, SECRETARIA, MEDICO |
| GET | `/medico/mis-horarios` | Horarios del médico autenticado | MEDICO |
| GET | `/medico/{id}/horarios` | Horarios de un médico | ADMINISTRADOR, SECRETARIA, MEDICO |
| PUT | `/medico/{id}/horarios` | Reemplazar los horarios de un médico | ADMINISTRADOR, SECRETARIA |

## Reportes

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/reporte/catalogo` | Catálogo de reportes y sus filtros | ADMINISTRADOR |
| GET | `/reporte/{codigo}` | Vista previa de un reporte | ADMINISTRADOR |
| GET | `/reporte/{codigo}/exportar` | Exportar un reporte a PDF o Excel | ADMINISTRADOR |

## Servicio

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/clinica/test` | Comprobar que el servicio responde | público |
| GET | `/clinica/test2` | Comprobación con sesión | cualquier usuario autenticado |
| GET | `/clinica/test3` | Comprobación con sesión y respuesta JSON | cualquier usuario autenticado |
| GET | `/email/test` | Enviar un correo de prueba | ADMINISTRADOR |

## Servicios

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/servicio/crear` | Crear un servicio | ADMINISTRADOR |
| GET | `/servicio/listar` | Listar servicios con su tarifa vigente | cualquier usuario autenticado |
| PUT | `/servicio/{id}` | Actualizar un servicio | ADMINISTRADOR |
| PATCH | `/servicio/{id}/estado` | Activar o inactivar un servicio | ADMINISTRADOR |

## Tarifas

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| POST | `/tarifa/crear` | Crear la tarifa de un servicio | ADMINISTRADOR |
| GET | `/tarifa/historial` | Historial de cambios de tarifas | cualquier usuario autenticado |
| GET | `/tarifa/listar` | Listar tarifas | cualquier usuario autenticado |
| PUT | `/tarifa/{id}` | Cambiar una tarifa | ADMINISTRADOR |
| GET | `/tarifa/{id}/historial` | Historial de cambios de la tarifa de un servicio | cualquier usuario autenticado |

## Usuarios y perfiles

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/perfil/listar` | Listar perfiles | ADMINISTRADOR |
| POST | `/usuario/actualizar` | Actualizar un usuario | ADMINISTRADOR |
| POST | `/usuario/guardar` | Crear un usuario | ADMINISTRADOR |
| GET | `/usuario/listar-ordenado` | Listar usuarios | ADMINISTRADOR |
| PATCH | `/usuario/{id}/estado` | Activar o inactivar un usuario | ADMINISTRADOR |

