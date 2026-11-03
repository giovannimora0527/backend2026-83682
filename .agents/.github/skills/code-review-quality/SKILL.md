---
name: code-review-quality
description: Realizar revisión técnica de cambios backend con foco en defectos, regresiones y mantenibilidad.
---

# Code Review Quality

## Checklist
- Corrección funcional y manejo de casos límite.
- Seguridad, autorización y exposición de datos.
- Transacciones, concurrencia y consistencia.
- N+1, consultas costosas y uso de memoria.
- Compatibilidad Java 17 / Boot 2.7.4.
- Contratos REST y compatibilidad hacia atrás.
- Pruebas suficientes y deterministas.
- Logging, observabilidad y errores.
- Complejidad, duplicación y mantenibilidad.

## Salida
Ordenar hallazgos por severidad: crítico, alto, medio, bajo. Cada hallazgo debe indicar archivo/línea, escenario de fallo, impacto y corrección. Distinguir defectos confirmados de sospechas. No editar el código durante una revisión salvo petición expresa.


## Quality gates
- Revisar configuración de SonarQube, cobertura, duplicación, complejidad y deuda.
- Corregir causas raíz de bugs y vulnerabilidades; no reducir umbrales ni ampliar exclusiones para ocultar problemas.
- Si SonarQube no está disponible, indicar que el análisis no fue ejecutado.
