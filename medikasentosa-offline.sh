#!/bin/bash
# ================================================
# MEDIKASENTOSA - OFFLINE DOWNLOAD SCRIPT
# Project: Medika Sentosa + Portofolio Stack
# Jalankan SEKALI saat ada internet (di rumah teman)
# ================================================

set -e

SAVE_DIR="$HOME/offline-packages"
LOG_FILE="$SAVE_DIR/download.log"
BASE_PORTFOLIO_DIR="$HOME/Belajar/Portofolio"

echo "================================================"
echo " MEDIKASENTOSA OFFLINE DOWNLOADER"
echo " Project: Medika Sentosa + Portofolio Stack"
echo "================================================"
echo " Save to: $SAVE_DIR"
echo " Base Dir: $BASE_PORTFOLIO_DIR"
echo "================================================"

# Buat direktori penyimpanan
mkdir -p "$SAVE_DIR"/{docker,gradle,npm,python/ml-packages,python/chatbot-packages,tools}

exec > >(tee -a "$LOG_FILE") 2>&1

# ==============================================
# 1. DOCKER IMAGES — Semua 27 images aktual
# ==============================================
echo ""
echo "[1/5] Docker images — Semua images aktual..."

MANIFEST="$SAVE_DIR/docker/manifest.txt"
> "$MANIFEST"

IMAGES=(
  # Base images
  "eclipse-temurin:21-jre-alpine"
  "gradle:8-jdk21-alpine"
  "node:24-alpine"
  "nginx:alpine"
  "python:3.10-slim"
  "python:3.11-slim"

  # Database + Cache
  "postgres:16-alpine"
  "redis:7-alpine"

  # Auth + Storage
  "quay.io/keycloak/keycloak:24.0"
  "minio/minio:latest"

  # Monitoring + Observability
  "prom/prometheus:latest"
  "grafana/grafana:latest"
  "jaegertracing/all-in-one:latest"
  "otel/opentelemetry-collector-contrib:latest"

  # DevSecOps tools
  "aquasec/trivy:latest"
  "hadolint/hadolint:latest"
  "zricethezav/gitleaks:latest"
  "sonarqube:community"

  # Dev tools
  "localstack/localstack:latest"

  # Medika Sentosa custom images
  "medika-backend-deps:latest"
  "medika-frontend-deps:latest"
  "medika-ml-deps:latest"
  "medika-chatbot-deps:latest"

  # Portofolio custom images
  "portofolio-backend:latest"
  "portofolio-frontend:latest"
  "portofolio-ml-service:latest"
  "portofolio-chatbot:latest"
)

for IMAGE in "${IMAGES[@]}"; do
  FILENAME=$(echo "$IMAGE" | tr '/: ' '---')

  echo "  Pulling $IMAGE ..."
  if docker pull "$IMAGE" 2>/dev/null; then
    echo "  Saving → $SAVE_DIR/docker/$FILENAME.tar"
    docker save "$IMAGE" -o "$SAVE_DIR/docker/$FILENAME.tar"
    echo "$IMAGE|$FILENAME" >> "$MANIFEST"
    echo "  ✅ $IMAGE selesai"
  else
    echo "  ⚠️  Gagal pull $IMAGE (mungkin custom image, skip pull)"
    if docker image inspect "$IMAGE" &>/dev/null; then
      echo "  Found locally, saving..."
      docker save "$IMAGE" -o "$SAVE_DIR/docker/$FILENAME.tar"
      echo "$IMAGE|$FILENAME" >> "$MANIFEST"
      echo "  ✅ $IMAGE saved dari local"
    fi
  fi
done

echo ""
echo "✅ Docker images selesai."
echo "   Manifest: $MANIFEST"

# ==============================================
# 2. GRADLE — Backend dependencies
# ==============================================
echo ""
echo "[2/5] Backend — Gradle dependencies..."

# Jalur pencarian backend
for BACKEND_PATH in \
  "$BASE_PORTFOLIO_DIR/medikasentosa" \
  "$BASE_PORTFOLIO_DIR/portofolio-backend" \
  "$HOME/medikasentosa" \
  "$HOME/project-final3-backend"; do
  if [ -f "$BACKEND_PATH/build.gradle" ] || [ -f "$BACKEND_PATH/build.gradle.kts" ]; then
    BACKEND_DIR="$BACKEND_PATH"
    break
  fi
