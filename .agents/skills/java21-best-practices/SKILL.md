---
name: java17-best-practices
description: Aplicar buenas prácticas Java 17 en código backend y revisar compatibilidad del lenguaje/runtime.
---

# Java21 Best Practices

## Alcance
Código Java de producción y pruebas en proyectos cuya línea base sea Java 21.

## Procedimiento
1. Confirmar `java.version`, `maven.compiler.release`, toolchain y JDK de CI.
2. Inspeccionar convenciones locales y librerías disponibles.
3. Usar características estables de Java 21: records cuando encajen como DTO/valores inmutables, switch expressions, text blocks, pattern matching limitado a `instanceof`, Streams con moderación.
4. Evitar APIs o sintaxis posteriores a Java 21 en cambios ordinarios.
5. Preferir inmutabilidad, composición, tipos explícitos y manejo claro de excepciones.
6. Evitar `Optional` en campos/DTO/JPA y parámetros salvo convención justificada; usarlo principalmente como retorno cuando aporte semántica.
7. No usar Streams para lógica con efectos secundarios ni crear abstracciones innecesarias.

## Validación
- Compilar con release 21.
- Ejecutar pruebas.
- Revisar complejidad, nullability, excepciones y compatibilidad de dependencias.

## No hacer
No convertir automáticamente todo POJO a `record`; no usar preview features; no asumir que compilar con un JDK más nuevo prueba compatibilidad con Java 17.
