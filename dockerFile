# Dockerfile for Backend Service

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-noble AS build
WORKDIR /app

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime using Google Distroless
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
