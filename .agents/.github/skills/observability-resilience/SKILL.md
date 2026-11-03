---
name: observability-resilience
description: Implementar logging, métricas, trazas, health checks y resiliencia.
---

# Observability Resilience

## Procedimiento
1. Inspeccionar observabilidad existente y estándares operativos.
2. Usar logs estructurados, niveles apropiados y correlation/trace IDs.
3. Excluir secretos y datos sensibles.
4. Definir métricas técnicas y de negocio con cardinalidad controlada.
5. Usar Actuator exponiendo solo endpoints necesarios y protegidos.
6. Configurar timeouts explícitos y políticas de retry/circuit breaker justificadas.
7. Evitar retries sobre operaciones no idempotentes sin estrategia.
8. Documentar readiness, liveness y dependencias críticas.

## Validación
Pruebas de health, logs, métricas, trazabilidad, timeout y fallos de dependencias.
