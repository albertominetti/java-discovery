#FROM openjdk:14-alpine
FROM openjdk:11.0.7-jre-slim
VOLUME /tmp
ENV CONSUL_HOST localhost
ENV CONSUL_PORT 8500
ENV LOGGING_FILE_PATH /log
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
