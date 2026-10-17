# Segundo parcial — Backend de clínica veterinaria

Backend Spring Boot 3.2.5 / Java 17. La base H2 facilita la demostración local.

## Arquitectura

`api` define contratos REST; `apicontroller` recibe solicitudes HTTP; `service` define operaciones de negocio; `serviceimpl` implementa validaciones y flujo; `repository` accede a datos; `models` representa entidades; `dto` define contratos de transferencia; `exception` centraliza errores.

## Endpoints

| Método | Endpoint | Uso |
|---|---|---|
| GET/POST/PUT | `/api/clientes`, `/api/clientes/{id}` | Listar, consultar, crear y editar clientes |
| GET | `/api/clientes/{id}/mascotas` | Consultar mascotas por cliente |
| GET/POST | `/api/pacientes`, `/api/pacientes/{id}` | Listar/crear/consultar mascotas |
| GET/POST/PUT | `/api/veterinarios`, `/api/veterinarios/{id}` | Listar, consultar, crear y editar veterinarios |
| GET | `/api/citas`, `/api/citas/{id}` | Listar y consultar cita individual |
| GET | `/api/citas/filtrar?fechaInicio=2026-10-01T00:00:00&fechaFin=2026-10-31T23:59:59` | Filtrar por rango de fecha/hora |
| POST/PUT | `/api/citas`, `/api/citas/{id}` | Crear y editar citas activas |
| GET/POST/PUT | `/api/historias`, `/api/historias/{id}` | Listar, consultar, crear y editar historias clínicas |
| POST/GET/PUT | `/api/anotaciones`, `/api/anotaciones/historia/{id}`, `/api/anotaciones/{id}` | Crear, listar por historia y editar anotaciones |
| GET/POST/PUT | `/api/formulas`, `/api/formulas/{id}` | Listar, consultar, crear y editar fórmulas médicas |

## Reglas de negocio

1. Una mascota debe pertenecer a un cliente registrado. Los combos de cliente/mascota consumen `/api/clientes/{id}/mascotas`.
2. No se registran citas en el pasado. La misma mascota o veterinario no puede tener otra cita activa dentro de 15 minutos. Los veterinarios deben existir y estar activos.
3. Las citas canceladas o atendidas/completadas no se editan como citas activas. Para reprogramar una cita cancelada, se crea otra.
4. La historia requiere una cita atendida/completada, una mascota coherente con la cita y un veterinario coincidente. Una cita solo puede originar una historia.
5. Cada anotación requiere historia y veterinario existentes/activo, descripción, observación y diagnóstico/evolución.
6. Una fórmula requiere mascota, cita, historia y veterinario coherentes; medicamento, dosis, frecuencia y duración positiva; y fecha de emisión no anterior a la cita.
7. Se bloquea el borrado físico de historias clínicas relacionadas con mascota/cita o anotaciones para conservar la trazabilidad clínica.
8. Errores de validación se responden con HTTP 400 y recursos inexistentes con HTTP 404. Los errores inesperados no exponen información interna.

## Ejecución

Requiere Java 17 y Maven:

```bash
mvn test
mvn spring-boot:run
```

API disponible en `http://localhost:8080`. Los datos de demostración se cargan en H2 al iniciar.

## Ejemplo JSON de cita

```json
{
  "fecha": "2026-10-20T10:00:00",
  "motivo": "Control general",
  "estado": "PENDIENTE",
  "clienteId": 1,
  "pacienteId": 1,
  "veterinarioId": 1
}
```

## Ejemplo JSON de anotación

```json
{
  "historiaMedicaId": 1,
  "veterinarioId": 1,
  "fecha": "2026-10-20T11:00:00",
  "descripcion": "Control de evolución",
  "observacion": "La mascota tolera el tratamiento",
  "diagnosticoEvolucion": "Disminución de la inflamación"
}
```
