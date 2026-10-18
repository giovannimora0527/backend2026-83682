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
| POST | `/mascota/guardar` | Crea una mascota (201). |
| POST o PUT | `/mascota/actualizar` | Actualiza una mascota (Angular usa POST). |
| DELETE | `/mascota/eliminar/{id}` | Elimina una mascota sin citas ni historias; si las tiene responde 409. |
| GET | `/clinica/test`, `/clinica/test2`, `/clinica/test3` (también `/veterinaria/...`) | Rutas de comprobación. |
| GET | `/raza/listar` | Lista razas. |
| GET | `/veterinarios`, `/veterinarios/{id}` | Lista o consulta veterinarios (tabla `medico`). |
| GET | `/usuario/test` | Comprobación del módulo de usuarios. |
| GET | `/usuario/listar`, `/usuario/listar-ordenado` | Usuarios por nombre, sin hash de contraseña. |
| POST | `/usuario/guardar` | Crea un usuario (201; 409 si el usuario o el correo ya existen). |
| POST o PUT | `/usuario/actualizar` | Actualiza un usuario; sin `password` conserva la contraseña. |
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

### Rutas conservadas del parcial

| Método | Ruta | Uso |
|---|---|---|
| GET | `/cliente/all` | Lista clientes (igual que `/cliente/listar`). |
| GET | `/cliente/cliente-documento?numeroDocumento=...` | Busca cliente por documento. |
| GET | `/mascota/buscar?nombre=...` | Busca mascota por nombre (sin distinguir mayúsculas). |
| GET | `/mascota/listar-ordenado?ascendente=true` | Igual que `/mascota/listar-mascotas-ordenado`. |
| GET | `/medico/all` | Lista médicos. |
| GET | `/medico/buscar?tipoDocumento=CC&numeroDocumento=...` | Busca médico por documento. |
| GET | `/medico/buscarPorRegistroProfesional?registroProfesional=...` | Busca médico por registro. |
| GET | `/cita/filtrar-by-fechas?fechaInicio=2026-01-01T00:00:00&fechaFinal=2026-12-31T23:59:59` | Citas entre fechas (ISO). |
| GET | `/formula-medica/listar-ordenado?asc=true` | Fórmulas ordenadas por fecha de registro. |

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

Los valores de estado son `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `CANCELADA` y `PROGRAMADA`. Las respuestas de error son JSON `{ status, error, message }`: 400 entradas o parámetros inválidos, 404 identificador o ruta inexistente, 405 método no permitido, 409 conflictos de disponibilidad, estado, duplicados o integridad, y 500 solo para errores inesperados (se registran en el log).

## CORS

CORS global para `http://localhost:4200` y `http://127.0.0.1:4200` (métodos GET, POST, PUT, PATCH, DELETE, OPTIONS; cabeceras `Content-Type`, `Authorization`, `Accept`). Se cambia con `CORS_ALLOWED_ORIGINS` (lista separada por comas). Otros orígenes reciben 403 en el preflight.
