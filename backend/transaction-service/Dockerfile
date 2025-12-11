# Multi-stage build for transaction-service
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

WORKDIR /app

# Copy parent POM and shared library first
COPY backend/parent-pom/pom.xml /app/parent-pom/pom.xml
COPY backend/shared-library /app/shared-library

# Build shared library
WORKDIR /app/shared-library
RUN mvn install -DskipTests

# Copy and build transaction-service
WORKDIR /app/transaction-service
COPY backend/transaction-service/pom.xml .
RUN mvn dependency:go-offline

COPY backend/transaction-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY --from=build /app/transaction-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
