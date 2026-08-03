#!/bin/bash
# ============================================
# DEPLOY RS MEDIKA SENTOSA — 5 Server
# ============================================
# Jalankan dari laptop development.
# Semua server harus sudah punya Docker + SSH key.
# ============================================

set -e

# ─── Konfigurasi Server ────────────────────
SVR1="user@10.0.1.10"   # PostgreSQL + Redis
SVR2="user@10.0.1.20"   # Backend Spring Boot
SVR3="user@10.0.1.25"   # Frontend Nginx
SVR4="user@10.0.1.30"   # Chatbot + ML Service
SVR5="user@10.0.1.40"   # GPU vLLM (bare-metal)

echo "============================================"
echo " 🚀 Deploy RS Medika Sentosa ke 5 Server"
echo "============================================"

case "${1:-all}" in

  all)
    echo ">>> Deploy semua server..."
    
    # 1. Database dulu
    echo ""
    echo "━━━ Server 1: PostgreSQL + Redis ━━━"
    scp docker-compose.svr1-db.yml $SVR1:~/medika/docker-compose.yml
    ssh $SVR1 "cd ~/medika && docker compose up -d"
    
    # 2. Python services (chatbot + ML)
    echo ""
    echo "━━━ Server 4: Chatbot + ML ━━━"
    scp docker-compose.svr4-python.yml $SVR4:~/medika/docker-compose.yml
    rsync -avz ../chatbot-service/ $SVR4:~/medika/chatbot-service/ --exclude chroma_db
    rsync -avz ../ml-service/ $SVR4:~/medika/ml-service/ --exclude models
    ssh $SVR4 "cd ~/medika && docker compose build && docker compose up -d"
    
    # 3. Backend
    echo ""
    echo "━━━ Server 2: Backend ━━━"
    scp docker-compose.svr2-backend.yml $SVR2:~/medika/docker-compose.yml
    rsync -avz ../medikasentosa/ $SVR2:~/medika/medikasentosa/ --exclude .gradle --exclude build
    ssh $SVR2 "cd ~/medika && docker compose build && docker compose up -d"
    
    # 4. Frontend
    echo ""
    echo "━━━ Server 3: Frontend ━━━"
    scp docker-compose.svr3-frontend.yml $SVR3:~/medika/docker-compose.yml
    rsync -avz ../hospital-frontend/ $SVR3:~/medika/hospital-frontend/ --exclude node_modules --exclude dist
    ssh $SVR3 "cd ~/medika && docker compose build && docker compose up -d"
    
    # 5. GPU
    echo ""
    echo "━━━ Server 5: GPU vLLM ━━━"
    echo "   (pastikan vllm serve sudah berjalan di :8100)"
    ;;

  svr1) ssh $SVR1 "cd ~/medika && docker compose up -d" ;;
  svr2) ssh $SVR2 "cd ~/medika && docker compose build && docker compose up -d" ;;
  svr3) ssh $SVR3 "cd ~/medika && docker compose build && docker compose up -d" ;;
  svr4) ssh $SVR4 "cd ~/medika && docker compose build && docker compose up -d" ;;
  svr5) echo "GPU server: pastikan vllm serve berjalan di :8100" ;;

  down)
    echo ">>> Stop semua server..."
    ssh $SVR1 "cd ~/medika && docker compose down"
    ssh $SVR2 "cd ~/medika && docker compose down"
    ssh $SVR3 "cd ~/medika && docker compose down"
    ssh $SVR4 "cd ~/medika && docker compose down"
    ;;

  *)
    echo "Usage:"
    echo "  ./deploy-all.sh          Deploy semua"
    echo "  ./deploy-all.sh svr1     Deploy server 1 aja"
    echo "  ./deploy-all.sh down     Stop semua"
    ;;
esac

echo ""
echo "============================================"
echo " ✅ Deploy selesai"
echo "    Frontend: http://10.0.1.25"
echo "    Backend:  http://10.0.1.20:8080"
echo "============================================"