done

if [ -n "${BACKEND_DIR:-}" ]; then
  echo "  Backend ditemukan: $BACKEND_DIR"
  cd "$BACKEND_DIR"
  echo "  Spring Boot · Java 21 · Gradle"
  echo "  Downloading dependencies..."
  ./gradlew dependencies --no-daemon -q 2>/dev/null || true
  echo "  Building project (skip test)..."
  ./gradlew build -x test --no-daemon -q 2>/dev/null || echo "  ⚠️  Build warning, lanjut..."
  cd "$HOME"

  echo "  Backing up ~/.gradle/caches..."
  if [ -d "$HOME/.gradle/caches" ]; then
    rsync -a --delete "$HOME/.gradle/caches/" "$SAVE_DIR/gradle/caches/" 2>/dev/null || \
    cp -r "$HOME/.gradle/caches" "$SAVE_DIR/gradle/" 2>/dev/null || true
    echo "  ✅ Gradle cache backed up"
  fi
  echo "✅ Gradle dependencies selesai."
else
  echo "⚠️  Backend directory tidak ditemukan."
fi

# ==============================================
# 3. NPM — Frontend dependencies
# ==============================================
echo ""
echo "[3/5] Frontend — npm packages..."

# Jalur pencarian frontend
for FRONTEND_PATH in \
  "$BASE_PORTFOLIO_DIR/hospital-frontendjs" \
  "$BASE_PORTFOLIO_DIR/hospital-frontend" \
  "$BASE_PORTFOLIO_DIR/portofolio-frontend" \
  "$HOME/hospital-frontendjs" \
  "$HOME/hospital-frontend" \
  "$HOME/project-final3-frontend"; do
  if [ -f "$FRONTEND_PATH/package.json" ]; then
    FRONTEND_DIR="$FRONTEND_PATH"
    break
  fi
done

if [ -n "${FRONTEND_DIR:-}" ]; then
  echo "  Frontend ditemukan: $FRONTEND_DIR"
  cd "$FRONTEND_DIR"
  echo "  React · Vite · Tailwind · JavaScript"
  echo "  Downloading npm packages..."

  rm -rf node_modules
  npm install 2>&1 | tail -3

  echo "  Copying node_modules → $SAVE_DIR/npm/"
  if [ -d "node_modules" ]; then
    rsync -a --delete node_modules/ "$SAVE_DIR/npm/node_modules/" 2>/dev/null || \
    cp -r node_modules "$SAVE_DIR/npm/" 2>/dev/null || true
    cp package-lock.json "$SAVE_DIR/npm/" 2>/dev/null || true
    cp package.json "$SAVE_DIR/npm/" 2>/dev/null || true
    echo "  ✅ node_modules backed up"
  fi
  cd "$HOME"
  echo "✅ npm packages selesai."
else
  echo "⚠️  Frontend directory tidak ditemukan."
fi

# ==============================================
# 4. PYTHON — ML Service + Chatbot dependencies
# ==============================================
echo ""
echo "[4/5] Python packages — ML Service + Chatbot..."

# ML Service
for ML_PATH in \
  "$BASE_PORTFOLIO_DIR/ml-service" \
  "$HOME/ml-service" \
  "$HOME/project-final3-ml"; do
  if [ -f "$ML_PATH/requirements.txt" ]; then
    ML_DIR="$ML_PATH"
    break
  fi
done

if [ -n "${ML_DIR:-}" ]; then
  echo "  ML Service ditemukan: $ML_DIR"
  echo "  FastAPI · TensorFlow · Python 3.10"
  echo "  Downloading ML packages..."
  pip download \
    -r "$ML_DIR/requirements.txt" \
    -d "$SAVE_DIR/python/ml-packages" \
    2>&1 | tail -5
  cp "$ML_DIR/requirements.txt" "$SAVE_DIR/python/ml-requirements.txt"
  echo "✅ ML Service Python packages selesai."
else
  echo "⚠️  ML Service requirements.txt tidak ditemukan."
fi

# Chatbot Service
for CHATBOT_PATH in \
  "$BASE_PORTFOLIO_DIR/chatbot-service" \
  "$HOME/chatbot-service" \
  "$HOME/project-final3-chatbot"; do
  if [ -f "$CHATBOT_PATH/requirements.txt" ]; then
    CHATBOT_DIR="$CHATBOT_PATH"
    break
  fi
