#!/bin/bash
# ================================================
# PROJECT FINAL 3 - OFFLINE DOWNLOAD SCRIPT
# Project Final 3 — Template untuk project baru
# + DevSecOps Stack & Observability
# Jalankan SEKALAI saat ada internet
# ================================================
# Template struktur project:
#   - project-final3-backend/   (Spring Boot 4.x, Java 21, Gradle)
#   - hospital-frontendjs/     (React 19, Vite 8, Tailwind 4, JavaScript)
#   - project-final3-ml/        (FastAPI, TensorFlow, Python 3.10)
#   - PostgreSQL 16 + Redis 7 (Docker)
#   - DevSecOps tools           (Trivy, Hadolint, Gitleaks, act, OWASP)
#   - Observability             (Prometheus, Grafana, OpenTelemetry, Jaeger)
# ================================================
# Cara pakai:
#   1. Letakkan script ini di ROOT folder project kamu
#   2. Jalankan: bash project-final3-offline.sh
#   3. Semua dependencies akan di-download ke ~/offline-packages/
#   4. Di rumah (offline): bash ~/offline-packages/install-offline.sh
# ================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SAVE_DIR="$HOME/offline-packages"

echo "================================================"
echo " PROJECT FINAL 3 OFFLINE DOWNLOADER"
echo " Project: Project Final 3 + DevSecOps"
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
  "quay.io/keycloak/keycloak:26.0"
  "localstack/localstack:3.8"
  "prom/prometheus:v2.54.0"
  "grafana/grafana:11.0.0"
  "minio/minio:RELEASE.2024-08-17T03-42-48Z"
  "sonarqube:community"
  "aquasec/trivy:0.58.0"
  "hadolint/hadolint:2.12.0"
  "zricethezav/gitleaks:8.19.0"

  # OpenTelemetry & Tracing Stack
  "otel/opentelemetry-collector-contrib:0.121.0"
  "jaegertracing/all-in-one:1.57"
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

if [ -f "$PROJECT_DIR/project-final3-backend/build.gradle" ] || [ -f "$PROJECT_DIR/medikasentosa/build.gradle" ]; then
  # Cari build.gradle yang ada
  if [ -f "$PROJECT_DIR/project-final3-backend/build.gradle" ]; then
    BACKEND_DIR="$PROJECT_DIR/project-final3-backend"
  else
    BACKEND_DIR="$PROJECT_DIR/medikasentosa"
  fi

  cd "$BACKEND_DIR"
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
  echo "⚠️  Backend build.gradle tidak ditemukan (project-final3-backend/ atau medikasentosa/)."
fi

# ==============================================
# 3. NPM — Frontend dependencies
# ==============================================
echo ""
echo "[3/6] Frontend — npm packages"

if [ -f "$PROJECT_DIR/hospital-frontendjs/package.json" ] || [ -f "$PROJECT_DIR/project-final3-frontend/package.json" ]; then
  if [ -f "$PROJECT_DIR/hospital-frontendjs/package.json" ]; then
    FRONTEND_DIR="$PROJECT_DIR/hospital-frontendjs"
  else
    FRONTEND_DIR="$PROJECT_DIR/project-final3-frontend"
  fi

  cd "$FRONTEND_DIR"
  echo "  React 19 · Vite 8 · Tailwind 4 · JavaScript"
  echo "  React Router 7.18 · Axios 1.18 · Lucide 1.21"
  echo "  @tanstack/react-query (v5) · dayjs · sonner"
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
  echo "⚠️  Frontend package.json tidak ditemukan (hospital-frontendjs/ atau project-final3-frontend/)."
fi

# ==============================================
# 4. PYTHON — ML Service dependencies
# ==============================================
echo ""
echo "[4/6] ML Service — Python packages"

if [ -f "$PROJECT_DIR/project-final3-ml/requirements.txt" ] || [ -f "$PROJECT_DIR/ml-service/requirements.txt" ]; then
  if [ -f "$PROJECT_DIR/project-final3-ml/requirements.txt" ]; then
    ML_DIR="$PROJECT_DIR/project-final3-ml"
  else
    ML_DIR="$PROJECT_DIR/ml-service"
  fi

  cd "$ML_DIR"
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
  echo "⚠️  ML Service requirements.txt tidak ditemukan (project-final3-ml/ atau ml-service/)."
fi

# ==============================================
# 5. CHATBOT SERVICE — Python packages (RAG + vLLM)
# ==============================================
echo ""
echo "[4b/6] Chatbot Service — Python packages"

if [ -f "$PROJECT_DIR/project-final3-chatbot/requirements.txt" ] || [ -f "$PROJECT_DIR/chatbot-service/requirements.txt" ]; then
  if [ -f "$PROJECT_DIR/project-final3-chatbot/requirements.txt" ]; then
    CHATBOT_DIR="$PROJECT_DIR/project-final3-chatbot"
  else
    CHATBOT_DIR="$PROJECT_DIR/chatbot-service"
  fi

  cd "$CHATBOT_DIR"
  echo "  FastAPI 0.115 · ChromaDB 0.5 · sentence-transformers 3.0"
  echo "  pypdf · python-docx · langchain-text-splitters"
  echo ""

  # Download packages for chatbot
  pip download \
    -r requirements.txt \
    -d "$SAVE_DIR/python/chatbot-packages" \
    2>&1 | tail -5

  # Simpan requirements.txt untuk referensi offline
  cp requirements.txt "$SAVE_DIR/python/chatbot-requirements.txt"
  cd "$PROJECT_DIR"
  echo "✅ Chatbot Python packages selesai."
else
  echo "⚠️  Chatbot requirements.txt tidak ditemukan (project-final3-chatbot/ atau chatbot-service/)."
