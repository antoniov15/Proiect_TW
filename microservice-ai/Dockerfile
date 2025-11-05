FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace/app

COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