done

if [ -n "${CHATBOT_DIR:-}" ]; then
  echo "  Chatbot Service ditemukan: $CHATBOT_DIR"
  echo "  FastAPI · ChromaDB · sentence-transformers · RAG"
  echo "  Downloading Chatbot packages..."
  pip download \
    -r "$CHATBOT_DIR/requirements.txt" \
    -d "$SAVE_DIR/python/chatbot-packages" \
    2>&1 | tail -5
  cp "$CHATBOT_DIR/requirements.txt" "$SAVE_DIR/python/chatbot-requirements.txt"
  echo "✅ Chatbot Python packages selesai."
else
  echo "⚠️  Chatbot requirements.txt tidak ditemukan."
fi

# ==============================================
# 5. DEVSECOPS TOOLS — Binaries
# ==============================================
echo ""
echo "[5/5] DevSecOps tools & binaries..."

# OpenTelemetry Java Agent
echo "  OpenTelemetry Java Agent..."
curl -sL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar" \
  -o "$SAVE_DIR/tools/opentelemetry-javaagent.jar" && \
  echo "  ✅ opentelemetry-javaagent.jar" || echo "  ⚠️  Gagal download otel agent"

# Trivy binary
echo "  Trivy binary..."
TRIVY_VERSION=$(curl -s "https://api.github.com/repos/aquasecurity/trivy/releases/latest" \
  | grep '"tag_name":' | sed -E 's/.*"v([^"]+)".*/\1/')
curl -sL "https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz" \
  -o "$SAVE_DIR/tools/trivy.tar.gz" && \
  tar -xzf "$SAVE_DIR/tools/trivy.tar.gz" -C "$SAVE_DIR/tools/" trivy 2>/dev/null && \
  chmod +x "$SAVE_DIR/tools/trivy" && \
  echo "  ✅ trivy v${TRIVY_VERSION}" || echo "  ⚠️  Gagal download trivy"

