---
name: hibernate-jackson-modernization
description: Preparar persistencia y serialización para las versiones administradas por Spring Boot 4.
---

# Hibernate Jackson Modernization

## Persistencia
- Inventariar anotaciones, tipos, dialectos, consultas HQL/JPQL/nativas, converters, listeners y generación de esquema.
- Verificar cambios de Hibernate/JPA en la versión exacta del BOM objetivo.
- Probar consultas, locking, paginación, transacciones y mappings complejos con motor real.

## Jackson
- Inventariar módulos, ObjectMapper custom, serializers/deserializers, mixins, naming, fechas y configuración.
- Verificar cambios de Jackson 2 a Jackson 3 y dependencias que aún requieran Jackson 2.
- Probar contratos JSON de entrada/salida y compatibilidad de clientes.

## Restricciones
No cambiar formato JSON ni comportamiento SQL deliberadamente sin aprobación y pruebas de contrato.
