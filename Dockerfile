# ---- Etapa 1: compilación con Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- Etapa 2: imagen de ejecución liviana ----
FROM eclipse-temurin:17-jre
ENV TZ=America/Bogota \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms128m -Xmx384m"
WORKDIR /app
RUN groupadd --system clinica && useradd --system --gid clinica clinica
COPY --from=build /app/target/clinica.jar app.jar
USER clinica
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
