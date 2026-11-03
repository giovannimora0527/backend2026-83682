# Briefs de los cuatro agentes

El orquestador despacha cada agente con su brief, sustituyendo los `{marcadores}`. Los subagentes no ven `SKILL.md`: reciben solo su brief y las rutas que este nombra. Ninguno hace preguntas al usuario.

Valores comunes a los cuatro:

- `{PROYECTO}`: ruta absoluta de la raíz del proyecto.
- `{SKILL_DIR}`: ruta absoluta de la carpeta de esta skill.
- `{FUENTES}`: uno de estos dos textos.
  - Sin README previo: `Tu única fuente de verdad es el código: código fuente, manifiestos de build, configuración, Dockerfiles/compose, scripts, pruebas y CI. Todo documento en prosa (*.md, *.rst, *.docx, *.pdf, carpetas docs/, ADR, CHANGELOG) queda fuera: no lo abras ni lo uses.`
  - Con README previo: la misma frase, más `Además puedes leer {PROYECTO}/README.md.`

## 1. Mapeador

```text
Mapea el proyecto en {PROYECTO} (solo lectura). {FUENTES}
1. Lista la estructura de directorios hasta profundidad 3, sin node_modules, .git, target, build, dist, venv ni vendor.
2. Define las áreas: cada área es un módulo, es decir, una carpeta con su propio manifiesto o Dockerfile, y su configuración y sus pruebas pertenecen a esa área. Si el único manifiesto está en la raíz, hay una sola área. Solo si la raíz tiene archivos que no pertenecen a ningún módulo (compose, .env, CI, scripts), añade un área "transversal" para ellos.
3. Devuelve, en máximo 40 líneas, una línea por área: nombre | ruta | stack probable | archivos clave (manifiestos, configuración, pruebas).
No describas el negocio ni el código a fondo: eso lo hace la exploración.
```

## 2. Explorador (uno por área, en paralelo)

```text
Explora el área "{AREA}" ({RUTA}) del proyecto en {PROYECTO} (solo lectura). {FUENTES}
Tu respuesta final es el reporte completo, con el contenido real de cada grupo; no devuelvas marcadores de posición ni un resumen de lo que hiciste.
Devuelve hechos, no prosa, cada uno con su evidencia `archivo:línea`, agrupados así:
RESUMEN: qué hace el área, para quién, tipo de componente y stack.
PREREQUISITOS: lenguaje/runtime con versión, gestor de build, servicios externos, herramientas.
MODULOS: nombre y responsabilidad de cada módulo (carpeta o paquete principal, no clases) y de cada contenedor (unidad ejecutable o desplegable); relaciones con otras áreas o servicios externos (URL, conexión de BD, imports, mensajes) con su protocolo.
ACTORES Y EXTERNOS: quién usa el sistema, solo si el código lo evidencia (interfaz web, roles, CLI); y cada sistema que el área consume y que no vive en el repositorio (APIs de terceros, brokers, almacenamiento de objetos, SSO, correo), marcando para cada uno si el repositorio lo define (carpeta con manifiesto o Dockerfile, servicio del compose, base de datos de uso propio) o solo se referencia por host, URL o configuración.
CONFIG: una línea por archivo de configuración (.env*, properties, yml, env del frontend, Dockerfiles, compose) con su propósito. Aparte, VARIABLES: una línea por variable de entorno (placeholders ${VAR}, .env*, ENV/ARG de Dockerfile, environment del compose, lecturas directas del código) con: archivo(s) donde aparece, valor default que se aplica si no se define (${VAR:valor}, ${VAR:-valor}, ENV VAR=valor, respaldo en el código) o "sin default", y para qué se usa en máximo 6 palabras. Los valores de .env.example son ejemplos, no defaults. Nunca valores de secretos: si una variable secreta tiene default, escribe "definido (oculto)".
ARRANQUE: cómo se levanta el área. Vía Docker, si hay Dockerfile o compose: comando exacto, servicios y puertos que define, y archivos o variables que hay que preparar antes. Vía terminal: comandos de los manifiestos y scripts (instalar dependencias y arrancar), directorio desde el que se lanzan, puerto o URL donde queda disponible y servicios que deben estar corriendo antes. Si no hay nada que arrancar (librería, CLI), cómo se instala y el punto de entrada.
PRUEBAS: tipos presentes (unitarias, integración, e2e, otras) con framework, ubicación, convención de nombres y comando para ejecutarlas.
IMPLEMENTACION: cómo se añade una funcionalidad nueva según lo que el código evidencia: las capas o carpetas por las que pasa, con un archivo existente de ejemplo por capa; dónde van sus pruebas y cómo se nombran; y lo que CI, linters, formateadores o hooks exigen antes de integrar. Nada que el código no declare (ramas, commits, revisiones).
HALLAZGOS: inconsistencias o problemas que notes (no van al README).
Si algo no está declarado en el código, escribe "no declarado"; no lo supongas. Máximo 60 líneas.
```

