# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# La cache de Maven se conserva entre compilaciones.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package && cp target/clinica-*.jar /app/app.jar

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --home-dir /app clinica && mkdir -p /secretos && chown clinica /secretos
WORKDIR /app
COPY --from=build /app/app.jar app.jar
COPY --chmod=0755 deploy/backend/entrypoint.sh /entrypoint.sh
USER clinica
VOLUME /secretos
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
