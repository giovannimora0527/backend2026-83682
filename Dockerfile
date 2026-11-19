FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src/main/ src/main/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
    && cp target/clinica-*.jar /build/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/app.jar ./app.jar
USER 10001:10001
EXPOSE 8080
# Las citas DATETIME usan hora local de la clínica, igual que el JDBC de Compose.
ENTRYPOINT ["java", "-Duser.timezone=America/Bogota", "-jar", "/app/app.jar"]