# Hadolint binary
echo "  Hadolint binary..."
HADOLINT_VERSION=$(curl -s "https://api.github.com/repos/hadolint/hadolint/releases/latest" \
  | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
curl -sL "https://github.com/hadolint/hadolint/releases/download/${HADOLINT_VERSION}/hadolint-Linux-x86_64" \
  -o "$SAVE_DIR/tools/hadolint" && \
  chmod +x "$SAVE_DIR/tools/hadolint" && \
  echo "  ✅ hadolint ${HADOLINT_VERSION}" || echo "  ⚠️  Gagal download hadolint"

# Gitleaks binary
echo "  Gitleaks binary..."
GITLEAKS_VERSION=$(curl -s "https://api.github.com/repos/gitleaks/gitleaks/releases/latest" \
  | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
GITLEAKS_TAG="${GITLEAKS_VERSION#v}"
curl -sL "https://github.com/gitleaks/gitleaks/releases/download/${GITLEAKS_VERSION}/gitleaks_${GITLEAKS_TAG}_linux_x64.tar.gz" \
  -o "$SAVE_DIR/tools/gitleaks.tar.gz" && \
  tar -xzf "$SAVE_DIR/tools/gitleaks.tar.gz" -C "$SAVE_DIR/tools/" gitleaks 2>/dev/null && \
  chmod +x "$SAVE_DIR/tools/gitleaks" && \
  echo "  ✅ gitleaks ${GITLEAKS_VERSION}" || echo "  ⚠️  Gagal download gitleaks"

# SonarScanner CLI
echo "  SonarScanner CLI..."
SONAR_VERSION="6.2.1.4610"
curl -sL "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_VERSION}-linux-x64.zip" \
  -o "$SAVE_DIR/tools/sonar-scanner.zip" && \
  echo "  ✅ sonar-scanner v${SONAR_VERSION}" || echo "  ⚠️  Gagal download sonar-scanner"

# act — GitHub Actions local runner
echo "  act (GitHub Actions runner)..."
curl -sL "https://raw.githubusercontent.com/nektos/act/master/install.sh" 2>/dev/null \
  | bash -s -- -b "$SAVE_DIR/tools" 2>/dev/null && \
  echo "  ✅ act" || echo "  ⚠️  Gagal download act"

# yq binary
echo "  yq binary..."
YQ_VERSION=$(curl -s "https://api.github.com/repos/mikefarah/yq/releases/latest" \
  | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
curl -sL "https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/yq_linux_amd64" \
  -o "$SAVE_DIR/tools/yq" && \
  chmod +x "$SAVE_DIR/tools/yq" && \
  echo "  ✅ yq ${YQ_VERSION}" || echo "  ⚠️  Gagal download yq"

echo "✅ DevSecOps tools selesai."

# ==============================================
# GENERATE INSTALL-OFFLINE.SH
# ==============================================
echo ""
echo "Generating install-offline.sh..."

cat > "$SAVE_DIR/install-offline.sh" << 'INSTALL'
#!/bin/bash
# ================================================
# INSTALL OFFLINE — Medika Sentosa Stack
# Jalankan di Ubuntu TANPA internet
# ================================================

set -e
SAVE_DIR="$HOME/offline-packages"
BASE_PORTFOLIO_DIR="$HOME/Belajar/Portofolio"

echo "================================================"
echo " INSTALL OFFLINE - Medika Sentosa Stack"
echo "================================================"

# ----------------------------------------------
# A. Load Docker images (pakai manifest)
# ----------------------------------------------
echo ""
echo "[A] Loading Docker images..."

MANIFEST="$SAVE_DIR/docker/manifest.txt"

if [ -f "$MANIFEST" ]; then
  while IFS='|' read -r IMAGE FILENAME; do
    TAR="$SAVE_DIR/docker/$FILENAME.tar"
    if [ -f "$TAR" ]; then
      echo "  Loading $IMAGE ..."
      docker load -i "$TAR" && echo "  ✅ $IMAGE" || echo "  ⚠️  Gagal load $IMAGE"
    else
      echo "  ⚠️  File tidak ditemukan: $TAR"
    fi
  done < "$MANIFEST"
else
  echo "  ⚠️  Manifest tidak ditemukan, coba load semua .tar..."
  for TAR in "$SAVE_DIR/docker/"*.tar; do
    echo "  Loading $(basename $TAR)..."
    docker load -i "$TAR" 2>/dev/null || echo "  ⚠️  Gagal: $TAR"
  done
fi
echo "✅ Docker images selesai."

# ----------------------------------------------
# B. Restore Gradle cache
# ----------------------------------------------
echo ""
echo "[B] Restore Gradle cache..."
if [ -d "$SAVE_DIR/gradle/caches" ]; then
  mkdir -p "$HOME/.gradle"
  rsync -a "$SAVE_DIR/gradle/caches/" "$HOME/.gradle/caches/" 2>/dev/null && \
    echo "✅ Gradle cache dipulihkan." || \
    echo "⚠️  rsync gagal, coba cp..."
  cp -r "$SAVE_DIR/gradle/caches" "$HOME/.gradle/" 2>/dev/null || true
else
  echo "⚠️  Tidak ada Gradle cache di offline-packages."
fi

# ----------------------------------------------
# C. Restore npm node_modules
# ----------------------------------------------
echo ""
echo "[C] Restore frontend dependencies..."

for FRONTEND_DIR in \
  "$BASE_PORTFOLIO_DIR/hospital-frontendjs" \
  "$BASE_PORTFOLIO_DIR/hospital-frontend" \
  "$BASE_PORTFOLIO_DIR/portofolio-frontend" \
  "$HOME/hospital-frontendjs" \
  "$HOME/hospital-frontend"; do
  if [ -f "$FRONTEND_DIR/package.json" ]; then
    break
  fi
  FRONTEND_DIR=""
done

if [ -n "${FRONTEND_DIR:-}" ] && [ -d "$SAVE_DIR/npm/node_modules" ]; then
  cd "$FRONTEND_DIR"
  rm -rf node_modules package-lock.json
  cp -r "$SAVE_DIR/npm/node_modules" node_modules
  cp "$SAVE_DIR/npm/package-lock.json" . 2>/dev/null || true
  npm rebuild 2>/dev/null || true
  echo "✅ Frontend dependencies dipulihkan."
  cd "$HOME"
else
  echo "⚠️  Frontend directory atau npm cache tidak ditemukan."
fi

# ----------------------------------------------
# D. Install Python packages offline (ML Service)
# ----------------------------------------------
echo ""
echo "[D] Install Python packages offline (ML Service)..."

for ML_DIR in \
  "$BASE_PORTFOLIO_DIR/ml-service" \
  "$HOME/ml-service"; do
  [ -d "$ML_DIR" ] && break
  ML_DIR=""
done

if [ -n "${ML_DIR:-}" ] && [ -d "$SAVE_DIR/python/ml-packages" ]; then
  [ ! -d "$ML_DIR/venv" ] && python3 -m venv "$ML_DIR/venv"
  source "$ML_DIR/venv/bin/activate"
  pip install \
    --no-index \
    --find-links="$SAVE_DIR/python/ml-packages" \
    -r "$SAVE_DIR/python/ml-requirements.txt" \
    2>&1 | tail -3
  deactivate
  echo "✅ ML Service packages dipulihkan."
else
  echo "⚠️  ML Service directory atau packages tidak ditemukan."
fi

# ----------------------------------------------
# E. Install Python packages offline (Chatbot)
# ----------------------------------------------
echo ""
echo "[E] Install Python packages offline (Chatbot)..."

for CHATBOT_DIR in \
  "$BASE_PORTFOLIO_DIR/chatbot-service" \
  "$HOME/chatbot-service"; do
  [ -d "$CHATBOT_DIR" ] && break
  CHATBOT_DIR=""
done

if [ -n "${CHATBOT_DIR:-}" ] && [ -d "$SAVE_DIR/python/chatbot-packages" ]; then
  [ ! -d "$CHATBOT_DIR/venv" ] && python3 -m venv "$CHATBOT_DIR/venv"
  source "$CHATBOT_DIR/venv/bin/activate"
  pip install \
    --no-index \
    --find-links="$SAVE_DIR/python/chatbot-packages" \
    -r "$SAVE_DIR/python/chatbot-requirements.txt" \
    2>&1 | tail -3
  deactivate
  echo "✅ Chatbot packages dipulihkan."
else
  echo "⚠️  Chatbot directory atau packages tidak ditemukan."
fi

# ----------------------------------------------
# F. Install DevSecOps tools
# ----------------------------------------------
echo ""
echo "[F] Install DevSecOps tools..."

SUDO_CMD=""
command -v sudo &>/dev/null && SUDO_CMD="sudo"

# Tools binary
for TOOL in trivy hadolint gitleaks act yq; do
  if [ -f "$SAVE_DIR/tools/$TOOL" ]; then
    $SUDO_CMD cp "$SAVE_DIR/tools/$TOOL" /usr/local/bin/
    $SUDO_CMD chmod +x "/usr/local/bin/$TOOL"
    echo "  ✅ $TOOL terinstall"
  fi
done

# SonarScanner
if [ -f "$SAVE_DIR/tools/sonar-scanner.zip" ]; then
  $SUDO_CMD unzip -q "$SAVE_DIR/tools/sonar-scanner.zip" -d /opt/ 2>/dev/null || true
  SONAR_DIR=$(ls -d /opt/sonar-scanner-* 2>/dev/null | head -1)
  [ -n "$SONAR_DIR" ] && \
    $SUDO_CMD ln -sf "$SONAR_DIR/bin/sonar-scanner" /usr/local/bin/sonar-scanner && \
    echo "  ✅ sonar-scanner terinstall"
fi

# OpenTelemetry agent — copy ke project backends
OTEL_JAR="$SAVE_DIR/tools/opentelemetry-javaagent.jar"
for BACKEND in \
  "$BASE_PORTFOLIO_DIR/medikasentosa" \
  "$BASE_PORTFOLIO_DIR/portofolio-backend" \
  "$HOME/medikasentosa"; do
  if [ -d "$BACKEND" ] && [ -f "$OTEL_JAR" ]; then
    cp "$OTEL_JAR" "$BACKEND/"
    echo "  ✅ opentelemetry-javaagent.jar → $BACKEND/"
  fi
done

echo "✅ DevSecOps tools selesai."

# ----------------------------------------------
# Selesai
# ----------------------------------------------
echo ""
echo "================================================"
echo " INSTALL OFFLINE SELESAI"
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
echo " Total Docker images: $(wc -l < $SAVE_DIR/docker/manifest.txt 2>/dev/null || echo '0') images"
echo ""
echo " Di Ubuntu nanti, jalankan:"
echo "   bash $SAVE_DIR/install-offline.sh"
echo ""
echo " Log tersimpan di: $LOG_FILE"
echo "================================================"
