FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN mkdir -p out

RUN javac -cp "src:lib/*" \
    -d out \
    $(find src -name "*.java")

RUN cp src/resources/*.properties out/

EXPOSE 8080
EXPOSE 8888/udp

CMD ["java",
     "-Djava.util.logging.config.file=out/logging.properties",
     "-cp",
     "out:lib/*",
     "service.GameTcpServer"]