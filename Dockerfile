FROM eclipse-temurin:25-jre

WORKDIR /app

# GitHub Actions creates this file from the Spring Boot executable JAR.
COPY app.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0 -Dfile.encoding=UTF-8"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
