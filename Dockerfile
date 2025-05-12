# -------- STAGE 1: Build the application --------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src/ src/

# Package the application
RUN mvn clean package -DskipTests

# -------- STAGE 2: Run the application --------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
