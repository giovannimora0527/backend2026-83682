---
name: docker-cicd-engineering
description: Mantener builds reproducibles, imágenes seguras y pipelines confiables.
---

# Docker Cicd Engineering

## Procedimiento
1. Inspeccionar Dockerfile, Compose, pipeline, registry y política de imágenes.
2. Usar imágenes base mantenidas y fijar tags/digests según política.
3. Separar build y runtime, ejecutar como usuario no root cuando sea viable.
4. No copiar secretos a imágenes ni capas.
5. Alinear JDK de build, runtime y Maven Wrapper.
6. Ejecutar pruebas, análisis de dependencias, escaneo de imagen y SBOM según herramientas disponibles.
7. Mantener healthchecks y configuración por entorno.
8. Validar despliegue y rollback en entorno no productivo.

## Migración
Actualizar imagen base y runtime a Java 25 solo después de validar compatibilidad de Boot 4, agentes, certificados y herramientas operativas.
