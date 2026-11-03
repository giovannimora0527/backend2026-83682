# backend-ai-engineering-skills

Paquete de instrucciones, skills, perfiles y flujos de trabajo para este backend. La línea base observada del repositorio es **Java 21 + Spring Boot 4.0.4**. Las skills de Java 21/Spring Boot 4 son recursos para una futura modernización, no una decisión ni autorización de migración.

## Objetivo

Estandarizar el trabajo de GitHub Copilot, OpenAI Codex y VS Code Agent Mode. El paquete prioriza:
- cambios pequeños, verificables y trazables;
- compatibilidad estricta con la línea base declarada;
- separación entre dominio y framework;
- pruebas y análisis de seguridad;
- evaluación explícita de incompatibilidades antes de modificar versiones;
- modernización basada en evidencia y sujeta a aprobación explícita del proyecto.

## Estado de modernización

No existe una migración aprobada para este proyecto. Si el usuario solicita evaluarla, primero se requiere una decisión explícita sobre versiones exactas de destino, alcance, pruebas y despliegue. Las skills de migración sirven para inventariar y evaluar evidencia; no autorizan actualizaciones de versiones por sí solas.

No asumir que la última versión disponible en internet es la versión aprobada por la organización. Registrar la versión exacta de destino y comprobar requisitos oficiales antes de ejecutar cambios.

## Estructura

```text
.agents/
├── AGENTS.md                 # Reglas para usar las skills en este backend
├── skills/                   # Skills disponibles (una carpeta por skill)
│   ├── <skill>/SKILL.md
│   ├── agentes.md             # Flujos de agentes para documentar el repo
│   ├── init-skill.md          # Skill para crear archivos de gobernanza
│   └── plantilla.md           # Plantilla auxiliar de documentación
├── agents/                   # Perfiles reutilizables de especialista
├── workflows/                # Flujos para desarrollo, revisión y seguridad
```

## Instalación

Este paquete está instalado bajo `.agents/`. Las skills están en `.agents/skills/<nombre>/SKILL.md`; los perfiles están en `.agents/agents/`. El `AGENTS.md` de la raíz enlaza estas instrucciones con la gobernanza SIP-245.

Los hooks y tareas de ejemplo no se activan automáticamente. Revisa cada comando contra el entorno real antes de integrarlos en Git, CI o VS Code.

## Uso por herramienta

### OpenAI Codex
- Consulta skills desde `.agents/skills/<nombre>/SKILL.md` y elígelas por tarea según `.agents/AGENTS.md`.
- Usa `AGENTS.md` como instrucciones del repositorio.
- Los perfiles de `.agents/agents/` son plantillas de rol; la disponibilidad de delegación depende de la versión/configuración del cliente.

### GitHub Copilot
- Usa `.github/copilot-instructions.md` como instrucciones generales.
- Usa `.github/instructions/*.instructions.md` para reglas por archivos.
- Los perfiles `.github/agents/*.agent.md` son perfiles de agente personalizados para clientes Copilot compatibles.
- `.agents/.github/copilot-instructions.md` es una plantilla de este paquete y no se carga desde esa ruta. Para activarla, publícala en `.github/copilot-instructions.md` y alinéala con la gobernanza.

### VS Code Agent Mode
- Usa las instrucciones y perfiles de Copilot en VS Code cuando la versión instalada admita esas funciones.
- `.vscode/tasks.json` proporciona tareas ejecutables; no concede permisos automáticos al agente.
- Revisa siempre las solicitudes de ejecución de comandos y cambios de archivos.

## Principios de seguridad

- No incluir secretos, tokens, contraseñas ni datos personales reales en prompts, fixtures o logs.
- No ejecutar migraciones destructivas ni comandos de producción desde un agente.
- No aplicar cambios masivos de dependencias sin aprobación humana.
- No desactivar pruebas, análisis estático, autenticación ni controles de seguridad para conseguir una compilación.
- No aceptar automáticamente resultados del agente: revisar diff, pruebas y riesgos.

## Comandos habituales

```bash
# Compilar y ejecutar pruebas
./mvnw clean verify

# Solo pruebas
./mvnw test

# Inspeccionar árbol de dependencias
./mvnw dependency:tree

# Verificar Java activo
java -version
./mvnw -version
```

En Windows PowerShell use `.\mvnw.cmd` si el repositorio incluye Maven Wrapper.

## Adaptación requerida por proyecto

Este paquete no conoce automáticamente:
- estructura real de paquetes;
- módulos Maven;
- versión exacta de MySQL u otro motor;
- política de ramas y CI;
- estándares de logging;
- proveedor de identidad;
- contratos API;
- requisitos regulatorios o electorales.

Completar esos datos antes de usarlo en un sistema crítico.
