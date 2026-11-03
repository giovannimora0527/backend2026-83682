---
name: maven-dependency-audit
description: Auditar POM, plugins, dependencias transitivas y compatibilidad.
---

# Maven Dependency Audit

## Procedimiento
1. Leer POM padre, módulos, profiles, dependencyManagement, plugins y wrapper.
2. Ejecutar `mvn help:effective-pom` y `mvn dependency:tree` si el entorno lo permite.
3. Identificar dependencias directas/transitivas, versiones duplicadas, snapshots y rangos.
4. Revisar mantenimiento, licencias, CVE y compatibilidad con Java/Boot objetivo.
5. Evitar sobrescribir versiones administradas por Spring Boot BOM salvo razón documentada.
6. Proponer actualizaciones en grupos pequeños y reversibles.

## Salida
Matriz: artefacto, versión actual, origen, versión objetivo, compatibilidad demostrada, riesgo, acción y fuente.
