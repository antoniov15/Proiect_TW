# Multi-stage Dockerfile for a Spring Boot (Maven) app
# Build stage: use Maven image with JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace/app

# Copy only the files necessary for a Maven build first (speeds layer caching)
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src ./src

# Ensure wrapper is executable and build the project (skip tests for faster build)
RUN chmod +x mvnw && ./mvnw -DskipTests package

# Runtime stage: lightweight JRE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from the build stage. Uses wildcard to pick the Spring Boot jar.
COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
