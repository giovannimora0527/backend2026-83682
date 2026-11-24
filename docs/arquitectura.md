# Arquitectura

## Contenedores

```mermaid
flowchart LR
    nav([Navegador]) -->|:4200| nginx[frontend<br/>nginx + Angular]
    nav -->|:8025| mail
    nginx -->|/clinica/v1| api[backend<br/>Spring Boot 4, Java 21]
    api -->|JDBC| db[(db<br/>MySQL 8.4)]
    api -->|SMTP 1025| mail[mailpit]
    db --- vol[(volumen datos_mysql)]
    api --- sec[(volumen secretos)]
```

nginx sirve Angular y reenvía `/clinica/v1` al backend, así el navegador usa un solo origen. La base guarda sus datos en un volumen de Docker y ejecuta `db/*.sql` solo la primera vez. Si no se define `DOCKER_JWT_SECRET`, el backend genera una clave y la conserva en el volumen `secretos`.

## Capas

```mermaid
flowchart TD
    api["api: rutas y documentación (interfaces)"] --> ctrl["apicontroller: permisos por método"]
    ctrl --> srv["service / serviceimpl: reglas y transacciones"]
    srv --> repo["repository: JPA, bloqueos y agregaciones"]
    repo --> ent["entity: tablas"]
    srv --> pdf["pdf, reporte, carga: documentos, reportes y archivos"]
    seg["security: JWT, roles y CORS"] -.-> ctrl
    exc["exception: errores uniformes"] -.-> ctrl
```

| Paquete | Contenido |
|---|---|
| `api`, `apicontroller` | Contrato REST y controladores con `@PreAuthorize` |
| `service`, `serviceimpl` | Reglas de negocio |
| `repository`, `entity` | Persistencia |
| `models` | DTO de entrada (`Rq`) y salida (`Rs`) |
| `security` | Filtro JWT, roles, usuario actual y CORS |
| `carga` | Lectura, validación e importación de archivos |
| `reporte` | Catálogo de reportes y exportadores |
| `pdf` | Fórmula, prefactura, comprobante y factura |
| `exception` | Formato único de errores |
| `config`, `util` | OpenAPI, JSON, correo asíncrono y utilidades |

Servicios compartidos que concentran una responsabilidad: `AgendaService` (ocupar la agenda), `ExistenciasService` (único que cambia cantidades) y `ConfiguracionService` (consecutivos).

## Autenticación

```mermaid
sequenceDiagram
    actor U as Usuario
    participant F as Frontend
    participant B as Backend
    participant M as Correo
    U->>F: usuario y contraseña
    F->>B: POST /auth/login
    B->>B: BCrypt; genera OTP (15 min, un solo uso)
    B->>M: envía OTP
    U->>F: OTP
    F->>B: POST /auth/validate-token
    B-->>F: JWT HS512 (1 h) con rol, menús, medicoId y jti
    F->>B: petición con Authorization: Bearer
    B->>B: firma, sesión activa, usuario activo y rol vigente
    F->>B: POST /auth/cerrar-sesion
    B->>B: la sesión del jti queda inactiva (401 desde ahí)
```

## De la cita al pago

```mermaid
flowchart LR
    C[Cita CONFIRMADA] -->|iniciar| A[Atención]
    A --> FM[Fórmula BORRADOR]
    FM -->|confirmar| FC[CONFIRMADA<br/>PENDIENTE_DESPACHO]
    A -->|finalizar| H[Cita ATENDIDA<br/>historia y prefactura]
    FC -->|preparar y confirmar| D[Despacho]
    D -->|salida por lote| I[(Inventario)]
    H --> F[Factura]
    D -->|solo lo entregado| F
    F --> P[Pagos]
    P --> R[Reportes]
    I --> R
```

## Transacciones y concurrencia

- Se bloquea siempre en el mismo orden para no interbloquear: médico → mascota; fórmula → despacho → productos → lotes; cita → fórmulas → consecutivos.
- Aislamiento `READ_COMMITTED` en las operaciones que leen después de bloquear.
- `@Version` en las entidades que se editan; un choque responde 409.
- La base también lo impide: columnas generadas únicas para factura vigente, tarifa activa, despacho en preparación y entrega facturada.
- La carga masiva usa una transacción por fila.
