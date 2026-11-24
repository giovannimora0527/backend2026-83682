# Backend — Clínica Veterinaria Uniminuto

API REST Spring Boot 4 (Java 17) con MySQL 8. Contexto: `/clinica/v1`, puerto 8080.
Swagger: http://localhost:8080/clinica/v1/swagger-ui.html

```bash
mvn spring-boot:run      # requiere la base creada con ../base-de-datos/clinica.sql
mvn test                 # pruebas unitarias de reglas de negocio
mvn -DskipTests package  # genera target/clinica.jar
```

Variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_HORAS`.
Documentación completa, arquitectura y credenciales de prueba en el [README principal](../README.md).
