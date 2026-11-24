# Backend de Clínica Veterinaria

API REST para la gestión de una clínica veterinaria: usuarios y perfiles, clientes, mascotas, citas con calendario, atención médica, fórmulas, inventario con lotes y carga masiva, despacho, facturación, pagos y reportes en PDF y Excel.

**Tecnologías:** Java 21, Spring Boot 4.0.4 (Web, JPA, Security, Validation, Mail), MySQL 8, JWT, Apache POI, Commons CSV, OpenPDF, springdoc y Maven.

## Ejecutar todo con Docker

Requisitos: Docker Desktop abierto y la carpeta del frontend junto a esta (`../frontend26-83682`). Los puertos 4200, 8080, 8025 y 3307 deben estar libres.

```powershell
docker compose up --build -d --wait
```

| Servicio | Dirección |
|---|---|
| Aplicación | http://localhost:4200 |
| Swagger | http://localhost:8080/clinica/v1/swagger-ui.html |
| Correo de prueba (OTP) | http://localhost:8025 |
| MySQL | `localhost:3307`, base y usuario `clinica` |

No hace falta configurar nada: la base se crea con datos de prueba y la clave JWT se genera sola. Otros comandos:

```powershell
docker compose ps                  # estado
docker compose logs -f backend     # registro
docker compose down                # apagar, conserva los datos
docker compose down -v             # apagar y borrar los datos
powershell -ExecutionPolicy Bypass -File scripts\levantar.ps1 -DesdeCero   # todo desde cero, evita la suspensión y mide el tiempo
```

Si otro programa usa un puerto, cámbialo: `$env:BACKEND_PORT='18080'` (también `FRONTEND_PORT`, `MAILPIT_PORT` y `DB_PORT_HOST`). La primera vez descarga imágenes y dependencias; después tarda cerca de un minuto.

**Si el equipo se suspende, los contenedores se congelan.** Antes de una presentación: `powercfg /change standby-timeout-ac 0`.

## Usuarios de prueba

Datos ficticios, solo para desarrollo. El código OTP llega a Mailpit.

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin2026*` | ADMINISTRADOR |
| `secretaria` | `Secre2026*` | SECRETARIA |
| `medico1` | `Medico2026*` | MEDICO |
| `medico2` | `Medico2026*` | MEDICO |
| `despacho` | `Despacho2026*` | DESPACHO_MEDICO |

## Ejecutar sin Docker

Requisitos: JDK 21 y MySQL 8 con una base `clinica`.

1. Copia `.env.example` como `.env` y complétalo. `.env` no se sube a Git.
2. Ejecuta en orden los scripts de `db/`. En una base con datos, respáldala antes y omite el `01`:

   | Script | Contenido |
   |---|---|
   | `01_esquema_base.sql` | Esquema inicial (solo base vacía) |
   | `02_migracion_examen_final.sql` | Migración; se puede repetir y conserva los datos |
   | `03_datos_iniciales.sql` | Datos de prueba |
   | `04_horarios_medicos_existentes.sql` | Horario para médicos sin uno |

3. Inicia con una de estas opciones:

   ```powershell
   .\mvnw.cmd spring-boot:run
   # En segundo plano y con el correo en Mailpit (-Parar para detenerlo):
   powershell -ExecutionPolicy Bypass -File scripts\iniciar-dev.ps1
   ```

## Variables de entorno

| Variable | Uso |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a MySQL |
| `JWT_SECRET` | Base64 de al menos 64 bytes aleatorios |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD` | Servidor de correo |
| `MAIL_AUTH`, `MAIL_STARTTLS` | `true` por defecto; `false` con Mailpit |
| `CORS_ORIGINS` | Orígenes permitidos, separados por coma |

Las de Docker Compose son opcionales y están en `.env.example`. Perfiles: `dev` (por defecto), `test`, `docker` y `prod`.

## Pruebas

```powershell
.\mvnw.cmd test
```

Usan H2 en memoria: no necesitan MySQL ni correo.

## Documentación

| Documento | Contenido |
|---|---|
| [docs/evidencias.md](docs/evidencias.md) | Guion de la sustentación |
| [docs/arquitectura.md](docs/arquitectura.md) | Contenedores, capas y flujos |
| [docs/modelo-datos.md](docs/modelo-datos.md) | Diagrama, tablas, relaciones y justificación |
| [docs/reglas-negocio.md](docs/reglas-negocio.md) | Reglas y dónde se validan |
| [docs/api.md](docs/api.md) | Ejemplos JSON y tabla de endpoints |
| [docs/CAMBIOS.md](docs/CAMBIOS.md) | Cambios, decisiones y desviaciones del contrato |

## Seguridad

Las credenciales que estuvieron en versiones anteriores del repositorio siguen en el historial de Git: deben rotarse. Los usuarios y contraseñas de prueba no deben usarse en producción.
