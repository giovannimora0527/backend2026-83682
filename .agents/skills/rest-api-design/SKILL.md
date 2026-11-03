---
name: rest-api-design
description: Diseñar APIs REST estables, documentadas, versionables y compatibles.
---

# Rest Api Design

## Procedimiento
1. Definir recursos, operaciones, autorización y contrato antes de implementar.
2. Usar métodos HTTP y códigos de estado semánticamente correctos.
3. Separar DTO de entidades y modelos internos.
4. Definir validación, paginación, ordenamiento y filtros.
5. Estandarizar errores y correlación de solicitudes.
6. Mantener compatibilidad hacia atrás; documentar cambios incompatibles y versionar cuando proceda.
7. Documentar OpenAPI usando la versión compatible con el proyecto.
8. No filtrar información interna en mensajes de error.

## Validación
Pruebas de contrato, validación de esquema OpenAPI y pruebas de autorización/errores.
