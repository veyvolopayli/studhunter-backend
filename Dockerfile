FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle buildFatJar

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
