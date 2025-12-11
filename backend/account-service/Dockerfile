# Multi-stage build for account-service
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

WORKDIR /app

# Copy parent POM and shared library first
COPY backend/parent-pom/pom.xml /app/parent-pom/pom.xml
COPY backend/shared-library /app/shared-library

# Build shared library
WORKDIR /app/shared-library
RUN mvn install -DskipTests

# Copy and build account-service
WORKDIR /app/account-service
COPY backend/account-service/pom.xml .
RUN mvn dependency:go-offline

COPY backend/account-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY --from=build /app/account-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
