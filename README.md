# Backend de clínica veterinaria

API REST con Java 21, Spring Boot 4.0.4, Spring Data JPA y MySQL 8. Arquitectura por capas:
`api` (contratos), `apicontroller` (REST), `service`/`serviceimpl`, `repository`, `entity` y `models` (DTO).
Trabaja sobre la base MySQL **existente** `clinica` sin crear ni modificar tablas.

## Inicio rápido (desarrollo)

1. Configura la contraseña de MySQL **sin subirla al repositorio**, con una de estas opciones:
   - Copia `config/clinica-local.properties.example` como `config/clinica-local.properties` y escribe la contraseña
     (ese archivo está en `.gitignore`), o
   - define la variable de entorno `DB_PASSWORD`.
2. Desde la carpeta del backend:

   ```powershell
   .\mvnw.cmd clean test          # pruebas (H2 en memoria, nunca la base real)
   .\mvnw.cmd spring-boot:run     # API en http://localhost:8080/clinica/v1
   ```
3. Frontend Angular (otra terminal, en `C:\Users\perdo\Downloads\frontend26-83682`): `npm start`
   y abre `http://localhost:4200` (la app usa rutas con `#`, por ejemplo `http://localhost:4200/#/inicio/citas`).

## Variables de entorno

| Variable | Uso | Valor por defecto (perfil `dev`) |
|---|---|---|
| `DB_PASSWORD` | Contraseña de MySQL | Ninguno; o `config/clinica-local.properties` |
| `DB_URL` | URL JDBC | `jdbc:mysql://localhost:3306/clinica?...` |
| `DB_USERNAME` | Usuario | `root` |
| `CORS_ALLOWED_ORIGINS` | Orígenes Angular (separados por coma) | `http://localhost:4200,http://127.0.0.1:4200` |
| `SPRING_PROFILES_ACTIVE` | Perfil | `dev` (`prod` exige `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`) |
| `SERVER_PORT` | Puerto | `8080` |

## Base de datos

`spring.jpa.hibernate.ddl-auto=none` y `spring.sql.init.mode=never`: la aplicación no crea, altera ni inicializa
tablas. Las entidades se adaptaron al esquema real; ver [docs/ESQUEMA.md](docs/ESQUEMA.md). La prueba
`EsquemaRealMappingTest` falla si alguna entidad mapea una tabla o columna que no exista en la base real.

## Endpoints

Rutas, filtros, paginación y errores en [docs/API.md](docs/API.md).
