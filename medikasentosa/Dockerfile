# ============================================
# BACKEND — Spring Boot 4.1.0 + Java 21
# Multi-stage: Gradle build → JRE runtime
# ============================================

# Stage 1: Build dengan Gradle
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app

# Salin Gradle wrapper & config dulu (cache layer)
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon -q || true

# Salin source code
COPY src src

# Build JAR (skip test)
RUN ./gradlew build -x test --no-daemon

# ============================================
# Stage 2: Runtime dengan JRE kecil
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl untuk healthcheck
RUN apk add --no-cache curl

# Copy JAR dari stage builder
COPY --from=builder /app/build/libs/ /app/libs/
RUN cp /app/libs/*.jar /app/app.jar 2>/dev/null; \
    JAR=$(ls /app/libs/*.jar | grep -v plain | head -1); \
    [ -n "$JAR" ] && cp "$JAR" /app/app.jar; \
    rm -rf /app/libs

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD curl -so /dev/null http://localhost:8080/ || curl -so /dev/null http://localhost:8080/swagger-ui/index.html || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
