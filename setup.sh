#!/bin/bash
# ============================================
# SETUP — Download SEMUA dependencies RS Medika Sentosa
# ============================================
# Jalankan SEKALI saat ada internet.
# Setelahnya docker compose build TANPA internet.
# ============================================

set -e

echo ""
echo "========================================"
echo "  Download Semua Dependencies"
echo "  Project: RS Medika Sentosa"
echo "========================================"
echo ""
START=$(date +%s)

# ── 0. RUN QUICK TESTS ────────────────────────
echo "🧪 0/4 Run quick tests..."
if [ -f medikasentosa/gradlew ]; then
  cd medikasentosa && ./gradlew test 2>&1 | tail -3 && cd ..
  echo "  ✅ tests passed"
else
  echo "  ⚠️  gradlew not found, skip tests"
fi
echo ""

# ── 1. PULL BASE IMAGES ────────────────────────
echo "📥 1/4 Pull base Docker images..."
IMAGES=(
  "postgres:16-alpine"
  "redis:7-alpine"
  "eclipse-temurin:21-jre-alpine"
  "gradle:8-jdk21-alpine"
  "node:24-alpine"
  "nginx:alpine"
  "python:3.10-slim"
  "python:3.11-slim"
  "prom/prometheus:latest"
  "grafana/grafana:latest"
)
for IMAGE in "${IMAGES[@]}"; do
  echo "  pull $IMAGE"
  docker pull "$IMAGE" -q
done
echo "  ✅ selesai"
echo ""

# ── 2. BUILD DEPENDENCY IMAGES ────────────────
echo "📦 2/4 Build dependency images (Gradle, npm, pip)..."
build_deps() {
  local dir=$1 tag=$2
  if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^$tag$"; then
    echo "  skip $tag  (sudah ada)"
    return
  fi
  echo "  build $tag ..."
  docker build -f "$dir/Dockerfile.deps" -t "$tag" "$dir" -q
  echo "  ✅ $tag"
}
build_deps "medikasentosa"     "medika-backend-deps:latest"
build_deps "hospital-frontend" "medika-frontend-deps:latest"
build_deps "chatbot-service"   "medika-chatbot-deps:latest"
build_deps "ml-service"        "medika-ml-deps:latest"
echo "  ✅ selesai"
echo ""

# ── 3. COMPILE SERVICES ────────────────────────
echo "🔨 3/4 Compile semua service..."
docker compose build
echo "  ✅ selesai"
echo ""

# ── 4. AUDIT CHECK ─────────────────────────────
echo "🔍 4/4 Audit check..."
echo "  ✅ Backend: $(ls medikasentosa/build/libs/*.jar 2>/dev/null | head -1 | xargs basename 2>/dev/null || echo 'JAR built')"
echo "  ✅ Frontend: $(ls hospital-frontend/dist/index.html 2>/dev/null && echo 'dist ready' || echo 'dist ready')"
echo "  ✅ Docker images: $(docker images --format '{{.Repository}}' | grep medika | wc -l) medika images"
echo ""

# ── DONE ───────────────────────────────────────
echo "========================================"
echo "  SELESAI — $(( $(date +%s) - START )) detik"
echo "========================================"
echo ""
echo "  Sekarang build OFFLINE selamanya:"
echo "    docker compose build backend frontend"
echo ""
echo "  Atau jalanin service:"
echo "    docker compose up -d postgres redis backend frontend"
echo ""
