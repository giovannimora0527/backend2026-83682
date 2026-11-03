# Backend de Clínica Veterinaria

API backend para la gestión de una clínica veterinaria. El proyecto expone servicios REST para usuarios, clientes, mascotas, citas, médicos, historias clínicas, fórmulas y catálogos.

## Tecnologías

- Java 21
- Spring Boot 4.0.4, Spring Web y Spring Data JPA
- MySQL Connector/J
- Maven (incluye Maven Wrapper)
- JUnit 5 y Spring Boot Test
- Lombok

## Arquitectura

Aplicación monolítica organizada por capas bajo `com.uniminuto.clinica`:

```text
Cliente HTTP
    -> apicontroller (adaptadores REST)
    -> service / serviceimpl (casos de uso y lógica)
    -> repository (Spring Data JPA)
    -> entity (modelo persistido)
    -> MySQL
```

Los contratos de endpoints están definidos en `api`; sus implementaciones están en `apicontroller`. `models` contiene objetos de solicitud/respuesta, `exception` maneja errores HTTP, `util` agrupa utilidades (OTP/JWT y contraseñas) y `config` contiene configuración de infraestructura. El envío de correo se implementa en `EmailServiceImpl`.

## Componentes y servicios

| Área | Responsabilidad |
|---|---|
| Autenticación (`Auth*`, `Session`, `OtpToken`) | Inicio con usuario/contraseña, OTP por correo y emisión/validación de JWT |
| Usuarios, perfiles y menús | Administración de usuarios, roles/perfiles y menús incluidos en el JWT |
| Clientes y mascotas | Consulta de clientes y gestión/búsqueda de mascotas por cliente, raza o nombre |
| Citas | Creación, actualización, consulta por fechas y consulta por mascota |
| Personal y catálogos | Médicos, especializaciones, razas y medicamentos |
| Historias clínicas | Historias médicas y anotaciones asociadas |
| Fórmulas médicas | Consulta, creación y actualización de fórmulas |
| Correo | Servicio SMTP para OTP y endpoint de comprobación |
| Persistencia | Repositorios JPA para entidades de negocio, autenticación y configuración de perfiles/menús |

Entidades principales: `Usuario`, `Perfil`, `Menu`, `Cliente`, `Mascota`, `Raza`, `Medico`, `Especializacion`, `Cita`, `HistoriaMedica`, `AnotacionHistoria`, `FormulaMedica` y `Medicamento`.

## API REST

La aplicación usa el prefijo `/clinica/v1`. URL local base: `http://localhost:8080/clinica/v1`. Las rutas siguientes están declaradas en los contratos de `api`; los métodos que reciben cuerpos usan JSON (`Content-Type: application/json`). Los nombres y campos de cada DTO/entidad se consultan en `models` y `entity`.

| Recurso | Método y ruta | Función / parámetros |
|---|---|---|
| Autenticación | `POST /auth/login` | Verifica credenciales e inicia desafío OTP |
| | `POST /auth/validate-token` | Valida OTP y devuelve JWT |
| | `POST /auth/cerrar-sesion` | Solicita cierre de sesión |
| Clínica | `GET /clinica/test`, `/clinica/test2`, `/clinica/test3` | Comprobaciones de servicio |
| Correo | `GET /email/test` | Comprobación del correo SMTP |
| Usuarios | `GET /usuario/listar-ordenado` | Lista usuarios |
| | `POST /usuario/guardar`, `/usuario/actualizar` | Crea o actualiza usuario |
| Clientes | `GET /cliente/listar` | Lista clientes |
| | `GET /cliente/buscar-por-documento?documento=...` | Busca por documento |
| Mascotas | `GET /mascota/listar`, `/mascota/listar-ordenado?ascendente=...` | Lista mascotas |
| | `GET /mascota/buscar-nombre?nombre=...` | Busca por nombre |
| | `GET /mascota/buscar-by-cliente?clienteId=...` | Filtra por cliente |
| | `GET /mascota/buscar-by-raza?razaId=...` | Filtra por raza |
| | `POST /mascota/guardar`, `/mascota/actualizar` | Crea o actualiza mascota |
| Citas | `GET /cita/listar` | Lista citas |
| | `GET /cita/listar-rango?fechaInicio=...&fechaFin=...` | Filtra por intervalo `LocalDateTime` |
| | `GET /cita/listar-por-mascota?mascotaId=...` | Filtra por mascota |
| | `POST /cita/crear`, `/cita/actualizar` | Crea o actualiza cita |
| Médicos | `GET /medico/listar`, `/medico/listar-disponibles?fechaInicio=...&fechaFin=...` | Lista médicos o disponibilidad |
| | `POST /medico/crear`, `/medico/actualizar` | Crea o actualiza médico |
| Razas | `GET /raza/listar` | Lista razas |
| | `POST /raza/guardar` | Crea raza |
| Especializaciones | `GET /especializacion/listar` | Lista especializaciones |
| | `POST /especializacion/crear` | Crea especialización |
| | `PUT /especializacion/actualizar` | Actualiza especialización |
| Medicamentos | `GET /medicamento/listar` | Lista medicamentos |
| Historias clínicas | `GET /historia-clinica/listar` | Lista historias |
| | `POST /historia-clinica/guardar`, `/historia-clinica/actualizar` | Crea o actualiza historia |
| Anotaciones | `POST /anotacion-historia/crear` | Crea anotación de historia clínica |
| Fórmulas | `GET /formula-medica/listar` | Lista fórmulas |
| | `POST /formula-medica/guardar`, `/formula-medica/actualizar` | Crea o actualiza fórmula |

