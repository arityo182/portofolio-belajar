#!/bin/bash
# ================================================
# DISASTERWATCH - OFFLINE DOWNLOAD SCRIPT
# Untuk Project Portofolio RS Medika Sentosa
# + DevSecOps Stack & Observability
# Jalankan SEKALI saat ada internet
# ================================================
# Project terdiri dari:
#   - medikasentosa/     (Spring Boot 4.1.0, Java 21, Gradle)
#   - hospital-frontend/ (React 19, Vite 8, Tailwind 4)
#   - ml-service/        (FastAPI, TensorFlow 2.21, Python 3.10)
#   - PostgreSQL 16 + Redis 7 (Docker)
#   - DevSecOps tools    (Trivy, Hadolint, Gitleaks, act, OWASP)
#   - Observability      (Prometheus, Grafana, OpenTelemetry, Jaeger)
# ================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SAVE_DIR="$HOME/offline-packages"

echo "================================================"
echo " DISASTERWATCH OFFLINE DOWNLOADER"
echo " Project: RS Medika Sentosa + DevSecOps"
echo "================================================"
echo " Project root : $PROJECT_DIR"
echo " Save to     : $SAVE_DIR"
echo "================================================"

# Buat direktori
mkdir -p "$SAVE_DIR"/{docker,gradle,npm,python,tools,maven}

# ==============================================
# 1. DOCKER IMAGES — Infrastructure + DevSecOps
# ==============================================
echo ""
echo "[1/6] Docker images — Infrastructure + DevSecOps Stack"

IMAGES=(
  # Infrastructure (dipakai project)
  "postgres:16-alpine"
  "redis:7-alpine"

  # DevSecOps & Observability (opsional)
  "quay.io/keycloak/keycloak:24.0"
  "localstack/localstack:latest"
  "prom/prometheus:latest"
  "grafana/grafana:latest"
  "minio/minio:latest"
  "sonarqube:community"
  "aquasec/trivy:latest"
  "hadolint/hadolint:latest"
  "zricethezav/gitleaks:latest"

  # OpenTelemetry & Tracing Stack
  "otel/opentelemetry-collector-contrib:latest"
  "jaegertracing/all-in-one:latest"
)

for IMAGE in "${IMAGES[@]}"; do
  echo "  Pulling $IMAGE ..."
  docker pull "$IMAGE" 2>/dev/null || echo "  ⚠️  docker tidak berjalan, skip pull."

  FILENAME=$(echo "$IMAGE" | tr '/:' '--')
  echo "  Saving → $SAVE_DIR/docker/$FILENAME.tar"
  docker save "$IMAGE" -o "$SAVE_DIR/docker/$FILENAME.tar" 2>/dev/null || true
done

echo "✅ Docker images selesai."

# ==============================================
# 2. GRADLE — Backend dependencies
# ==============================================
echo ""
echo "[2/6] Backend — Gradle dependencies"

if [ -f "$PROJECT_DIR/medikasentosa/build.gradle" ]; then
  cd "$PROJECT_DIR/medikasentosa"
  echo "  Spring Boot 4.1.0 · Java 21"
  echo "  Downloading dependencies..."
  ./gradlew dependencies --no-daemon -q 2>/dev/null || true
  echo "  Building project (skip test)..."
  ./gradlew build -x test --no-daemon -q 2>/dev/null || echo "  ⚠️  Build warning, lanjut..."
  cd "$PROJECT_DIR"

  # Backup Gradle cache
  echo "  Backing up ~/.gradle/caches..."
  if [ -d "$HOME/.gradle/caches" ]; then
    rsync -a --delete "$HOME/.gradle/caches/" "$SAVE_DIR/gradle/caches/" 2>/dev/null || \
    cp -r "$HOME/.gradle/caches" "$SAVE_DIR/gradle/" 2>/dev/null || true
  fi
  echo "✅ Gradle dependencies selesai."
else
  echo "⚠️  medikasentosa/build.gradle tidak ditemukan."
fi

# ==============================================
# 3. NPM — Frontend dependencies
# ==============================================
echo ""
echo "[3/6] Frontend — npm packages"

if [ -f "$PROJECT_DIR/hospital-frontend/package.json" ]; then
  cd "$PROJECT_DIR/hospital-frontend"
  echo "  React 19 · Vite 8 · Tailwind 4 · TypeScript 5.9"
  echo "  React Router 7.18 · Axios 1.18 · Lucide 1.21"
  echo ""
  echo "  Downloading npm packages..."

  # Bersihkan node_modules dulu biar fresh
  rm -rf node_modules

  # Install dependencies
  npm install 2>&1 | tail -3

  # Copy node_modules ke offline storage
  echo "  Copying node_modules → $SAVE_DIR/npm/"
  if [ -d "node_modules" ]; then
    rsync -a --delete node_modules/ "$SAVE_DIR/npm/node_modules/" 2>/dev/null || \
    cp -r node_modules "$SAVE_DIR/npm/" 2>/dev/null || true
    cp package-lock.json "$SAVE_DIR/npm/" 2>/dev/null || true
    cp package.json "$SAVE_DIR/npm/" 2>/dev/null || true
  fi

  cd "$PROJECT_DIR"
  echo "✅ npm packages selesai."
