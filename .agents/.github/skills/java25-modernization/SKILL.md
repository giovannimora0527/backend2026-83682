---
name: java25-modernization
description: Preparar y validar ejecución/compilación del backend con Java 25.
---

# Java25 Modernization

## Procedimiento
1. Confirmar soporte de JDK 25 por Spring Boot objetivo, Maven, plugins, agentes APM, librerías nativas e imagen Docker.
2. Ejecutar build con toolchain/JDK 25 en entorno aislado.
3. Revisar flags JVM, GC, memoria, TLS, reflection, serialización y dependencias con acceso a internals.
4. Adoptar nuevas características del lenguaje solo cuando aporten valor y el baseline objetivo sea 25.
5. No mezclar refactorización idiomática con resolución de incompatibilidades de runtime.
6. Comparar pruebas, rendimiento y consumo frente al baseline.

## Validación
Build reproducible, pruebas completas, smoke tests, escaneo de dependencias e imagen runtime validada.
