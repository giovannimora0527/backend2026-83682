---
name: unit-integration-testing
description: Crear pruebas unitarias, integración, API y persistencia con resultados reproducibles.
---

# Unit Integration Testing

## Procedimiento
1. Identificar comportamiento observable y criterios de aceptación.
2. Priorizar pruebas de dominio sin contexto Spring.
3. Usar JUnit 5 y Mockito según dependencias existentes.
4. Usar MockMvc para endpoints y Testcontainers si el proyecto lo admite para infraestructura real.
5. Cubrir escenarios positivos, negativos, límites, autorización, transacciones y regresiones.
6. Evitar sleeps, dependencia de orden y datos externos no controlados.
7. No sobre-mockear ni probar detalles privados.
8. Reportar cobertura como dato, no como sustituto de calidad.

## Validación
Ejecutar pruebas focalizadas y luego `./mvnw clean verify` cuando el tiempo/entorno lo permita. Informar exactamente qué se ejecutó.
