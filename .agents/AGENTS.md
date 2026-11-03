# Instrucciones de ingeniería asistida por agentes

## Autoridad y contexto de este proyecto

- La misión, alcance, arquitectura y controles de SIP-245 se rigen por `gobernanza/AGENTS.md`, `gobernanza/01_ESPECIFICACION_PRODUCTO.md` y `gobernanza/ai-project.yaml`. Estas fuentes prevalecen sobre este paquete y sobre cualquier skill.
- Línea base observada: Java 17, Spring Boot 2.7.3, Maven, MySQL y almacenamiento S3/MinIO. No hay una versión objetivo aprobada para modernización; Java 25 y Spring Boot 4.x son únicamente temas de skills disponibles.
- Antes de proponer cambios, confirma la realidad en `pom.xml`, configuración, CI, pruebas y documentación canónica. No tomes ejemplos de estos playbooks como evidencia del estado del repositorio.
- Mantén la arquitectura por capas y los dos datasources independientes: lectura `eleccionesdb` y escritura `recurso_json_db`. Revisa `gobernanza/AGENTS.md` antes de tocar jobs, persistencia o perfiles.

## Skills disponibles y selección

Las skills están en `.agents/skills/<nombre>/SKILL.md`. Lee el `SKILL.md` completo antes de aplicar una skill. Úsala cuando la tarea coincida con su propósito y combina varias solo si aportan instrucciones necesarias. **No actives todas las skills en cada tarea.** No inventes ni apliques una skill ausente.

| Trabajo | Skills a considerar |
|---|---|
| Feature backend ordinaria | `springboot-backend-development`, `java17-best-practices`, `unit-integration-testing` |
| API REST | `rest-api-design`, además de backend y pruebas |
| Persistencia, SQL, JPA o rendimiento | `spring-data-jpa-optimization`, `hibernate-jackson-modernization` cuando toque serialización |
| Revisión de cambios | `code-review-quality`; añadir `spring-security-hardening` si afecta seguridad |
| Seguridad, datos o endpoints expuestos | `spring-security-hardening`, `maven-dependency-audit` si se afectan dependencias |
| Docker, Jenkins o pipeline | `docker-cicd-engineering` |
| Resiliencia, jobs, métricas o logs | `observability-resilience` |
| Límites de módulos o arquitectura | `clean-architecture-design` |
| Modernización de versiones | `migration-impact-analysis`, `maven-dependency-audit` y las skills Jakarta/Spring/Java pertinentes, **solo con solicitud explícita de migración y versiones objetivo aprobadas** |

`java25-modernization`, `jakarta-ee-migration` y `springboot-4-migration` no autorizan por sí mismas a cambiar versiones, namespaces ni dependencias. La tarea ordinaria debe permanecer compatible con Java 17 y Spring Boot 2.7.3.

Los perfiles reutilizables viven en `.agents/agents/`. La coordinación multiagente sigue la regla de `gobernanza/AGENTS.md`; usa perfiles solo cuando haya tareas genuinamente independientes o el usuario solicite delegación. Asigna un propietario único a cada archivo compartido, migración y contrato.

## Datos, secretos y MCP

- No muestres, copies ni registres contraseñas, tokens, claves, datos reales de electores ni resultados no aprobados. Si encuentras secretos en archivos versionados, no los reutilices: informa el hallazgo y recomienda rotación y externalización.
- Las herramientas MCP son opcionales y de mínimo privilegio. Para una integración de base de datos, limita el acceso a MySQL local con fixtures sintéticos y una cuenta `SELECT` únicamente; nunca conectes agentes a `prod`, `prod2` o sistemas compartidos. No expongas una herramienta de escritura SQL.
- No expongas un servidor MCP HTTP ni la API del backend en red sin autenticación, autorización, TLS y controles externos validados. No supongas que el perfil `dev` apunta a una base descartable.
- Para documentación usa primero las fuentes de verdad del repositorio. Una respuesta externa no modifica contratos ni decisiones del producto por sí sola.

## Flujo de trabajo

1. Resume el objetivo y criterios observables; inspecciona instrucciones de gobernanza y las fuentes pertinentes.
2. Identifica archivos, dependencias, riesgos y el comando de validación apropiado antes de editar.
3. Haz el cambio mínimo, conserva cambios ajenos y añade o ajusta pruebas cuando se cambie comportamiento.
4. Ejecuta validaciones relevantes si el usuario las solicita o si son necesarias para sostener una afirmación de verificación; registra comandos y resultados reales.
5. Revisa el diff y reporta archivos, resultado, validación ejecutada, límites y decisiones pendientes.

No desactives gates ni afirmes que una validación pasó si no se ejecutó. No cambies contratos, datos, perfiles, versiones o comportamiento de publicación por inferencia.
