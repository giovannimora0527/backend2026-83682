---
name: spring-data-jpa-optimization
description: Diseñar persistencia segura y eficiente con Spring Data JPA/Hibernate.
---

# Spring Data Jpa Optimization

## Procedimiento
1. Revisar entidades, cardinalidades, fetch types, cascadas y esquema real.
2. Evitar `EAGER` indiscriminado y relaciones bidireccionales innecesarias.
3. Detectar N+1 con logs/estadísticas y resolver mediante fetch join, entity graph o proyecciones.
4. Usar paginación para colecciones grandes; evitar paginación sobre fetch join de colecciones sin estrategia.
5. Definir transacciones explícitas y límites claros.
6. Usar DTO/proyecciones para lecturas; no serializar entidades.
7. Revisar índices con planes de ejecución y volumen real.
8. No ejecutar DDL destructivo automáticamente.

## Validación
Pruebas de repositorio con motor compatible; verificar SQL, conteo de consultas, consistencia transaccional y migraciones.

## Migración
Inventariar APIs Hibernate, dialectos, tipos, naming, consultas nativas y cambios de versión. No asumir compatibilidad entre Hibernate de Boot 2.7 y Boot 4.


## Evolución de esquema
- Identificar la herramienta de migración existente; no introducir otra sin justificar.
- Crear migraciones incrementales y versionadas.
- No editar migraciones ya aplicadas en entornos compartidos.
- Evaluar locks, duración, índices, volumen y ventana de mantenimiento.
- Usar expand-and-contract para cambios incompatibles en despliegues graduales.
- No ejecutar migraciones en producción desde el agente.
- Probar desde esquema vacío y desde versiones previas soportadas.
