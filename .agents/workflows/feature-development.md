# Flujo: desarrollo de funcionalidad

1. Definir historia, alcance y criterios de aceptación.
2. Activar `backend-architect` si afecta varios módulos, contratos o límites.
3. Inspeccionar implementación actual, POM y pruebas.
4. Crear plan con archivos probables y riesgos.
5. Implementar con `backend-developer`.
6. Solicitar revisión independiente con `code-reviewer`.
7. Ejecutar pruebas con `qa-engineer`.
8. Ejecutar análisis de seguridad cuando se afecten identidad, datos o endpoints.
9. Revisar diff y documentación.
10. Entregar resumen, pruebas reales y riesgos pendientes.

**Gate:** no integrar si hay fallos críticos, pruebas no explicadas o cambios de contrato no aprobados.
