# ===================================================
# CropLink — Farm-to-Door Delivery Platform
# Multi-Stage Dockerfile for Render Deployment
# ===================================================

# Stage 1: Build the Application with Maven
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create persistent/local storage directory
RUN mkdir -p uploads/products

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Default port (Render overrides this with $PORT)
ENV PORT=8080
EXPOSE 8080

# Run Spring Boot with tuned JVM flags for low-memory cloud tiers (Render / Railway / Fly)
ENTRYPOINT ["sh", "-c", "java -XX:+UseSerialGC -Xmx350m -Xss512k -Dserver.port=${PORT} -jar app.jar"]
