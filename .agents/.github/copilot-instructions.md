# GitHub Copilot — instrucciones del repositorio

Este backend parte de Java 17 + Spring Boot 2.7.3. No hay una modernización a Java 25 / Spring Boot 4.x aprobada.

## Comportamiento esperado
- Lee primero `AGENTS.md` y los `pom.xml` aplicables.
- Respeta la estructura, convenciones y patrones existentes antes de proponer otros.
- No migres versiones ni paquetes como parte de una funcionalidad ordinaria.
- Escribe código compatible con Java 17 mientras esa sea la línea base.
- Mantén los controladores delgados y no expongas entidades JPA.
- Usa inyección por constructor, DTO, validación, manejo centralizado de errores y pruebas.
- No añadas dependencias si la funcionalidad puede resolverse con las existentes.
- Para seguridad, persistencia, transacciones y concurrencia, explica los riesgos relevantes.
- No inventes resultados de pruebas ni disponibilidad de APIs.
- Antes de una refactorización amplia, presenta el plan y limita el alcance.
- Selecciona skills del catálogo `.agents/skills/<nombre>/SKILL.md` según la tarea; no actives todas por defecto.
- Para código ordinario considera `springboot-backend-development`, `java17-best-practices` y `unit-integration-testing`; añade API, JPA, seguridad, CI o resiliencia solo si aplica.
- Usa skills de Java 25, Jakarta EE y Spring Boot 4 únicamente cuando el usuario solicite modernización y se aprueben versiones objetivo exactas.

## Al modificar código
1. Inspecciona clases relacionadas y pruebas existentes.
2. Identifica compatibilidad con Java 17 / Boot 2.7.4.
3. Implementa el cambio mínimo.
4. Añade o actualiza pruebas.
5. Ejecuta los comandos Maven relevantes si están disponibles.
6. Resume cambios y resultados reales.

## Migración
Para solicitudes aprobadas de migración, activa `migration-impact-analysis`, `maven-dependency-audit` y las skills de Spring, Jakarta, Java, persistencia, seguridad y pruebas que correspondan a la evidencia del proyecto. No asumas que Boot 4.x o Java 25 son el destino aprobado.
