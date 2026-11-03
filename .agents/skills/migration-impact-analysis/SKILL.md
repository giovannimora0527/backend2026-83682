---
name: migration-impact-analysis
description: Analizar una migración Spring Boot/Java con versiones de origen y destino confirmadas por el proyecto.
---

# Migration Impact Analysis

## Objetivo
Producir un diagnóstico basado en evidencia antes de cambiar código o versiones.

## Inventario obligatorio
- Java, Maven Wrapper, plugins y toolchains.
- Spring Boot starters y módulos Spring.
- Spring Security y configuración de filtros.
- Spring Data, JPA, Hibernate, dialectos y consultas.
- `javax.*`, `jakarta.*`, Servlet y contenedor.
- Jackson, validación, OpenAPI y serialización.
- Logging, Actuator, métricas, tracing.
- Clientes HTTP, mensajería, caché, jobs y SDK de terceros.
- Docker, imágenes base, CI/CD, runtime y despliegue.
- Tests, fixtures, cobertura y contratos externos.

## Procedimiento
1. Registrar versiones exactas desde POM, árbol de dependencias y CI.
2. Confirmar versiones exactas de destino con el responsable; no asumir Boot 4.x ni JDK 25.
3. Consultar guías oficiales y notas de migración correspondientes.
4. Clasificar hallazgos: bloqueante, alto, medio, bajo.
5. Separar cambios mecánicos de cambios semánticos/funcionales.
6. Estimar esfuerzo por componente y definir secuencia/rollback.
7. No modificar dependencias durante el diagnóstico salvo solicitud explícita.

## Entregable
Resumen ejecutivo, inventario, matriz de compatibilidad, riesgos, plan, pruebas requeridas y decisiones pendientes.
