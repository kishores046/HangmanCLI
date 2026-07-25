
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine AS prod

WORKDIR /app

RUN mkdir -p /app/logs

COPY --from=build /app/target/classes ./target/classes
COPY --from=build /app/target/dependency ./target/dependency

EXPOSE 8080
EXPOSE 8088/udp

ENTRYPOINT ["java", \
     "-Djava.util.logging.config.file=target/classes/logging.properties", \
     "-cp", "target/classes:target/dependency/*", \
     "service.GameTcpServer"]