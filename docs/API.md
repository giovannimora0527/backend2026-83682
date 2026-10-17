# API de clínica veterinaria

La URL base se forma con `server.servlet.contextPath` (por defecto `/clinica/v1`) y el puerto configurado (por defecto `8080`). Las respuestas de listados usan `{ content, page, size, totalElements, totalPages }`. `page` inicia en cero; `size` admite de 1 a 100. El orden predeterminado es descendente. `sort` solo acepta los campos permitidos para cada recurso.

## Rutas

| Método | Ruta | Uso |
|---|---|---|
| GET | `/cliente/listar` | Lista clientes existentes. |
| GET | `/cliente/{id}` | Consulta un cliente. |
| GET | `/cliente/{id}/mascotas` | Lista mascotas del cliente. |
| GET | `/mascota/listar` | Lista mascotas. |
| GET | `/mascota/listar-mascotas-ordenado?ascendente=true` | Ordena mascotas por nombre. |
| GET | `/mascota/buscar-by-raza?razaId=1` | Filtra por raza. |
| GET | `/mascota/buscar-by-cliente?clienteId=1` | Filtra por cliente. |
| GET | `/mascota/{id}` | Consulta una mascota. |
| POST | `/mascota/guardar` | Crea una mascota. |
| POST | `/mascota/actualizar` | Actualiza una mascota (contrato existente). |
| GET | `/clinica/test`, `/clinica/test2`, `/clinica/test3` | Rutas de comprobación existentes. |
| GET | `/veterinarios`, `/veterinarios/{id}` | Lista o consulta veterinarios. |
| GET | `/citas` | Lista citas. Filtros: `clienteId`, `mascotaId`, `veterinarioId`, `fecha`, `estado`. |
| GET | `/citas/{id}` | Consulta una cita. |
| POST | `/citas` | Registra una cita. |
| PUT | `/citas/{id}` | Actualiza una cita pendiente o cambia estado conforme a las transiciones permitidas. |
| GET | `/historias-clinicas` | Lista historias. Filtros: `clienteId`, `mascotaId`, `veterinarioId`, `fecha`. |
| GET | `/historias-clinicas/{id}` | Consulta historia y su primera página de anotaciones. |
| GET | `/historias-clinicas/{id}/anotaciones` | Lista anotaciones paginadas de la historia. |
| POST | `/historias-clinicas` | Crea una historia clínica. |
| GET | `/anotaciones` | Lista anotaciones. Filtros: `historiaClinicaId`, `veterinarioId`, `fecha`. |
| GET | `/anotaciones/{id}` | Consulta una anotación. |
| POST | `/anotaciones` | Crea una anotación. |
| GET | `/formulas-medicas` | Lista fórmulas. Filtros: `clienteId`, `mascotaId`, `veterinarioId`, `medicamento`, `fecha`. |
| GET | `/formulas-medicas/{id}` | Consulta una fórmula. |
| POST | `/formulas-medicas` | Crea una fórmula. |
| PUT | `/formulas-medicas/{id}` | Actualiza una fórmula existente. |

Los recursos clínicos aceptan `page`, `size`, `sort` y `direction=ASC|DESC`. Citas ordena por fecha y hora descendentes por defecto; los otros listados ordenan por su fecha correspondiente descendente.

## Ejemplo de cita

```http
POST /clinica/v1/citas
Content-Type: application/json
```

```json
{
  "clienteId": 12,
  "mascotaId": 8,
  "veterinarioId": 3,
  "fecha": "2026-11-10",
  "hora": "10:30:00",
  "motivoConsulta": "Control general",
  "estado": "PENDIENTE"
}
```

Los valores de estado son `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `CANCELADA` y `PROGRAMADA`. Las respuestas de error tienen `{ status, error, message }`; se usan 400 para entradas inválidas, 404 para identificadores inexistentes y 409 para conflictos de disponibilidad/estado o integridad.

## CORS

En el perfil `dev`, CORS permite el origen `http://localhost:4200`. Se puede cambiar con `CORS_ALLOWED_ORIGIN`. En `prod`, CORS permanece deshabilitado salvo que se configure explícitamente un único origen con esa variable.
