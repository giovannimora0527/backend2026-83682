# Backend de clínica veterinaria

API REST desarrollada con Java 21, Spring Boot, Spring Web, Spring Data JPA y MySQL. Mantiene la arquitectura por capas del proyecto (`api`, `apicontroller`, `service`, `serviceimpl`, `repository`, `entity` y `models`).

## Configuración

Define estas variables de entorno antes de iniciar:

| Variable | Uso | Valor de desarrollo predeterminado |
|---|---|---|
| `DB_URL` | URL JDBC de MySQL | En dev: `jdbc:mysql://localhost:3306/clinica`; obligatorio en prod |
| `DB_USERNAME` | Usuario de base de datos | En dev: `root`; obligatorio en prod |
| `DB_PASSWORD` | Contraseña de base de datos | Sin valor; debe configurarse |
| `CORS_ALLOWED_ORIGIN` | Origen Angular permitido | Dev: `http://localhost:4200`; prod: no habilitado si se omite |

El contexto HTTP es `/clinica/v1` y el puerto predeterminado es `8080`. El perfil `dev` se activa por defecto. En producción, activa `prod` explícitamente y configura las variables con el mecanismo de secretos del entorno. No se imprimen las sentencias SQL ni las credenciales.

## Base de datos

El proyecto no tenía migraciones ni esquema de citas y registros clínicos. Revisa [docs/ESQUEMA.md](docs/ESQUEMA.md) y valida la migración manual en `src/main/resources/db/manual/V1__modulos_clinicos.sql` contra la base de desarrollo antes de aplicarla. No se ejecuta automáticamente ni al iniciar la aplicación. La tabla de veterinarios debe contener registros reales; no se insertan datos de ejemplo.

## Ejecutar y probar

```powershell
mvn.cmd spring-boot:run
mvn.cmd test
```

Las pruebas de contexto usan H2 en memoria y no se conectan a la base real. Requieren resolver las dependencias del proyecto la primera vez.

## Endpoints

La lista de rutas, parámetros de filtros/orden, modelos de petición y respuestas está en [docs/API.md](docs/API.md). Los módulos agregados cubren veterinarios, citas, historias clínicas, anotaciones y fórmulas médicas; se conservan las rutas previas de clientes y mascotas.
