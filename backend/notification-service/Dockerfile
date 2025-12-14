# Multi-stage build for notification-service
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

WORKDIR /app

# Copy parent POM and shared library first
COPY backend/parent-pom/pom.xml /app/parent-pom/pom.xml
COPY backend/shared-library /app/shared-library

# Build shared library
WORKDIR /app/shared-library
RUN mvn install -DskipTests

# Copy and build notification-service
WORKDIR /app/notification-service
COPY backend/notification-service/pom.xml .
RUN mvn dependency:go-offline

COPY backend/notification-service/src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY --from=build /app/notification-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
