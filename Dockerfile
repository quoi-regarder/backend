# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Install Maven only for the build stage
RUN apk add --no-cache maven

# Copy the entire project to make sure all modules are available
COPY . .

# Run Maven to resolve dependencies and build the project
RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Set timezone
RUN apk add --no-cache tzdata
ENV TZ=Europe/Paris

# Copy only the final JAR file from the build stagec
COPY --from=build /app/controller/target/*-exe.jar quoi-regarder.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/quoi-regarder.jar"]
