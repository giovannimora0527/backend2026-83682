# Clínica Veterinaria UNIMINUTO

Backend del examen final de Programación Web Full Stack. Repositorio `backend2026-83682`. El frontend Angular vive en `frontend26-83682`.

El servicio cubre inventario, citas, atención, fórmulas, dispensación, facturación, pagos y reportes. Spring Boot valida un JWT en cada operación protegida y MySQL conserva la trazabilidad. Nginx reenvía la API desde la aplicación web.

## Requisitos

- Java 21 y Maven Wrapper para desarrollo.
- Docker Desktop o Docker Engine con Compose para una instalación en contenedores.
- MySQL 8.4 para ejecución local. Docker Compose provee su propia instancia.

## Instalación nueva con Docker

Ejecuta estos comandos en PowerShell desde la raíz del backend. El nombre de proyecto debe ser nuevo para obtener un volumen vacío. Genera los valores en una sesión privada y consérvalos para futuras recreaciones de esa instancia.

```powershell
$env:MYSQL_PASSWORD = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:MYSQL_ROOT_PASSWORD = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
docker compose -p clinica-demo -f docker-compose.yml -f docker/instalacion/compose.inicial.yaml up -d --build --wait
```

MySQL ejecuta [esquema y catálogos públicos](docker/instalacion/) al inicializar el volumen. Esos scripts crean perfiles y permisos. El administrador de la instancia crea cuentas de demostración con contraseñas BCrypt. El generador interactivo `docker/instalacion/AltaUsuario.java` recibe la contraseña por consola.

```powershell
.\mvnw.cmd dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
$classpath = (Get-Content target/classpath.txt -Raw).Trim()
java --class-path "$classpath" docker/instalacion/AltaUsuario.java demo_admin ADMIN
```

Inserta la salida del generador únicamente en una base de demostración autorizada. Para el perfil Médico, vincula la cuenta con su registro en `usuario_medico`. Los roles son Administrador, Recepción, Médico y Despacho Médico. Consulta [el ejemplo de variables](docker/instalacion/entorno.env.example) sin copiar secretos al repositorio.

En el frontend hermano:

```powershell
$env:BACKEND_NETWORK = 'clinica-demo_default'
docker compose -p clinica-demo-web up -d --build --wait
```

| Servicio | URL |
|---|---|
| Frontend | [localhost:18080/#/login](http://localhost:18080/#/login) |
| Swagger directo | [localhost:18081/clinica/v1/swagger-ui/index.html](http://localhost:18081/clinica/v1/swagger-ui/index.html) |
| Swagger por Nginx | [localhost:18080/clinica/v1/swagger-ui/index.html](http://localhost:18080/clinica/v1/swagger-ui/index.html) |
| Salud de la API | [localhost:18081/clinica/v1/clinica/test](http://localhost:18081/clinica/v1/clinica/test) |

Para actualizar una instalación existente, respalda y confirma la versión de origen. Los archivos [V001 a V007](docker/migrations/) se aplican en orden únicamente sobre sus líneas base previstas. Los scripts de instalación nueva ya incluyen el esquema final.

## Desarrollo y pruebas

Configura `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` y `JWT_SECRET` para un MySQL de desarrollo. Usa `SPRING_PROFILES_ACTIVE=prod` para seleccionar la configuración parametrizada. Maven Wrapper permite arrancar el servicio con `mvnw.cmd spring-boot:run` y compilar y ejecutar pruebas con `mvnw.cmd -B verify`. Las pruebas MySQL especiales del repositorio requieren una instancia desechable preparada para ellas.

## API y reglas de negocio

Todos los endpoints usan la base `/clinica/v1`. Obtén un token con `POST /auth/login` y utiliza Bearer en Swagger. `GET /menu/propio` devuelve los permisos de navegación. Los controladores delegan a servicios transaccionales y repositorios JPA o JDBC.

- Administración y Recepción consultan, crean y editan clientes. La baja mediante `DELETE /cliente/{id}` archiva el registro y está reservada a Administración. Se conservan las mascotas, citas y atenciones asociadas.
- `GET /cita/mascotas/{id}/historial` pagina las citas anteriores y muestra la atención asociada cuando existe. El médico recibe únicamente las citas asignadas a su cuenta.
- Las citas detectan cruces por médico y mascota. El contenedor usa la hora de Bogotá.
- Confirmar una fórmula no reduce existencias. El despacho confirmado registra salidas por lote y puede revertirse con trazabilidad.
- La factura preserva importes y referencias a servicios y despachos. Los pagos se vinculan a la factura.
- `GET /reportes/EXISTENCIAS` sin fechas consulta saldo actual. Con `desde` y `hasta` devuelve saldo físico de apertura y cierre por lote, basado en movimientos conciliados. Los sufijos `/pdf` y `/excel` aplican el mismo rango. El histórico no incluye el precio de venta ni el estado histórico del producto, porque el catálogo no registra versiones de esos campos. Una cadena que no concilia devuelve 409.
