# Multi-stage build for gateway-service
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

WORKDIR /app

# Copy parent POM
COPY backend/parent-pom/pom.xml /app/parent-pom/pom.xml

# Copy and build gateway-service
WORKDIR /app/gateway-service
COPY backend/gateway-service/pom.xml .
RUN mvn dependency:go-offline

COPY backend/gateway-service/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY --from=build /app/gateway-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