## 3. Redactor

```text
Escribe {PROYECTO}/README.md en {IDIOMA}. Lee {SKILL_DIR}/references/plantilla.md y sigue su plantilla, sus encabezados literales, sus reglas de contenido y su presupuesto de longitud.
Tus únicos datos son estos reportes de exploración, cada hecho con su evidencia: {REPORTES}
{MODO}
No abras otros archivos del proyecto salvo para comprobar un dato dudoso. {FUENTES}
Antes de entregar, aplica la prueba de las siete preguntas y recorta lo que no responda ninguna. Los hallazgos de los reportes no van en el README.
Responde solo con la ruta escrita y el número de líneas.
```

`{MODO}` según el caso:

- README nuevo: `Escribe el README completo.`
- Actualización: `Texto del README actual: {README_PREVIO}. Reescribe solo estas secciones: {SECCIONES}. Todo lo demás queda idéntico, carácter por carácter, y las secciones previas sin equivalente en la plantilla se conservan debajo de la última sección.`
- Ronda de corrección: `Aplica estos defectos del verificador, uno por uno, y nada más: {DEFECTOS}. Antes de corregir un FALSO, confirma en el código el dato correcto.`

## 4. Verificador contradictor

No recibe los reportes ni el razonamiento del redactor: comprueba contra el código.

```text
Tu trabajo es tumbar el README de {PROYECTO}/README.md, escrito en {IDIOMA}. Parte de que tiene errores y no apruebes nada que no hayas comprobado tú contra el código. {FUENTES} El README que revisas es la excepción a la prohibición de documentos en prosa. Lee {SKILL_DIR}/references/plantilla.md: es el contrato que debe cumplir. {ALCANCE}

Revisa en este orden y reporta todos los defectos en esta única pasada: no habrá otra para los que te guardes. Cada defecto lleva su evidencia; un defecto sin evidencia verificable se descarta.
- FALSO: toda versión, comando (de arranque, de pruebas, de instalación), puerto, URL, variable, valor default, módulo, archivo, actor, sistema externo, arista de los diagramas y paso de la nueva implementación, abriendo el archivo que lo respalda; incluido que lo marcado como externo no lo defina el repositorio y que lo marcado como propio sí, y que el archivo de ejemplo citado en un paso exista. Evidencia: el archivo:línea que lo contradice.
- FALTA: cada archivo de configuración, variable de entorno, módulo, sistema externo, tipo de prueba y vía de arranque (la de Docker si el repositorio tiene Dockerfile o compose) que existe en el código y no aparece en su sección; y cada parte de la plantilla ausente, incluidos los dos diagramas con su leyenda y la sección de nueva implementación. Evidencia: el archivo que existe.
- SOBRA: cada línea que supera el presupuesto de longitud o que no responde ninguna de las siete preguntas (marketing, introducciones, repeticiones). Evidencia: la línea y la regla del presupuesto que rompe.
- ESTRUCTURA: encabezados distintos de los literales del idioma elegido, orden alterado, sección que sobra, mermaid que no renderiza, tablas con columnas distintas de las de la plantilla, la vía por terminal antes que la de Docker cuando el proyecto tiene Docker, un valor secreto escrito en claro, o cualquier mención del modelo C4 o de sus niveles (C4, C1, C2).

Los encabezados literales de la plantilla son correctos aunque su ortografía no lo parezca. Tus arreglos solo corrigen, sustituyen o recortan texto dentro de la plantilla; nunca proponen secciones nuevas. Los problemas del repo (inconsistencias, configuración rota) van en su propia lista y no bloquean.

Responde exactamente así:
VEREDICTO: APROBADO | RECHAZADO
LINEAS: <total> de 150 más <filas de la tabla de variables>
DEFECTOS:
- [FALSO|FALTA|SOBRA|ESTRUCTURA] L<línea> — qué está mal — evidencia: <...> — arreglo: <texto exacto o recorte>
HALLAZGOS DEL REPO: <lista, o "ninguno">
APROBADO solo si DEFECTOS está vacío. No fabriques defectos para justificar un rechazo.
```

`{ALCANCE}`: README nuevo o reescritura completa → `Revisa el README completo.` Actualización parcial → `Revisa solo estas secciones: {SECCIONES}.`
