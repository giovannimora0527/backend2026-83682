---
name: springboot-backend-development
description: Crear y mantener componentes backend compatibles con Spring Boot 2.7.3 en este repositorio.
---

# Springboot Backend Development

## Alcance
Controladores, servicios, configuración, DTO, validaciones, eventos y manejo de errores.

## Procedimiento
1. Inspeccionar `pom.xml`, configuración y estructura existente.
2. Confirmar que el cambio pertenece a la capa correcta.
3. Mantener controllers como adaptadores HTTP; delegar casos de uso.
4. Usar inyección por constructor y configuración externa tipada (`@ConfigurationProperties`) cuando corresponda.
5. Aplicar Bean Validation en límites de entrada y reglas de dominio en el dominio/aplicación.
6. Manejar errores con contrato consistente; no filtrar detalles internos.
7. Evitar estado mutable compartido en beans singleton.
8. Mantener propiedades por perfil sin secretos en el repositorio.

## Validación
Compilar, ejecutar pruebas de contexto/API y verificar perfiles/configuración.

## Compatibilidad futura
Evitar APIs obsoletas conocidas y aislar configuración/framework del dominio. Verificar cada API contra la versión declarada en `pom.xml` y no asumir que sigue igual en Boot 4.
