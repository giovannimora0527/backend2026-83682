# Backend Clínica Veterinaria — Primer Parcial Programación Web

Proyecto en **Spring Boot** para el examen del NRC-83862 (Docente: Ing. Giovanni Mora Jaimes).

## 1. Arquitectura del proyecto

Se siguió exactamente la arquitectura dibujada en el tablero de clase:

```
com.uniminuto.clinica
 ├─ ClinicaApplication.java      -> clase principal (ClinicaApp)
 ├─ models/                      -> entidades JPA (tablas de la base de datos)
 ├─ dto/                         -> objetos que viajan en las peticiones/respuestas JSON
 ├─ repository/                  -> acceso a datos (Spring Data JPA)
 ├─ service/                     -> INTERFACES de la lógica de negocio
 ├─ serviceimpl/                 -> clases que IMPLEMENTAN las interfaces de service
 ├─ api/                         -> INTERFACES de los controladores (contrato REST)
 ├─ apicontroller/               -> clases @RestController que IMPLEMENTAN las interfaces de api
 ├─ exception/                   -> excepciones propias + manejador global de errores
 └─ config/                      -> datos de ejemplo (para poder probar la API de una vez)
```

Cada controlador usa `@CrossOrigin` y `@RequestMapping("/algo")` tal como se explicó en
clase, y cada `xxxController` implementa una interfaz `xxxApi` (el "contrato" del servicio),
igual que cada `xxxServiceImpl` implementa una interfaz `xxxService`.

## 2. Modelo de datos

- **Paciente**: la mascota (no la pide el enunciado explícitamente, pero es necesaria para
  poder relacionar citas, formulas e historias con alguien).
- **Cita**: fecha, motivo, estado (`PENDIENTE`, `CONFIRMADA`, `COMPLETADA`, `CANCELADA`) y el paciente.
- **FormulaMedica**: medicamento, dosis, indicaciones, cantidad tomada del inventario, fecha de creación.
- **HistoriaMedica**: diagnóstico, observaciones, fecha de creación y su paciente.
- **AnotacionHistoria**: nota puntual asociada a una `HistoriaMedica` (relación 1 a muchos).

## 3. Cómo ejecutar el proyecto

Requisitos: **Java 17+** y **Maven** (o usar el wrapper si lo agregas).

```bash
cd clinica-veterinaria
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

La base de datos es **H2 en memoria** (no hay que instalar nada). Se puede ver el contenido
de las tablas en `http://localhost:8080/h2-console` usando:
- JDBC URL: `jdbc:h2:mem:clinicadb`
- Usuario: `sa`
- Contraseña: (vacía)

Al iniciar, la clase `DatosDePrueba` crea automáticamente 2 pacientes, 3 citas, 2 fórmulas
médicas y 1 historia médica con una anotación, para poder probar los endpoints de inmediato.

## 4. Endpoints disponibles

### Pacientes (apoyo, no pedido explícitamente)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/pacientes` | Crea un paciente |
| GET | `/api/pacientes` | Lista todos los pacientes |

### Fórmulas médicas (Requerimiento 1)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/formulas` | Lista las fórmulas del inventario, de la más reciente a la más antigua |

### Citas (Requerimientos 2, 3 y 4)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/citas/filtrar?fechaInicio=2026-01-01T00:00:00&fechaFin=2026-12-31T23:59:59` | Filtra citas por rango de fecha, de la más reciente a la más antigua |
| POST | `/api/citas` | Crea una nueva cita |
| PUT | `/api/citas/{id}` | Actualiza una cita existente |

Ejemplo de body para crear una cita:
```json
{
  "fecha": "2026-10-01T09:00:00",
  "motivo": "Vacunación anual",
  "estado": "PENDIENTE",
  "pacienteId": 1
}
```

### Historias médicas (Requerimiento 5 — CRUD completo)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/historias` | Crea una historia médica |
| GET | `/api/historias` | Lista todas las historias médicas |
| GET | `/api/historias?fechaInicio=...&fechaFin=...` | Filtra historias médicas por rango de fecha |
| GET | `/api/historias/{id}` | Consulta una historia médica por id |
| PUT | `/api/historias/{id}` | Actualiza una historia médica |
| DELETE | `/api/historias/{id}` | Elimina una historia médica |

Ejemplo de body para crear una historia médica:
```json
{
  "diagnostico": "Otitis leve",
  "observaciones": "Revisar en 10 días",
  "pacienteId": 1
}
```

### Anotaciones de historia médica (Requerimiento 5 — solo crear, listar y actualizar)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/anotaciones` | Crea una anotación dentro de una historia médica |
| GET | `/api/anotaciones/historia/{historiaMedicaId}` | Lista las anotaciones de una historia médica |
| PUT | `/api/anotaciones/{id}` | Actualiza una anotación |

Ejemplo de body para crear una anotación:
```json
{
  "descripcion": "Se aplica limpieza y gotas óticas",
  "historiaMedicaId": 1
}
```

## 5. Manejo de errores

Todos los errores (recurso no encontrado, fechas inválidas, datos faltantes, errores
inesperados) son capturados por `ManejadorGlobalExcepciones` y devueltos siempre con el
mismo formato JSON:

```json
{
  "fecha": "2026-09-19T10:00:00",
  "codigo": 404,
  "mensaje": "No se encontro la cita con id 99",
  "ruta": "/api/citas/99"
}
```

## 6. Notas para la sustentación

- El proyecto usa **DTOs** para no exponer las entidades JPA directamente (evita relaciones
  circulares al convertir a JSON y separa el modelo de base de datos del modelo de la API).
- Cada método, clase y atributo tiene comentarios **Javadoc** explicando qué hace y por qué.
- No se usó ninguna librería "mágica" (como Lombok) para que todo el código sea explícito
  y fácil de explicar.
- El proyecto no se pudo compilar dentro de este entorno porque no tiene acceso a
  Maven Central; se recomienda ejecutar `mvn clean compile` en tu máquina (con internet)
  antes de la entrega para confirmar que compila sin errores.
