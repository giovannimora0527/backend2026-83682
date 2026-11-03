---
name: springboot-4-migration
description: Planificar y ejecutar modernización hacia Spring Boot 4.x / Spring Framework 7.
---

# Springboot 4 Migration

## Precondiciones
- Versión exacta de Spring Boot 4.x seleccionada.
- JDK 25 y Maven soportados por el proyecto/CI.
- Inventario y baseline de pruebas disponibles.
- Dependencias críticas verificadas con sus fabricantes.
- Rama de migración y estrategia de rollback aprobadas.

## Procedimiento
1. Consultar requisitos y guía oficial de migración para la versión exacta.
2. Crear rama aislada y registrar baseline de build/tests.
3. Actualizar BOM/parent y plugins de forma controlada.
4. Resolver cambios de paquetes y APIs Jakarta donde corresponda; no reemplazar todos los `javax`.
5. Adaptar configuración, starters y módulos que hayan cambiado.
6. Actualizar Spring Security, Spring Data/Hibernate, Jackson, validación, testing y observabilidad conforme a compatibilidad demostrada.
7. Compilar por módulos y ejecutar pruebas progresivamente.
8. Validar contratos REST, seguridad, SQL, transacciones, serialización y rendimiento.
9. Documentar incompatibilidades no resueltas y alternativas.

## Restricciones
- No afirmar que el salto directo es soportado sin verificar la guía oficial.
- No actualizar todas las librerías a "latest".
- No eliminar pruebas ni degradar controles.
- No aplicar cambios de esquema o producción automáticamente.

## Salida
Lista de cambios, incompatibilidades, pruebas ejecutadas, resultados, riesgos residuales y rollback.
