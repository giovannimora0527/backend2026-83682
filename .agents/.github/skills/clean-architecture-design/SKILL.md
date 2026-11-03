---
name: clean-architecture-design
description: Diseñar límites de dominio y arquitectura limpia/hexagonal para backend empresarial.
---

# Clean Architecture Design

## Principios
- Dominio independiente de Spring, JPA, HTTP y proveedores externos.
- Dependencias de código dirigidas hacia el dominio.
- Puertos expresan necesidades; adaptadores implementan integración.
- No introducir capas vacías ni interfaces por cada clase sin motivo.

## Procedimiento
1. Identificar capacidades, reglas y casos de uso.
2. Separar dominio, aplicación, adaptadores de entrada y adaptadores de salida.
3. Definir puertos según necesidades del caso de uso.
4. Implementar adaptadores REST, persistencia y servicios externos fuera del dominio.
5. Mantener transacciones en la frontera de aplicación apropiada.
6. Documentar dependencias y decisiones arquitectónicas.

## Validación
Pruebas unitarias del dominio sin levantar Spring; pruebas de integración para adaptadores; revisar ciclos de dependencias.
