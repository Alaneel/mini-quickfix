# Multi-stage Dockerfile for Mini QuickFIX

# Stage 1: Build stage
FROM maven:3.9.5-eclipse-temurin-11 AS build

LABEL maintainer="Alan Wang"
LABEL description="Mini QuickFIX - Lightweight FIX Protocol Library"

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY checkstyle.xml .

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:11-jre-alpine

LABEL maintainer="Alan Wang"
LABEL description="Mini QuickFIX Runtime"

# Install curl for healthchecks
RUN apk add --no-cache curl

# Create app user
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy configuration files
COPY --from=build /app/src/main/resources/application.properties ./config/

# Create directories for logs and storage
RUN mkdir -p /app/logs /app/fix_store && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose ports
EXPOSE 9878

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:9878/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
