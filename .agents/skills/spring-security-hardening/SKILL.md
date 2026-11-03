---
name: spring-security-hardening
description: Revisar e implementar controles de seguridad Spring Security y OWASP.
---

# Spring Security Hardening

## Procedimiento
1. Identificar mecanismo de identidad, flujos, roles, recursos y límites de confianza.
2. Revisar configuración existente antes de modificar filtros o cadenas de seguridad.
3. Aplicar mínimo privilegio y autorización en servidor.
4. Validar firma, emisor, audiencia, expiración y algoritmos permitidos de tokens según el diseño.
5. Evitar almacenar contraseñas en texto claro; usar encoder apropiado.
6. Revisar CORS, CSRF, sesiones, headers, rate limiting y manejo de secretos según el tipo de cliente.
7. No registrar tokens, credenciales ni datos sensibles.
8. No desactivar seguridad para facilitar pruebas; usar perfiles y fixtures seguros.

## Validación
Pruebas de autenticación, autorización, acceso denegado, expiración y configuración. No declarar cumplimiento normativo sin evaluación formal.
