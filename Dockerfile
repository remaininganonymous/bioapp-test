FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/com.example.ktor-sample-all.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]