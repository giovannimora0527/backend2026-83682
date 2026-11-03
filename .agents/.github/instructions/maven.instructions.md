---
applyTo: "**/pom.xml"
---
- Inspeccionar el POM padre, módulos y Maven Wrapper antes de editar.
- No introducir versiones dinámicas ni duplicar versiones administradas por BOM.
- Mantener coherencia entre `java.version`, `maven.compiler.release`, toolchains y CI.
- Verificar compatibilidad de plugins con Maven y JDK reales.
- Justificar cada dependencia nueva y revisar mantenimiento, licencia y vulnerabilidades.
- No cambiar la versión de Spring Boot en una tarea funcional ordinaria.
