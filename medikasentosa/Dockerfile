# ============================================
# BACKEND — Spring Boot 4.1.0 + Java 21
# ============================================
# OFFLINE: FROM medika-backend-deps (deps udah di-image)
# Ganti source code? Rebuild TANPA internet
# Nambah library? Rebuild Dockerfile.deps dulu
# ============================================

FROM medika-backend-deps:latest AS builder
WORKDIR /app
# build.gradle, settings.gradle, gradlew, gradle/ SUDAH ADA
# ~/.gradle/caches/ SUDAH TERISI dependencies

# Salin source code
COPY src src

# Build JAR — zero download
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

# Non-root user (security best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app
USER appuser

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD curl -so /dev/null http://localhost:8080/ || curl -so /dev/null http://localhost:8080/swagger-ui/index.html || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
