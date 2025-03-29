# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Install Maven only for the build stage
RUN apk add --no-cache maven

# Copy the entire project to make sure all modules are available
COPY . .

# Accept the Sentry auth token as a build argument
ARG SENTRY_AUTH_TOKEN

# Set it as an environment variable for Maven to use
ENV SENTRY_AUTH_TOKEN=${SENTRY_AUTH_TOKEN}

# Run Maven to resolve dependencies and build the project
RUN mvn clean package -e

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Set timezone
RUN apk add --no-cache tzdata
ENV TZ=Europe/Paris

# Copy only the final JAR file from the build stage
COPY --from=build /app/controller/target/*-exe.jar quoi-regarder.jar

# Create a directory for storage if needed
RUN mkdir -p /app/storage

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/quoi-regarder.jar"]