else
  echo "⚠️  hospital-frontend/package.json tidak ditemukan."
fi

# ==============================================
# 4. PYTHON — ML Service dependencies
# ==============================================
echo ""
echo "[4/6] ML Service — Python packages"

if [ -f "$PROJECT_DIR/ml-service/requirements.txt" ]; then
  cd "$PROJECT_DIR/ml-service"
  echo "  FastAPI 0.138 · TensorFlow 2.21 · Python 3.10"
  echo ""

  # Gunakan requirements.txt ASLI dari project (51 package eksak)
  pip download \
    -r requirements.txt \
    -d "$SAVE_DIR/python/packages" \
    2>&1 | tail -5

  # Simpan requirements.txt untuk referensi offline
  cp requirements.txt "$SAVE_DIR/python/"
  cd "$PROJECT_DIR"
  echo "✅ Python packages selesai."
else
  echo "⚠️  ml-service/requirements.txt tidak ditemukan."
fi

# ==============================================
# 5. DEVSECOPS TOOLS & AGENTS — Binaries
# ==============================================
echo ""
echo "[5/6] DevSecOps tools & Agents..."

# OpenTelemetry Java Agent (Untuk Spring Boot Auto-Instrumentation)
echo "  Downloading OpenTelemetry Java Agent..."
curl -sL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar" \
  -o "$SAVE_DIR/tools/opentelemetry-javaagent.jar"

# Trivy binary
echo "  Downloading Trivy..."
TRIVY_VERSION="0.53.0"
curl -sL "https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz" \
  -o "$SAVE_DIR/tools/trivy.tar.gz"
tar -xzf "$SAVE_DIR/tools/trivy.tar.gz" -C "$SAVE_DIR/tools/" trivy 2>/dev/null
chmod +x "$SAVE_DIR/tools/trivy" 2>/dev/null || true

# act — GitHub Actions local runner
echo "  Downloading act..."
curl -sL "https://raw.githubusercontent.com/nektos/act/master/install.sh" 2>/dev/null \
  | bash -s -- -b "$SAVE_DIR/tools" 2>/dev/null || true

# Hadolint — Dockerfile linter
echo "  Downloading Hadolint..."
curl -sL "https://github.com/hadolint/hadolint/releases/latest/download/hadolint-Linux-x86_64" \
  -o "$SAVE_DIR/tools/hadolint" 2>/dev/null
chmod +x "$SAVE_DIR/tools/hadolint" 2>/dev/null || true

# Gitleaks — secret scanner
echo "  Downloading Gitleaks..."
GITLEAKS_VERSION="8.18.4"
curl -sL "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz" \
  -o "$SAVE_DIR/tools/gitleaks.tar.gz"
tar -xzf "$SAVE_DIR/tools/gitleaks.tar.gz" -C "$SAVE_DIR/tools/" gitleaks 2>/dev/null
chmod +x "$SAVE_DIR/tools/gitleaks" 2>/dev/null || true

# OWASP Dependency Check
echo "  Downloading OWASP Dependency Check..."
OWASP_VERSION="10.0.3"
curl -sL "https://github.com/jeremylong/DependencyCheck/releases/download/v${OWASP_VERSION}/dependency-check-${OWASP_VERSION}-release.zip" \
  -o "$SAVE_DIR/tools/owasp-dependency-check.zip"
unzip -q "$SAVE_DIR/tools/owasp-dependency-check.zip" -d "$SAVE_DIR/tools/" 2>/dev/null || true

echo "✅ DevSecOps tools selesai."

# ==============================================
# 6. INSTALL SCRIPT — Untuk offline
# ==============================================
echo ""
echo "[6/6] Membuat install script offline..."

cat > "$SAVE_DIR/install-offline.sh" << 'INSTALL'
#!/bin/bash
# ================================================
# INSTALL OFFLINE — RS Medika Sentosa + DevSecOps
# Jalankan di rumah TANPA internet
# ================================================

set -e
SAVE_DIR="$HOME/offline-packages"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
[ -d "$PROJECT_DIR/medikasentosa" ] || PROJECT_DIR="$(pwd)"

echo "================================================"
echo " INSTALL OFFLINE - RS Medika Sentosa"
echo "================================================"

# ----------------------------------------------
# A. Load Docker images
# ----------------------------------------------
echo ""
echo "[A] Loading Docker images..."
for TAR in "$SAVE_DIR/docker/"*.tar; do
  NAME=$(basename "$TAR" .tar | tr '--' '/:')
  echo "  Loading $NAME ..."
  docker load -i "$TAR" 2>/dev/null || echo "  ⚠️  Gagal load $TAR"
done
echo "✅ Docker images siap."

# ----------------------------------------------
# B. Restore Gradle cache
# ----------------------------------------------
echo ""
echo "[B] Restore Gradle cache..."
if [ -d "$SAVE_DIR/gradle/caches" ]; then
  mkdir -p "$HOME/.gradle"
  rsync -a "$SAVE_DIR/gradle/caches/" "$HOME/.gradle/caches/" 2>/dev/null
  echo "✅ Gradle cache dipulihkan."
