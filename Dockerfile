FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:25-jre-noble
WORKDIR /app

RUN groupadd --gid 10001 mecanica \
    && useradd --uid 10001 --gid mecanica --no-create-home --no-log-init mecanica

COPY --from=build --chown=mecanica:mecanica /app/target/*.jar app.jar

USER mecanica

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
