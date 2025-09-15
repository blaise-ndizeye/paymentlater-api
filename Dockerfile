# Multi-stage Dockerfile for PaymentLaterAPI
# Stage 1: Build and Test Stage
FROM eclipse-temurin:21-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Install necessary packages
RUN apk update && apk add --no-cache \
    curl \
    bash

# Copy Gradle wrapper and build files
COPY gradle/ gradle/
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Set environment variables for tests
ENV MONGODB_URI="mongodb://localhost:27017/payment_later_test"
ENV MONGODB_DATABASE="payment_later_test"
ENV JWT_SECRET_BASE64="dGVzdEp3dFNlY3JldEZvckRvY2tlckJ1aWxkU3RhZ2U="
ENV MAIL_SERVER_EMAIL="test@example.com"
ENV MAIL_SERVER_PASSWORD="test-password"

# Run tests
RUN ./gradlew test --no-daemon

# Build the application
RUN ./gradlew bootJar --no-daemon

# Verify the JAR file was created
RUN ls -la build/libs/

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Set working directory
WORKDIR /app

# Create a non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -S -D -u 1001 -G appgroup appuser

# Install curl for health checks
RUN apk update && apk add --no-cache curl

# Copy the JAR file from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 1010

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:1010/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]