fi

# ==============================================
# 6. DEVSECOPS TOOLS & AGENTS — Binaries
# ==============================================
echo ""
echo "[5/6] DevSecOps tools & Agents..."

# OpenTelemetry Java Agent (Untuk Spring Boot Auto-Instrumentation)
echo "  Downloading OpenTelemetry Java Agent..."
curl -sL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar" \
  -o "$SAVE_DIR/tools/opentelemetry-javaagent.jar"

# Trivy binary
echo "  Downloading Trivy..."
TRIVY_VERSION="0.58.0"
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
GITLEAKS_VERSION="8.19.0"
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
# 7. INSTALL SCRIPT — Untuk offline
# ==============================================
echo ""
echo "[6/6] Membuat install script offline..."

cat > "$SAVE_DIR/install-offline.sh" << 'INSTALL'
#!/bin/bash
# ================================================
# INSTALL OFFLINE — Project Final 3 + DevSecOps
# Jalankan di rumah TANPA internet
# ================================================

set -e
SAVE_DIR="$HOME/offline-packages"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
[ -d "$PROJECT_DIR/project-final3-backend" ] || [ -d "$PROJECT_DIR/medikasentosa" ] || PROJECT_DIR="$(pwd)"

echo "================================================"
echo " INSTALL OFFLINE - Project Final 3"
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
if [ -d "$SAVE_DIR/npm/node_modules" ]; then
  # Cari frontend directory
  if [ -f "$PROJECT_DIR/hospital-frontendjs/package.json" ]; then
    FRONTEND_DIR="$PROJECT_DIR/hospital-frontendjs"
  elif [ -f "$PROJECT_DIR/project-final3-frontend/package.json" ]; then
    FRONTEND_DIR="$PROJECT_DIR/project-final3-frontend"
  else
    FRONTEND_DIR=""
  fi

  if [ -n "$FRONTEND_DIR" ]; then
    cd "$FRONTEND_DIR"

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
    echo "⚠️  Frontend directory tidak ditemukan."
  fi
else
  echo "⚠️  Tidak ada npm cache."
fi

# ----------------------------------------------
# D. Install Python packages offline (ML Service)
# ----------------------------------------------
echo ""
echo "[D] Install Python packages offline (ML Service)..."
if [ -d "$SAVE_DIR/python/packages" ] && [ -f "$SAVE_DIR/python/requirements.txt" ]; then

  # Cari ML directory
  if [ -d "$PROJECT_DIR/project-final3-ml" ]; then
    ML_DIR="$PROJECT_DIR/project-final3-ml"
  elif [ -d "$PROJECT_DIR/ml-service" ]; then
    ML_DIR="$PROJECT_DIR/ml-service"
  else
    ML_DIR=""
  fi

  if [ -n "$ML_DIR" ]; then
    # Buat virtualenv kalau belum ada
    if [ ! -d "$ML_DIR/venv" ]; then
      python3 -m venv "$ML_DIR/venv"
    fi

    source "$ML_DIR/venv/bin/activate"

    pip install \
      --no-index \
      --find-links="$SAVE_DIR/python/packages" \
      -r "$SAVE_DIR/python/requirements.txt" \
      2>&1 | tail -3

    deactivate
    echo "✅ ML Service Python packages siap (venv)."
  else
    echo "⚠️  ML Service directory tidak ditemukan."
  fi
else
  echo "⚠️  Tidak ada Python packages."
fi

# ----------------------------------------------
# E. Install Python packages offline (Chatbot Service)
# ----------------------------------------------
echo ""
echo "[E] Install Python packages offline (Chatbot Service)..."
if [ -d "$SAVE_DIR/python/chatbot-packages" ] && [ -f "$SAVE_DIR/python/chatbot-requirements.txt" ]; then

  # Cari Chatbot directory
  if [ -d "$PROJECT_DIR/project-final3-chatbot" ]; then
    CHATBOT_DIR="$PROJECT_DIR/project-final3-chatbot"
  elif [ -d "$PROJECT_DIR/chatbot-service" ]; then
    CHATBOT_DIR="$PROJECT_DIR/chatbot-service"
  else
    CHATBOT_DIR=""
  fi

  if [ -n "$CHATBOT_DIR" ]; then
    # Buat virtualenv kalau belum ada
    if [ ! -d "$CHATBOT_DIR/venv" ]; then
      python3 -m venv "$CHATBOT_DIR/venv"
    fi

    source "$CHATBOT_DIR/venv/bin/activate"

    pip install \
      --no-index \
      --find-links="$SAVE_DIR/python/chatbot-packages" \
      -r "$SAVE_DIR/python/chatbot-requirements.txt" \
      2>&1 | tail -3

    deactivate
    echo "✅ Chatbot Service Python packages siap (venv)."
  else
    echo "⚠️  Chatbot Service directory tidak ditemukan."
  fi
else
  echo "⚠️  Tidak ada Chatbot Python packages."
fi

# ----------------------------------------------
# F. Install DevSecOps tools
# ----------------------------------------------
echo ""
echo "[F] Install DevSecOps tools..."
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
echo "  # 2. Backend (Spring Boot)"
echo "  cd project-final3-backend && ./gradlew bootRun"
echo ""
echo "  # 3. Frontend (React + Vite)"
echo "  cd hospital-frontendjs && npm run dev"
echo ""
echo "  # 4. ML Service (FastAPI + TensorFlow)"
echo "  cd project-final3-ml && source venv/bin/activate"
echo "  python -m app.main"
echo ""
echo "  # 5. Chatbot Service (FastAPI + RAG + vLLM)"
echo "  cd project-final3-chatbot && source venv/bin/activate"
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