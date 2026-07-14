FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN mvn clean package

EXPOSE 8080
EXPOSE 8888/udp

CMD ["java",
     "-Djava.util.logging.config.file=target/classes/logging.properties",
     "-cp",
     "target/classes:target/dependency/*",
     "service.GameTcpServer"]