else
  echo "⚠️  Tidak ada Gradle cache."
fi

# ----------------------------------------------
# C. Restore npm node_modules
# ----------------------------------------------
echo ""
echo "[C] Restore frontend dependencies..."
if [ -d "$SAVE_DIR/npm/node_modules" ] && [ -f "$PROJECT_DIR/hospital-frontend/package.json" ]; then
  cd "$PROJECT_DIR/hospital-frontend"

  # Hapus node_modules lama kalau ada
  rm -rf node_modules package-lock.json

  # Copy dari offline storage
  cp -r "$SAVE_DIR/npm/node_modules" node_modules
  cp "$SAVE_DIR/npm/package-lock.json" . 2>/dev/null || true

  # Rebuild native modules biar cocok sama OS
  npm rebuild 2>/dev/null || true

  echo "✅ Frontend dependencies siap."
  cd "$PROJECT_DIR"
else
  echo "⚠️  Tidak ada npm cache."
fi

# ----------------------------------------------
# D. Install Python packages offline
# ----------------------------------------------
echo ""
echo "[D] Install Python packages offline..."
if [ -d "$SAVE_DIR/python/packages" ] && [ -f "$SAVE_DIR/python/requirements.txt" ]; then

  # Buat virtualenv kalau belum ada
  if [ ! -d "$PROJECT_DIR/ml-service/venv" ]; then
    python3 -m venv "$PROJECT_DIR/ml-service/venv"
  fi

  source "$PROJECT_DIR/ml-service/venv/bin/activate"

  pip install \
    --no-index \
    --find-links="$SAVE_DIR/python/packages" \
    -r "$SAVE_DIR/python/requirements.txt" \
    2>&1 | tail -3

  deactivate
  echo "✅ Python packages siap (venv)."
else
  echo "⚠️  Tidak ada Python packages."
fi

# ----------------------------------------------
# E. Install DevSecOps tools
# ----------------------------------------------
echo ""
echo "[E] Install DevSecOps tools..."
echo "  Tools tersedia di: $SAVE_DIR/tools/"
echo "  - opentelemetry-javaagent.jar"
echo "  - trivy"
echo "  - hadolint"
echo "  - gitleaks"
echo "  - act"
echo "  - OWASP Dependency Check"

# Copy tools ke PATH
if command -v sudo &>/dev/null; then
  SUDO=sudo
else
  SUDO=""
fi

[ -f "$SAVE_DIR/tools/trivy" ] && $SUDO cp "$SAVE_DIR/tools/trivy" /usr/local/bin/ 2>/dev/null && echo "  ✅ trivy terinstall" || true
[ -f "$SAVE_DIR/tools/hadolint" ] && $SUDO cp "$SAVE_DIR/tools/hadolint" /usr/local/bin/ 2>/dev/null && echo "  ✅ hadolint terinstall" || true
[ -f "$SAVE_DIR/tools/gitleaks" ] && $SUDO cp "$SAVE_DIR/tools/gitleaks" /usr/local/bin/ 2>/dev/null && echo "  ✅ gitleaks terinstall" || true
[ -f "$SAVE_DIR/tools/act" ] && $SUDO cp "$SAVE_DIR/tools/act" /usr/local/bin/ 2>/dev/null && echo "  ✅ act terinstall" || true

echo "✅ DevSecOps tools siap."

# ----------------------------------------------
# Selesai
# ----------------------------------------------
echo ""
echo "================================================"
echo " INSTALL OFFLINE SELESAI"
echo "================================================"
echo ""
echo "  Jalankan project dengan:"
echo ""
echo "  # 1. Docker (PostgreSQL + Redis)"
echo "  docker compose up -d"
echo ""
echo "  # 2. Backend"
echo "  cd medikasentosa && ./gradlew bootRun"
echo ""
echo "  # 3. Frontend"
echo "  cd hospital-frontend && npm run dev"
echo ""
echo "  # 4. ML Service"
echo "  cd ml-service && source venv/bin/activate"
echo "  python -m app.main"
echo ""
echo "  # Opsional: OpenTelemetry Java Agent"
echo "  java -javaagent:$SAVE_DIR/tools/opentelemetry-javaagent.jar -jar app.jar"
echo ""
echo "================================================"
INSTALL

chmod +x "$SAVE_DIR/install-offline.sh"

# ==============================================
# SUMMARY
# ==============================================
echo ""
echo "================================================"
echo " DOWNLOAD SELESAI"
echo "================================================"
echo ""
echo " Lokasi: $SAVE_DIR"
echo ""
du -sh "$SAVE_DIR/docker" \
       "$SAVE_DIR/gradle" \
       "$SAVE_DIR/npm" \
       "$SAVE_DIR/python" \
       "$SAVE_DIR/tools" \
       2>/dev/null || true
echo ""
echo " Di rumah, jalankan:"
echo "   bash $SAVE_DIR/install-offline.sh"
echo ""
echo "================================================"