Los endpoints usan `@CrossOrigin("*")`; restringe orígenes antes de exponer el servicio en producción. Varias operaciones GET declaran `consumes: application/json`; según el cliente HTTP, puede requerirse enviar `Content-Type: application/json` incluso sin cuerpo. No hay especificación OpenAPI/Swagger configurada en las dependencias actuales.

## Configuración

`application.properties` activa el perfil `dev`, establece puerto `8080`, contexto `/clinica/v1` y conexión MySQL local. También importa opcionalmente `./application-secrets.properties`. `application-dev.properties` y `application-prod.properties` existen, pero actualmente contienen una configuración de base de datos local equivalente; no constituyen una configuración de producción aislada.

Configura las credenciales y claves fuera del repositorio usando variables de entorno de Spring, por ejemplo:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/clinica?serverTimezone=America/Bogota'
$env:SPRING_DATASOURCE_USERNAME = 'usuario_app'
$env:SPRING_DATASOURCE_PASSWORD = 'contraseña'
$env:SPRING_MAIL_USERNAME = 'cuenta-smtp'
$env:SPRING_MAIL_PASSWORD = 'secreto-smtp'
$env:SPRING_MAIL_TO = 'destino@example.com'
$env:JWT_SECRET = '<secreto aleatorio Base64 de al menos 64 bytes>'
```

La clave JWT se usa para firmar HS512 y debe ser aleatoria, fuerte y distinta por entorno. No uses las credenciales de ejemplo/locales para despliegue.

## Ejecución local

Requisitos: JDK 21, MySQL disponible y una base de datos `clinica` inicializada con el esquema requerido por las entidades. No se incluye un script de migraciones ni Docker Compose; prepara el esquema antes de iniciar.

```powershell
# Configurar las variables de entorno indicadas arriba y luego:
.\mvnw.cmd spring-boot:run
```

La aplicación inicia en `http://localhost:8080/clinica/v1`. Para crear el artefacto ejecutable:

```powershell
.\mvnw.cmd clean package
java -jar target/clinica-0.0.1-SNAPSHOT.jar
```

Para seleccionar perfil explícitamente: `java -jar target/clinica-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`. Revisa y completa la configuración de producción antes de usar ese perfil.

## Pruebas

Ejecuta la suite con:

```powershell
.\mvnw.cmd test
```

La suite actual incluye una prueba de carga del contexto de Spring y pruebas unitarias de utilidades para JWT (firma HS512, expiración de una hora, claims, validación de secretos) y generación de OTP. No se observan pruebas de integración de endpoints ni una base de datos de prueba dedicada; la carga de contexto puede necesitar conectividad/configuración de MySQL según el entorno.

## Seguridad y operación

- Las rutas habilitan CORS para cualquier origen y no se observa una cadena de filtros Spring Security que proteja globalmente los recursos. No asumas que el JWT se valida automáticamente en cada endpoint.
- El flujo de autenticación usa OTP con expiración de 15 minutos y límite de intentos, y crea JWT con expiración de una hora. Revisa el estado de cierre de sesión: `cerrarSesion` devuelve actualmente `null` en el servicio.
- `PasswordUtil` usa MD5 para las contraseñas, algoritmo inadecuado para almacenamiento de credenciales. Migra a BCrypt, Argon2 o scrypt antes de un despliegue real.
- El manejador global devuelve `ex.getMessage()` para errores internos; evita filtrar detalles de implementación al cliente y registra detalles de forma segura en servidor.
- SQL detallado está habilitado en configuración. Desactívalo en producción y evita registrar credenciales/datos personales.
- Usa TLS, cuenta de base de datos con privilegios mínimos, secretos gestionados por el entorno, copias de seguridad y restricciones de red. No versionar secretos ni guardar archivos locales de secretos en Git.
- Hay credenciales y secretos sensibles en la configuración actualmente versionada. Deben considerarse expuestos: rótalos y elimina su uso; moverlos a variables de entorno no invalida los valores ya expuestos.
- El `CommandLineRunner` `DatabaseConnectionTest` imprime metadatos de conexión al iniciar; revisa si esa salida es apropiada para el entorno productivo.

## Despliegue

Empaqueta como JAR con Java 21 y Maven Wrapper, provisiona MySQL y aplica el esquema compatible, configura las variables de entorno seguras, activa el perfil requerido y ejecuta el JAR detrás de un proxy con HTTPS. Configura supervisión de proceso, logs con redacción de datos sensibles, respaldos y estrategia de migración de base de datos. No hay imagen Docker, pipeline CI/CD, migraciones versionadas ni manifiestos de infraestructura incluidos en el repositorio.
