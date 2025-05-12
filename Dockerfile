# -------- STAGE 1: Build the application --------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy Maven config and dependencies first (for caching)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN ./mvnw dependency:go-offline

# Now copy source and build
COPY src/ src/
RUN ./mvnw package -DskipTests

# -------- STAGE 2: Run the application --------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
