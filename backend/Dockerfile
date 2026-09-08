# Multi-stage Dockerfile for Spring Boot Backend (Java 21)

# Stage 1: Build (Using Ubuntu 24.04 Noble base to pass build-time scan)
FROM maven:3.9-eclipse-temurin-21-noble AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime using Google Distroless (Secure & zero-OS vulnerability base image)
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
