---
name: jakarta-ee-migration
description: Analizar y migrar APIs Java EE/Jakarta EE en una modernización Spring.
---

# Jakarta Ee Migration

## Procedimiento
1. Buscar imports y referencias `javax.*` y `jakarta.*`.
2. Clasificar cada paquete: API Jakarta migrada, Java SE que permanece en `javax`, librería de terceros o código generado.
3. Revisar compatibilidad de dependencias y versiones de APIs.
4. Migrar imports y configuraciones solo con mapeo confirmado.
5. Actualizar validación, Servlet, Persistence, Mail y otras APIs según el stack real.
6. Regenerar código generado si aplica.
7. Ejecutar pruebas de compilación, serialización, validación y contenedor.

## Advertencia
No realizar reemplazo global `javax` → `jakarta`: paquetes como `javax.crypto`, `javax.sql`, `javax.net` y otros Java SE no se renombraron.
